/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * A governed thing that will be mapped to a Java entity.
 * (e.g. Value, Template)
 *
 * @author Kristof Szabados
 */
public abstract class GovernedSimple extends Governed implements IGovernedSimple {

	public static enum CodeSectionType {
		/** Unknown (i.e. not specified) */
		CS_UNKNOWN,
		/**
		 * Initialized before processing the configuration file.
		 *
		 * Example: constants, default value for module parameters.
		 */
		CS_PRE_INIT,
		/**
		 * Initialized after processing the configuration file.
		 *
		 * Example: non-parameterized templates.
		 */
		CS_POST_INIT,
		/**
		 * Initialized with the component entities.
		 *
		 * Example: initial value for component variables, default
		 * duration for timers.
		 */
		CS_INIT_COMP,
		/**
		 * Initialized immediately at the place of definition.
		 * Applicable to local definitions only.
		 *
		 * Example: initial value for a local variable.
		 */
		CS_INLINE
	}

	/**
	 * A prefix that shall be inserted before the genname when initializing
	 * the Java object. Without this prefix the genname points to a read-only
	 * Java object reference. For example, `const_c1' is a writable object with
	 * limited access (file static), but `c1' is a global const reference
	 * pointing to it.
	 * Possible values: "const_", "modulepar_", "template_"
	 * */
	private String genNamePrefix;

	/**
	 * Indicates the section of the output code where the initializer Java
	 * sequence has to be put. If entity A refers to entity B and both has
	 * to be initialized in the same section, the initializer of B must
	 * precede the initializer of A. If the initializer of A and B has to be
	 * put into different sections the right order is provided automatically
	 * by the run-time environment.
	 * */
	private CodeSectionType codeSection = CodeSectionType.CS_UNKNOWN;

	public void setGenNamePrefix(final String prefix) {
		genNamePrefix = prefix;
	}

	@Override
	/** {@inheritDoc} */
	public CodeSectionType getCodeSection() {
		return codeSection;
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		this.codeSection = codeSection;
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
