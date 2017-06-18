package org.eclipse.titan.regressiontests.designer.statictests.cfgFile.define.macro_reference;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;

public class AST_syntax_warning_tests {
	//TSTM_macro_test_testcases_ttcn

	@org.junit.Test
	public void TSTM_macro_test_testcases_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(TSTM_macro_test_testcases_ttcn_initializer(), "cfgFile/define/macro_reference/TSTM_macro_test_testcases.ttcn");
	}

	 private ArrayList<MarkerToCheck> TSTM_macro_test_testcases_ttcn_initializer() {
			//TSTM_macro_test_testcases.ttcn
			ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
			int lineNum = 21;
			markersToCheck.add(new MarkerToCheck("Group style definition of module parameters is deprecated and may be fully removed in a future edition of the TTCN-3 standard ",  lineNum, IMarker.SEVERITY_WARNING));

			return markersToCheck;
	}
}
