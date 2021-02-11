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
	
	public static Map<String, SampleProject> getJavaProjects() {
		return AVAILABLE_JAVA_PROJECTS;
	}

	public static Map<String, SampleProject> getProjects() {
		return AVAILABLE_PROJECTS;
	}

	/** private constructor to disable instantiation */
	private SampleProjects() {
		// Do nothing
	}
}
