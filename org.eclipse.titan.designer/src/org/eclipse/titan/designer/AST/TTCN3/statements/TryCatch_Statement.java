/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock.ReturnStatus_type;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The Statement class represents a try-catch statement.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class TryCatch_Statement extends Statement {

	private static final String FULLNAMEPART1 = ".try";
	private static final String FULLNAMEPART2 = ".catch";
	private static final String STATEMENT_NAME = "try-catch";

	private final StatementBlock tryBlock;
	private final Identifier exceptionIdentifier;
	private final StatementBlock catchBlock;

	private final StatementBlock catchSurroundingBlock;

	public TryCatch_Statement(final StatementBlock tryBlock, final Identifier exceptionIdentifier, final StatementBlock catchBlock) {
		this.tryBlock = tryBlock;
		this.exceptionIdentifier = exceptionIdentifier;
		this.catchBlock = catchBlock;

		tryBlock.setFullNameParent(this);

		catchSurroundingBlock = new StatementBlock();
		catchSurroundingBlock.setFullNameParent(this);
		if ( exceptionIdentifier == null ) {
			return;
		}
		catchSurroundingBlock.setLocation(exceptionIdentifier.getLocation());
		final Type strType = new CharString_Type();
		strType.setLocation(exceptionIdentifier.getLocation());
		final Def_Var strDefinition = new Def_Var( exceptionIdentifier, strType, null, false );
		strDefinition.setLocation(exceptionIdentifier.getLocation());
		final Statement strStatement = new Definition_Statement(strDefinition);
		strStatement.setLocation(exceptionIdentifier.getLocation());
		catchSurroundingBlock.addStatement(strStatement, true);

		catchBlock.setFullNameParent(catchSurroundingBlock);
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_TRY_CATCH;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (tryBlock == child) {
			return builder.append(FULLNAMEPART1);
		} else if (catchBlock == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		tryBlock.setMyScope(scope);

		catchSurroundingBlock.setMyScope(scope);
		catchBlock.setMyScope(catchSurroundingBlock);
	}

	@Override
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		super.setMyStatementBlock(statementBlock, index);
		if (tryBlock != null) {
			tryBlock.setMyStatementBlock(statementBlock, index);
		}
		if (catchSurroundingBlock != null) {
			catchSurroundingBlock.setMyStatementBlock(statementBlock, index);
		}
		if (catchBlock != null) {
			catchBlock.setMyStatementBlock(statementBlock, index);
		}
	}

	@Override
	public void setMyDefinition(final Definition definition) {
		if (tryBlock != null) {
			tryBlock.setMyDefinition(definition);
		}
		if (catchSurroundingBlock != null) {
			catchSurroundingBlock.setMyDefinition(definition);
		}
		if (catchBlock != null) {
			catchBlock.setMyDefinition(definition);
		}
	}

	@Override
	public void setMyAltguards(final AltGuards altGuards) {
		if (tryBlock != null) {
			tryBlock.setMyAltguards(altGuards);
		}
		if (catchSurroundingBlock != null) {
			catchSurroundingBlock.setMyAltguards(altGuards);
		}
		if (catchBlock != null) {
			catchBlock.setMyAltguards(altGuards);
		}
	}

	/**
	 * Used to tell break and continue statements if they are located with an altstep, a loop or none.
	 *
	 * @param pAltGuards the altguards set only within altguards
	 * @param pLoopStmt the loop statement, set only within loops.
	 * */
	protected void setMyLaicStmt(final AltGuards pAltGuards, final Statement pLoopStmt) {
		if (tryBlock != null) {
			tryBlock.setMyLaicStmt(pAltGuards, pLoopStmt);
		}
		if (catchBlock != null) {
			catchBlock.setMyLaicStmt(pAltGuards, pLoopStmt);
		}
	}

	@Override
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		final ReturnStatus_type tryReturn = tryBlock.hasReturn(timestamp);
		final ReturnStatus_type catchReturn = catchBlock.hasReturn(timestamp);

		// if both branches has the try-catch has, if none of them has the try-catch also does not have.
		if(tryReturn.equals(catchReturn)) {
			return tryReturn;
		}

		// if only the try or the catch branch had a return ... or one of them only might have a return.
		return ReturnStatus_type.RS_MAYBE;
	}

	@Override
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		tryBlock.check(timestamp);
		catchSurroundingBlock.check(timestamp);
		catchBlock.check(timestamp);

		lastTimeChecked = timestamp;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (tryBlock != null) {
			tryBlock.updateSyntax(reparser, false);
			reparser.updateLocation(tryBlock.getLocation());
		}

		if (exceptionIdentifier != null) {
			reparser.updateLocation(exceptionIdentifier.getLocation());
		}

		if (catchBlock != null) {
			catchBlock.updateSyntax(reparser, false);
			reparser.updateLocation(catchBlock.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (tryBlock != null) {
			tryBlock.findReferences(referenceFinder, foundIdentifiers);
		}
		if (catchSurroundingBlock != null) {
			catchSurroundingBlock.findReferences(referenceFinder, foundIdentifiers);
		}
		if (catchBlock != null) {
			catchBlock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (tryBlock != null && !tryBlock.accept(v)) {
			return false;
		}

		if (catchSurroundingBlock != null && !catchSurroundingBlock.accept(v)) {
			return false;
		}

		if (catchBlock != null && !catchBlock.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		aData.addCommonLibraryImport("TTCN_Runtime");
		aData.addCommonLibraryImport("TtcnError");

		final String tempId = aData.getTemporaryVariableName();

		source.append("try {\n");
		source.append("TTCN_Runtime.increase_try_catch_counter();\n");
		tryBlock.generateCode(aData, source);

		source.append(MessageFormat.format("'}' catch(final TtcnError {0}) '{'\n", tempId));
		catchSurroundingBlock.generateCode(aData, source);

		source.append(MessageFormat.format("{0} = new TitanCharString({1}.get_message());\n", exceptionIdentifier.getName(), tempId));
		catchBlock.generateCode(aData, source);
		source.append("}\n");
		source.append("finally {\n");
		source.append("TTCN_Runtime.decrease_try_catch_counter();\n");
		source.append("}\n");
	}
}
