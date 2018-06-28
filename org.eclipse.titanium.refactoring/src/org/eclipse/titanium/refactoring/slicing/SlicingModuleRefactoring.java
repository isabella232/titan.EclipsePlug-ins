/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.slicing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class represents the 'Slicing by modules' refactoring operation.
 * <p>
 * This refactoring operation minimizes all visibility modifiers in the given
 *   files/folders/projects, which are contained in a {@link IStructuredSelection} object.
 * The operation can be executed using the mechanisms in the superclass, through a wizard for example
 *
 * @author Bianka Bekefi
 */
public class SlicingModuleRefactoring extends SlicingRefactoring {
	public static final String PROJECTCONTAINSERRORS = "The project `{0}'' contains errors, which might corrupt the result of the refactoring";
	public static final String PROJECTCONTAINSTTCNPPFILES = "The project `{0}'' contains .ttcnpp files, which might corrupt the result of the refactoring";

	private Set<IProject> projects = new HashSet<IProject>();
	private Module destinationModule;
	private List<Module> selectedModules = new ArrayList<Module>();

	private Object[] affectedObjects;		//the list of objects affected by the change

	public SlicingModuleRefactoring(final IStructuredSelection selection, SlicingSettings settings) {
		super(selection, settings);
		
		final Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			final Object o = it.next();
			if (o instanceof IResource) {
				final IProject temp = ((IResource) o).getProject();
				projects.add(temp);
			}
		}
	}

	public Object[] getAffectedObjects() {
		return affectedObjects;
	}

	//METHODS FROM REFACTORING

	@Override
	public String getName() {
		return "Slicing by modules";
	}
	
	@Override
	public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (this.structSelection == null) {
			return null;
		}

		final CompositeChange cchange = new CompositeChange("SlicingRefactoring");
		for (Map.Entry<Module, List<FunctionData>> entry : functions.entrySet()) {
			final ChangeCreator chCreator = new ChangeCreator((IFile)entry.getKey().getLocation().getFile(), settings, entry.getValue());
			chCreator.perform();
			final Change ch = chCreator.getChange();
			if (ch != null) {
				cchange.add(ch);
			}			
		}
		return cchange;
	}
	
	@Override
	public Map<Module, List<FunctionData> > getModules() {
		if (this.structSelection == null) {
			return null;
		}
		if (functions != null) {
			return functions;
		}
		final Iterator<?> it1 = this.structSelection.iterator();
		while (it1.hasNext()) {
			final Object o = it1.next();
			if (!(o instanceof IResource)) {
				continue;
			}
			
			final IResource res = (IResource)o;
			final SelectedModulesVisitor vis = new SelectedModulesVisitor();
			try {
				res.accept(vis);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			selectedModules.addAll(vis.getSelectedModules());
		}
	

		functions = new HashMap<Module, List<FunctionData> >();
		for (Module module : selectedModules) {
			if (module instanceof TTCN3Module) {
				functions.put(module, new ArrayList<FunctionData>());
			}
		}
		return functions;
	}
	
	@Override
	public Map<Module, List<FunctionData> > getFunctions() {
		return functions;
	}
	
	public List<FunctionData> selectMovableFunctions(TTCN3Module ttcn3module, SubMonitor progress) {
		if (!functions.get(ttcn3module).isEmpty()) {
			return functions.get(ttcn3module);
		}
		for (ILocateableNode node : ttcn3module.getDefinitions()) {
			if (node instanceof Def_Function) {
				Def_Function fun = (Def_Function)node;
				ReferenceVisitor refVis = new ReferenceVisitor();
				fun.accept(refVis);
				boolean dependent = false;
				for (ILocateableNode node2 : refVis.getLocations()) {
					if (node2 instanceof Reference) {
						final Assignment assignment = ((Reference) node2).getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false, null);
						if (assignment != null && !assignment.isLocal() && assignment.getMyScope().getModuleScope().equals(fun.getMyScope().getModuleScope())) {
							dependent = true;
							break;
						}
					}
				}
				if (!dependent) {
					FunctionData fd = new FunctionData(fun, createFunctionBody(fun, ttcn3module));
					fd.setModule(ttcn3module);
					functions.get(ttcn3module).add(fd);
				}
			} 
			progress.worked(1);
		}
		return functions.get(ttcn3module);
	}
	
	public Map<Module, List<FunctionData>> chooseDestination() {
		for (Map.Entry<Module, List<FunctionData> > entry : functions.entrySet()) {
			for (FunctionData fun : entry.getValue()) {
				if (fun.isToBeMoved() && (fun.getRefactoringMethod() == null || settings.isChanged() /*!fun.getRefactoringMethod().equals(settings.getMethod()))*/ /*&& fun.getDestinations().isEmpty()*/)) {
					fun.clearDestinations();
					fun.setRefactoringMethod(settings.getMethod());
					
					List<Module> usedModules = collectUsedModules(fun.getDefiniton(), entry.getKey());
					fun.setUsedModules(usedModules);
					if (usedModules.size() > 0) {	
						switch (settings.getMethod()) {
							case LENGTH: 
								chooseModuleByLength(usedModules, fun);
								break;
							case IMPORTS:
								chooseModuleByImports(usedModules, fun);
								break;
							case LENGTHANDIMPORTS:
								chooseModuleByLengthAndImports(usedModules, fun);
								break;
						}
					}
					if (settings.getMethod().equals(SlicingMethod.COMPONENT)) {
						chooseModuleByComponent(fun);
					}
					addUnusedModules(fun, usedModules);
				}
			}
		}
		settings.setChanged(false);
		return functions;
	}
	
	private String createFunctionBody(Def_Function fun, Module module) {
		String body2 = "";
		try {
			final InputStream istream = ((IFile)module.getLocation().getFile()).getContents();
			final BufferedReader br = new BufferedReader(new InputStreamReader(istream));
			final int startOffset = fun.getLocation().getOffset();
			final int endOffset = fun.getLocation().getEndOffset();
			br.skip(startOffset);
			final char[] contentBuf = new char[endOffset-startOffset];
			br.read(contentBuf, 0, endOffset-startOffset);
			for (char c : contentBuf) {
				body2 += c;
			}
			
			br.close();
			istream.close();
			
		} catch (CoreException ce) {
			ce.printStackTrace();
			return "";
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return "";
		}

		return body2;
	}

	
	private void chooseModuleByLength(List<Module> usedModules, FunctionData function) {
		List<Module> filteredList = filterByExcludedNames(usedModules);
		if (filteredList.size() > 0) {
			Collections.sort(filteredList, new Comparator<Module>() {
				@Override
				public int compare(Module m1, Module m2) {
					return ((m1.getLocation().getEndOffset()-m1.getLocation().getOffset())-(m2.getLocation().getEndOffset()-m2.getLocation().getOffset())); 
				}
			});
			destinationModule = filteredList.get(0);
			int destLength = destinationModule.getLocation().getEndOffset()-destinationModule.getLocation().getOffset();
			for (int i=1; i<filteredList.size(); i++) {
				Module m = filteredList.get(i);
				if ((settings.getExcludedModuleNames() != null && settings.getExcludedModuleNames().matcher(m.getIdentifier().getTtcnName()).matches())
						/*&& (selectedModules.isEmpty() || selectedModules.contains(m))*/) {
					continue;
				}
				int moduleLength = m.getLocation().getEndOffset()-m.getLocation().getOffset();
				if (moduleLength == destLength && !m.equals(destinationModule)) {
					function.addDestination(m, 100, -1);
				}
				else {
					function.addDestination(m, ((int)((double)destLength*100 / (double)moduleLength)), -1);
				}
			}
			function.addDestination(destinationModule, 100, -1);
		}
		//function.setFinalDestination(new Destination(destinationModule, 100, function, -1));
	}
	
	private List<Module> filterByExcludedNames(List<Module> usedModules) {
		List<Module> filteredList = new ArrayList<Module>();
		for (Module m : usedModules) {
			if (!(settings.getExcludedModuleNames() != null && settings.getExcludedModuleNames().matcher(m.getIdentifier().getTtcnName()).matches())
					/*&& (selectedModules.isEmpty() || selectedModules.contains(m))*/) {
				
				filteredList.add(m);
			}
			else {
			}	
		}
		return filteredList;
	}
	
	private void chooseModuleByImports(List<Module> usedModules, FunctionData function) {
		List<Module> filteredList = filterByExcludedNames(usedModules);
		if (filteredList.size() > 0) {
			List<Entry<Module, Integer>> list = countMissingImports(filteredList, function);
	        destinationModule = list.get(0).getKey();
	        int min = list.get(0).getValue();
	        list.remove(0);
	        for (Entry<Module, Integer> e : list) {
	        	if (e.getValue() == min) {
	        		function.addDestination(e.getKey(), 100, e.getValue());
	        	}
	        	else {
	        		double value = (double)min*100 / (double)e.getValue();
	        		function.addDestination(e.getKey(), (int)((double)min*100 / (double)e.getValue()), e.getValue());
	        	}
	        }
			function.addDestination(destinationModule, 100, min);
		}
		//function.setFinalDestination(new Destination(destinationModule, 100, function, min));
	}
	
	private List<Entry<Module, Integer>> countMissingImports(List<Module> usedModules, FunctionData function) {
		Map<Module, Integer> importsCounter = new HashMap<Module, Integer>();
		for (Module m : usedModules) {
			int counter = 0;
			List<Module> imports = ((TTCN3Module)m).getImportedModules();
			for (Module m2 : usedModules) {
				if (!m.equals(m2) && imports.contains(m2)) {
					counter++;
				}
			}
			importsCounter.put(m, usedModules.size() - counter - 1);
		}
		
		List<Entry<Module, Integer>> list = new ArrayList<Entry<Module, Integer>>(importsCounter.entrySet());
		Collections.sort(list, new Comparator<Entry<Module, Integer>>() {
			@Override
			public int compare(Entry<Module, Integer> e1, Entry<Module, Integer> e2) { 
				return e1.getValue().compareTo(e2.getValue()); 
			}
		});
		return list;
	}
	
	private void chooseModuleByLengthAndImports(List<Module> usedModules, FunctionData function) {
		List<Entry<Module, Integer>> list = countMissingImports(usedModules, function);
		int leastMissingImports = list.get(0).getValue();
		
		Collections.sort(list, new Comparator<Entry<Module, Integer>>() {
			@Override
			public int compare(Entry<Module, Integer> m1, Entry<Module, Integer> m2) {
				return ((m1.getKey().getLocation().getEndOffset()-m1.getKey().getLocation().getOffset())-(m2.getKey().getLocation().getEndOffset()-m2.getKey().getLocation().getOffset())); 
			}
		});
		Module shortestModule = list.get(0).getKey();
		int shortestLength = shortestModule.getLocation().getEndOffset()-shortestModule.getLocation().getOffset();
		
		for (Entry<Module, Integer> entry : list) {
			int moduleLength = entry.getKey().getLocation().getEndOffset()-entry.getKey().getLocation().getOffset();
			double value1 = ((double)shortestLength / (double)moduleLength);
			double value2 = (double)leastMissingImports / (double)entry.getValue();
			double value3 = value1*value2*100;
			function.addDestination(entry.getKey(), (int)value3, entry.getValue());
		}
	}
	
	private List<Module> collectUsedModules(Def_Function function, Module ttcn3module) {
		List<Module> modules = new ArrayList<Module>();
		boolean dependent = false;
		for (Module m : ttcn3module.getImportedModules()) {
			dependent = false;
			if (m instanceof TTCN3Module) {
				ReferenceVisitor refVis = new ReferenceVisitor();
				function.accept(refVis);
				for (ILocateableNode node2 : refVis.getLocations()) {
					if (node2 instanceof Reference) {
						final Assignment assignment = ((Reference) node2).getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false, null);
						if (assignment != null && assignment.getMyScope().getModuleScope().equals(m)) {
							dependent = true;
						}
					}
				}
				if (dependent) {
					modules.add(m);
				}
			} 
		}
		return modules;
	}
	
	
	@Override
	public IProject getProject() {
		return projects.iterator().next();
	}
	
	public void chooseModuleByComponent(FunctionData function) {
		Component_Type comp = function.getDefiniton().getRunsOnType(CompilationTimeStamp.getBaseTimestamp());
		Map<Module, Integer> compCounter = new HashMap<Module, Integer>();
		List<Component_Type> extendedComps = new ArrayList<Component_Type>();
		for (ComponentTypeBody ctb : comp.getComponentBody().getExtensions().getComponentBodies()) {
			if (!extendedComps.contains(ctb)) {
				extendedComps.add(ctb.getMyType());
			}
		}
		
		for (int i=0; i<extendedComps.size(); i++) {
			Component_Type ct = extendedComps.get(i);
			for (ComponentTypeBody ctb : ct.getComponentBody().getExtensions().getComponentBodies()) {
				if (!extendedComps.contains(ctb)) {
					extendedComps.add(ctb.getMyType());
				}
			}
		}
		
		
		for(IProject project : projects) {
			final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
			List<Module> modules = filterByExcludedNames(new ArrayList<Module>(projectSourceParser.getModules()));
			for (Module m : modules) {
				if (!m.equals(function.getModule())) {
					ModuleVisitor vis = new ModuleVisitor(comp, extendedComps);
					m.accept(vis);
					compCounter.put(m, vis.getCounter());
				}
			}
		}	
		
		List<Entry<Module, Integer>> list = new ArrayList<Entry<Module, Integer>>(compCounter.entrySet());
		
		Collections.sort(list, new Comparator<Entry<Module, Integer>>() {
			@Override
			public int compare(Entry<Module, Integer> m1, Entry<Module, Integer> m2) {
				return (-1)*m1.getValue().compareTo(m2.getValue());
			}
		});
		destinationModule = list.get(0).getKey();
		int max = list.get(0).getValue();
		if (max > 0) {
			function.addDestination(list.get(0).getKey(), 100, -1);
		}
		else {
			function.addDestination(list.get(0).getKey(), 0, -1);
		}
		for (int i=1; i<list.size(); i++) {
			if (max == 0) {
				function.addDestination(list.get(i).getKey(), 0, -1);
			}
			else if (list.get(i).getValue() == max) {
				function.addDestination(list.get(i).getKey(), 100, -1);
			}
			else {
				double val = (double)list.get(i).getValue() / (double)max;
				function.addDestination(list.get(i).getKey(), (int)(val*100), -1);
			}
		}
		//function.setFinalDestination(new Destination(destinationModule, 100, function, -1));
	}
	
	public void addUnusedModules(FunctionData fun, List<Module> usedModules) {
		for (Module m : selectedModules) {
			if (!usedModules.contains(m) && !m.equals(fun.getModule())) {
				fun.addDestination(m, 0, -1);
			}
		}
	}

	
	private class SelectedModulesVisitor implements IResourceVisitor {

		private List<Module> selectedModules = new ArrayList<Module>();	
		
		public SelectedModulesVisitor() {
		}

		@Override
		public boolean visit(final IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(((IFile)resource).getProject());
				final Module module = sourceParser.containedModule((IFile)resource);
				if (module != null) {
					selectedModules.add(module);
				}
				//SKIP
				return false;
			}
			//CONTINUE
			return true;
		}
		
		public List<Module> getSelectedModules() {
			return selectedModules;
		}
	}
	
	private static class ModuleVisitor extends ASTVisitor {

		private int counter;	
		private Component_Type comp;
		private List<Component_Type> components;
		
		public ModuleVisitor(Component_Type comp, List<Component_Type> components) {
			this.comp = comp;
			this.components = components;
			counter = 0;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Def_Function) {
				Def_Function fun = (Def_Function)node;
				Component_Type componentType = fun.getRunsOnType(CompilationTimeStamp.getBaseTimestamp());
				if (componentType !=null && 
						( componentType.equals(comp) 
								|| components.contains(componentType))) {
					counter++;
				}
				//locations.add((Reference)node);
			}
			return V_CONTINUE;
		}
		
		public int getCounter() {
			return counter;
		}
		
		
		private static class LocationComparator implements Comparator<ILocateableNode> {

			@Override
			public int compare(final ILocateableNode arg0, final ILocateableNode arg1) {
				final IResource f0 = arg0.getLocation().getFile();
				final IResource f1 = arg1.getLocation().getFile();
				if (!f0.equals(f1)) {
					return f0.getFullPath().toString().compareTo(f1.getFullPath().toString());
				}

				final int o0 = arg0.getLocation().getOffset();
				final int o1 = arg1.getLocation().getOffset();
				return (o0 < o1) ? -1 : ((o0 == o1) ? 0 : 1);
			}
		}
	}

	
	private static class ReferenceVisitor extends ASTVisitor {

		private final NavigableSet<ILocateableNode> locations;
		
		ReferenceVisitor() {
			locations = new TreeSet<ILocateableNode>(new LocationComparator());
		}

		private NavigableSet<ILocateableNode> getLocations() {
			return locations;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (node instanceof Reference) {
				locations.add((Reference)node);
			}
			return V_CONTINUE;
		}
		
		
		private static class LocationComparator implements Comparator<ILocateableNode> {

			@Override
			public int compare(final ILocateableNode arg0, final ILocateableNode arg1) {
				final IResource f0 = arg0.getLocation().getFile();
				final IResource f1 = arg1.getLocation().getFile();
				if (!f0.equals(f1)) {
					return f0.getFullPath().toString().compareTo(f1.getFullPath().toString());
				}

				final int o0 = arg0.getLocation().getOffset();
				final int o1 = arg1.getLocation().getOffset();
				return (o0 < o1) ? -1 : ((o0 == o1) ? 0 : 1);
			}
		}
	}
	
}
