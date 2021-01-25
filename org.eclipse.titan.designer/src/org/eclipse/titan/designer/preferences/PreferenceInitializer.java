/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences;

import java.util.Map;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.titan.designer.Activator;
import org.eclipse.titan.designer.GeneralConstants;
import org.osgi.service.prefs.Preferences;
import org.eclipse.swt.graphics.RGB;

/**
 * This class is used for initializing the internal values to their default state.
 * 
 * @author Kristof Szabados
 * @author Miklos Magyari
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public final void initializeDefaultPreferences() {
		final IPreferenceStore preferenceStore = getPreference();

		final String ttcn3Dir = System.getenv("TTCN3_DIR");
		if (ttcn3Dir != null) {
			preferenceStore.setDefault(PreferenceConstants.TITAN_INSTALLATION_PATH, ttcn3Dir);
		}

		final String licenseFile = System.getenv("TTCN3_LICENSE_FILE");
		if (licenseFile != null) {
			preferenceStore.setDefault(PreferenceConstants.LICENSE_FILE_PATH, licenseFile);
		}

		preferenceStore.setDefault(PreferenceConstants.COMPILERMARKERSAFTERANALYZATION,  PreferenceConstantValues.COMPILEROPTIONOUTDATE);
		preferenceStore.setDefault(PreferenceConstants.ONTHEFLYMARKERSAFTERCOMPILER,  PreferenceConstantValues.ONTHEFLYOPTIONSTAY);
		preferenceStore.setDefault(PreferenceConstants.REPORTPROGRAMERRORWITHMARKER, false);
		preferenceStore.setDefault(PreferenceConstants.TREATONTHEFLYERRORSFATALFORBUILD, false);
		preferenceStore.setDefault(PreferenceConstants.CHECKFORLOWMEMORY, true);
		preferenceStore.setDefault(PreferenceConstants.USEONTHEFLYPARSING, true);
		preferenceStore.setDefault(PreferenceConstants.USEINCREMENTALPARSING, false);
		preferenceStore.setDefault(PreferenceConstants.DELAYSEMANTICCHECKINGTILLSAVE, true);
		preferenceStore.setDefault(PreferenceConstants.RECONCILERTIMEOUT, 1);
		preferenceStore.setDefault(PreferenceConstants.ENABLEREALTIMEEXTENSION, false);
		preferenceStore.setDefault(PreferenceConstants.PROCESSINGUNITSTOUSE, PreferenceConstantValues.AVAILABLEPROCESSORS);
		preferenceStore.setDefault(PreferenceConstants.CONSOLE_ACTION_BEFORE_BUILD, PreferenceConstantValues.BEFORE_BUILD_PRINT_CONSOLE_DELIMITERS);

		//		content assistance
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_SINGLE_PROPOSAL_INSERTION, false);
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_COMMON_PREFIX_INSERTION, false);
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_PROPOSAL_SORTING, PreferenceConstantValues.SORT_BY_RELEVANCE);
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION, true);
		preferenceStore.setDefault(PreferenceConstants.CONTENTASSISTANT_AUTO_ACTIVATION_DELAY, 100);

		//		export
		preferenceStore.setDefault(PreferenceConstants.EXPORT_EXCLUDE_WORKING_DIRECTORY_CONTENTS, true);
		preferenceStore.setDefault(PreferenceConstants.EXPORT_EXCLUDE_DOT_RESOURCES, true);
		preferenceStore.setDefault(PreferenceConstants.EXPORT_EXCLUDE_LINKED_CONTENTS, false);
		preferenceStore.setDefault(PreferenceConstants.EXPORT_SAVE_DEFAULT_VALUES, false);
		preferenceStore.setDefault(PreferenceConstants.EXPORT_PACK_ALL_PROJECTS_INTO_ONE, false);
		preferenceStore.setDefault(PreferenceConstants.USE_TPD_NAME, false);
		preferenceStore.setDefault(PreferenceConstants.ORIG_TPD_URI, "");

		preferenceStore.setDefault(PreferenceConstants.EXPORT_AUTOMATIC_EXPORT, false);
		preferenceStore.setDefault(PreferenceConstants.EXPORT_REQUEST_LOCATION, false);

		//		folding
		preferenceStore.setDefault(PreferenceConstants.FOLDING_ENABLED, true);
		preferenceStore.setDefault(PreferenceConstants.FOLD_COMMENTS, true);
		preferenceStore.setDefault(PreferenceConstants.FOLD_STATEMENT_BLOCKS, true);
		preferenceStore.setDefault(PreferenceConstants.FOLD_PARENTHESIS, true);
		preferenceStore.setDefault(PreferenceConstants.FOLD_DISTANCE, 3);

		preferenceStore.setDefault(PreferenceConstants.MATCHING_BRACKET_ENABLED, true);

		// 		indentation
		preferenceStore.setDefault(PreferenceConstants.INDENTATION_TAB_POLICY, PreferenceConstantValues.TAB_POLICY_2);
		preferenceStore.setDefault(PreferenceConstants.INDENTATION_SIZE, "2");

		//		typing
		preferenceStore.setDefault(PreferenceConstants.CLOSE_APOSTROPHE, true);
		preferenceStore.setDefault(PreferenceConstants.CLOSE_PARANTHESES, true);
		preferenceStore.setDefault(PreferenceConstants.CLOSE_SQUARE, true);
		preferenceStore.setDefault(PreferenceConstants.CLOSE_BRACES, true);
		preferenceStore.setDefault(PreferenceConstants.AUTOMATICALLY_MOVE_BRACES, true);
		//		matching brackets
		preferenceStore.setDefault(PreferenceConstants.COLOR_MATCHING_BRACKET, StringConverter.asString(PreferenceConstantValues.GREY20));
		preferenceStore.setDefault(PreferenceConstants.EXCLUDED_RESOURCES, "");
		preferenceStore.setDefault(PreferenceConstants.T3DOC_ENABLE, false);

		markOccurrences(preferenceStore);
		onTheFlyChecker(preferenceStore);
		titanActions(preferenceStore);
		outline(preferenceStore);
		namingConventionPreferences(preferenceStore);
		color(preferenceStore);
		debug(preferenceStore);
		findDefinition(preferenceStore);
		
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("org.eclipse.e4.ui.css.swt.theme");
		prefs.addPreferenceChangeListener(new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				Map<String, Object> colorMap = getColorMap();
				for (Map.Entry<String, Object> entry : colorMap.entrySet()) {
					if (entry.getValue() instanceof RGB) {
						preferenceStore.setDefault(entry.getKey(), StringConverter.asString((RGB)entry.getValue()));
						preferenceStore.setValue(entry.getKey(), StringConverter.asString((RGB)entry.getValue()));
					}
					else if (entry.getValue() instanceof Boolean) {
						preferenceStore.setDefault(entry.getKey(), (Boolean) entry.getValue());
						preferenceStore.setValue(entry.getKey(), (Boolean) entry.getValue());
					}
				}
			}
		});
	}

	private void markOccurrences(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_ENABLED, true);
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_DELAY, 300);
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_KEEP_MARKS, true);
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_ASN1_ASSIGNMENTS, true);
		preferenceStore.setDefault(PreferenceConstants.MARK_OCCURRENCES_TTCN3_ASSIGNMENTS, true);
	}

	private void onTheFlyChecker(final IPreferenceStore preferenceStore) {
		//on-the-fly checker
		preferenceStore.setDefault(PreferenceConstants.REPORTUNSUPPORTEDCONSTRUCTS, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.DEFAULTASOPTIONAL, false);
		preferenceStore.setDefault(PreferenceConstants.REPORT_IGNORED_PREPROCESSOR_DIRECTIVES, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORTTYPECOMPATIBILITY, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORTERRORSINEXTENSIONSYNTAX, GeneralConstants.WARNING);
		preferenceStore.setDefault(PreferenceConstants.REPORT_STRICT_CONSTANTS, false);
	}

	private void titanActions(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.TITANACTIONS_PROCESSEXCLUDEDRESOURCES, false);
		preferenceStore.setDefault(PreferenceConstants.TITANACTIONS_DEFAULT_AS_OMIT, false);
	}

	private void outline(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_SORTED, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_CATEGORISED, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_GROUPED, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_HIDE_FUNCTIONS, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_HIDE_TEMPLATES, false);
		preferenceStore.setDefault(PreferenceConstants.OUTLINE_HIDE_TYPES, false);
	}

	private void namingConventionPreferences(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTIONPROBLEMS, GeneralConstants.IGNORE);
		//module names
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_TTCN3MODULE, ".*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_ASN1MODULE, ".*");
		//global TTCN-3 definitions
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_ALTSTEP, "as_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_CONSTANT, "cg_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALCONSTANT, "ec_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_FUNCTION, "f_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALFUNCTION, "ef_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_MODULEPAR, "tsp.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_PORT, ".*_PT");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TEMPLATE, "t.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_TESTCASE, "tc_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TIMER, "T.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_TYPE, ".*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_GROUP, "[A-Z].*");
		//local TTCN-3 definitions:
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_CONSTANT, "cl.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARIABLE, "vl.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TEMPLATE, "t.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARTEMPLATE, "vt.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TIMER, "TL_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_FORMAL_PARAMETER, "pl_.*");
		//component internal TTCN-3 definitions
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_CONSTANT, "c_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_VARIABLE, "v_.*");
		preferenceStore.setDefault(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_TIMER, "T_.*");
		//other:
		preferenceStore.setDefault(PreferenceConstants.REPORT_MODULENAME_IN_DEFINITION, GeneralConstants.IGNORE);
		preferenceStore.setDefault(PreferenceConstants.REPORT_VISIBILITY_IN_DEFINITION, GeneralConstants.IGNORE);

	}

	private void color(final IPreferenceStore preferenceStore) {
		// color theme dependent
		Map<String, Object> colorMap = getColorMap();
		for (Map.Entry<String, Object> entry : colorMap.entrySet()) {
			if (entry.getValue() instanceof RGB)
				preferenceStore.setDefault(entry.getKey(), StringConverter.asString((RGB)entry.getValue()));
			else if (entry.getValue() instanceof Boolean)
				preferenceStore.setDefault(entry.getKey(), (Boolean) entry.getValue());
		}
	}

	private void debug(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.DISPLAYDEBUGINFORMATION, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_PREFERENCE_PAGE_ENABLED, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_CONSOLE_TIMESTAMP, true);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_CONSOLE_AST_ELEM, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_CONSOLE_PARSE_TREE, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_CONSOLE_LOG_TO_SYSOUT, false);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_LOAD_TOKENS_TO_PROCESS_IN_A_ROW, 100);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_LOAD_SLEEP_BETWEEN_FILES, 10);
		preferenceStore.setDefault(PreferenceConstants.DEBUG_LOAD_YIELD_BETWEEN_CHECKS, true);
	}

	private void findDefinition(final IPreferenceStore preferenceStore) {
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_WS, true);
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_FUNCT, true);
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_GLOBAL, true);
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_MODULES, true);
		preferenceStore.setDefault(PreferenceConstants.FIND_DEF_TYPES, true);
	}

	public IPreferenceStore getPreference() {
		return Activator.getDefault().getPreferenceStore();
	}
	
	private Map<String, Object> getColorMap() {
		Preferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.e4.ui.css.swt.theme");
		String theme = preferences.get("themeid", "default");
		
		if (theme.toLowerCase().contains("dark"))
			return PreferenceConstantValues.DarkColorMap;
		else
			return PreferenceConstantValues.DefaultColorMap;
	}
}
