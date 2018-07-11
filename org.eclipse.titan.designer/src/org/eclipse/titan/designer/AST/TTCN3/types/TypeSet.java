/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represent a general set of types.
 *
 * @author Kristof Szabados
 * */
public final class TypeSet extends ASTNode {

	private final ArrayList<IType> types;
	private final Map<String, IType> typeMap;

	public TypeSet() {
		super();
		types = new ArrayList<IType>();
		typeMap = new HashMap<String, IType>();
	}

	/**
	 * Add a new type to the set.
	 *
	 * @param type the type to add to the set.
	 * */
	public void addType(final IType type) {
		if (type != null) {
			types.add(type);
			types.trimToSize();
			typeMap.put(type.getTypename(), type);
		}
	}

	/** @return the number of types in the set */
	public int getNofTypes() {
		return types.size();
	}

	/**
	 * Returns the type at the given index.
	 *
	 * @param index the index of the type we wish to receive.
	 *
	 * @return the type at the provided index.
	 * */
	public IType getTypeByIndex(final int index) {
		return types.get(index);
	}

	/**
	 * Checks if the provided type is present in the set.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param type the type to check for.
	 *
	 * @return true if the type is found in the set, false otherwise.
	 * */
	public boolean hasType(final CompilationTimeStamp timestamp, final IType type) {
		if (type == null) {
			return false;
		}

		final IType tempType = type.getTypeRefdLast(timestamp);
		if (tempType.getIsErroneous(timestamp)) {
			return true;
		}

		return typeMap.containsKey(tempType.getTypename());
	}

	/**
	 * Calculates the number of types that are compatible with the provided
	 * type.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle
	 * @param type the type to check against
	 *
	 * @return the number of compatible types
	 * */
	public int getNofCompatibleTypes(final CompilationTimeStamp timestamp, final IType type) {
		if (type.getTypeRefdLast(timestamp).getIsErroneous(timestamp)) {
			return 1;
		}

		int result = 0;
		for (int i = 0, size = types.size(); i < size; i++) {
			if (types.get(i).isCompatible(timestamp, type, null, null, null)) {
				result++;
			}
		}

		return result;
	}

	/**
	 * Calculates the index of the provided type in this set.
	 *
	 * @param type the type to search for.
	 *
	 * @return the index of the type, or -1 if not found.
	 * */
	public int getIndexByType(final IType type) {
		final String name = type.getTypename();
		for (int i = 0, size = types.size(); i < size; i++) {
			if (types.get(i).getTypename().equals(name)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Checks if a type with the provided name is present in the set or not.
	 *
	 * @param name the name to look for.
	 *
	 * @return true if a type with the provided name is found in the set, false
	 *         otherwise
	 * */
	public boolean hasTypeWithName(final String name) {
		return typeMap.containsKey(name);
	}

	public IType getTypeByName(final String name) {
		return typeMap.get(name);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (types != null) {
			for (final IType t : types) {
				if (!t.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
