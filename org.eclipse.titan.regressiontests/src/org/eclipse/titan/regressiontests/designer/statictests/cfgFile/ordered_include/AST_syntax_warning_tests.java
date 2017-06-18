package org.eclipse.titan.regressiontests.designer.statictests.cfgFile.ordered_include;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;

public class AST_syntax_warning_tests {
	//oi_ttcn

	@org.junit.Test
	public void oi_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(oi_ttcn_initializer(), "cfgFile/ordered_include/oi.ttcn");
	}

	 private ArrayList<MarkerToCheck> oi_ttcn_initializer() {
			//oi.ttcn
			ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
			int lineNum = 14;
			markersToCheck.add(new MarkerToCheck("Group style definition of module parameters is deprecated and may be fully removed in a future edition of the TTCN-3 standard ",  lineNum, IMarker.SEVERITY_WARNING));

			return markersToCheck;
	}
}
