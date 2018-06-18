/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.slicing;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;

/**
 * @author Bianka Bekefi
 * */
public class SlicingWizardFunctionsPage extends UserInputWizardPage {

	private SlicingRefactoring refactoring;
	private StyledText functionBody;
	private CheckboxTreeViewer tree;
	
	SlicingWizardFunctionsPage(final String name, SlicingRefactoring refactoring) {
		super(name);
		this.refactoring = refactoring;
		refactoring.getSettings().setType(SlicingType.MODULE);
		refactoring.getSettings().setMethod(SlicingMethod.LENGTH);
	}
	
	@Override
	public void createControl(final Composite parent) {
		final Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		initializeDialogUnits(top);
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    top.setLayout(new GridLayout(1, false));
	    
	    Label title = new Label(top, SWT.NULL);
		title.setText("Select the functions you want to move.");
		
	    Composite comp1 = new Composite(top, SWT.NONE);
		initializeDialogUnits(comp1);
		comp1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		comp1.setLayout(new FillLayout());
		
		Composite comp2 = new Composite(top, SWT.NONE);
		initializeDialogUnits(comp2);
		comp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		comp2.setLayout(new FillLayout());
		
	    tree = new CheckboxTreeViewer (comp1, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setContentProvider(new DataProvider());
		tree.setLabelProvider(new DataLabelProvider());
		tree.setInput(refactoring.getModules());
		
		tree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(final CheckStateChangedEvent event) {
				if (event.getElement() instanceof Module && event.getChecked() && refactoring.getFunctions().get(event.getElement()).isEmpty()) {
					WorkspaceJob wj = new WorkspaceJob("Find functions in module: "+event.getElement()) {
						@Override
						public IStatus runInWorkspace(final IProgressMonitor monitor) {
							try {
								SubMonitor progress = SubMonitor.convert(monitor, ((TTCN3Module)event.getElement()).getDefinitions().getNofAssignments());
								progress.setTaskName("Analysis of module: "+event.getElement());
								final Object[] children = refactoring.selectMovableFunctions((TTCN3Module)event.getElement(), progress).toArray();
								progress.done();
								Display.getDefault().syncExec(new Runnable() {
									@Override
									public void run() {
										if (children != null) {
											tree.add(event.getElement(), children);
											tree.setSubtreeChecked(event.getElement(), event.getChecked());
										}
										tree.expandToLevel(event.getElement(), 2);
										setCheckedFunctions();
									}
								});
								return Status.OK_STATUS;
							} finally {
							}
						}
					};
					wj.setSystem(false);
					wj.setUser(true);
					wj.schedule();
				}
				else {
					tree.setSubtreeChecked(event.getElement(), event.getChecked());
					setCheckedFunctions();
				}	
		   }
				
		});

		functionBody = new StyledText(comp2, SWT.V_SCROLL | SWT.H_SCROLL);
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				for (Map.Entry<Module, List<FunctionData>> entry : refactoring.getFunctions().entrySet()) {
					for (FunctionData fd : entry.getValue()) {
						if (event.getSelection().toString().substring(1, event.getSelection().toString().length()-1).equals(fd.toString())) {
							functionBody.setText(fd.getFunctionBody());
						}
					}
				}
			}				
		});

		setCheckedFunctions();
		if (refactoring.getStatus().hasEntries()) {
	    	setErrorMessage(refactoring.getStatus().getEntryAt(0).getMessage()+"\nThis feature is work in progress.");
	    } 
		else {
			setErrorMessage("This feature is work in progress.");
	    }		
	}
	
	public SlicingRefactoring getRefactoring() {
		return refactoring;
	}
	
	public void refreshTree() {
		tree.refresh();
		for (List<FunctionData> list : refactoring.getFunctions().values()) {
			for (FunctionData fd : list) {
				if (fd.isToBeMoved()) {
					tree.setSubtreeChecked(fd, true);
				}
				else {
					tree.setSubtreeChecked(fd, false);
				}
			}
		}
		if (refactoring.getStatus().hasEntries()) {
	    	setErrorMessage(refactoring.getStatus().getEntryAt(0).getMessage()+"\nThis feature is work in progress.");
	    } 
		else {
			setErrorMessage("This feature is work in progress.");
	    }	
	}
	
	public void setCheckedFunctions() {
		List<Object> checked = Arrays.asList(tree.getCheckedElements());
		
		boolean isChecked = false;
		for (Map.Entry<Module, List<FunctionData>> entry : refactoring.getFunctions().entrySet()) {
			for (FunctionData fd : entry.getValue()) {
				if (checked.contains(fd)) {
					isChecked = true;
					fd.setToBeMoved(true);
				}
				else {
					fd.setToBeMoved(false);
				}
			}
		}
		if (tree.getCheckedElements().length == 0 || !isChecked) {
			setPageComplete(false);
		}
		else if (isChecked && !isPageComplete()) {
			setPageComplete(true);
		}
	}
	
	
	@Override
	public IWizardPage getNextPage() {
		IWizardPage page2 = super.getNextPage();
		if (page2.getControl() != null) {
			if (page2 instanceof SlicingWizardDestinationsPage) {
				((SlicingWizardDestinationsPage)page2).refreshTree();
			}
		}
		return page2;
    }

	class DataProvider implements ITreeContentProvider {
		
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Map<?, ?>) {
				Module[] modules = new Module[((Map<Module, List<FunctionData>>)inputElement).keySet().size()];
				((Map<Module, List<FunctionData>>)inputElement).keySet().toArray(modules);
				Arrays.sort(modules, new Comparator<Module>() {
			        @Override
			        public int compare(Module m1, Module m2)
			        {
			            return  m1.getIdentifier().getDisplayName().compareToIgnoreCase(m2.getIdentifier().getDisplayName());
			        }
			    });
				if (modules.length == 0) {
					return new Object[] {"There are no functions to be moved."};
				}
				return modules;
			}
			return new Object[] {"There are no functions to be moved."};
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Module) {
				if (!refactoring.getFunctions().get(parentElement).isEmpty()) {					
					Collections.sort(refactoring.getFunctions().get(parentElement));
					return refactoring.getFunctions().get(parentElement).toArray();
				}
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof Module) {
				return true;
			}
			return false;
		}
		
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {			
		}
	}
}


class DataLabelProvider extends LabelProvider {
	@Override
	public String getText(final Object element) {
		if (element instanceof FunctionData) {
			return ((FunctionData)element).getDefiniton().getIdentifier().getDisplayName();
		}
		else if (element instanceof Module) {
			return ((Module)element).getIdentifier().getDisplayName();
		}
		else if (element instanceof Destination) {
			Destination dest = (Destination)element;
			if (dest.getNewImports() != -1) {
				return dest.getModule().getIdentifier().getDisplayName()+"                     "+dest.getRating()+" ("+dest.getNewImports()+" new imports)";
			}
			else {
				return dest.getModule().getIdentifier().getDisplayName()+"                     "+dest.getRating();
			}
		}
		return element.toString();
	}
}