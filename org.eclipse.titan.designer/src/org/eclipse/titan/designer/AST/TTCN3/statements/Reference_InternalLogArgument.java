/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class Reference_InternalLogArgument extends InternalLogArgument {
	private final Reference reference;

	public Reference_InternalLogArgument(final Reference reference) {
		super(ArgumentType.Reference);
		this.reference = reference;
	}

	public Reference getReference() {
		return reference;
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (reference != null) {
			reference.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (reference == null) {
			return;
		}

		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment != null) {
			referenceChain.markState();
			referenceChain.add(assignment);
			referenceChain.previousState();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression ) {
		if (reference != null) {
			reference.generateConstRef(aData, expression);
			expression.expression.append(".log()");
		}
	}
}
