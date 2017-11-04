/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.titanium.markers.spotters.BaseProjectCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
*
* @author Farkas Izabella Ingrid
*/
public class UnusedGlobalDefinitionProject extends BaseProjectCodeSmellSpotter {

	public UnusedGlobalDefinitionProject() {
		super(CodeSmellType.UNUSED_GLOBAL_DEFINITION_PROJECT);
	}

	// FIXME: implement
	@Override
	protected void process(IProject project, Problems problems) {
		TITANDebugConsole.print("Unused global definition\n");

		final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(project);
		final Set<String> knownModuleNames = projectSourceParser.getKnownModuleNames();
		final List<Module> modules = new ArrayList<Module>();
		for (final String moduleName : new TreeSet<String>(knownModuleNames)) {
			Module module = projectSourceParser.getModuleByName(moduleName); 
			modules.add(module);
			//problems.report(module.getIdentifier().getLocation(), "Possibly unused global definition");
		}
	}

}
