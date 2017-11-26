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
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.Anytype_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Helper class for the SelectUnionCase_Statement class.
 * Represent the body part of a select union.
 * Holds a list of the select union cases that were parsed from the source code.
 *
 * @see SelectUnionCase_Statement
 * @see SelectUnionCase
 *
 * @author Arpad Lovassy
 * @author Farkas Izabella Ingrid
 */
public final class SelectUnionCases extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".select_union_case_";

	private final List<SelectUnionCase> selectUnionCases;

	public SelectUnionCases() {
		selectUnionCases = new ArrayList<SelectUnionCase>();
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			if (selectUnionCases.get(i) == child) {
				return builder.append(FULLNAMEPART).append(Integer.toString(i + 1));
			}
		}

		return builder;
	}

	public int getSize() {
		return selectUnionCases.size();
	}

	public SelectUnionCase getSelectUnionCase(final int index) {
		if( index < selectUnionCases.size()) {
			return selectUnionCases.get(index);
		} else {
			return null;
		}
	}

	/**
	 * Adds a select case branch.
	 * <p>
	 * The parameter can not be null, that case is handled in the parser.
	 *
	 * @param selectCase
	 *                the select case to be added.
	 * */
	public void addSelectUnionCase(final SelectUnionCase selectUnionCase) {
		selectUnionCases.add(selectUnionCase);
		selectUnionCase.setFullNameParent(this);
	}

	/**
	 * Sets the scope of the contained select case branches.
	 *
	 * @param scope
	 *                the scope to be set.
	 * */
	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			selectUnionCases.get(i).setMyScope(scope);
		}
	}

	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			selectUnionCases.get(i).setMyStatementBlock(statementBlock, index);
		}
	}

	public void setMyDefinition(final Definition definition) {
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			selectUnionCases.get(i).setMyDefinition(definition);
		}
	}

	public void setMyAltguards(final AltGuards altGuards) {
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			selectUnionCases.get(i).setMyAltguards(altGuards);
		}
	}

	/**
	 * Used to tell break and continue statements if they are located with an altstep, a loop or none.
	 *
	 * @param pAltGuards the altguards set only within altguards
	 * @param pLoopStmt the loop statement, set only within loops.
	 * */
	public void setMyLaicStmt(final AltGuards pAltGuards, final Statement pLoopStmt) {
		for (SelectUnionCase selectCase : selectUnionCases) {
			selectCase.getStatementBlock().setMyLaicStmt(pAltGuards, pLoopStmt);
		}
	}

	/**
	 * Checks whether the select cases have a return statement, either
	 * directly or embedded.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 *
	 * @return the return status of the select cases.
	 * */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		StatementBlock.ReturnStatus_type result = StatementBlock.ReturnStatus_type.RS_MAYBE;
		boolean hasElse = false;
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			final SelectUnionCase selectUnionCase = selectUnionCases.get(i);
			switch (selectUnionCase.hasReturn(timestamp)) {
			case RS_NO:
				if (result == StatementBlock.ReturnStatus_type.RS_YES) {
					return StatementBlock.ReturnStatus_type.RS_MAYBE;
				}

				result = StatementBlock.ReturnStatus_type.RS_NO;
				break;
			case RS_YES:
				if (result == StatementBlock.ReturnStatus_type.RS_NO) {
					return StatementBlock.ReturnStatus_type.RS_MAYBE;
				}

				result = StatementBlock.ReturnStatus_type.RS_YES;
				break;
			default:
				return StatementBlock.ReturnStatus_type.RS_MAYBE;
			}

			if (selectUnionCase.hasElse()) {
				hasElse = true;
				break;
			}
		}

		if (!hasElse && result == StatementBlock.ReturnStatus_type.RS_YES) {
			return StatementBlock.ReturnStatus_type.RS_MAYBE;
		}

		return result;
	}

	/**
	 * Used when generating code for interleaved statement.
	 * If the block has no receiving statements, then the general code generation can be used
	 *  (which may use blocks).
	 * */
	public boolean hasReceivingStatement() {
		for (int i = 0; i < selectUnionCases.size(); i++) {
			if (selectUnionCases.get(i).hasReceivingStatement()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Does the semantic checking of the select case list of union type
	 *
	 * @param aTimestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param aUnionType
	 *                the referenced union type of the select expression, to check the cases against.
	 *                It can not be null.
	 * @param aFieldNames
	 *                union field names, which are not covered yet.
	 *                If a field name is found, it is removed from the list.
	 *                If case else is found, all the filed names are removed from the list, because all the cases are covered.
	 */
	public void check( final CompilationTimeStamp aTimestamp, final TTCN3_Choice_Type aUnionType, final List<String> aFieldNames ) {
		boolean unreachable = false;
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			unreachable = selectUnionCases.get(i).check( aTimestamp, aUnionType, unreachable, aFieldNames );
		}
	}

	/**
	 * Does the semantic checking of the select case list of anytype type
	 *
	 * @param aTimestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param aAnytypeType
	 *                the referenced anytype type of the select expression, to check the cases against.
	 *                It can not be null.
	 * @param aTypesCovered
	 *                names of types, which are already covered.
	 *                If a new type is found, it is added to the list.
	 */
	public void check( final CompilationTimeStamp aTimestamp, final Anytype_Type aAnytypeType, final List<String> aTypesCovered ) {
		boolean unreachable = false;
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			unreachable = selectUnionCases.get(i).check( aTimestamp, aAnytypeType, unreachable, aTypesCovered );
		}
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			selectUnionCases.get(i).checkAllowedInterleave();
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public void postCheck() {
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			selectUnionCases.get(i).postCheck();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		SelectUnionCase branch;
		for (int i = 0, size = selectUnionCases.size(); i < size; i++) {
			branch = selectUnionCases.get(i);

			branch.updateSyntax(reparser, false);
			reparser.updateLocation(branch.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (selectUnionCases == null) {
			return;
		}

		for (final SelectUnionCase sc : selectUnionCases) {
			sc.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (selectUnionCases != null) {
			for (final SelectUnionCase sc : selectUnionCases) {
				if (!sc.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}

	public List<SelectUnionCase> getSelectUnionCaseArray() {
		return selectUnionCases;
	}

	/**
	 * Add generated java code for the list of select union cases.
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source code generated
	 */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		source.append("case UNBOUND_VALUE :\n");
		source.append("throw new TtcnError(\"The union in the head shall be initialized\");\n");
		final AtomicBoolean unreach = new AtomicBoolean(false);

		for (int i = 0; i < selectUnionCases.size(); i++) {
			selectUnionCases.get(i).generateCode(aData, source, unreach);
			if (unreach.get()) {
				break;
			}
		}

		if (!unreach.get()) {
			source.append("default:\nbreak;\n");
		}
	}
}
