/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.LoadBalancingUtilities;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * Helper class to check broken parts.
 *
 * @author Peter Olah
 * @author Kristof Szabados
 */
public final class BrokenPartsChecker {
	private static final int NUMBER_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();

	private final SubMonitor progress;

	private final IProgressMonitor monitor;

	private final CompilationTimeStamp compilationCounter;

	private final SelectionMethodBase selectionMethod;

	public BrokenPartsChecker(final SubMonitor monitor, final CompilationTimeStamp compilationCounter, final SelectionMethodBase selectionMethod) {
		this.compilationCounter = compilationCounter;
		this.selectionMethod = selectionMethod;
		this.monitor = monitor;

		progress = SubMonitor.convert(monitor, 100);
	}

	public void doChecking() {
		monitor.subTask("Semantic checking");

		final BrokenPartsViaReferences brokenParts = (BrokenPartsViaReferences)selectionMethod;
		if (brokenParts.getAnalyzeOnlyDefinitions()) {
			final Map<Module, List<Assignment>> moduleAndBrokenDefinitions = brokenParts.getModuleAndBrokenDefs();
			definitionsChecker(moduleAndBrokenDefinitions);
		} else {
			generalChecker();
		}


		monitor.subTask("Doing post semantic checks");

		for (final Module module : selectionMethod.getModulesToCheck()) {
			module.postCheck();
		}

		progress.done();
	}

	private void generalChecker() {
		final List<Module> modulesToCheck = selectionMethod.getModulesToCheck();

		progress.setTaskName("Semantic check");
		progress.setWorkRemaining(modulesToCheck.size());

		for (final Module module : selectionMethod.getModulesToSkip()) {
			module.setSkippedFromSemanticChecking(true);
		}
		for (final Module module : modulesToCheck) {
			module.setSkippedFromSemanticChecking(false);
		}

		// process the modules one-by-one
		final long absoluteStart = System.nanoTime();
		final IPreferencesService preferenceService = Platform.getPreferencesService();
		final boolean useParallelSemanticChecking = preferenceService.getBoolean(
				ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.USEPARALLELSEMATICCHECKING, true, null);
		if (useParallelSemanticChecking) {
			// When enabled do a quick parallel checking on the modules, where it is possible.
			// 2 modules can be checked in parallel if the codes to be checked do not overlap.
			// On an abstract level we create layers of the subtree that needs to be check,
			//  based on how far a module is from an already checked module.
			// Each iteration builds up a new layer/set, and checks its elements in parallel.
			// Please note, that this will not let all modules be processed in parallel,
			//  modules in import loops (and all modules depending on them) have to be checked in the single threaded way.
			final ThreadPoolExecutor executor = new ThreadPoolExecutor(NUMBER_OF_PROCESSORS, NUMBER_OF_PROCESSORS, 10, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			executor.setThreadFactory(new ThreadFactory() {
				@Override
				public Thread newThread(final Runnable r) {
					final Thread t = new Thread(r);
					t.setPriority(LoadBalancingUtilities.getThreadPriority());
					return t;
				}
			});

			final List<Module> modulesToCheckCopy = new ArrayList<Module>(modulesToCheck);
			// iterate until parallel processing is still possible
			boolean mightStillProcessElements = true;
			while (mightStillProcessElements) {
				// build the layer/set of modules that can be checked in parallel in this iteration.
				final List<Module> modulesToCheckParallely = new ArrayList<Module>(modulesToCheckCopy.size());
				for (final Module module : modulesToCheckCopy) {
					final List<Module> importedModules = module.getImportedModules();
					boolean ok = true;
					for (final Module importedModule : importedModules) {
						if (!importedModule.getSkippedFromSemanticChecking() && (importedModule.getLastCompilationTimeStamp() == null || importedModule.getLastCompilationTimeStamp() != compilationCounter)) {
							ok = false;
						}
					}
					if (ok) {
						modulesToCheckParallely.add(module);
					}
				}
				modulesToCheckCopy.removeAll(modulesToCheckParallely);
				TITANDebugConsole.println("  **can check " + modulesToCheckParallely.size() + " modules " + modulesToCheckCopy.size() + " remains");
				mightStillProcessElements = modulesToCheckParallely.size() > 0;
				// do the parallel checking
				final CountDownLatch latch = new CountDownLatch(modulesToCheckParallely.size());
				for (final Module module : modulesToCheckParallely) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							if (progress.isCanceled()) {
								latch.countDown();
								throw new OperationCanceledException();
							}

							try {
								module.check(compilationCounter);
								progress.worked(1);
							} finally {
								latch.countDown();
							}
						}
					});
				}

				try {
					latch.await();
				} catch (InterruptedException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
				TITANDebugConsole.println("  **It took " + (System.nanoTime() - absoluteStart) * (1e-9) + " seconds so far for Designer to check the modules in parallel mode");
			}
			executor.shutdown();
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			executor.shutdownNow();
		}

		for (final Module module : modulesToCheck) {
			progress.subTask("Semantically checking module: " + module.getName());

			module.check(compilationCounter);

			progress.worked(1);
		}
		TITANDebugConsole.println("  **It took " + (System.nanoTime() - absoluteStart) * (1e-9) + " seconds for Designer to check the modules");

		for (final Module module : selectionMethod.getModulesToSkip()) {
			module.setSkippedFromSemanticChecking(false);
		}
	}

	private void definitionsChecker(final Map<Module, List<Assignment>> moduleAndBrokenDefs) {
		progress.setTaskName("Semantic check");
		progress.setWorkRemaining(moduleAndBrokenDefs.size());

		for (final Map.Entry<Module, List<Assignment>> entry : moduleAndBrokenDefs.entrySet()) {
			final Module module = entry.getKey();

			progress.subTask("Semantically checking broken parts in module: " + module.getName());

			if (module instanceof TTCN3Module) {
				((TTCN3Module) module).checkWithDefinitions(compilationCounter, entry.getValue());
			} else {
				module.check(compilationCounter);
			}

			progress.worked(1);
		}
	}
}
