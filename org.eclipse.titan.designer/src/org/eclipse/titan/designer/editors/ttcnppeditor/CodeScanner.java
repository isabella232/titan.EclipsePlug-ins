/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcnppeditor;

import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.titan.designer.editors.ColorManager;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * @author Kristof Szabados
 * */
public final class CodeScanner extends RuleBasedScanner {

	public CodeScanner(final ColorManager colorManager) {
		List<IRule> rules = org.eclipse.titan.designer.editors.ttcn3editor.CodeScanner.getTTCNRules(colorManager);
		// multi-line preprocessor directives:
		IToken preprocessor = colorManager.createTokenFromPreference(PreferenceConstants.COLOR_PREPROCESSOR);
		rules.add(new EndOfLineRule("#", preprocessor, '\\', true));
		setRules(rules.toArray(new IRule[rules.size()]));
	}
}
