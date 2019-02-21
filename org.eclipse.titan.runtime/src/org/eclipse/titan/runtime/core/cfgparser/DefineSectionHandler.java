/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.Token;
import org.eclipse.titan.runtime.core.TtcnError;

/**
 * Stores temporary config editor data of the define section
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class DefineSectionHandler {

	private Map<String, String> definitions = new HashMap<String, String>();

	public Map<String, String> getDefinitions() {
		return definitions;
	}

	public void addDefinition(final String name, final String value) {
		definitions.put(name, value);
	}

	/**
	 * Gets the value of a macro or an environment variable
	 * @param aDefinition macro or environment variable
	 * @return macro or environment variable value, or null if there is no such definition
	 */
	private String getDefinitionValue(String aDefinition){
		if ( definitions != null && definitions.containsKey( aDefinition ) ) {
			return definitions.get( aDefinition );
		} else {
			return null;
		}
	}

	// pattern for matching macro string, for example: \$a
	private final static String PATTERN_STRING_MACRO = "\\$\\s*([A-Za-z][A-Za-z0-9_]*)\\s*";
	private final static Pattern PATTERN_MACRO = Pattern.compile(PATTERN_STRING_MACRO);

	// pattern for matching macro string with brace, for example: \${a}
	private final static String PATTERN_STRING_MACRO_BRACE = "\\$\\s*\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*\\}";
	private final static Pattern PATTERN_MACRO_BRACE = Pattern.compile(PATTERN_STRING_MACRO_BRACE);

	// pattern for matching typed macro string, for example: \${a, float}
	private final static String PATTERN_STRING_TYPED_MACRO = "\\$\\s*\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*,\\s*[A-Za-z][A-Za-z0-9_]*\\s*\\}";
	private final static Pattern PATTERN_TYPED_MACRO = Pattern.compile(PATTERN_STRING_TYPED_MACRO);

	// pattern for matching macro string in quotes, for example: "\$a"
	private final static Pattern PATTERN_MACRO_QUOTED = Pattern.compile("^\"(" + PATTERN_STRING_MACRO + ")\"$");

	// pattern for matching macro string with brace in quotes, for example: "\${a}"
	private final static Pattern PATTERN_MACRO_BRACE_QUOTED = Pattern.compile("^\"(" + PATTERN_STRING_MACRO_BRACE + ")\"$");

	// pattern for matching typed macro string in quotes, for example: "\${a, float}"
	private final static Pattern PATTERN_TYPED_MACRO_QUOTED = Pattern.compile("^\"(" + PATTERN_STRING_TYPED_MACRO + ")\"$");

	// pattern for matching macro string in escaped quotes, for example: \"\$a\"
	private final static Pattern PATTERN_MACRO_ESCAPED_QUOTED = Pattern.compile("^\\\"(" + PATTERN_STRING_MACRO + ")\\\"$");

	// pattern for matching macro string with brace in escaped quotes, for example: \"\${a}\"
	private final static Pattern PATTERN_MACRO_BRACE_ESCAPED_QUOTED = Pattern.compile("^\\\"(" + PATTERN_STRING_MACRO_BRACE + ")\\\"$");

	// pattern for matching typed macro string in escaped quotes, for example: \"\${a, float}\"
	private final static Pattern PATTERN_TYPED_MACRO_ESCAPED_QUOTED = Pattern.compile("^\\\"(" + PATTERN_STRING_TYPED_MACRO + ")\\\"$");

	/**
	 * Extracts macro name from macro string
	 * @param macroString macro string, for example: \$a, \${a}
	 * @return extracted macro name without extra characters, for example: a
	 */
	private static String getMacroName( final String macroString ) {
		Matcher m = PATTERN_MACRO.matcher( macroString );
		if ( m.find() ) {
			return m.group(1);
		}

		m = PATTERN_MACRO_BRACE.matcher( macroString );
		if ( m.find() ) {
			return m.group(1);
		}

		return null;
	}

	/**
	 * @param macroString macro string in quotes, for example: "\$a", "\${a}", "\${a, float}",  \"\$a\", \"\${a}\", \"\${a, float}\"
	 * @return macro without quotes, or null if no match
	 */
	public static String removeMacroQuotes( final String macroString ) {
		Matcher m = PATTERN_MACRO_QUOTED.matcher( macroString );
		if ( m.find() ) {
			return m.group(1);
		}

		m = PATTERN_MACRO_BRACE_QUOTED.matcher( macroString );
		if ( m.find() ) {
			return m.group(1);
		}

		m = PATTERN_TYPED_MACRO_QUOTED.matcher( macroString );
		if ( m.find() ) {
			return m.group(1);
		}

		m = PATTERN_MACRO_ESCAPED_QUOTED.matcher( macroString );
		if ( m.find() ) {
			return m.group(1);
		}

		m = PATTERN_MACRO_BRACE_ESCAPED_QUOTED.matcher( macroString );
		if ( m.find() ) {
			return m.group(1);
		}

		m = PATTERN_TYPED_MACRO_ESCAPED_QUOTED.matcher( macroString );
		if ( m.find() ) {
			return m.group(1);
		}

		return null;
	}

	/**
	 * @param s string in quotes
	 * @return string without quotes, or null if no match
	 */
	public static String removeQuotes( final String s ) {
		if ( s == null || s.length() < 2 ) {
			return null;
		}

		// last index
		final int last = s.length() - 1;
		if ( '"' == s.charAt( 0 ) && '"' == s.charAt( last ) ) {
			return s.substring( 1, last );
		}

		return null;
	}

	/**
	 * Extracts macro name from typed macro string
	 * @param aMacroString macro string, for example: \${a, float}
	 * @return extracted macro name without extra characters, for example: a
	 */
	private static String getTypedMacroName( final String aMacroString ) {
		final Matcher m = PATTERN_TYPED_MACRO.matcher( aMacroString );
		if ( m.find() ) {
			return m.group(1);
		} else {
			return null;
		}
	}

	/**
	 * Gets the macro value string of a macro (without type)
	 * @param macroToken the macro token text
	 * @return the macro value string
	 *         or "" if macro is invalid. In this case an error marker is also created
	 */
	public String getMacroValue( final Token macroToken ) {
		final String definition = getMacroName( macroToken.getText() );
		final String value = getDefinitionValue( definition );
		if ( value == null ) {
			throw new TtcnError("Macro definition not found: "+definition);
		}
		return value;
	}

	/**
	 * Gets the macro value string of a macro (with type)
	 * @param macroToken the macro token
	 * @return the macro value string
	 *         or "" if macro is invalid. In this case an error marker is also created
	 */
	public String getTypedMacroValue( final Token macroToken ) {
		final String definition = getTypedMacroName( macroToken.getText() );
		final String value = getDefinitionValue( definition );
		if ( value == null ) {
			throw new TtcnError("Macro definition not found: "+definition);
		}
		return value;
	}

}
