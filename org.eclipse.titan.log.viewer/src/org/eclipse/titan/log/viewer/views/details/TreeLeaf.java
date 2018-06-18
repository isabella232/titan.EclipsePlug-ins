/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.details;

public class TreeLeaf extends TreeObject {

	private String value;

	public TreeLeaf(final String name, final String value) {
		super(name);
		this.value = value;
	}

	public String getValue() {
		return value;
	}


	@Override
	public String toString() {
		return getName() + " := " + value;
	}

}
