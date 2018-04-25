/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.slicing;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Bianka Bekefi
 * */
public class SlicingRefactoring extends Refactoring {
	public static final String PROJECTCONTAINSERRORS = "The project `{0}'' contains errors, which might corrupt the result of the refactoring";
	public static final String PROJECTCONTAINSTTCNPPFILES = "The project `{0}'' contains .ttcnpp files, which might corrupt the result of the refactoring";
	private static final String ONTHEFLYANALAYSISDISABLED = "The on-the-fly analysis is disabled, there is semantic information present to work on";

	private final Set<IProject> projects = new HashSet<IProject>();
	protected final IStructuredSelection structSelection;
	protected final SlicingSettings settings;
	protected Map<Module, List<FunctionData> > functions;
	private SlicingModuleRefactoring moduleRefactoring;
	private RefactoringStatus result;
	
	public SlicingRefactoring(IStructuredSelection structSelection, SlicingSettings settings) {
		this.structSelection = structSelection;
		this.settings = settings;
		
		final Iterator<?> it = structSelection.iterator();
		while (it.hasNext()) {
			final Object o = it.next();
			if (o instanceof IResource) {
				final IProject temp = ((IResource) o).getProject();
				projects.add(temp);
			}
		}
	}
	
	@Override
	public String getName() {
		return "Slicing";
	}

	@Override
	public RefactoringStatus checkInitialConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		result = new RefactoringStatus();
		try{
			pm.beginTask("Checking preconditions...", 2);
			
			final IPreferencesService prefs = Platform.getPreferencesService();//PreferenceConstants.USEONTHEFLYPARSING
			if (! prefs.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.USEONTHEFLYPARSING, false, null)) {
				result.addError(ONTHEFLYANALAYSISDISABLED);
			}
			
			// check that there are no ttcnpp files in the
			// project
			for (IProject project : projects) {
				if (hasTtcnppFiles(project)) {//FIXME actually all referencing and referenced projects need to be checked too !
					result.addError(MessageFormat.format(PROJECTCONTAINSTTCNPPFILES, project));
				}
			}
		    pm.worked(1);
		    
		    // check that there are no error markers in the
		    // project
		    for (IProject project : projects) {
		 		final IMarker[] markers = project.findMarkers(null, true, IResource.DEPTH_INFINITE);
		 		for (IMarker marker : markers) {
		 			if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR)) {
		 				result.addError(MessageFormat.format(PROJECTCONTAINSERRORS, project));
		 				break;
		 			}
		 		}
		 	}
		 	pm.worked(1);
			
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			result.addFatalError(e.getMessage());
		} finally {
			pm.done();
		}
		return result;
	}

	@Override
	public RefactoringStatus checkFinalConditions(final IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}
	
	public static boolean hasTtcnppFiles(final IResource resource) throws CoreException {
		if (resource instanceof IProject || resource instanceof IFolder) {
			final IResource[] children = resource instanceof IFolder ? ((IFolder) resource).members() : ((IProject) resource).members();
			for (IResource res : children) {
				if (hasTtcnppFiles(res)) {
					return true;
				}
			}
		} else if (resource instanceof IFile) {
			final IFile file = (IFile) resource;
			return "ttcnpp".equals(file.getFileExtension());
		}
		return false;
	}

	public RefactoringStatus getStatus() {
		return result;
	}
	
	
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return moduleRefactoring.createChange(pm);
	}
	
	
	public IProject getProject() {
		if(moduleRefactoring == null) {
			moduleRefactoring = new SlicingModuleRefactoring(structSelection, settings/*method, excludedModuleNames*/);
			
		}
		return moduleRefactoring.getProject();
	}
	
	public Map<Module, List<FunctionData>> getModules() {
		if(moduleRefactoring == null) {
			moduleRefactoring = new SlicingModuleRefactoring(structSelection, settings/*method, excludedModuleNames*/);
			
		}
		return moduleRefactoring.getModules();
	}	
	
	public List<FunctionData> selectMovableFunctions(TTCN3Module module, SubMonitor progress) {
		return moduleRefactoring.selectMovableFunctions(module, progress);
	}
	
	public  Map<Module, List<FunctionData> > getFunctions() {
		return moduleRefactoring.getFunctions();
	}
	
	public Map<Module, List<FunctionData>> getDestinations() {
		return moduleRefactoring.chooseDestination();
	}
	
	
	public static class SlicingSettings {
		private SlicingType type;
		private SlicingMethod method;
		private Pattern excludedModuleNames = null;
		private boolean changed = true;
		
		public SlicingType getType() {
			return type;
		}
		public void setType(SlicingType type) {
			this.type = type;
		}
		public SlicingMethod getMethod() {
			return method;
		}
		
		public boolean isChanged() {
			return changed;
		}
		
		public void setMethod(SlicingMethod method) {
			this.method = method;
		}
		public Pattern getExcludedModuleNames() {
			return excludedModuleNames;
		}
		public void setExcludedModuleNames(Pattern excludedModuleNames) {
			this.excludedModuleNames = excludedModuleNames;
		}
		
		public void setChanged(boolean changed) {
			this.changed = changed;
		}
	}
	
	public SlicingSettings getSettings() {
		return settings;
	}

}

enum SlicingType {
	MODULE, COMPONENT
}

enum SlicingMethod {
	LENGTH, IMPORTS, LENGTHANDIMPORTS, COMPONENT
}

