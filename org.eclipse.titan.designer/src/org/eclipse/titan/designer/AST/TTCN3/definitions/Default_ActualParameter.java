/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an actual parameter that has the value of a default actual
 * parameter that was assigned to the formal parameter.
 *
 * @author Kristof Szabados
 * */
public final class Default_ActualParameter extends ActualParameter {
	// generated value
	private final ActualParameter defaultActualParameter;

	public Default_ActualParameter(final ActualParameter defaultActualParameter) {
		this.defaultActualParameter = defaultActualParameter;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression(final FormalParameter formalParameter) {
		if (defaultActualParameter != null) {
			return defaultActualParameter.hasSingleExpression(formalParameter);
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (defaultActualParameter != null) {
			defaultActualParameter.setCodeSection(codeSection);
		}
	}

	public ActualParameter getActualParameter() {
		return defaultActualParameter;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (defaultActualParameter != null) {
			referenceChain.markState();
			defaultActualParameter.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (defaultActualParameter != null) {
			if (!defaultActualParameter.accept(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression, final FormalParameter formalParameter) {
		if (defaultActualParameter != null) {
			defaultActualParameter.generateCode(aData, expression, formalParameter);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (defaultActualParameter != null) {
			defaultActualParameter.reArrangeInitCode(aData, source, usageModule);
		}
	}
}
