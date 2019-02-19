/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Stop_Execution_Statement extends Statement {
	private static final String STATEMENT_NAME = "stop";

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_STOP_EXECUTION;
	}

	@Override
	/** {@inheritDoc} */
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		//Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		// nothing to be done
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
		// no members
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		aData.addCommonLibraryImport("TTCN_Runtime");
		source.append("TTCN_Runtime.stop_execution();\n");

		final Definition definition = myStatementBlock.getMyDefinition();
		if (definition.getAssignmentType() == Assignment_type.A_FUNCTION_RVAL) {
			final IType type = definition.getType(CompilationTimeStamp.getBaseTimestamp());
			final String typeGeneratedName = type.getGenNameValue( aData, source, getMyScope() );
			source.append(MessageFormat.format("return new {0}();\n", typeGeneratedName));
		} else if (definition.getAssignmentType() == Assignment_type.A_FUNCTION_RTEMP) {
			final IType type = definition.getType(CompilationTimeStamp.getBaseTimestamp());
			final String typeGeneratedName = type.getGenNameTemplate(aData, source, getMyScope());
			source.append(MessageFormat.format("return new {0}();\n", typeGeneratedName));
		}
	}
}
