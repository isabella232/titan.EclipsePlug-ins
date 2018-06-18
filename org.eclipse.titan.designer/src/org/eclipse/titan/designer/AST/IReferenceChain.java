/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Kristof Szabados
 * */
public interface IReferenceChain {

	String CIRCULARREFERENCE = "Circular reference chain: `{0}''";

	/**
	 * Releases the actual referenceChain object.
	 * Any further access is unsafe.
	 * */
	void release();

	/**
	 * Checks if the rference chain contains the parameter without reporting an error.
	 *
	 * @param chainLink the link to add
	 * @return true if this link was already present in the chain, false otherwise
	 * */
	boolean contains(final IReferenceChainElement chainLink);

	/**
	 * Adds an element to the end of the chain.
	 *
	 * @param chainLink the link to add
	 * @return false if this link was already present in the chain, true otherwise
	 * */
	boolean add(final IReferenceChainElement chainLink);

	/**
	 * Marks the actual state of the reference chain, so that later the chain can be returned into this state.
	 * */
	void markState();

	/**
	 * Returns the chain of references into its last saved state and deletes the last state mark.
	 * */
	void previousState();
}
