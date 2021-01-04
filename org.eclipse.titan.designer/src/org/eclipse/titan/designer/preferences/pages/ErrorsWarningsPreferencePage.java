/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * @author Kristof Szabados
 * */
public final class ErrorsWarningsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences of the on-the-fly analyzer";

	private static final String REPORTUNSUPPORTEDCONSTRUCTS = "Language constructs not supported yet:";
	private static final String REPORTUNSUPPORTEDCONSTRUCTS_TOOLTIP = "For example pattern subtyping in TTCN-3.";
	private static final String HANDLEDEFAULTASOPTIONAL = "DEFAULT elements of ASN.1 sequence and set types as OPTIONAL:";
	private static final String HANDLEDEFAULTASOPTIONALTOOTIP = "Handle the DEFAULT elements of set and sequence ASN.1 types as being optional.\n"
			+ "This is compatibility opition.";

	private static final String REPORT_IGNORED_PREPROCESSOR_DIRECTIVES = "Report ignored preprocessor directives:";
	private static final String REPORT_IGNORED_PREPROCESSOR_DIRECTIVES_TOOLTIP = "Some preprocessor directives (#line,#pragma,etc.) are ignored.\n"
			+ "These should either be removed or the file is an intermediate file (already preprocessed ttcnpp)"
			+ " that contains line markers.\n"
			+ "It is probably a bad idea to edit the intermediate file instead of the original ttcnpp file.";
	private static final String REPORTTYPECOMPATIBILITY = "Report uses of structured-type compatibility:";
	private static final String REPORTTYPECOMPATIBILITY_TOOLTIP = "When structured-type compatibility is used in the code.";
	private static final String REPORTERRORSINEXTENSIONSYNTAX = "Report incorrect syntax in extension attributes:";
	private static final String REPORTERRORSINEXTENSIONSYNTAX_TOOLTIP = "According to the standard"
			+ " syntax errors in the extension attribute should not be reported,"
			+ " but should be assumed as correct for some other tool.";
	private static final String REPORT_STRICT_CONSTANTS = "Use stricter checks for constants, templates and variables";
	private static final String REPORT_STRICT_CONSTANTS_TOOLTIP = "Although it is valid to leave fields of constants and literals unbound,"
			+ " in some cases this was not the intention.";

	private static final String[][] IGNORE_WARNING_ERROR = new String[][] { { "Ignore", GeneralConstants.IGNORE },
			{ "Warning", GeneralConstants.WARNING }, { "Error", GeneralConstants.ERROR } };

	private boolean changed = false;
	private Composite pagecomp;

	public ErrorsWarningsPreferencePage() {
		super(GRID);
	}

	/**
	 * Creates an expandable composite on the user interface.
	 * 
	 * @param parent
	 *                the parent composite where this one can be added to.
	 * @param title
	 *                the title of the new composite.
	 * 
	 * @return the created composite.
	 * */
	private ExpandableComposite createExtendableComposite(final Composite parent, final String title) {
		final ExpandableComposite ex = new ExpandableComposite(parent, SWT.NONE, ExpandableComposite.TWISTIE
				| ExpandableComposite.CLIENT_INDENT | ExpandableComposite.COMPACT);
		ex.setText(title);
		ex.setExpanded(false);
		final GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		ex.setLayoutData(data);
		ex.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				Composite temp = parent;
				while (temp != null && !(temp instanceof ScrolledComposite)) {
					temp = temp.getParent();
				}

				if (temp != null) {
					final Point point = pagecomp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					((ScrolledComposite) temp).setMinSize(point);
					((ScrolledComposite) temp).layout(true, true);
				}
			}
		});

		return ex;
	}

	@Override
	protected Control createContents(final Composite parent) {
		pagecomp = new Composite(parent, SWT.NONE);
		pagecomp.setLayout(new GridLayout(1, false));
		pagecomp.setLayoutData(new GridData(GridData.FILL_BOTH));
		return super.createContents(pagecomp);
	}

	@Override
	protected void createFieldEditors() {
		final Composite tempParent = getFieldEditorParent();

		createCodeStyleSection(tempParent);
		createUnnecessaryCodeSection(tempParent);
		createPotentialProgrammingProblemsSection(tempParent);
	}

	/**
	 * Creates the section of potential issues related to code style being
	 * used. These are not really problems, but rather self constraints the
	 * programmer can practice on himself to enforce the writing of better
	 * code.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createCodeStyleSection(final Composite parent) {
		final ExpandableComposite expandable = createExtendableComposite(parent, "Code style problems");
		final Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(true);

		ComboFieldEditor comboedit = new ComboFieldEditor(PreferenceConstants.REPORTUNSUPPORTEDCONSTRUCTS, REPORTUNSUPPORTEDCONSTRUCTS,
				IGNORE_WARNING_ERROR, comp);
		Label text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTUNSUPPORTEDCONSTRUCTS_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		BooleanFieldEditor defaultAsOptional = new BooleanFieldEditor(PreferenceConstants.DEFAULTASOPTIONAL, HANDLEDEFAULTASOPTIONAL,
				BooleanFieldEditor.SEPARATE_LABEL, comp);
		defaultAsOptional.getLabelControl(comp).setToolTipText(HANDLEDEFAULTASOPTIONALTOOTIP);
		defaultAsOptional.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(defaultAsOptional);

		comboedit = new ComboFieldEditor(PreferenceConstants.REPORTTYPECOMPATIBILITY, REPORTTYPECOMPATIBILITY, IGNORE_WARNING_ERROR, comp);
		text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTTYPECOMPATIBILITY_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);

		defaultAsOptional = new BooleanFieldEditor(PreferenceConstants.REPORT_STRICT_CONSTANTS, REPORT_STRICT_CONSTANTS,
				BooleanFieldEditor.SEPARATE_LABEL, comp);
		defaultAsOptional.getLabelControl(comp).setToolTipText(REPORT_STRICT_CONSTANTS_TOOLTIP);
		defaultAsOptional.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(defaultAsOptional);
	}

	/**
	 * Creates the section of potential issues related to potential
	 * programming problems. All of these should be considered as normal
	 * errors, but on our current level we might not be able to detect them
	 * in all cases correctly.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createPotentialProgrammingProblemsSection(final Composite parent) {
		final ExpandableComposite expandable = createExtendableComposite(parent, "Potential programming problems");
		final Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(false);

		ComboFieldEditor comboedit = new ComboFieldEditor(PreferenceConstants.REPORTERRORSINEXTENSIONSYNTAX, REPORTERRORSINEXTENSIONSYNTAX,
				IGNORE_WARNING_ERROR, comp);
		Label text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORTERRORSINEXTENSIONSYNTAX_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);
	}

	/**
	 * Creates the section of potential issues related to unnecessary code
	 * in the projects.
	 * 
	 * @param parent
	 *                the parent composite to put the section under.
	 * */
	private void createUnnecessaryCodeSection(final Composite parent) {
		final ExpandableComposite expandable = createExtendableComposite(parent, "Unnecessary code");
		final Composite comp = new Composite(expandable, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expandable.setClient(comp);
		expandable.setExpanded(false);

		ComboFieldEditor comboedit = new ComboFieldEditor(PreferenceConstants.REPORT_IGNORED_PREPROCESSOR_DIRECTIVES, REPORT_IGNORED_PREPROCESSOR_DIRECTIVES,
				IGNORE_WARNING_ERROR, comp);
		Label text = comboedit.getLabelControl(comp);
		text.setToolTipText(REPORT_IGNORED_PREPROCESSOR_DIRECTIVES_TOOLTIP);
		comboedit.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				changed = true;
			}
		});
		addField(comboedit);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		changed = true;
		super.propertyChange(event);
	}

	@Override
	public boolean performOk() {
		final boolean result = super.performOk();
		if (changed && getPreferenceStore().getBoolean(PreferenceConstants.USEONTHEFLYPARSING)) {
			changed = false;

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(null, "Error/Warning settings changed",
							"Error/Warning settings have changed, the known projects have to be re-analyzed completly.\nThis might take some time.");
				}
			});

			GlobalParser.clearSemanticInformation();
			GlobalParser.reAnalyzeSemantically();
		}

		return result;
	}
}
