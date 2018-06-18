/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests;

import org.eclipse.titan.regressiontests.designer.statictests.Basic_tests.templates.TemplateTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	AST_Syntax_warning_tests.class,
	AST_warnings_tests.class, 
	AST_tests.class,
	TemplateTestSuite.class, 
	LazyTryCatchTest.class,
	SelectUnionTest.class,
	ExpectedValueTypeTest.class,
	Structured.class,
	ConfigFileTest.class
})
public class Basic_tests {
}
