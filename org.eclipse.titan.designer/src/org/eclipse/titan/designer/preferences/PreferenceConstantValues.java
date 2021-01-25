/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.preferences;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.graphics.RGB;

/**
 * Possible values of internal preference settings.
 * 
 * @author Kristof Szabados
 * @author Miklos Magyari
 * */
public final class PreferenceConstantValues {

	// syntax coloring
	public static final RGB GREY20 = new RGB(51, 51, 51);
	public static final RGB LIGHTGREY = new RGB(230, 230, 230);
	public static final RGB WHITE = new RGB(255, 255, 255);
	public static final RGB BLACK = new RGB(0, 0, 0);
	public static final RGB BROWN = new RGB(165, 42, 42);
	public static final RGB SADDLE_BROWN = new RGB(139, 69, 69);
	public static final RGB DARKGREEN = new RGB(0, 100, 0);
	public static final RGB LIGHTGREEN = new RGB(118, 242, 139);
	public static final RGB SEAGREEN = new RGB(46, 139, 87);
	public static final RGB ROYALBLUE4 = new RGB(39, 64, 139);
	public static final RGB LIGHTBLUE = new RGB(151, 208, 236);
	public static final RGB BLUE = new RGB(0, 0, 255);
	public static final RGB CHOCOLATE = new RGB(210, 105, 30);
	public static final RGB STEELBLUE = new RGB(70, 130, 180);
	public static final RGB RED = new RGB(255, 0, 0);
	public static final RGB STEELBLUE4 = new RGB(54, 100, 139);
	public static final RGB VIOLETRED4 = new RGB(139, 34, 82);
	public static final RGB LIGHTRED = new RGB(255, 82, 82);
	public static final RGB GREY30 = new RGB(77, 77, 77);
	public static final RGB PLUM = new RGB(221, 160, 221);
	public static final RGB YELLOW = new RGB(225, 225, 127);
	public static final RGB LIGHTORANGE = new RGB(225, 225, 127);
	
	@SuppressWarnings("serial")
	public static final Map<String, Object> DefaultColorMap = new HashMap<String, Object>() {{
		// general settings
		put(PreferenceConstants.COLOR_NORMAL_TEXT + PreferenceConstants.FOREGROUND, VIOLETRED4);
		put(PreferenceConstants.COLOR_NORMAL_TEXT + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_NORMAL_TEXT + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_STRINGS + PreferenceConstants.FOREGROUND, DARKGREEN);
		put(PreferenceConstants.COLOR_STRINGS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_STRINGS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_COMMENTS + PreferenceConstants.FOREGROUND, GREY20);
		put(PreferenceConstants.COLOR_COMMENTS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_COMMENTS + PreferenceConstants.BACKGROUND, WHITE);
		
		// asn1 specific
		put(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.FOREGROUND, WHITE);
		put(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_CMIP_VERB + PreferenceConstants.FOREGROUND, VIOLETRED4);
		put(PreferenceConstants.COLOR_CMIP_VERB + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_CMIP_VERB + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_COMPARE_TYPE + PreferenceConstants.FOREGROUND, ROYALBLUE4);
		put(PreferenceConstants.COLOR_COMPARE_TYPE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_COMPARE_TYPE + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_STATUS + PreferenceConstants.FOREGROUND, SADDLE_BROWN);
		put(PreferenceConstants.COLOR_STATUS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_STATUS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TAG + PreferenceConstants.FOREGROUND, DARKGREEN);
		put(PreferenceConstants.COLOR_TAG + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TAG + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_STORAGE + PreferenceConstants.FOREGROUND, SADDLE_BROWN);
		put(PreferenceConstants.COLOR_STORAGE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_STORAGE + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_MODIFIER + PreferenceConstants.FOREGROUND, CHOCOLATE);
		put(PreferenceConstants.COLOR_MODIFIER + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_MODIFIER + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_ACCESS_TYPE + PreferenceConstants.FOREGROUND, ROYALBLUE4);
		put(PreferenceConstants.COLOR_ACCESS_TYPE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_ACCESS_TYPE + PreferenceConstants.BACKGROUND, WHITE);
		
		// config specific
		put(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.FOREGROUND, BLACK);
		put(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.FOREGROUND, SEAGREEN);
		put(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.FOREGROUND, SEAGREEN);
		put(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES + PreferenceConstants.FOREGROUND, SADDLE_BROWN);
		put(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES + PreferenceConstants.BACKGROUND, WHITE);
		
		// ttcn3 specific
		put(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.FOREGROUND, BLACK);
		put(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_PREPROCESSOR + PreferenceConstants.FOREGROUND, ROYALBLUE4);
		put(PreferenceConstants.COLOR_PREPROCESSOR + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_PREPROCESSOR + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.FOREGROUND, BLACK);
		put(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_TEMPLATE_MATCH + PreferenceConstants.FOREGROUND, ROYALBLUE4);
		put(PreferenceConstants.COLOR_TEMPLATE_MATCH + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TEMPLATE_MATCH + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_VERDICT_OP + PreferenceConstants.FOREGROUND, BLUE);
		put(PreferenceConstants.COLOR_VERDICT_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_VERDICT_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_SUT_OP + PreferenceConstants.FOREGROUND, BLUE);
		put(PreferenceConstants.COLOR_SUT_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_SUT_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_FUNCTION_OP + PreferenceConstants.FOREGROUND, BLUE);
		put(PreferenceConstants.COLOR_FUNCTION_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_FUNCTION_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TYPE + PreferenceConstants.FOREGROUND, BROWN);
		put(PreferenceConstants.COLOR_TYPE + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TYPE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TYPE + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_TIMER_OP + PreferenceConstants.FOREGROUND, BLUE);
		put(PreferenceConstants.COLOR_TIMER_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TIMER_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_PORT_OP + PreferenceConstants.FOREGROUND, BLUE);
		put(PreferenceConstants.COLOR_PORT_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_PORT_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_CONFIG_OP + PreferenceConstants.FOREGROUND, BLUE);
		put(PreferenceConstants.COLOR_CONFIG_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_CONFIG_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_PREDEFINED_OP + PreferenceConstants.FOREGROUND, BLACK);
		put(PreferenceConstants.COLOR_PREDEFINED_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_PREDEFINED_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_BOOLEAN_CONST + PreferenceConstants.FOREGROUND, DARKGREEN);
		put(PreferenceConstants.COLOR_BOOLEAN_CONST + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_BOOLEAN_CONST + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST + PreferenceConstants.FOREGROUND, DARKGREEN);
		put(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_OTHER_CONST + PreferenceConstants.FOREGROUND, DARKGREEN);
		put(PreferenceConstants.COLOR_OTHER_CONST + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_OTHER_CONST + PreferenceConstants.USEBACKGROUNDCOLOR, false);
	}};
	
	@SuppressWarnings("serial")
	public static final Map<String, Object> DarkColorMap = new HashMap<String, Object>() {{
		// general settings
		put(PreferenceConstants.COLOR_NORMAL_TEXT + PreferenceConstants.FOREGROUND, LIGHTRED);
		put(PreferenceConstants.COLOR_NORMAL_TEXT + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_NORMAL_TEXT + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_STRINGS + PreferenceConstants.FOREGROUND, LIGHTGREEN);
		put(PreferenceConstants.COLOR_STRINGS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_STRINGS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_COMMENTS + PreferenceConstants.FOREGROUND, LIGHTGREY);
		put(PreferenceConstants.COLOR_COMMENTS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_COMMENTS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_COMPARE_TYPE + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_STORAGE + PreferenceConstants.FOREGROUND, LIGHTORANGE);

		// asn1 specific
		put(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.FOREGROUND, WHITE);
		put(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_ASN1_KEYWORDS + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_CMIP_VERB + PreferenceConstants.FOREGROUND, LIGHTRED);
		put(PreferenceConstants.COLOR_CMIP_VERB + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_CMIP_VERB + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_COMPARE_TYPE + PreferenceConstants.FOREGROUND, LIGHTBLUE);
		put(PreferenceConstants.COLOR_COMPARE_TYPE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_STATUS + PreferenceConstants.FOREGROUND, LIGHTORANGE);
		put(PreferenceConstants.COLOR_STATUS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_STATUS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TAG + PreferenceConstants.FOREGROUND, LIGHTGREEN);
		put(PreferenceConstants.COLOR_TAG + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TAG + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_STORAGE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_STORAGE + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_MODIFIER + PreferenceConstants.FOREGROUND, YELLOW);
		put(PreferenceConstants.COLOR_MODIFIER + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_MODIFIER + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_ACCESS_TYPE + PreferenceConstants.FOREGROUND, LIGHTBLUE);
		put(PreferenceConstants.COLOR_ACCESS_TYPE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_ACCESS_TYPE + PreferenceConstants.BACKGROUND, WHITE);

		// config specific
		put(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.FOREGROUND, WHITE);
		put(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_CONFIG_KEYWORDS + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.FOREGROUND, LIGHTGREEN);
		put(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_SECTION_TITLE + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.FOREGROUND, LIGHTGREEN);
		put(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_FILE_AND_CONTROL_MASK_OPTIONS + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES + PreferenceConstants.FOREGROUND, LIGHTORANGE);
		put(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_EXTERNAL_COMMAND_TYPES + PreferenceConstants.BACKGROUND, WHITE);
		
		// ttcn3 specific
		put(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.FOREGROUND, WHITE);
		put(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TTCN3_KEYWORDS + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_PREPROCESSOR + PreferenceConstants.FOREGROUND, LIGHTBLUE);
		put(PreferenceConstants.COLOR_PREPROCESSOR + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_PREPROCESSOR + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.FOREGROUND, WHITE);
		put(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_VISIBILITY_OP + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_TEMPLATE_MATCH + PreferenceConstants.FOREGROUND, YELLOW);
		put(PreferenceConstants.COLOR_TEMPLATE_MATCH + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TEMPLATE_MATCH + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_VERDICT_OP + PreferenceConstants.FOREGROUND, YELLOW);
		put(PreferenceConstants.COLOR_VERDICT_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_VERDICT_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_SUT_OP + PreferenceConstants.FOREGROUND, YELLOW);
		put(PreferenceConstants.COLOR_SUT_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_SUT_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_FUNCTION_OP + PreferenceConstants.FOREGROUND, LIGHTORANGE);
		put(PreferenceConstants.COLOR_FUNCTION_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_FUNCTION_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TYPE + PreferenceConstants.FOREGROUND, YELLOW);
		put(PreferenceConstants.COLOR_TYPE + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TYPE + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TYPE + PreferenceConstants.BOLD, true);
		put(PreferenceConstants.COLOR_TIMER_OP + PreferenceConstants.FOREGROUND, YELLOW);
		put(PreferenceConstants.COLOR_TIMER_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TIMER_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_PORT_OP + PreferenceConstants.FOREGROUND, LIGHTORANGE);
		put(PreferenceConstants.COLOR_PORT_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_PORT_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_CONFIG_OP + PreferenceConstants.FOREGROUND, LIGHTORANGE);
		put(PreferenceConstants.COLOR_CONFIG_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_CONFIG_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_PREDEFINED_OP + PreferenceConstants.FOREGROUND, WHITE);
		put(PreferenceConstants.COLOR_PREDEFINED_OP + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_PREDEFINED_OP + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_BOOLEAN_CONST + PreferenceConstants.FOREGROUND, LIGHTGREEN);
		put(PreferenceConstants.COLOR_BOOLEAN_CONST + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_BOOLEAN_CONST + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST + PreferenceConstants.FOREGROUND, LIGHTGREEN);
		put(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_TTCN3_VERDICT_CONST + PreferenceConstants.USEBACKGROUNDCOLOR, false);
		put(PreferenceConstants.COLOR_OTHER_CONST + PreferenceConstants.FOREGROUND, LIGHTGREEN);
		put(PreferenceConstants.COLOR_OTHER_CONST + PreferenceConstants.BACKGROUND, WHITE);
		put(PreferenceConstants.COLOR_OTHER_CONST + PreferenceConstants.USEBACKGROUNDCOLOR, false);
	}};
	
	// options for the compiler on how compiler and designer markers interact
	public static final String COMPILEROPTIONSTAY = "Stay unchanged";
	public static final String COMPILEROPTIONOUTDATE = "Become outdated";
	public static final String COMPILEROPTIONREMOVE = "Are removed";

	// options for selecting the broken parts to analyze
	public static final String MODULESELECTIONORIGINAL = "Original";
	public static final String BROKENPARTSVIAREFERENCES = "Broken parts via references";

	// options for the designer on how compiler and designer markers interact
	public static final String ONTHEFLYOPTIONSTAY = "Stay";
	public static final String ONTHEFLYOPTIONREMOVE = "Are removed";

	// The amount of processing cores in the hardware
	public static final int AVAILABLEPROCESSORS = Runtime.getRuntime().availableProcessors();

	// How should the content assist order the elements in its list.
	public static final String SORT_ALPHABETICALLY = "alphabetically";
	public static final String SORT_BY_RELEVANCE = "by relevance";

	// indentation policies on how to handles tabulators
	public static final String TAB_POLICY_1 = "Tab";
	public static final String TAB_POLICY_2 = "Spaces";

	//What to do on the console before build (consoleActionBeforeBuild):
	public static final String BEFORE_BUILD_NOTHING_TO_DO = "Nothing";
	public static final String BEFORE_BUILD_CLEAR_CONSOLE = "Clear";
	public static final String BEFORE_BUILD_PRINT_CONSOLE_DELIMITERS = "Print delimiter";


	/** private constructor to disable instantiation */
	private PreferenceConstantValues() {
		//Do nothing
	}
}
