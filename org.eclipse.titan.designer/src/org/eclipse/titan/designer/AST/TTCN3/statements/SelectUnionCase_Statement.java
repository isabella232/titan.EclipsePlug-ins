/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
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
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
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
 * @author Farkas Izabella Ingrid
 */
public final class SelectUnionCase_Statement extends Statement {
	private static final String UNDETERMINABLETYPE = "Cannot determine the type of the expression";
	private static final String TYPE_MUST_BE_UNION_OR_ANYTYPE = "The type of the expression must be union or anytype";
	private static final String CASENOTCOVERED = "Cases not covered for the following fields: ";

	private static final String FULLNAMEPART1 = ".expression";
	private static final String FULLNAMEPART2 = ".selectunioncases";
	private static final String STATEMENT_NAME = "select-union-case";

	private final Value expression;
	private final SelectUnionCases selectUnionCases;

	public SelectUnionCase_Statement(final Value expression, final SelectUnionCases aSelectUnionCases) {
		this.expression = expression;
		this.selectUnionCases = aSelectUnionCases;

		if (expression != null) {
			expression.setFullNameParent(this);
		}
		selectUnionCases.setFullNameParent(this);
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
		} else if (selectUnionCases == child) {
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
		selectUnionCases.setMyScope(scope);
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (expression != null) {
			expression.setCodeSection(codeSection);
		}
		selectUnionCases.setCodeSection(codeSection);
	}

	@Override
	/** {@inheritDoc} */
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		selectUnionCases.setMyStatementBlock(statementBlock, index);
	}

	@Override
	/** {@inheritDoc} */
	public void setMyDefinition(final Definition definition) {
		selectUnionCases.setMyDefinition(definition);
	}

	@Override
	/** {@inheritDoc} */
	public void setMyAltguards(final AltGuards altGuards) {
		selectUnionCases.setMyAltguards(altGuards);
	}

	@Override
	/** {@inheritDoc} */
	protected void setMyLaicStmt(final AltGuards pAltGuards, final Statement pLoopStmt) {
		selectUnionCases.setMyLaicStmt(pAltGuards, pLoopStmt);
	}

	@Override
	/** {@inheritDoc} */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		return selectUnionCases.hasReturn(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasReceivingStatement() {
		if (selectUnionCases != null) {
			return selectUnionCases.hasReceivingStatement();
		}

		return false;
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
		governor.checkThisValue(timestamp, temp, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false,
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
		} else if ( refd instanceof ASN1_Choice_Type ) {
			//referenced ASN.1 CHOICE type to check
			final ASN1_Choice_Type anytypeType = (ASN1_Choice_Type)refd;
			checkChoiceType( timestamp, anytypeType );
		} else {
			expression.getLocation().reportSemanticError( TYPE_MUST_BE_UNION_OR_ANYTYPE );
			//special operations to check the body of the cases even if the select expression is erroneous
			for(int i=0; i< selectUnionCases.getSize();i++) {
				selectUnionCases.getSelectUnionCase(i).checkStatementBlock(timestamp);
			}
			return;
		}
	}

	/**
	 * Checks if select union expression is union
	 * @param aTimestamp the timestamp of the actual semantic check cycle.
	 * @param aUnionType referenced union type to check
	 */
	private void checkUnionType( final CompilationTimeStamp aTimestamp, final TTCN3_Choice_Type aUnionType ) {
		// list of union field names. Names of processed field names are removed from the list
		final List<String> fieldNames = new ArrayList<String>();
		for ( int i = 0; i < aUnionType.getNofComponents(); i++ ) {
			final String compName = aUnionType.getComponentIdentifierByIndex( i ).getName();
			fieldNames.add( compName );
		}

		selectUnionCases.check( aTimestamp, aUnionType, fieldNames );

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
	 * Checks if select union expression is a ASN.1 CHOICE
	 * @param aTimestamp the timestamp of the actual semantic check cycle.
	 * @param aUnionType referenced CHOICE type to check
	 */
	private void checkChoiceType( final CompilationTimeStamp aTimestamp, final ASN1_Choice_Type aChoiceType ) {
		// list of union field names. Names of processed field names are removed from the list
		final List<String> fieldNames = new ArrayList<String>();
		for ( int i = 0; i < aChoiceType.getNofComponents(); i++ ) {
			final String compName = aChoiceType.getComponentIdentifierByIndex( i ).getName();
			fieldNames.add( compName );
		}

		selectUnionCases.check( aTimestamp, aChoiceType, fieldNames );

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
	private void checkAnytypeType( final CompilationTimeStamp aTimestamp, final Anytype_Type aAnytypeType ) {
		// list of types, which are already covered
		final List<String> typesCovered = new ArrayList<String>();
		for ( int i = 0; i < aAnytypeType.getNofComponents(); i++ ) {
			final String compName = aAnytypeType.getComponentByIndex(i).getIdentifier().getName();
			typesCovered.add( compName );
		}
		selectUnionCases.check( aTimestamp, aAnytypeType, typesCovered );
	}

	@Override
	/** {@inheritDoc} */
	public void checkAllowedInterleave() {
		selectUnionCases.checkAllowedInterleave();
	}

	@Override
	/** {@inheritDoc} */
	public void postCheck() {
		selectUnionCases.postCheck();
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

		if (selectUnionCases != null) {
			selectUnionCases.updateSyntax(reparser, false);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (expression != null) {
			expression.findReferences(referenceFinder, foundIdentifiers);
		}
		if (selectUnionCases != null) {
			selectUnionCases.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (expression != null && !expression.accept(v)) {
			return false;
		}
		if (selectUnionCases != null && !selectUnionCases.accept(v)) {
			return false;
		}
		return true;
	}

	public Value getExpression() {
		return expression;
	}

	public SelectUnionCases getSelectUnionCases() {
		return selectUnionCases;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		final ExpressionStruct expressionStruct = new ExpressionStruct();
		expression.generateCodeExpressionMandatory(aData, expressionStruct, true);
		source.append(expressionStruct.preamble);
		source.append(MessageFormat.format("switch({0}.get_selection()) '{'\n", expressionStruct.expression));
		selectUnionCases.generateCode(aData, source);
		source.append("}\n");

		source.append(expressionStruct.postamble);
	}
}
