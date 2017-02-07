package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;
import org.junit.Test;

public class SelectUnionTest {

	
	private static final String DIR_PATH = "src/Basic_tests/";
	
	//=== Positive tests ===
	@Test
	public void SelectUnion_Test() throws Exception {
		checkZeroMarkersOnFile("SelectUnionTest.ttcn");
	}

	private static void checkZeroMarkersOnFile(final String fileName) {
		final String filePath = DIR_PATH + fileName;
		
		Designer_plugin_tests.checkRealZeroSemanticMarkersOnFile(filePath);
	}
	
	//=== Negative tests ===
	@Test
	public void SelectUnionNegativeTest_ttcn() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile( SelectUnionNegativeTest_ttcn_initializer(), "src/Basic_tests/SelectUnionNegativeTest.ttcn");
	}
	
	private ArrayList<MarkerToCheck> SelectUnionNegativeTest_ttcn_initializer() {
		//SelectUnionNegativeTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(7);
		int lineNum = 21;
		markersToCheck.add(new MarkerToCheck("The type of the expression must be union or anytype",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 26;
		markersToCheck.add(new MarkerToCheck("Union `@SelectUnionNegativeTest.Message' has no field `msgBAD'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("integer value was expected",  ++lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 24;
		markersToCheck.add(new MarkerToCheck("Anytype `@SelectUnionNegativeTest.anytype' has no field `octetstring'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 127;
		markersToCheck.add(new MarkerToCheck("Anytype `@SelectUnionNegativeTest.anytype' has no field `octetstring'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Anytype `@SelectUnionNegativeTest.anytype' has no field `universal charstring'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Anytype `@SelectUnionNegativeTest.anytype' has no field `NotExistingType'",  ++lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}
	
	@Test
	public void SelectUnionNegativeTest_ttcn_w() throws Exception {
		Designer_plugin_tests.checkSemanticMarkersOnFile( SelectUnionNegativeTest_ttcn_initializer_w(), "src/Basic_tests/SelectUnionNegativeTest.ttcn");
	}
	
	private ArrayList<MarkerToCheck> SelectUnionNegativeTest_ttcn_initializer_w() {
		//SelectUnionNegativeTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(9);
		int lineNum = 43;
		markersToCheck.add(new MarkerToCheck("Cases not covered for the following fields: msg2",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 43;
		markersToCheck.add(new MarkerToCheck("Cases not covered for the following fields: msg2",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 4;
		markersToCheck.add(new MarkerToCheck("Case `msg1' is already covered",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 23;
		markersToCheck.add(new MarkerToCheck("Case `msg1' is already covered",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 22;
		markersToCheck.add(new MarkerToCheck("Case `msg2' is already covered",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 21;
		markersToCheck.add(new MarkerToCheck("Case `integer' is already covered",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 19;
		markersToCheck.add(new MarkerToCheck("Case `integer' is already covered",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 2;
		markersToCheck.add(new MarkerToCheck("Case `charstring' is already covered",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 20;
		markersToCheck.add(new MarkerToCheck("Case `charstring' is already covered",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

}