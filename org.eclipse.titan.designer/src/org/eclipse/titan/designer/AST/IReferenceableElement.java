/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.designer.declarationsearch.Declaration;

/**
 * @author Szabolcs Beres
 * */
public interface IReferenceableElement {

	/**
	 * Resolves the given reference with this type.
	 *
	 * @param reference The reference to resolve.
	 * @param subRefIdx The index of the sub-reference which belongs to this type.
	 * @param lastSubreference The last sub-reference to resolve.
	 * @return The resolved declaration or <code>null</code>, if the reference can not be resolved by this type.
	 */
	Declaration resolveReference(final Reference reference, final int subRefIdx, final ISubReference lastSubreference);

}
