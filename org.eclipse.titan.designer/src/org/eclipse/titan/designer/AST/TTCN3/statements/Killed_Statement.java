/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Killed_Statement extends Statement {
	private static final String FULLNAMEPART1 = ".componentreference";
	private static final String FULLNAMEPART2 = ".redirectIndex";
	private static final String STATEMENT_NAME = "killed";

	private final Value componentReference;
	//when componentReference is null, this show if the killed was called with any component or all component
	private final boolean isAny;
	
	//FIXME does not yet handle value redirection
	final Reference redirect;
	private final boolean anyFrom;
	private final Reference redirectIndex;

	public Killed_Statement(final Value componentReference, final Reference redirect, final boolean isAny, final boolean any_from, final Reference redirectIndex) {
		this.componentReference = componentReference;
		this.isAny = isAny;
		this.redirect = redirect;
		this.anyFrom = any_from;
		this.redirectIndex = redirectIndex;

		if (componentReference != null) {
			componentReference.setFullNameParent(this);
		}
		if (redirect != null) {
			redirect.setFullNameParent(this);
		}
		if (redirectIndex != null) {
			redirectIndex.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_KILLED;
	}

	@Override
	/** {@inheritDoc} */
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (componentReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (redirectIndex == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (componentReference != null) {
			componentReference.setMyScope(scope);
		}
		if (redirectIndex != null) {
			redirectIndex.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasReceivingStatement() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canRepeat() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		IType referencedType = Port_Utility.checkComponentReference(timestamp, this, componentReference, false, false, anyFrom);

		if (redirectIndex != null && referencedType != null) {
			referencedType = referencedType.getTypeRefdLast(timestamp);
			final ArrayDimensions temp = new ArrayDimensions();
			while (referencedType.getTypetype() == Type_type.TYPE_ARRAY) {
				temp.add(((Array_Type)referencedType).getDimension());
				referencedType = ((Array_Type)referencedType).getElementType();
			}
			checkIndexRedirection(timestamp, redirectIndex, temp, anyFrom, "component");
		}
		if (redirectIndex != null) {
			redirectIndex.setUsedOnLeftHandSide();
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (componentReference != null) {
			componentReference.updateSyntax(reparser, false);
			reparser.updateLocation(componentReference.getLocation());
		}

		if (redirectIndex != null) {
			redirectIndex.updateSyntax(reparser, false);
			reparser.updateLocation(redirectIndex.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReference != null) {
			componentReference.findReferences(referenceFinder, foundIdentifiers);
		}

		if (redirectIndex != null) {
			redirectIndex.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (componentReference != null && !componentReference.accept(v)) {
			return false;
		}
		if (redirectIndex != null && !redirectIndex.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		final ExpressionStruct expression = new ExpressionStruct();
		generateCodeExpression(aData, expression, null);

		PortGenerator.generateCodeStandalone(aData, source, expression.expression.toString(), getStatementName(), canRepeat(), getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final String callTimer) {
		aData.addCommonLibraryImport("TTCN_Runtime");
		aData.addBuiltinTypeImport("TitanComponent");

		if (componentReference != null) {
			// compref.killed
			componentReference.generateCodeExpressionMandatory(aData, expression, true);
			expression.expression.append(".killed(");
			if (redirectIndex == null) {
				expression.expression.append("null");
			} else {
				generateCodeIndexRedirect(aData, expression, redirectIndex, getMyScope());
			}
			expression.expression.append(')');
		} else if (isAny) {
			// any component.killed
			expression.expression.append("TTCN_Runtime.component_killed(TitanComponent.ANY_COMPREF)");
		} else {
			// all component.killed
			expression.expression.append("TTCN_Runtime.component_killed(TitanComponent.ALL_COMPREF)");
		}
	}
}
