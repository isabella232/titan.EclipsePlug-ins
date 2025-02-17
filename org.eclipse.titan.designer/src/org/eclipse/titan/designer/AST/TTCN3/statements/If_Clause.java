/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.Boolean_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value.Operation_type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The If_Clause class represents a single clause (branch) of a TTCN3 if
 * statement.
 *
 * @see If_Clauses
 * @see If_Statement
 *
 * @author Kristof Szabados
 * */
public final class If_Clause extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String BOOLEANEXPECTED = "A value or expression of type boolean was expected";

	private static final String FULLNAMEPART1 = ".expr";
	private static final String FULLNAMEPART2 = ".block";

	/** The conditional expression. */
	private final Value expression;

	/**
	 * the statementblock of the branch.
	 * <p>
	 * This can be null
	 * */
	private final StatementBlock statementblock;

	private Location location = NULL_Location.INSTANCE;

	public If_Clause(final Value expression, final StatementBlock statementblock) {
		this.expression = expression;
		this.statementblock = statementblock;

		if (expression != null) {
			expression.setFullNameParent(this);
		}
		if (statementblock != null) {
			statementblock.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (expression == child) {
			return builder.append(FULLNAMEPART1);
		} else if (statementblock == child) {
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
		if (statementblock != null) {
			statementblock.setMyScope(scope);
		}
	}

	/**
	 * Sets the code_section attribute for the statements in this if clause to the provided value.
	 *
	 * @param codeSection the code section where these statements should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (expression != null) {
			expression.setCodeSection(codeSection);
		}
		if (statementblock != null) {
			statementblock.setCodeSection(codeSection);
		}
	}

	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		if (statementblock != null) {
			statementblock.setMyStatementBlock(statementBlock, index);
		}
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

	public void setMyDefinition(final Definition definition) {
		if (statementblock != null) {
			statementblock.setMyDefinition(definition);
		}
	}

	public void setMyAltguards(final AltGuards altGuards) {
		if (statementblock != null) {
			statementblock.setMyAltguards(altGuards);
		}
	}

	/**
	 * Checks whether the if clause has a return statement, either directly
	 * or embedded.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 *
	 * @return the return status of the if clause.
	 * */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (statementblock != null) {
			return statementblock.hasReturn(timestamp);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	/**
	 * Used when generating code for interleaved statement.
	 * If the block has no receiving statements, then the general code generation can be used
	 *  (which may use blocks).
	 * */
	public boolean hasReceivingStatement() {
		if (statementblock != null) {
			return statementblock.hasReceivingStatement(0);
		}

		return false;
	}

	/**
	 * Does the semantic checking of this branch.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param unreachable
	 *                boolean parameter telling if this if statement was
	 *                already found unreachable by previous clauses or not
	 *
	 * @return true if following clauses are unreachable
	 * */
	public boolean check(final CompilationTimeStamp timestamp, final boolean unreachable) {
		boolean unreachable2 = unreachable;
		if (expression != null) {
			final IValue last = expression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			final Type_type temporalType = last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!last.getIsErroneous(timestamp) && !Type_type.TYPE_UNDEFINED.equals(temporalType)) {
				if (!Type_type.TYPE_BOOL.equals(temporalType)) {
					last.getLocation().reportSemanticError(BOOLEANEXPECTED);
					expression.setIsErroneous(true);
				} else if (!expression.isUnfoldable(timestamp)) {
					if (((Boolean_Value) last).getValue()) {
						unreachable2 = true;
					}
				}

				if(expression.getMyGovernor() == null) {
					expression.setMyGovernor(new Boolean_Type());
				}
			}
		}
		if (statementblock != null) {
			statementblock.check(timestamp);
		}

		return unreachable2;
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		if (statementblock != null) {
			statementblock.checkAllowedInterleave();
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public void postCheck() {
		if (statementblock != null) {
			statementblock.postCheck();
		}
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

		if (statementblock != null) {
			statementblock.updateSyntax(reparser, false);
			reparser.updateLocation(statementblock.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (expression != null) {
			expression.findReferences(referenceFinder, foundIdentifiers);
		}
		if (statementblock != null) {
			statementblock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	/**
	 * Checks whether the condition is a negated expression.
	 *
	 * @return true if it is negated
	 */
	public boolean isNegatedCondition() {
		return expression != null && Value_type.EXPRESSION_VALUE.equals(expression.getValuetype())
				&& Operation_type.NOT_OPERATION.equals(((Expression_Value) expression).getOperationType());
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (expression != null && !expression.accept(v)) {
			return false;
		}
		if (statementblock != null && !statementblock.accept(v)) {
			return false;
		}
		return true;
	}

	public Value getExpression() {
		return expression;
	}

	public StatementBlock getStatementBlock() {
		return statementblock;
	}

	/**
	 * Add generated java code for a single if clause.
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source code generated
	 * @param blockCount the number of block already created
	 * @param unReachable tells whether this branch is already unreachable because of previous conditions
	 * @param eachFalse true if the branches so far all evaluated to a false condition in compile time.
	 */
	public void generateCode( final JavaGenData aData, final StringBuilder source, final AtomicInteger blockCount, final AtomicBoolean unReachable, final AtomicBoolean eachFalse) {
		if (unReachable.get()) {
			return;
		}
		if (!expression.isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
			final IValue last = expression.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			if (((Boolean_Value) last).getValue()) {
				unReachable.set(true);
			} else {
				return;
			}
		}

		if(!eachFalse.get()) {
			source.append("else ");
		}
		if(!unReachable.get()) {
			if(!eachFalse.get()) {
				source.append("{\n");
				blockCount.incrementAndGet();
			}

			if (expression.returnsNative()) {
				expression.generateCodeTmp(aData, source, "if (", blockCount);
				source.append(')');
			} else {
				aData.addBuiltinTypeImport( "TitanBoolean" );

				expression.generateCodeTmp(aData, source, "if (TitanBoolean.get_native(", blockCount);
				source.append(") )");
			}
		}
		eachFalse.set(false);
		source.append("{\n");
		statementblock.generateCode(aData, source);
		source.append("}\n");
	}
}
