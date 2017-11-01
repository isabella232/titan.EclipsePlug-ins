/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock.ReturnStatus_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Boolean_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The While_Statement class represents TTCN3 while statements.
 *
 * @author Kristof Szabados
 * */
public final class While_Statement extends Statement {
	private static final String BOOLEANEXPECTED = "A value or expression of type boolean was expected";
	private static final String NEVERREACH = "Control never reaches this code because the conditional expression evaluates to false";
	private static final String INFINITELOOP = "Inifinite loop detected: the program can not escape from this while statement";

	private static final String FULLNAMEPART1 = ".expr";
	private static final String FULLNAMEPART2 = ".block";
	private static final String STATEMENT_NAME = "while";

	/** The conditional expression. */
	private final Value expression;

	/**
	 * the statementblock of the while statement.
	 * <p>
	 * This can be null
	 * */
	private final StatementBlock statementblock;

	private boolean isInfiniteLoop = false;

	public While_Statement(final Value expression, final StatementBlock statementblock) {
		this.expression = expression;
		this.statementblock = statementblock;

		if (expression != null) {
			expression.setFullNameParent(this);
		}
		if (statementblock != null) {
			statementblock.setFullNameParent(this);
			statementblock.setOwnerIsLoop();
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_WHILE;
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
		} else if (statementblock == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	public Value getExpression() {
		return expression;
	}

	public StatementBlock getStatementBlock() {
		return statementblock;
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

	@Override
	/** {@inheritDoc} */
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		if (statementblock != null) {
			statementblock.setMyStatementBlock(statementBlock, index);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setMyDefinition(final Definition definition) {
		if (statementblock != null) {
			statementblock.setMyDefinition(definition);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setMyAltguards(final AltGuards altGuards) {
		if (statementblock != null) {
			statementblock.setMyAltguards(altGuards);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return isInfiniteLoop;
	}

	@Override
	/** {@inheritDoc} */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (statementblock != null) {
			if (StatementBlock.ReturnStatus_type.RS_NO.equals(statementblock.hasReturn(timestamp))) {
				return StatementBlock.ReturnStatus_type.RS_NO;
			}

			return StatementBlock.ReturnStatus_type.RS_MAYBE;
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		isInfiniteLoop = false;

		if (expression != null) {
			final IValue last = expression.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);

			final Type_type temporalType = last.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (!last.getIsErroneous(timestamp)) {
				if (!Type_type.TYPE_BOOL.equals(temporalType)) {
					last.getLocation().reportSemanticError(BOOLEANEXPECTED);
					expression.setIsErroneous(true);
				} else if (!expression.isUnfoldable(timestamp)) {
					if (!((Boolean_Value) last).getValue()) {
						expression.getLocation().reportConfigurableSemanticProblem(
								Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
										PreferenceConstants.REPORTUNNECESSARYCONTROLS,
										GeneralConstants.WARNING, null), NEVERREACH);
					} else if (ReturnStatus_type.RS_NO.equals(hasReturn(timestamp))) {
						isInfiniteLoop = true;
						location.reportConfigurableSemanticProblem(
								Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
										PreferenceConstants.REPORTINFINITELOOPS, GeneralConstants.WARNING,
										null), INFINITELOOP);
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

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void checkAllowedInterleave() {
		if (statementblock != null) {
			statementblock.checkAllowedInterleave();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void postCheck() {
		super.postCheck();

		if (statementblock != null) {
			statementblock.postCheck();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean enveloped = false;

			if (expression != null) {
				if (reparser.envelopsDamage(expression.getLocation())) {
					expression.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(expression.getLocation());
				}
			}

			if (statementblock != null) {
				if (enveloped) {
					statementblock.updateSyntax(reparser, false);
					reparser.updateLocation(statementblock.getLocation());
				} else if (reparser.envelopsDamage(statementblock.getLocation())) {
					statementblock.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(statementblock.getLocation());
				}
			}

			if (!enveloped) {
				throw new ReParseException();
			}

			return;
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

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		boolean condition_always_true = false;
		boolean condition_always_false = false;
		final IValue last = expression.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
		if (!last.isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
			if (((Boolean_Value) expression).getValue()) {
				condition_always_true = true;
			} else {
				condition_always_false = true;
			}
		}

		//TODO this could be merged before
		if (condition_always_false) {
			source.append("/* never occurs */;\n");
		} else {
			source.append("for ( ; ; ) {\n");
			//FIXME implement loop label if needed

			// do not generate the exit condition for infinite loops
			if (!condition_always_true) {
				final AtomicInteger blockCount = new AtomicInteger(0);
				if (last.returnsNative()) {
					last.generateCodeTmp(aData, source, "if (!", blockCount);
					source.append(") {\n");
				} else {
					aData.addBuiltinTypeImport( "TitanBoolean" );

					last.generateCodeTmp(aData, source, "if (!TitanBoolean.getNative(", blockCount);
					source.append(")) {\n");
				}
				source.append("break;\n");
				source.append("}\n");
				for(int i = 0 ; i < blockCount.get(); i++) {
					source.append("}\n");
				}
			}
			//FIXME add debugger support

			statementblock.generateCode(aData, source);
			source.append("}\n");
		}
	}
}
