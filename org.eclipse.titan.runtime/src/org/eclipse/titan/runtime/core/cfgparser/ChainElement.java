/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

/**
 * Simple binary tree to detect circular reference.
 * An element knows only its parent, from the element's point of view only the chain to the root matters.
 * Used for [INCLUDE], [ORDERED_INCLUDE] and [DEFINE]
 *
 * @author Arpad Lovassy
 *
 * @param <T> value type of an element
 */
public class ChainElement<T> {

	/**
	 * previous element in the chain,
	 * null, if this is the first element
	 */
	private final ChainElement<T> prev;

	/**
	 * Element value, not null
	 */
	private final T value;

	public ChainElement(final ChainElement<T> prev, final T value) {
		super();
		this.prev = prev;
		this.value = value;
	}

	/**
	 * @param candidate element to check
	 * @return true, if <code>candidate</code> is already in the chain, false otherwise
	 */
	public boolean contains(final T candidate) {
		ChainElement<T> e = this;
		while ( e != null ) {
			if ( e.value.equals(candidate) ) {
				return true;
			}
			e = e.prev;
		}
		return false;
	}

	/**
	 * Gets chain value as a string in this format: file1->file2->...->filen
	 */
	public String dump() {
		final StringBuilder sb = new StringBuilder();
		dump(sb);
		return sb.toString();
	}

	private void dump(final StringBuilder sb) {
		if ( prev != null ) {
			prev.dump(sb);
			sb.append("->");
		}
		sb.append(value);
	}
}
