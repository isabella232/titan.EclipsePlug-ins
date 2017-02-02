package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests;

import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.junit.Test;

public class SelectUnionTest {

	private static final String DIR_PATH = "src/Basic_tests/";

	@Test
	public void SelectUnion_Test() throws Exception {
		checkZeroMarkersOnFile("SelectUnionTest.ttcn");
	}

	private static void checkZeroMarkersOnFile(final String fileName) {
		final String filePath = DIR_PATH + fileName;
		
		Designer_plugin_tests.checkRealZeroSemanticMarkersOnFile(filePath);
	}

}