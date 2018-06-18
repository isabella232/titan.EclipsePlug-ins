/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * This interface represents a type that references an other type in some way.
 *
 * @author Kristof Szabados
 * */
public interface IReferencingType extends IReferenceChainElement {
	String TYPEREFERENCEEXPECTED = "`{0}'' is not a reference to a type";
	String INVALIDREFERENCETYPE = "invalid reference type";
	//	String CIRCULARTYPEREFERENCE = "circular type reference chain: `{0}''";

	/**
	 * Find and return the type referenced.
	 * @param timestamp the timestamp of the actual build cycle
	 * @param refChain the reference chain used to detect circular references.
	 *
	 * @return the referenced type, or the actual type in case of an error
	 * */
	IType getTypeRefd(final CompilationTimeStamp timestamp, IReferenceChain refChain);
}
