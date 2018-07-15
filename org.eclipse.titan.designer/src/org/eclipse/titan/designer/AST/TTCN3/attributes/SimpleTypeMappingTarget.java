/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator.MessageTypeMappingTarget;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents an simple type mapping target (source == target).
 *
 * @author Kristof Szabados
 * */
public final class SimpleTypeMappingTarget extends TypeMappingTarget {

	private final Type targetType;

	public SimpleTypeMappingTarget(final Type targetType) {
		this.targetType = targetType;
	}

	@Override
	/** {@inheritDoc} */
	public TypeMapping_type getTypeMappingType() {
		return TypeMapping_type.SIMPLE;
	}

	@Override
	/** {@inheritDoc} */
	public String getMappingName() {
		return "simple";
	}

	@Override
	/** {@inheritDoc} */
	public Type getTargetType() {
		return targetType;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final Type sourceType, final Port_Type portType, final boolean legacy, final boolean incoming) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (sourceType != null && !sourceType.isIdentical(timestamp, targetType)) {
			targetType.getLocation().reportSemanticError(
					MessageFormat.format("The source and target types must be the same: `{0}'' was expected instead of `{1}''",
							sourceType.getTypename(), targetType.getTypename()));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (targetType != null) {
			targetType.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (targetType != null && !targetType.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public MessageTypeMappingTarget fillTypeMappingTarget(final JavaGenData aData, final StringBuilder source, final IType sourceType, final Scope myScope, final AtomicBoolean hasSliding) {
		hasSliding.set(false);

		if (targetType == null) {
			return new PortGenerator.MessageTypeMappingTarget(null, null);
		}

		return new PortGenerator.MessageTypeMappingTarget(targetType.getGenNameValue(aData, source, myScope), targetType.getTypename());
	}
}
