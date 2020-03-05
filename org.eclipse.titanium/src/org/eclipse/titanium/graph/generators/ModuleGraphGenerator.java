/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.graph.generators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ModuleImportation;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * @author Gabor Jenei
 * 			This class implements {@link #createGraph()} for module
 *         graph.
 * @see GraphGenerator
 */
public class ModuleGraphGenerator extends GraphGenerator {

	/**
	 * Constructor
	 *
	 * @param project
	 *            : The project to create graph for
	 * @param eHandler
	 *            : An object that implements error reporting capabilities
	 */
	public ModuleGraphGenerator(final IProject project, final ErrorHandler eHandler) {
		super(project, eHandler);
		if (eHandler == null) {
			errorHandler.reportErrorMessage("The referenced error handler mustn't be null (source: ModuleGraphGenerator)");
		}
	}

	@Override
	protected void createGraph() {

		// analyze the project if needed
		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(project);
		if (sourceParser.getLastTimeChecked() == null) {
			WorkspaceJob job = sourceParser.analyzeAll();

			while (job == null) {
				try {
					Thread.sleep(500);
					job = sourceParser.analyzeAll();
				} catch (InterruptedException e) {
					ErrorReporter.logExceptionStackTrace("Error while waiting for analyzis result", e);
				}
			}

			try {
				job.join();
			} catch (InterruptedException e) {
				ErrorReporter.logExceptionStackTrace("Error while parsing the project", e);
			}
		}

		final List<IProject> visitedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getAllReachableProjects();
		final Map<String, Identifier> globalKnownModules = new HashMap<String, Identifier>();
		final Map<String, Identifier> globalKnownModules2 = new HashMap<String, Identifier>();

		// Collect the known module names
		for (int i = 0; i < visitedProjects.size(); ++i) {
			final IProject currentProject = visitedProjects.get(i);
			final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(currentProject);
			final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
			final List<Module> modules = new ArrayList<Module>();
			for (final String moduleName : new TreeSet<String>(knownModuleNames)) {
				final Module module = projectSourceParser.getModuleByName(moduleName);
				modules.add(module);
				final Identifier moduleID = module.getIdentifier();
				globalKnownModules2.put(moduleName, moduleID);

				// add known modules
				final NodeDescriptor actNode = new NodeDescriptor(moduleID.getDisplayName(), moduleID.getName(), currentProject,
						false, moduleID.getLocation());
				if (!graph.containsVertex(actNode)) {
					graph.addVertex(actNode);
					labels.put(actNode.getName(), actNode);
				}
			}
		}
		for (int i = 0; i < visitedProjects.size(); ++i) {
			final IProject currentProject = visitedProjects.get(i);
			final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(currentProject);
			final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
			final List<Module> modules = new ArrayList<Module>();
			for (final String moduleName : new TreeSet<String>(knownModuleNames)) {
				final Module module = projectSourceParser.getModuleByName(moduleName);
				modules.add(module);
			}
			for (final Module module : modules) {
				final ModuleImportsCheck importsCheck = new ModuleImportsCheck();
				module.accept(importsCheck);
				for (final ModuleImportation im : importsCheck.getImports()) {
					final Identifier importIdentifier = im.getIdentifier();
					if (!globalKnownModules.containsKey(importIdentifier.getName())) {
						//add missing modules
						final NodeDescriptor actNode = new NodeDescriptor(importIdentifier.getDisplayName(), importIdentifier.getName(),
								currentProject, true, importIdentifier.getLocation());
						if (!graph.containsVertex(actNode)) {
							graph.addVertex(actNode);
							labels.put(actNode.getName(), actNode);
						}
					}

					//add edges
					final String from = module.getIdentifier().getName();
					final String to = importIdentifier.getName();
					final EdgeDescriptor edge = new EdgeDescriptor(from + "->" + to, Color.black);
					// if(!graph.containsEdge(edge))
					graph.addEdge(edge, labels.get(from), labels.get(to), EdgeType.DIRECTED);
				}
			}
		}
	}

	class ModuleImportsCheck extends ASTVisitor {
		private final Set<ModuleImportation> setOfModules = new HashSet<ModuleImportation>();

		public ModuleImportsCheck() {
			setOfModules.clear();
		}

		public Set<ModuleImportation> getImports() {
			return setOfModules;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if(node instanceof ModuleImportation){
				final ModuleImportation mod = (ModuleImportation) node;
				setOfModules.add(mod);

				return V_SKIP;
			}

			if (node instanceof Assignment) {
				return V_SKIP;
			}

			return V_CONTINUE;
		}
	}
}
