package org.eclipse.titan.regressiontests.designer.statictests.cfgFile.define.structured;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;

public class AST_syntax_warning_tests {
	//structured_ttcn

	@org.junit.Test
	public void structured_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(structured_ttcn_initializer(), "cfgFile/define/structured/structured.ttcn");
	}

	 private ArrayList<MarkerToCheck> structured_ttcn_initializer() {
		//structured.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 17;
		markersToCheck.add(new MarkerToCheck("Group style definition of module parameters is deprecated and may be fully removed in a future edition of the TTCN-3 standard ",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}
}
