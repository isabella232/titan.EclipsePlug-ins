/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences.pages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This preference page holds the controls and functionality to set the matching
 * bracket feature related options.
 * 
 * @author Kristof Szabados
 */
public final class ShowMatchingBracketPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	static final String DESCRIPTION = "Preferences for the editor's matching brackets";
	static final String HIGHLIGHT_MATCHING_BRACKETS = "Highlight matching brackets";
	static final String COLOR = "color:";

	public ShowMatchingBracketPage() {
		super(GRID);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setDescription(DESCRIPTION);
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor enableMatchingBrackets = new BooleanFieldEditor(PreferenceConstants.MATCHING_BRACKET_ENABLED,
				HIGHLIGHT_MATCHING_BRACKETS, getFieldEditorParent());
		addField(enableMatchingBrackets);

		ColorFieldEditor matchingBracketColor = new ColorFieldEditor(PreferenceConstants.COLOR_MATCHING_BRACKET, COLOR,
				getFieldEditorParent());
		addField(matchingBracketColor);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
