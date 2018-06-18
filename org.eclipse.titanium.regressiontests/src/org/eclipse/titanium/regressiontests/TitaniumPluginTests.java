/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.regressiontests;

import java.util.Locale;

import org.eclipse.titanium.regressiontests.titanium.markers.CodeSmellChecker;
import org.eclipse.titanium.regressiontests.titanium.metrics.MetricsChecker;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * The tests specific to the Titanium plugin.
 * */
@RunWith(Suite.class)
@SuiteClasses({
		GeneralTests.class,
		CodeSmellChecker.class,
		MetricsChecker.class
})
public class TitaniumPluginTests {

	@BeforeClass
	public static void setUp() {
		Locale.setDefault(new Locale("en", "EN")); // the number format used is the english one
	}
}
