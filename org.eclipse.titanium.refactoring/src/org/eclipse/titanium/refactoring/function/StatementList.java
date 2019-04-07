/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;

/**
 * A special ASTNode which contains a list of Statement nodes.
 *
 * @author Viktor Varga
 */
class StatementList extends ASTNode implements ILocateableNode {

	protected Location location;
	protected List<Statement> statements;
	protected StatementBlock myStatementBlock;

	protected StatementList(final List<Statement> statements) {
		this.statements = statements;
		if (statements == null) {
			this.statements = new ArrayList<Statement>();
		}
		if (!this.statements.isEmpty()) {
			final Statement statement = this.statements.get(0);
			myStatementBlock = statement.getMyStatementBlock();
			location = new Location(statement.getLocation().getFile(),
					statement.getLocation().getLine(),
					statement.getLocation().getOffset(),
					this.statements.get(this.statements.size()-1).getLocation().getEndOffset());
		}
	}

	protected boolean isEmpty() {
		return statements.isEmpty();
	}

	protected int getSize() {
		return statements.size();
	}
	protected Statement getStatementByIndex(final int ind) {
		return statements.get(ind);
	}

	@Override
	public void setLocation(final Location location) {
		this.location = location;
	}
	protected void increaseLocationEndOffset(final int incBy) {
		location.setEndOffset(location.getEndOffset()+incBy);
	}
	@Override
	public Location getLocation() {
		return location;
	}
	protected StatementBlock getMyStatementBlock() {
		return myStatementBlock;
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		for (final Statement s: statements) {
			if (!s.accept(v)) {
				return false;
			}
		}

		return true;
	}

	public String createDebugInfo() {
		final StringBuilder sb = new StringBuilder();
		sb.append("ExtractToFunctionRefactoring->StatementList debug info:");
		sb.append("\n  Loc: ");
		if (location == null) {
			sb.append("null");
		} else {
			sb.append(location.getFile().getFullPath());
			sb.append(": ");
			sb.append(location.getOffset());
			sb.append("->");
			sb.append(location.getEndOffset());
		}
		sb.append("\n  MySB info: ");
		if (myStatementBlock == null || myStatementBlock.getMyDefinition() == null) {
			sb.append("null");
		} else {
			sb.append(myStatementBlock.getMyDefinition().getIdentifier());
		}
		sb.append("\n  Statements: ");
		sb.append("(count: ");
		sb.append(statements.size());
		sb.append(") \n");
		for (final Statement s: statements) {
			sb.append("    ");
			sb.append(s.getStatementName());
			sb.append(", loc: ");
			if (s.getLocation() == null) {
				sb.append("null");
			} else {
				sb.append(s.getLocation().getOffset());
				sb.append("->");
				sb.append(s.getLocation().getEndOffset());
				sb.append(" in line ");
				sb.append(s.getLocation().getLine());
			}
			sb.append('\n');
		}
		sb.append('\n');
		return sb.toString();
	}

}


