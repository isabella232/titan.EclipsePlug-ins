/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.cfgFile.module_parameters;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;

public class AST_syntax_warning_tests {
	//A_asn

	@org.junit.Test
	public void A_asn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(A_asn_initializer(), "cfgFile/module_parameters/references/A.asn");
	}

	private ArrayList<MarkerToCheck> A_asn_initializer() {
		//A.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 5;
		markersToCheck.add(new MarkerToCheck("EXTENSIBILITY IMPLIED is not supported.",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}
}
