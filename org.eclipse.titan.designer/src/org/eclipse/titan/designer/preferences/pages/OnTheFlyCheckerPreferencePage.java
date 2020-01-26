/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.OutOfMemoryCheck;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kristof Szabados
 * */
public final class OnTheFlyCheckerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String DESCRIPTION = "Preferences of the on-the-fly checker";

	private static final String CHECK_MEMORY = "Warn and disable parsing before the system runs out of memory";
	private static final String ENABLE_PARSING = "Enable parsing of TTCN-3, ASN.1 and Runtime Configuration files";
	private static final String ENABLE_INCREMENTAL_PARSING = "Enable the incremental parsing of TTCN-3 files (EXPERIMENTAL)";
	private static final String ENABLE_PARALLEL_CHECKING = "Enable the parallel semantic checking of modules (EXPERIMENTAL)";
	private static final String DELAY_SEMANTIC_CHECKING = "Delay the on-the-fly semantic check till the file is saved";
	private static final String RECONCILER_TIMEOUT = "Timeout in seconds before on-the-fly check starts";
	private static final String ENABLE_REALTIME = "Enable support for the realtime extension";

	private	Composite composite;
	private BooleanFieldEditor checkForLowMemory;
	private BooleanFieldEditor useOnTheFlyParsing;
	private BooleanFieldEditor useIncrementalParsing;
	private BooleanFieldEditor useParallelSemanticChecking;
	private BooleanFieldEditor delaySemanticCheckTillSave;
	private IntegerFieldEditor reconcilerTimeout;
	private BooleanFieldEditor enableRealtimeSupport;

	private Boolean realtimeSupport;

	public OnTheFlyCheckerPreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		Composite tempParent = getFieldEditorParent();

		checkForLowMemory = new BooleanFieldEditor(PreferenceConstants.CHECKFORLOWMEMORY, CHECK_MEMORY, tempParent);
		addField(checkForLowMemory);

		useOnTheFlyParsing = new BooleanFieldEditor(PreferenceConstants.USEONTHEFLYPARSING, ENABLE_PARSING, tempParent);
		addField(useOnTheFlyParsing);

		useIncrementalParsing = new BooleanFieldEditor(PreferenceConstants.USEINCREMENTALPARSING, ENABLE_INCREMENTAL_PARSING, tempParent);
		addField(useIncrementalParsing);

		useParallelSemanticChecking = new BooleanFieldEditor(PreferenceConstants.USEPARALLELSEMATICCHECKING, ENABLE_PARALLEL_CHECKING, tempParent);
		addField(useParallelSemanticChecking);

		composite = new Composite(tempParent, SWT.NONE);
		final GridLayout compositeLayout = new GridLayout();
		composite.setLayout(compositeLayout);
		final GridData compositeData = new GridData(GridData.FILL);
		compositeData.grabExcessHorizontalSpace = true;
		compositeData.horizontalAlignment = SWT.FILL;
		composite.setLayoutData(compositeData);

		reconcilerTimeout = new IntegerFieldEditor(PreferenceConstants.RECONCILERTIMEOUT, RECONCILER_TIMEOUT, composite);
		reconcilerTimeout.setValidRange(0, 10);
		reconcilerTimeout.setTextLimit(2);
		addField(reconcilerTimeout);

		delaySemanticCheckTillSave = new BooleanFieldEditor(PreferenceConstants.DELAYSEMANTICCHECKINGTILLSAVE, DELAY_SEMANTIC_CHECKING, tempParent);
		addField(delaySemanticCheckTillSave);

		enableRealtimeSupport = new BooleanFieldEditor(PreferenceConstants.ENABLEREALTIMEEXTENSION, ENABLE_REALTIME, tempParent);
		addField(enableRealtimeSupport);
		realtimeSupport = new Boolean(enableRealtimeSupport.getBooleanValue());
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
	public void dispose() {
		if (getPreferenceStore().getBoolean(PreferenceConstants.USEONTHEFLYPARSING)) {
			OutOfMemoryCheck.resetOutOfMemoryflag();
		}
		useOnTheFlyParsing.dispose();
		useIncrementalParsing.dispose();
		useParallelSemanticChecking.dispose();
		delaySemanticCheckTillSave.dispose();
		reconcilerTimeout.dispose();
		enableRealtimeSupport.dispose();
		composite.dispose();
		super.dispose();
	}

	private boolean isImportantChanged() {
		return false;
	}

	@Override
	public void performApply() {
		if (isImportantChanged() && getPreferenceStore().getBoolean(PreferenceConstants.USEONTHEFLYPARSING)) {
			ErrorReporter.parallelWarningDisplayInMessageDialog(
				"On-the-fly analyzer",
				"Settings of the on-the-fly analyzer have changed, the known projects have to be re-analyzed completly.\n" 
				+ "This might take some time.");


			GlobalParser.clearSemanticInformation();
			GlobalParser.reAnalyzeSemantically();
		}
		if (getPreferenceStore().getBoolean(PreferenceConstants.USEONTHEFLYPARSING)) {
			OutOfMemoryCheck.resetOutOfMemoryflag();
		}
		if (realtimeSupport != null && realtimeSupport != enableRealtimeSupport.getBooleanValue()) {
			realtimeSupport = enableRealtimeSupport.getBooleanValue();
			ErrorReporter.parallelWarningDisplayInMessageDialog(
					"On-the-fly analyzer",
					"The setting of the on-the-fly analyzer's support for the realtime extension has changed.\n" 
					+ "Eclipse will need to be restarted for this change to take effect.");
		}
		super.performApply();

	}
	@Override
	public boolean performOk() {
		boolean result = super.performOk();

		if (realtimeSupport != null && realtimeSupport != getPreferenceStore().getBoolean(PreferenceConstants.ENABLEREALTIMEEXTENSION)) {
			ErrorReporter.parallelWarningDisplayInMessageDialog(
					"On-the-fly analyzer",
					"The setting of the on-the-fly analyzer's support for the realtime extension has changed.\n" 
					+ "Eclipse needs to be restarted for this change to take effect.");
		}
		return result;
	}
}
