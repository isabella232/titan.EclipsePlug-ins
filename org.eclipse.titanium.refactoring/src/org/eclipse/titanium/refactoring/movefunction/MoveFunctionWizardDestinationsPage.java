/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.movefunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.designer.AST.Module;

/**
 * @author Bianka Bekefi
 * */
public class MoveFunctionWizardDestinationsPage extends UserInputWizardPage {

	private final MoveFunctionRefactoring refactoring;
	private CheckboxTreeViewer tree;
	protected static boolean displayZeros;
	private Button displayZerosCheckBox;

	MoveFunctionWizardDestinationsPage(final String name, final MoveFunctionRefactoring refactoring) {
		super(name);
		this.refactoring = refactoring;
		refactoring.getSettings().setType(MoveFunctionType.MODULE);
		refactoring.getSettings().setMethod(MoveFunctionMethod.LENGTH);
		refactoring.getSettings().setExcludedModuleNames(Pattern.compile(".*_types"));
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		initializeDialogUnits(top);
		top.setLayout(new GridLayout(1, false));

		final Composite comp = new Composite(top, SWT.NONE);
		initializeDialogUnits(comp);
		comp.setLayout(new GridLayout(1, false));

		final Label label = new Label(comp, SWT.NONE);
		label.setText("Choose method: ");
		final Button shortestModule = new Button(comp, SWT.RADIO);
		shortestModule.setText("choose shortest module");
		shortestModule.setSelection(true);
		shortestModule.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				if (refactoring.getSettings().getMethod().equals(MoveFunctionMethod.LENGTH) && !refactoring.getSettings().isChanged()) {
					refactoring.getSettings().setChanged(false);
				}
				else {
					refactoring.getSettings().setMethod(MoveFunctionMethod.LENGTH);
					refactoring.getSettings().setChanged(true);
				}

				refreshTree();
			};
		});


		final Button leastImports = new Button(comp, SWT.RADIO);
		leastImports.setText("insert the least new imports");
		leastImports.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				if (refactoring.getSettings().getMethod().equals(MoveFunctionMethod.IMPORTS) && !refactoring.getSettings().isChanged()) {
					refactoring.getSettings().setChanged(false);
				}
				else {
					refactoring.getSettings().setMethod(MoveFunctionMethod.IMPORTS);
					refactoring.getSettings().setChanged(true);
				}
				refreshTree();
			};
		});
		/*
		Button lengthAndImports = new Button(comp, SWT.RADIO);
		lengthAndImports.setText("both");
		lengthAndImports.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				refactoring.getSettings().setMethod(SlicingMethod.LENGTHANDIMPORTS);
				refreshTree();
	         };
		});
		 */
		final Button component = new Button(comp, SWT.RADIO);
		component.setText("by component");
		component.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent event) {
				if (refactoring.getSettings().getMethod().equals(MoveFunctionMethod.COMPONENT) && !refactoring.getSettings().isChanged()) {
					refactoring.getSettings().setChanged(false);
				}
				else {
					refactoring.getSettings().setMethod(MoveFunctionMethod.COMPONENT);
					refactoring.getSettings().setChanged(true);
				}
				refreshTree();
			};
		});

		final Label excludedModules = new Label(comp, SWT.FILL);
		excludedModules.setText("Module names to exclude (separate regex expressions with a comma): ");
		final Text excludedModulesField = new Text(comp, SWT.FILL);


		displayZerosCheckBox = new Button(comp, SWT.CHECK);
		displayZerosCheckBox.setSelection(displayZeros);
		displayZerosCheckBox.setText("Display destinations with 0 value");
		displayZerosCheckBox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				final Button btn = (Button) event.getSource();
				displayZeros = btn.getSelection();
				tree.refresh();
			}
		});

		final Label descriptionLabel = new Label(comp, SWT.FILL);
		descriptionLabel.setText("Destinations for the selected functions:");

		displayDestinations(top);
		excludedModulesField.setText(".*_types                              ");

		excludedModulesField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				final String regex = excludedModulesField.getText().replaceAll(",", "|").replaceAll(" +", " ").trim();
				try {
					final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

					if (refactoring.getSettings().getExcludedModuleNames().equals(pattern) && !refactoring.getSettings().isChanged()) {
						refactoring.getSettings().setChanged(false);
					}
					else {
						refactoring.getSettings().setExcludedModuleNames(pattern);
						refactoring.getSettings().setChanged(true);
					}
					refreshTree();
					setErrorMessage(null);
				} catch (PatternSyntaxException exception) {
					setErrorMessage("Regex expression is wrong.");
				}
			}
		});
		setTreeChecked();
		setErrorMessage(null);
	}

	public void displayDestinations(final Composite parent) {
		final Composite comp = new Composite(parent, SWT.NONE);
		initializeDialogUnits(comp);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		comp.setLayout(new FillLayout());

		refactoring.getDestinations();
		tree = new CheckboxTreeViewer (comp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setContentProvider(new DestinationDataProvider());
		tree.setLabelProvider(new DataLabelProvider());
		tree.setInput(refactoring.getFunctions());
		tree.expandToLevel(2);

		tree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(final CheckStateChangedEvent event) {
				if (event.getElement() instanceof FunctionData) {
					if (event.getChecked()) {
						tree.setChecked(event.getElement(), event.getChecked());
						((FunctionData)event.getElement()).setToBeMoved(true);

					}
					else {
						tree.setSubtreeChecked(event.getElement(), false);
						((FunctionData)event.getElement()).setFinalDestination(null);
					}
				}
				else if (event.getElement() instanceof Destination) {
					final Destination dest = (Destination)event.getElement();
					tree.setChecked(dest, event.getChecked());
					for (final Map.Entry<Module, List<FunctionData>> entry : refactoring.getFunctions().entrySet()) {
						if (entry.getValue().contains(dest.getFunctionData())) {
							for (final FunctionData fd : entry.getValue()) {
								if (dest.getFunctionData().equals(fd)) {
									if (fd.getFinalDestination() != null & !dest.equals(fd.getFinalDestination())) {
										tree.setChecked(fd.getFinalDestination(), false);
									}
									if (event.getChecked()) {
										fd.setFinalDestination(dest);
									}
									else {
										fd.setFinalDestination(null);
									}
									break;
								}
							}
							break;
						}
					}
				}
				setTreeChecked();
			}
		});
		setTreeChecked();
	}

	public void refreshTree() {
		refactoring.getDestinations();
		tree.refresh();
		setTreeChecked();
	}

	public void setTreeChecked() {
		boolean hasDestination = false;
		for (final List<FunctionData> list : refactoring.getFunctions().values()) {
			for (final FunctionData fd : list) {
				if (fd.isToBeMoved()) {
					tree.setChecked(fd, true);
					if (fd.getFinalDestination() != null) {
						hasDestination = true;
						tree.setChecked(fd.getFinalDestination(), true);
					}
				}
				else {
					tree.setSubtreeChecked(fd, false);
				}
			}
		}
		tree.refresh();
		setPageComplete(hasDestination);
	}

	@Override
	public IWizardPage getPreviousPage() {
		final IWizardPage page2 = super.getPreviousPage();
		if (page2 instanceof MoveFunctionWizardFunctionsPage) {
			((MoveFunctionWizardFunctionsPage)page2).refreshTree();
		}
		return page2;
	}
}

class DestinationDataProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof Map<?, ?>) {
			final Map<Module, List<FunctionData>> map = (Map<Module, List<FunctionData>>)inputElement;
			final ArrayList<FunctionData> obj = new ArrayList<FunctionData>();
			for (final Map.Entry<Module, List<FunctionData>> entry : map.entrySet()) {
				for (final FunctionData fd : entry.getValue()) {
					if (fd.isToBeMoved() && !fd.getDestinations().isEmpty()) {
						obj.add(fd);
					}
				}
			}
			if (obj.isEmpty()) {
				return new Object[] {"No new destinations found for the function(s)."};
			}
			Collections.sort(obj);

			return obj.toArray();
		}
		return new Object[] {"No new destinations found for the function(s)."};
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof FunctionData) {
			if (!((FunctionData)parentElement).getDestinations().isEmpty()) {
				final List<Destination> destinations = new ArrayList<Destination>();
				for (final Destination dest : ((FunctionData)parentElement).getDestinations()) {
					if (!MoveFunctionWizardDestinationsPage.displayZeros && dest.getRating()!= 0) {
						destinations.add(dest);
					} else if (MoveFunctionWizardDestinationsPage.displayZeros) {
						destinations.add(dest);
					}
				}
				Collections.sort(destinations, new Comparator<Destination>() {

					@Override
					public int compare(final Destination arg0, final Destination arg1) {
						final int val = (-1)*(arg0.getRating() - arg1.getRating());
						int val2 = 0;
						if (val == 0 && arg0.getNewImports() != -1) {
							val2 = arg1.getNewImports() - arg0.getNewImports();
						}
						if (val == 0 && val2 == 0) {
							return arg0.getModule().getIdentifier().getDisplayName().compareTo(arg1.getModule().getIdentifier().getDisplayName());
						}
						return val;
					}

				});
				return destinations.toArray();
			}
		}
		return null;
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof Module) {
			return false;
		}
		else if (element instanceof FunctionData) {
			if (!((FunctionData)element).getDestinations().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

}

