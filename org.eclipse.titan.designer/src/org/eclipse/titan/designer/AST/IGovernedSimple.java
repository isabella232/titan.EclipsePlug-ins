/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.compiler.BuildTimestamp;

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

	/**
	 * Returns the build timestamp of the last time this governedsimple was built.
	 *  */
	public BuildTimestamp getLastTimeBuilt();

	/***
	 * Returns the Java expression that refers to the object, which has to be
	 * initialized.
	 * */
	public String get_lhs_name();

	/**
	 * Returns whether the entity is a top-level one (i.e. it is not
	 * embedded into another entity). The function examines whether the
	 * genname is a single identifier or not.
	 * */
	public boolean isTopLevel();
}
