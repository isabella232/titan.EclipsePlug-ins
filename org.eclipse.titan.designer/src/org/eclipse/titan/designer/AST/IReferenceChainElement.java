/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * This interface represents an element, that can be used, as a link of a reference chain, to detect circular references.
 *
 * @author Kristof Szabados
 * */
public interface IReferenceChainElement {

	/**
	 * @return the description of this object as a chain link.
	 * */
	String chainedDescription();

	/**
	 * @return the location of the chain link
	 * */
	Location getChainLocation();
}
