/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titanium.Activator;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titanium.preferences.PreferenceConstants;

/**
 * This class marks the following code smell:
 * 	The definition name is longer then the recommended length.
 * 	This can be categorized as a code style problem.
 * 
 * Currently the default value for function name length is set at 42.
 * 	Changeable at package org.eclipse.titanium.preferences; class PreferenceInitializer. 
 *  The getInt() initializer in this class constructor is set to 42,
 *  because in some extreme case Spotter is loaded before Preferences. 
 * 
 * @author Madan Anand
 */

public class DefinitionNameTooLong extends BaseModuleCodeSmellSpotter{
	private static final String WARNING_MESSAGE = "Definition name length : `{0}'', longer than recommended length : `{1}''";;
	private final int recommendedLenghtOfFunctionName;
	
	public DefinitionNameTooLong() {
		super(CodeSmellType.DEFINITION_NAME_TOO_LONG);
		recommendedLenghtOfFunctionName = Platform.getPreferencesService().getInt(Activator.PLUGIN_ID,
				PreferenceConstants.DEFINITION_NAME_TOO_LONG_LENGTH, 42, null);
	}
	
	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof Definition) {
			final Definition s = (Definition) node;
			int lengthOfFunctionName=s.getIdentifier().getDisplayName().length();
			if(lengthOfFunctionName>recommendedLenghtOfFunctionName){
				final String msg = MessageFormat.format(WARNING_MESSAGE,
						lengthOfFunctionName, recommendedLenghtOfFunctionName);
				problems.report(s.getLocation(), msg);
			}
		}
	}	

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Definition.class);
		return ret;
	}

		

}
