/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.types.Altstep_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Function_Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
/**
 * @author Kristof Szabados
 * */
public final class Unknown_Applied_Statement extends Statement {
	private static final String FUNCTIONORALTSTEPVALUEXPECTED = "A value of type function or altstep was expected instead of `{0}''";
	private static final String UNUSEDRETURNVALUE = "The value returned by function type `{0}'' is not used";

	private static final String FULLNAMEPART1 = ".reference";
	private static final String FULLNAMEPART2 = ".<parameters>";
	private static final String STATEMENT_NAME = "function or altstep type application";

	private final Value dereferredValue;
	private final ParsedActualParameters actualParameterList;

	private Statement realStatement;
	/** The index of this statement in its parent statement block. */
	private int statementIndex;

	public Unknown_Applied_Statement(final Value dereferredValue, final ParsedActualParameters actualParameterList) {
		this.dereferredValue = dereferredValue;
		this.actualParameterList = actualParameterList;

		if (dereferredValue != null) {
			dereferredValue.setFullNameParent(this);
		}
		if (actualParameterList != null) {
			actualParameterList.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_UNKNOWN_APPLIED_INSTANE;
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

		if (dereferredValue == child) {
			return builder.append(FULLNAMEPART1);
		} else if (actualParameterList == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (dereferredValue != null) {
			dereferredValue.setMyScope(scope);
		}
		if (actualParameterList != null) {
			actualParameterList.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		statementIndex = index;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasReceivingStatement() {
		if (realStatement != null) {
			return realStatement.hasReceivingStatement();
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

		if (dereferredValue == null) {
			return;
		}

		dereferredValue.setLoweridToReference(timestamp);
		IType type = dereferredValue.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
		if (type != null) {
			type = type.getTypeRefdLast(timestamp);
		}
		if (type == null) {
			return;
		}

		final ActualParameterList tempActualParameters = new ActualParameterList();
		FormalParameterList formalParameterList;

		switch (type.getTypetype()) {
		case TYPE_FUNCTION:
			if (realStatement == null || !Statement_type.S_FUNCTION_APPLIED.equals(realStatement.getType())) {
				realStatement = new Function_Applied_Statement(dereferredValue, actualParameterList, tempActualParameters);
				realStatement.setFullNameParent(this);
				realStatement.setLocation(location);
				realStatement.setMyStatementBlock(getMyStatementBlock(), statementIndex);
			}
			realStatement.check(timestamp);
			if (((Function_Type) type).getReturnType() != null) {
				location.reportConfigurableSemanticProblem(
						Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.REPORTUNUSEDFUNCTIONRETURNVALUES, GeneralConstants.WARNING, null),
								MessageFormat.format(UNUSEDRETURNVALUE, type.getTypename()));
			}

			formalParameterList = ((Function_Type) type).getFormalParameters();
			formalParameterList.checkActualParameterList(timestamp, actualParameterList, tempActualParameters);
			break;
		case TYPE_ALTSTEP:
			if (realStatement == null || !Statement_type.S_ALTSTEP_APPLIED.equals(realStatement.getType())) {
				realStatement = new Altstep_Applied_Statement(dereferredValue, actualParameterList, tempActualParameters);
				realStatement.setFullNameParent(this);
				realStatement.setLocation(location);
				realStatement.setMyStatementBlock(getMyStatementBlock(), statementIndex);
			}
			realStatement.check(timestamp);

			formalParameterList = ((Altstep_Type) type).getFormalParameters();
			formalParameterList.checkActualParameterList(timestamp, actualParameterList, tempActualParameters);
			break;
		default:
			dereferredValue.getLocation().reportSemanticError(MessageFormat.format(FUNCTIONORALTSTEPVALUEXPECTED, type.getTypename()));
			break;
		}

		if (myStatementBlock != null) {
			myStatementBlock.checkRunsOnScope(timestamp, type, this, "call");
		}
		tempActualParameters.setFullNameParent(this);
		tempActualParameters.setMyScope(myScope);
	}

	@Override
	/** {@inheritDoc} */
	public void checkAllowedInterleave() {
		if (realStatement != null) {
			realStatement.checkAllowedInterleave();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (dereferredValue != null) {
			dereferredValue.updateSyntax(reparser, false);
			reparser.updateLocation(dereferredValue.getLocation());
		}

		if (actualParameterList != null) {
			actualParameterList.updateSyntax(reparser, false);
			reparser.updateLocation(actualParameterList.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (realStatement != null) {
			realStatement.findReferences(referenceFinder, foundIdentifiers);
		} else {
			if (dereferredValue != null) {
				dereferredValue.findReferences(referenceFinder, foundIdentifiers);
			}
			if (actualParameterList != null) {
				actualParameterList.findReferences(referenceFinder, foundIdentifiers);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (realStatement != null) {
			return realStatement.accept(v);
		} else {
			if (dereferredValue != null && !dereferredValue.accept(v)) {
				return false;
			}
			if (actualParameterList != null && !actualParameterList.accept(v)) {
				return false;
			}
			return true;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		if (realStatement == null) {
			ErrorReporter.INTERNAL_ERROR("Encountered an uncheked apply statement `" + getFullName() + "''");
			return;
		}

		realStatement.generateCode(aData, source);
	}
}
