/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a list of type mappings.
 *
 * @author Kristof Szabados
 * */
public final class TypeMappings extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private final List<TypeMapping> mappings;
	private final Map<String, TypeMapping> mappingsMap;

	/** the time when this type mapping was check the last time. */
	private CompilationTimeStamp lastTimeChecked;

	private Location location;

	public TypeMappings() {
		mappings = new ArrayList<TypeMapping>();
		mappingsMap = new HashMap<String, TypeMapping>();
	}

	@Override
	/** {@inheritDoc} */
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public Location getLocation() {
		return location;
	}

	public void addMapping(final TypeMapping mapping) {
		mappings.add(mapping);
		mapping.setFullNameParent(this);
	}

	public int getNofMappings() {
		return mappings.size();
	}

	public TypeMapping getMappingByIndex(final int index) {
		return mappings.get(index);
	}

	public boolean hasMappingForType(final CompilationTimeStamp timestamp, final IType type) {
		if (type.getIsErroneous(timestamp)) {
			return true;
		}

		return mappingsMap.containsKey(type.getTypename());
	}

	public TypeMapping getMappingForType(final CompilationTimeStamp timestamp, final IType type) {
		return mappingsMap.get(type.getTypename());
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = mappings.size(); i < size; i++) {
			if (mappings.get(i) == child) {
				return builder.append(".<mapping").append(i + 1).append('>');
			}
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		for (int i = 0, size = mappings.size(); i < size; i++) {
			mappings.get(i).setMyScope(scope);
		}
	}

	/**
	 * Copy over the mappings from the provided mapping list.
	 *
	 * @param otherMappings
	 *                the other list of mappings.
	 * */
	public void copyMappings(final TypeMappings otherMappings) {
		for (int i = 0, size = otherMappings.getNofMappings(); i < size; i++) {
			mappings.add(otherMappings.getMappingByIndex(i));
		}

		// join the locations
		getLocation().setEndOffset(otherMappings.getLocation().getEndOffset());
	}

	/**
	 * Does the semantic checking of the type mapping.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param portType
	 *                the type of the mapping port.
	 * @param legacy
	 *                is this the legacy behavior.
	 * @param incoming
	 *                is it mapping in incoming direction?
	 * */
	public void check(final CompilationTimeStamp timestamp, final Port_Type portType, final boolean legacy, final boolean incoming) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		mappingsMap.clear();
		for (int i = 0, size = mappings.size(); i < size; i++) {
			final TypeMapping mapping = mappings.get(i);
			mapping.check(timestamp, portType, legacy, incoming);
			final Type sourceType = mapping.getSourceType();

			if (sourceType != null && !sourceType.getTypeRefdLast(timestamp).getIsErroneous(timestamp)) {
				final String sourceName = sourceType.getTypename();
				if (mappingsMap.containsKey(sourceName)) {
					sourceType.getLocation().reportSemanticError(
							MessageFormat.format("Duplicate mapping for type `{0}''", sourceName));
					final String message = MessageFormat.format("The mapping of the type `{0}'' is already given here",
							sourceName);
					mappingsMap.get(sourceName).getLocation().reportSemanticWarning(message);
				} else {
					mappingsMap.put(sourceName, mapping);
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (int i = 0, size = mappings.size(); i < size; i++) {
			final TypeMapping mapping = mappings.get(i);

			mapping.updateSyntax(reparser, false);
			reparser.updateLocation(mapping.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (mappings != null) {
			for (final TypeMapping tm : mappings) {
				tm.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (mappings != null) {
			for (final TypeMapping tm : mappings) {
				if (!tm.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
