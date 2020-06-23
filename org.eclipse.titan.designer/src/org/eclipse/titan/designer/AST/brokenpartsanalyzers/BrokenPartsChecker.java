/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
		if (modulesToCheck.isEmpty()) {
			return;
		}

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
			// Please note, that this will not let all modules be processed in parallel,
			//  modules in import loops (and all modules depending on them) have to be checked in the single threaded way.
			final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(final Runnable r) {
					final Thread t = new Thread(r);
					t.setPriority(LoadBalancingUtilities.getThreadPriority());
					return t;
				}
			});

			final AtomicInteger activeExecutorCount = new AtomicInteger(0);
			final List<Module> modulesToCheckCopy = new ArrayList<Module>(modulesToCheck);
			Collections.sort(modulesToCheckCopy, new Comparator<Module>() {
				@Override
				public int compare(final Module o1, final Module o2) {
					return o2.getAssignments().getNofAssignments() - o1.getAssignments().getNofAssignments();
				}
			});

			final ArrayList<Module> modulesToCheckParallely = new ArrayList<Module>();
			//initial filling
			for (final Module module : modulesToCheckCopy) {
				final List<Module> importedModules = module.getImportedModules();
				boolean ok = true;
				for (final Module importedModule : importedModules) {
					if (!importedModule.getSkippedFromSemanticChecking() && (importedModule.getLastCompilationTimeStamp() == null || importedModule.getLastCompilationTimeStamp() != compilationCounter)) {
						ok = false;
						break;
					}
				}
				if (ok) {
					modulesToCheckParallely.add(module);
				}
			}

			final CountDownLatch latch = new CountDownLatch(modulesToCheck.size());

			if (modulesToCheckParallely.isEmpty()) {
				// When we can not start in parallel mode, just start somewhere
				modulesToCheckParallely.add(modulesToCheckCopy.remove(0));
			} else {
				modulesToCheckCopy.removeAll(modulesToCheckParallely);
			}

			final LinkedBlockingDeque<Module> modulesBeingChecked = new LinkedBlockingDeque<Module>(modulesToCheckParallely);

			synchronized(modulesToCheckCopy) {
				for (final Module module : modulesToCheckParallely) {
					addToExecutor(module, executor, latch, compilationCounter, absoluteStart, modulesToCheckCopy, modulesBeingChecked, activeExecutorCount, progress);
				}
			}

			try {
				latch.await();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			TITANDebugConsole.println("  **It took " + (System.nanoTime() - absoluteStart) * (1e-9) + " seconds so far for Designer to check the modules in parallel mode");

			executor.shutdown();
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace(e);
			}
			executor.shutdownNow();
		} else {
			for (final Module module : modulesToCheck) {
				progress.subTask("Semantically checking module: " + module.getName());

				final long absoluteStart2 = System.nanoTime();
				module.check(compilationCounter);
				final long now = System.nanoTime();
				TITANDebugConsole.println("" + (absoluteStart2 - absoluteStart) + "," + (now - absoluteStart) + "");


				progress.worked(1);
			}
			TITANDebugConsole.println("  **It took " + (System.nanoTime() - absoluteStart) * (1e-9) + " seconds for Designer to check the modules");
		}

		for (final Module module : selectionMethod.getModulesToSkip()) {
			module.setSkippedFromSemanticChecking(false);
		}
	}

	private static void addToExecutor(final Module module, final ExecutorService executor, final CountDownLatch latch, final CompilationTimeStamp compilationCounter, final long absoluteStart,
			final List<Module> modulesToCheckCopy, final LinkedBlockingDeque<Module> modulesBeingChecked, final AtomicInteger activeExecutorCount, final SubMonitor progress) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (progress.isCanceled()) {
					latch.countDown();
					throw new OperationCanceledException();
				}

				try {
					activeExecutorCount.incrementAndGet();
					modulesBeingChecked.add(module);
					final long absoluteStart2 = System.nanoTime();
					module.check(compilationCounter);
					final long now = System.nanoTime();
					modulesBeingChecked.remove(module);
					TITANDebugConsole.println("  **It took (" + (absoluteStart2 - absoluteStart) + "," + (now - absoluteStart) + ") " + (now - absoluteStart2) * (1e-9) + " seconds for Designer to check " + module.getName());
					progress.worked(1);
				} finally {
					latch.countDown();

					final ArrayList<Module> modulesToCheckParallely = new ArrayList<Module>();
					synchronized(modulesToCheckCopy) {
						for (final Module module : modulesToCheckCopy) {
							final List<Module> importedModules = module.getImportedModules();
							boolean ok = true;
							for (final Module importedModule : importedModules) {
								if (modulesBeingChecked.contains(importedModule) || (!importedModule.getSkippedFromSemanticChecking() && (importedModule.getLastCompilationTimeStamp() == null || importedModule.getLastCompilationTimeStamp() != compilationCounter))) {
									ok = false;
									break;
								}
							}
							if (ok) {
								modulesToCheckParallely.add(module);
							}
						}
						modulesToCheckCopy.removeAll(modulesToCheckParallely);

						for (final Module module : modulesToCheckParallely) {
							addToExecutor(module, executor, latch, compilationCounter, absoluteStart, modulesToCheckCopy, modulesBeingChecked, activeExecutorCount, progress);
						}

						if (activeExecutorCount.decrementAndGet() == 0 && modulesToCheckParallely.isEmpty() && !modulesToCheckCopy.isEmpty()) {
							// there are more modules to check, but none can be checked in the normal way
							// and this is the last executor running.
							// current heuristic: just select one to keep checking ... and hope this breaks the loop stopping parallelism.
							final Module module = modulesToCheckCopy.remove(0);
							addToExecutor(module, executor, latch, compilationCounter, absoluteStart, modulesToCheckCopy, modulesBeingChecked, activeExecutorCount, progress);
						}
					}
				}
			}
		});
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
