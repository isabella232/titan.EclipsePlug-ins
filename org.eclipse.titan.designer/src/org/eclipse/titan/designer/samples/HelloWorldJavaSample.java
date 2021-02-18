/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.samples;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.titan.designer.compiler.ProjectSourceCompiler;

/**
 * @author Adam Knapp
 * */
public class HelloWorldJavaSample  extends HelloWorldSample {
	/** The contents of the files. key - filename, value - content*/
	private static final Map<String, String> OTHER_FILE_CONTENT = new HashMap<String, String>();
	private static final Map<String, String> SOURCE_FILE_CONTENT = new HashMap<String, String>();

	protected static final String TESTPORT_JAVA_PACKAGE = "package {0};\n\n";
	protected static final String TESTPORT_JAVA_IMPORT = 
			  "import {0}.MyExample.PCOType_BASE;\n"
			+ "import " + ProjectSourceCompiler.getPackageRuntimeRoot() + ".TitanCharString;\n"; 
	protected static final String TESTPORT_JAVA_BODY =
			  "\npublic class PCOType extends PCOType_BASE {\n\n"
			+ "\tpublic PCOType(String port_name) {\n"
			+ "\t\tsuper(port_name);\n"
			+ "\t}\n\n"
			+ "\t@Override\n"
			+ "\tprotected void outgoing_send(TitanCharString send_par) {\n"
			+ "\t\tSystem.out.println(send_par);\n"
			+ "\t\tincoming_message(new TitanCharString(\"Hello, TTCN-3!\"));\n"
			+ "\t}\n"
			+ "}";

	static {
		SOURCE_FILE_CONTENT.put("MyExample.ttcn", MYEXAMPLE_TTCN);
		SOURCE_FILE_CONTENT.put("MyExample.cfg", MYEXAMPLE_CFG);
	}

	@Override
	protected void preconfigure(IProject project) {
		if (project == null) {
			return;
		}
		final String userProvidedRoot = ProjectSourceCompiler.getUserProvidedRoot(project);
		StringBuilder testPort = new StringBuilder();
		generateTestPort(project, testPort);
		OTHER_FILE_CONTENT.clear();
		OTHER_FILE_CONTENT.put(userProvidedRoot + "/PCOType.java", testPort.toString());
	}

	@Override
	protected void configure(final IProject project) {
		// Nothing to do yet
	}

	@Override
	public Map<String, String> getOtherFileContent() {
		return OTHER_FILE_CONTENT;
	}

	@Override
	public Map<String, String> getSourceFileContent() {
		return SOURCE_FILE_CONTENT;
	}

	/**
	 * Generates the user provided test port Java source for the Hello World project
	 * @param project Project in which the code is needed
	 * @param contentBuilder This will contain the generated code as string
	 */
	private void generateTestPort(IProject project, StringBuilder contentBuilder) {
		if (contentBuilder == null) {
			return;
		}
		ProjectSourceCompiler.generateCommonHeaderComments(contentBuilder);
		contentBuilder.append(MessageFormat.format(TESTPORT_JAVA_PACKAGE, ProjectSourceCompiler.getPackageUserProvidedRoot(project)))
		.append(MessageFormat.format(TESTPORT_JAVA_IMPORT, ProjectSourceCompiler.getPackageGeneratedRoot(project)))
		.append(TESTPORT_JAVA_BODY);
	}
}
