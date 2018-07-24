/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ComponentSectionHandler {
	private List<Component> components = new ArrayList<Component>();

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(final List<Component> components) {
		this.components = components;
	}

	public static class Component {
		private String componentName = null;
		private String hostName = null;

		public String getComponentName() {
			return componentName;
		}

		public void setComponentName(final String componentName) {
			this.componentName = componentName;
		}

		public String getHostName() {
			return hostName;
		}

		public void setHostName(final String hostName) {
			this.hostName = hostName;
		}
	}
}
