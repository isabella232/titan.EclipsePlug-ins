/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.pages;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Label;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.preferences.PreferenceConstants;
import org.eclipse.titan.log.viewer.preferences.data.KeywordColor;
import org.eclipse.titan.log.viewer.preferences.fieldeditors.StringListEditor;
import org.eclipse.titan.log.viewer.utils.ImportExportUtils;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.ui.IWorkbench;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog.
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */
public class HighlightKeywordsPage extends LogViewerPreferenceRootPage {

	private StringListEditor highLightEditor;
	private BooleanFieldEditor useHighLight;

	/**
	 * Constructor
	 */
	public HighlightKeywordsPage() {
		super(GRID, false);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("HighlightKeywordsPage.0")); //$NON-NLS-1$
	}

	@Override
	public void createFieldEditors() {

		new Label(getFieldEditorParent(), SWT.NONE);

		this.useHighLight = new BooleanFieldEditor(
				PreferenceConstants.PREF_USE_HIGHLIGHT_ID,
				Messages.getString("HighlightKeywordsPage.1"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.useHighLight);

		this.highLightEditor = new StringListEditor(PreferenceConstants.PREF_HIGHLIGHT_KEYWORD_ID,
				"", //$NON-NLS-1$
				getFieldEditorParent(),
				false,
				false);

		addField(this.highLightEditor);

	}

	@Override
	protected String getPageId() {
		return PreferenceConstants.PAGE_ID_HIGHLIGHT_KEYWORDS_PAGE;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, String> getCurrentPreferences(final String separator) {
		final Map<String, String> currentPrefs = new HashMap<String, String>();
		final String highlight = String.valueOf(this.useHighLight.getBooleanValue());

		String keywords = ""; //$NON-NLS-1$
		final String[] elements = this.highLightEditor.getElements();

		for (final String element : elements) {
			keywords = keywords.concat(element + separator);
		}
		currentPrefs.put(PreferenceConstants.PREF_USE_HIGHLIGHT_ID, highlight);
		currentPrefs.put(PreferenceConstants.PREF_HIGHLIGHT_KEYWORD_ID, keywords);
		return currentPrefs;
	}

	/**
	 * Method for getting the current preferences in the preference store
	 * @return map of the preferences
	 */
	private Map<String, Object[]> getCurrentPreferencesSeparated() {
		final Map<String, Object[]> currentPrefs = new HashMap<String, Object[]>();
		final String[] highlight = new String[] {String.valueOf(this.useHighLight.getBooleanValue())};
		currentPrefs.put(PreferenceConstants.PREF_USE_HIGHLIGHT_ID, highlight);
		final String[] listItems = this.highLightEditor.getElements();
		final Map<String, RGB> colors = this.highLightEditor.getColors();
		final KeywordColor[] values = new KeywordColor[this.highLightEditor.getElements().length];

		for (int i = 0; i < listItems.length; i++) {
			final String item = listItems[i];
			final RGB color = colors.get(item);
			final KeywordColor keywordColor = new KeywordColor(item, color);
			values[i] = keywordColor;

		}
		currentPrefs.put(this.highLightEditor.getPreferenceName(), values);
		return currentPrefs;
	}

	@Override
	protected void exportPreferences() {
		ImportExportUtils.exportColorSettings(getPageId(), getCurrentPreferencesSeparated(), true);

	}

	@Override
	protected void importPreferences() {
		final Map<String, String> prop = ImportExportUtils.importSettings(PreferenceConstants.PAGE_ID_HIGHLIGHT_KEYWORDS_PAGE);
		//if cancel
		if (prop == null) {
			return;
		}
		setOldPreferences(getCurrentPreferences(File.pathSeparator));
		setProperties(prop);
		// Settings changed -> Enable apply button
		getApplyButton().setEnabled(true);
	}

	@Override
	protected void updatePage() {
		this.useHighLight.loadDefault();
		this.useHighLight.load();
		this.highLightEditor.loadDefault();
		this.highLightEditor.load();
	}

	@Override
	public void init(final IWorkbench workbench) {
		// Do nothing
	}

}
