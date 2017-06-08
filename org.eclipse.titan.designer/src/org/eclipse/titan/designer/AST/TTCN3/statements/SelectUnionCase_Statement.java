/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.Anytype_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a TTCN-3 select union statement.
 *
 * @see SelectUnionCases
 * @see SelectUnionCase
 *
 * @author Arpad Lovassy
 */
public final class SelectUnionCase_Statement extends Statement {
	private static final String UNDETERMINABLETYPE = "Cannot determine the type of the expression";
	private static final String TYPE_MUST_BE_UNION_OR_ANYTYPE = "The type of the expression must be union or anytype";
	private static final String CASENOTCOVERED = "Cases not covered for the following fields: ";

	private static final String FULLNAMEPART1 = ".expression";
	private static final String FULLNAMEPART2 = ".selectunioncases";
	private static final String STATEMENT_NAME = "select-union-case";

	private final Value expression;
	private final SelectUnionCases mSelectUnionCases;

	public SelectUnionCase_Statement(final Value expression, final SelectUnionCases aSelectUnionCases) {
		this.expression = expression;
		this.mSelectUnionCases = aSelectUnionCases;

		if (expression != null) {
			expression.setFullNameParent(this);
		}
		mSelectUnionCases.setFullNameParent(this);
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_SELECT;
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

		if (expression == child) {
			return builder.append(FULLNAMEPART1);
		} else if (mSelectUnionCases == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (expression != null) {
			expression.setMyScope(scope);
		}
		mSelectUnionCases.setMyScope(scope);
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		mSelectUnionCases.setMyStatementBlock(statementBlock, index);
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		mSelectUnionCases.setMyDefinition(definition);
	}

	@Override
	public void setMyAltguards(final AltGuards altGuards) {
		mSelectUnionCases.setMyAltguards(altGuards);
	}

	@Override
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		return mSelectUnionCases.hasReturn(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (expression == null) {
			return;
		}

		IValue temp = expression.setLoweridToReference(timestamp);
		final IType governor = temp.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

		if (governor == null) {
			if (!temp.getIsErroneous(timestamp)) {
				expression.getLocation().reportSemanticError(UNDETERMINABLETYPE);
			}
			return;
		}

		temp = governor.checkThisValueRef(timestamp, expression);
		governor.checkThisValue(timestamp, temp, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false,
				true, false, false));

		//referenced type
		final IType refd = governor.getTypeRefdLast( timestamp );

		if ( refd instanceof TTCN3_Choice_Type ) {
			//referenced union type to check
			final TTCN3_Choice_Type unionType = (TTCN3_Choice_Type)refd;
			checkUnionType( timestamp, unionType );
		} else if ( refd instanceof Anytype_Type ) {
			//referenced anytype type to check
			final Anytype_Type anytypeType = (Anytype_Type)refd;
			checkAnytypeType( timestamp, anytypeType );
		} else {
			expression.getLocation().reportSemanticError( TYPE_MUST_BE_UNION_OR_ANYTYPE );
			for(int i=0; i< mSelectUnionCases.getSize();i++) {
				mSelectUnionCases.getSelectUnionCase(i).checkStatementBlock(timestamp);
			}
			return;
		}
	}

	/**
	 * Checks if select union expression is union
	 * @param aTimestamp the timestamp of the actual semantic check cycle.
	 * @param aUnionType referenced union type to check
	 */
	private void checkUnionType( final CompilationTimeStamp aTimestamp, TTCN3_Choice_Type aUnionType ) {
		// list of union field names. Names of processed field names are removed from the list
		final List<String> fieldNames = new ArrayList<String>();
		for ( int i = 0; i < aUnionType.getNofComponents(); i++ ) {
			final String compName = aUnionType.getComponentIdentifierByIndex( i ).getName();
			fieldNames.add( compName );
		}

		mSelectUnionCases.check( aTimestamp, aUnionType, fieldNames );

		if ( !fieldNames.isEmpty() ) {
			final StringBuilder sb = new StringBuilder( CASENOTCOVERED );
			for ( int i = 0; i < fieldNames.size(); i++ ) {
				if ( i > 0 ) {
					sb.append(", ");
				}
				sb.append( fieldNames.get( i ) );
			}
			location.reportSemanticWarning( sb.toString() );
		}
	}

	/**
	 * Checks if select union expression is anytype
	 * @param aTimestamp the timestamp of the actual semantic check cycle.
	 * @param aAnytypeType referenced anytype type to check
	 */
	private void checkAnytypeType( final CompilationTimeStamp aTimestamp, Anytype_Type aAnytypeType ) {
		// list of types, which are already covered
		final List<String> typesCovered = new ArrayList<String>();
		for ( int i = 0; i < aAnytypeType.getNofComponents(); i++ ) {
			final String compName = aAnytypeType.getComponentByIndex(i).getIdentifier().getName();
			typesCovered.add( compName );
		}
		mSelectUnionCases.check( aTimestamp, aAnytypeType, typesCovered );
	}

	@Override
	public void checkAllowedInterleave() {
		mSelectUnionCases.checkAllowedInterleave();
	}

	@Override
	public void postCheck() {
		mSelectUnionCases.postCheck();
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (expression != null) {
			expression.updateSyntax(reparser, false);
			reparser.updateLocation(expression.getLocation());
		}

		if (mSelectUnionCases != null) {
			mSelectUnionCases.updateSyntax(reparser, false);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (expression != null) {
			expression.findReferences(referenceFinder, foundIdentifiers);
		}
		if (mSelectUnionCases != null) {
			mSelectUnionCases.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (expression != null && !expression.accept(v)) {
			return false;
		}
		if (mSelectUnionCases != null && !mSelectUnionCases.accept(v)) {
			return false;
		}
		return true;
	}

	public Value getExpression() {
		return expression;
	}

	public SelectUnionCases getSelectUnionCases() {
		return mSelectUnionCases;
	}
}
