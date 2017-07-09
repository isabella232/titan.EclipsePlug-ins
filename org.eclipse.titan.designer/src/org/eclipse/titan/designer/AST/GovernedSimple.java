/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * A governed thing that will be mapped to a C++ entity.
 * (e.g. Value, Template)
 *
 * @author Kristof Szabados
 */
public abstract class GovernedSimple extends Governed implements IGovernedSimple {

	/**
	 * A prefix that shall be inserted before the genname when initializing
	 * the Java object. Without this prefix the genname points to a read-only
	 * Java object reference. For example, `const_c1' is a writable object with
	 * limited access (file static), but `c1' is a global const reference
	 * pointing to it.
	 * Possible values: "const_", "modulepar_", "template_"
	 *
	 * TODO: check might not be needed in the Java code generator
	 * */
	private String genNamePrefix;

	public void setGenNamePrefix(final String prefix) {
		genNamePrefix = prefix;
	}

	/***
	 * Returns the Java expression that refers to the object, which has to be
	 * initialized.
	 * */
	public String get_lhs_name() {
		String returnValue = new String();
		if (genNamePrefix != null) {
			returnValue = returnValue + genNamePrefix;
		}
		returnValue = returnValue + getGenNameOwn();

		return returnValue;
	}
}
