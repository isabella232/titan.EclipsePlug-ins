package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;
import org.junit.Test;

public class Structured {

	@Test
	public void structured_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile( structured_ttcn_initializer(), "cfgFile/define/structured/structured.ttcn");
//		Designer_plugin_tests.checkSyntaxMarkersOnFile(structured_cfg_initializer(), "cfgFile/define/structured/structured.cfg");
		Designer_plugin_tests.checkRealZeroSyntaxMarkersOnFile("cfgFile/define/structured/structured.cfg");
	}
	
	private ArrayList<MarkerToCheck> structured_ttcn_initializer() {
		//structured.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(35);
		int lineNum = 107;
		markersToCheck.add(new MarkerToCheck("The constant `@structured.CLASSMARK2_GSM_ONLY' with name CLASSMARK2_GSM_ONLY breaks the naming convention  `cg_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 26;
		markersToCheck.add(new MarkerToCheck("The constant `@structured.CR_DEFAULT' with name CR_DEFAULT breaks the naming convention  `cg_.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 85;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 7;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 12;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 6;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("The template variable `vl_expected' with name vl_expected breaks the naming convention  `vt.*'",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	//structured_cfg
    //plugin repaired, no fault anymore
//	private ArrayList<MarkerToCheck> structured_cfg_initializer() {
//		//structured.cfg
//		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(0);
//		return markersToCheck;
//	}

}
