/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.markers.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.core.LoadBalancingUtilities;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.markers.handler.Marker;
import org.eclipse.titanium.markers.handler.MarkerHandler;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.spotters.BaseProjectCodeSmellSpotter;

/**
 * The core controller class of the code smell module
 * <p>
 * The <code>Analyzer</code> is responsible for conducting the code smell
 * analysis of a ttcn3 project or a single module. This includes handling and
 * executing the code smell spotters, locking the project during the analysis to
 * prevent modification.
 * <p>
 * Analyzer instances are immutable, but (slightly) expensive to construct.
 * Instances are obtained via the builder facility (see {@link #builder()}).
 * <p>
 * For performance reasons the {@link #withAll()} and {@link #withPreference()}
 * methods can also be used to obtain <code>Analyzer</code> instances. These are
 * cached instances, updated on preference setting changes.
 *
 * @author poroszd
 *
 */
public class Analyzer {
	private final Map<Class<? extends IVisitableNode>, Set<BaseModuleCodeSmellSpotter>> actions;
	private final Set<BaseProjectCodeSmellSpotter> projectActions;

	Analyzer(final Map<Class<? extends IVisitableNode>, Set<BaseModuleCodeSmellSpotter>> actions, final Set<BaseProjectCodeSmellSpotter> projectActions) {
		this.actions = actions;
		this.projectActions = projectActions;
	}

	// TODO: Run spotters parallel in a thread-pool
	private class CodeSmellVisitor extends ASTVisitor {
		private final List<Marker> markers;

		public CodeSmellVisitor() {
			markers = new ArrayList<Marker>();
		}

		@Override
		public int visit(final IVisitableNode node) {
			final Set<BaseModuleCodeSmellSpotter> actionsOnNode = actions.get(node.getClass());
			if (actionsOnNode != null) {
				for (final BaseModuleCodeSmellSpotter spotter : actionsOnNode) {
					markers.addAll(spotter.checkNode(node));
				}
			}
			return V_CONTINUE;
		}
	}

	private List<Marker> internalAnalyzeModule(final Module module) {
		final CodeSmellVisitor v = new CodeSmellVisitor();
		module.accept(v);
		return v.markers;
	}

	private List<Marker> internalAnalyzeProject(final IProject project) {
		final List<Marker> markers = new ArrayList<Marker>();
		for (final BaseProjectCodeSmellSpotter spotter : projectActions) {
			List<Marker> ms;
			synchronized (project) {
				ms = spotter.checkProject(project);
			}
			markers.addAll(ms);
		}
		return markers;
	}

	/**
	 * Analyze a whole project.
	 * <p>
	 * Executes the configured code smell spotters on the given project. Locking
	 * the project to prevent modification of the AST is handled internally.
	 *
	 * @param monitor
	 *            shows progress and makes it interruptable
	 * @param module
	 *            the ttcn3 project to analyze
	 *
	 * @return the code smells found in the given project
	 */
	public MarkerHandler analyzeProject(final IProgressMonitor monitor, final IProject project) {
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
		final SubMonitor progress = SubMonitor.convert(monitor, 1 + knownModuleNames.size());
		progress.subTask("Project level analysis");
		final Map<IResource, List<Marker>> markers = new ConcurrentHashMap<IResource, List<Marker>>();
		markers.put(project, internalAnalyzeProject(project));
		progress.worked(1);

		final List<Module> knownModules = new ArrayList<Module>(knownModuleNames.size());
		for (final String moduleName : knownModuleNames) {
			final Module mod = projectSourceParser.getModuleByName(moduleName);
			knownModules.add(mod);
		}
		Collections.sort(knownModules, new Comparator<Module>() {
			@Override
			public int compare(final Module o1, final Module o2) {
				return o2.getAssignments().getNofAssignments() - o1.getAssignments().getNofAssignments();
			}
		});

		final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(final Runnable r) {
				final Thread t = new Thread(r);
				t.setPriority(LoadBalancingUtilities.getThreadPriority());
				return t;
			}
		});
		final CountDownLatch latch = new CountDownLatch(knownModuleNames.size());
		for (final Module module : knownModules) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					if (progress.isCanceled()) {
						latch.countDown();
						throw new OperationCanceledException();
					}

					try {
						progress.subTask("Analyzing module " + module.getName());
						final IResource moduleResource = module.getLocation().getFile();
						markers.put(moduleResource, internalAnalyzeModule(module));
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
		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		executor.shutdownNow();

		return new MarkerHandler(markers);
	}

	/**
	 * The factory method of {@link AnalyzerBuilder}s.
	 * <p>
	 * To obtain an <code>Analyzer</code> instance, one have to call this
	 * method, configure the builder appropriately and let the builder construct
	 * the <code>Analyzer</code> itself. This is cruical to ensure the
	 * immutability, thus thread-safety of this class.
	 *
	 * @return a new {@link AnalyzerBuilder} instance
	 */
	public static AnalyzerBuilder builder() {
		return new AnalyzerBuilder();
	}


}
