/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests;

import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.junit.Test;

public class LazyTryCatchTest {

	private static final String DIR_PATH = "src/Basic_tests/";

	@Test
	public void LazyTryCatch_Test() throws Exception {
		checkZeroMarkersOnFile("LazyTryCatchTest.ttcn");
	}

	private static void checkZeroMarkersOnFile(final String fileName) {
		final String filePath = DIR_PATH + fileName;
		
		Designer_plugin_tests.checkRealZeroSemanticMarkersOnFile(filePath);
	}

}
