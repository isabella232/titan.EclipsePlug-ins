/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests.templates;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;
import org.junit.Test;
//import org.junit.Ignore;

//@Ignore("The tested feature is not ready yet")
public class TemplateRestrictionTest {
	//templateRestrictionTest_ttcn
	@Test
	public void templateRestrictionTest_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile(templateRestrictionTest_ttcn_initializer_errors(), "src/Basic_tests/templates/templateRestrictionTest.ttcn");
		Designer_plugin_tests.checkSemanticMarkersOnFile(templateRestrictionTest_ttcn_initializer_warnings(), "src/Basic_tests/templates/templateRestrictionTest.ttcn");
	}

	private ArrayList<MarkerToCheck> templateRestrictionTest_ttcn_initializer_errors() {
		//templateRestrictionTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(13);
		int lineNum = 29;
		int i = 0;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("Formal parameter with template restriction `present' not allowed here", lineNum, IMarker.SEVERITY_ERROR));
		}
		markersToCheck.add(new MarkerToCheck("Restriction 'value' or 'omit' on template does not allow usage of `any or omit'",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Restriction 'value' or 'omit' on template does not allow usage of `any or omit'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 16;
		markersToCheck.add(new MarkerToCheck("Restriction 'value' or 'omit' on function does not allow usage of `value range match'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 6;
		for (i = 0; i < 6; i++) {
			markersToCheck.add(new MarkerToCheck("Restriction 'value' or 'omit' on template parameter does not allow usage of `any or omit'", lineNum++, IMarker.SEVERITY_ERROR));
		}
		markersToCheck.add(new MarkerToCheck("Restriction 'value' or 'omit' on template parameter does not allow usage of `any value'",  --lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Restriction 'present' on template variable does not allow usage of `ifpresent'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}


	private ArrayList<MarkerToCheck> templateRestrictionTest_ttcn_initializer_warnings() {
		//templateRestrictionTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 62;
		markersToCheck.add(new MarkerToCheck("Inadequate restriction on the referenced template variable `vt_i', this may cause a dynamic test case error at runtime",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}
}
