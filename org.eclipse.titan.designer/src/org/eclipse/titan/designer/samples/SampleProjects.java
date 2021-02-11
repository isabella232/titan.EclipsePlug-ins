/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.samples;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Knapp
 * */
public final class SampleProjects {
	private static final Map<String, SampleProject> AVAILABLE_PROJECTS = new HashMap<String, SampleProject>();
	static {
		final SampleProject emptyProject = new EmptyProjectSample();
		final HelloWorldSample helloWorld = new HelloWorldSample();

		AVAILABLE_PROJECTS.put(emptyProject.getName(), emptyProject);
		AVAILABLE_PROJECTS.put(helloWorld.getName(), helloWorld);
	}

	private static final Map<String, SampleProject> AVAILABLE_JAVA_PROJECTS = new HashMap<String, SampleProject>();
	static {
		final SampleProject emptyProject = new EmptyProjectSample();
		final HelloWorldJavaSample helloWorld = new HelloWorldJavaSample();

		AVAILABLE_JAVA_PROJECTS.put(emptyProject.getName(), emptyProject);
		AVAILABLE_JAVA_PROJECTS.put(helloWorld.getName(), helloWorld);
	}

	/**
	 * Returns the list of Titan Java sample projects
	 * @return The list of Titan Java sample projects
	 */
	public static Map<String, SampleProject> getJavaProjects() {
		return AVAILABLE_JAVA_PROJECTS;
	}

	/**
	 * Returns the list of Titan C++ sample projects
	 * @return The list of Titan C++ sample projects
	 */
	public static Map<String, SampleProject> getProjects() {
		return AVAILABLE_PROJECTS;
	}

	/** private constructor to disable instantiation */
	private SampleProjects() {
		// Do nothing
	}
}
