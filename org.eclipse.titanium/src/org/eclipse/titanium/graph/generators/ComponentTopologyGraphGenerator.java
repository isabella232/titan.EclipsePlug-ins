/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.graph.generators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.PortReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.Connect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Function_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Map_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Start_Component_Statement;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ComponentCreateExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.error.ErrorHandler;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;

import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * This is a {@link GraphGenerator} class for the component finder graph
 *
 * @see GraphGenerator
 * @author Bianka Bekefi
 */
public class ComponentTopologyGraphGenerator extends GraphGenerator {
	private IFile selectedFile;
	
	
	public ComponentTopologyGraphGenerator(final IFile selectedFile, final IProject project, final ErrorHandler eHandler) {
		super(project, eHandler);
		this.selectedFile = selectedFile;
	}


	@Override
	protected void createGraph() {
		final TestcaseCollector tcc = new TestcaseCollector();
		
		
		final IProject sourceProj = selectedFile.getProject();
		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(sourceProj);
		final Module selectedModule = projectSourceParser.containedModule(selectedFile);
		selectedModule.accept(tcc);
		
		for (final Def_Testcase tc : tcc.getTestcases()) {
			final HashMap<Component_Type, List<Component_Type>> components = new HashMap<Component_Type, List<Component_Type>>();
			final Component_Type ct = tc.getRunsOnType(CompilationTimeStamp.getBaseTimestamp());
			components.put(ct, new ArrayList<Component_Type>());
			final TestcaseVisitor vis = new TestcaseVisitor(new ArrayList<Def_Function>(), components, ct);
			tc.accept(vis);
			
			NodeDescriptor node = new NodeDescriptor(tc.getFullName()+"\n"+ct.getFullName(), tc.getFullName()+"\n"+ct.getFullName(),
					NodeColours.DARK_RED, project, false, ct.getLocation());
			graph.addVertex(node);
			labels.put(node.getName(), node);
			for (final Entry<Component_Type, List<Component_Type>> entry : vis.getComponents().entrySet()) {
				if (!ct.equals(entry.getKey())) {
					node = new NodeDescriptor(entry.getKey().getFullName(), tc.getFullName()+"\n"+entry.getKey().getFullName(),
							NodeColours.LIGHT_GREEN, project, false, entry.getKey().getLocation());
					if (!graph.containsVertex(node)) {
						graph.addVertex(node);
						labels.put(node.getName(), node);
					}
				}

				for (final Component_Type ct2 : entry.getValue()) {
					final NodeDescriptor node2 = new NodeDescriptor(ct2.getFullName(), tc.getFullName()+"\n"+ct2.getFullName(),
							NodeColours.LIGHT_GREEN, project, false, ct2.getLocation());
					if (!graph.containsVertex(node2)) {
						graph.addVertex(node2);
						labels.put(node2.getName(), node2);
					}
					
					final EdgeDescriptor edge = new EdgeDescriptor(tc.getFullName()+"\n"+entry.getKey().getFullName() + "->" + 
							ct2.getFullName(), Color.black);
					if (!graph.containsEdge(edge)) {
						graph.addEdge(edge, labels.get(tc.getFullName()+"\n"+entry.getKey().getFullName()), 
								labels.get(tc.getFullName()+"\n"+ct2.getFullName()), EdgeType.DIRECTED);
					}
				}
			}
		}	
	}
	
	private static class TestcaseVisitor extends ASTVisitor {

		private HashMap<Component_Type, List<Component_Type>> components = new HashMap<Component_Type, List<Component_Type>>();
		private List<Def_Function> checkedFunctions;
		private int counter;
		private boolean cce;
		private Component_Type comp;
		
		TestcaseVisitor(final List<Def_Function> checkedFunctions, final HashMap<Component_Type, List<Component_Type>> components, final Component_Type comp) {
			this.components.putAll(components);
			this.checkedFunctions = checkedFunctions;
			counter = -1;
			cce = false;
			this.comp = comp;
		}
		
		private HashMap<Component_Type, List<Component_Type>> getComponents() {
			return components;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Connect_Statement || node instanceof Map_Statement) {
				counter = 0;
			}
			else if (node instanceof PortReference && (counter == 0 || counter == 1)) {
				counter++;
				final PortReference pr = ((PortReference)node);
				
				final Assignment as = pr.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				if (as != null && as instanceof Def_Port) {
					final Def_Port dp = (Def_Port)as;
					final ModuleVisitor mv = new ModuleVisitor(dp);
					final Module m = dp.getMyScope().getModuleScope();
					m.accept(mv);
					for (final Component_Type ct : mv.getComponents()) {
						if (!components.containsKey(comp)) {
							components.put(comp, new ArrayList<Component_Type>());
						}
							
						if (!components.get(comp).contains(ct) && !comp.equals(ct)) {
							components.get(comp).add(ct);
						}
					}
				}
			}
			else if (node instanceof Function_Instance_Statement) {
				final Function_Instance_Statement fis = (Function_Instance_Statement)node;
				final Assignment as = fis.getReference().getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
				analyzeFunction(as, comp);			
			}
			else if (node instanceof ComponentCreateExpression) {
				cce = true;
			}
			else if (node instanceof Reference && cce) {
				cce = false;
				final Reference ref = (Reference)node;
				final Assignment as = ref.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
				if (as.getType(CompilationTimeStamp.getBaseTimestamp()) instanceof Component_Type) {
					final Component_Type ct = (Component_Type)as.getType(CompilationTimeStamp.getBaseTimestamp());
					if (!components.containsKey(comp)) {
						components.put(comp, new ArrayList<Component_Type>());
					}
					if (!components.get(comp).contains(ct) && !comp.equals(ct)) {
						components.get(comp).add(ct);
					}
				}
			}
			else if (node instanceof Start_Component_Statement) {
				final Assignment as = ((Start_Component_Statement)node).getFunctionInstanceReference().getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), true);
				analyzeFunction(as, comp);
			}

			return V_CONTINUE;
		}
		
		public void analyzeFunction(final Assignment assignment, final Component_Type component) {
			if (assignment != null && assignment instanceof Def_Function) {
				final Def_Function df = (Def_Function)assignment;
				if (!checkedFunctions.contains(df)) {
					checkedFunctions.add(df);
					TestcaseVisitor tv = null;
					if (df.getRunsOnType(CompilationTimeStamp.getBaseTimestamp()) != null) {
						final Component_Type fComp = df.getRunsOnType(CompilationTimeStamp.getBaseTimestamp());
						if (!components.containsKey(comp)) {
							components.put(comp, new ArrayList<Component_Type>());
						}
						if (!fComp.equals(component) && !components.get(component).contains(fComp)) {
							components.get(component).add(fComp);
							components.put(fComp, new ArrayList<Component_Type>());
						}
						tv = new TestcaseVisitor(checkedFunctions, components, fComp);
					} else {
						tv = new TestcaseVisitor(checkedFunctions, components, component);
					}
					df.accept(tv);
					components.putAll(tv.getComponents());
				}
			}
		}

	}
	
	private static class ModuleVisitor extends ASTVisitor {

		private List<Component_Type> comps = new ArrayList<Component_Type>();
		private Def_Port port;
		
		ModuleVisitor(final Def_Port port) {
			comps = new ArrayList<Component_Type>();
			this.port = port;
		}

		private List<Component_Type> getComponents() {
			return comps;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Component_Type) {
				final Component_Type ct = (Component_Type)node;
				final List<Definition> defs = ct.getComponentBody().getDefinitions();
				for (final Definition def : defs) {
					if (def != null && def.equals(port)) {
						comps.add(ct);
						return V_ABORT;
					}
				}
			}
			return V_CONTINUE;
		}

	}
	
	private static class TestcaseCollector extends ASTVisitor {

		private List<Def_Testcase> tcs = new ArrayList<Def_Testcase>();
		
		TestcaseCollector() {
			tcs = new ArrayList<Def_Testcase>();
		}

		private List<Def_Testcase> getTestcases() {
			return tcs;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Def_Testcase) {
				tcs.add((Def_Testcase)node);
			}
			return V_CONTINUE;
		}

	}
}