/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.sonar.metrics;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "project" )
@XmlSeeAlso(ModuleMetricsDto.class)
class ProjectMetricsDto {

	private String name;

	@XmlElementWrapper(name = "modules")
	private final List<ModuleMetricsDto> modules = new ArrayList<ModuleMetricsDto>();

	public ProjectMetricsDto(final String name) {
		this.name = name;
	}

	protected ProjectMetricsDto() {
		// for JAXB
	}


	public void addModule(final ModuleMetricsDto module) {
		this.modules.add(module);
	}

	public List<ModuleMetricsDto> getModules() {
		return modules;
	}

	public String getName() {
		return name;
	}
}

