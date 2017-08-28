/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;

/**
 * @author Kristof Szabados
 * */
public interface IGovernedSimple extends IGoverned {

	public void setGenNamePrefix(final String prefix);

	/**
	 * @return the code section where this governed simple is generated.
	 * */
	public CodeSectionType getCodeSection();

	/**
	 * Sets the code_section attribute of this governed simple object to the provided value.
	 *
	 * @param codeSection the code section where this governed simple should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection);

	/***
	 * Returns the Java expression that refers to the object, which has to be
	 * initialized.
	 * */
	public String get_lhs_name();
}
