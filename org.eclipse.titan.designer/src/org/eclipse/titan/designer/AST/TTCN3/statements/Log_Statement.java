/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.values.Macro_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Log_Statement extends Statement {
	private static final String FULLNAMEPART = ".logagruments";
	private static final String STATEMENT_NAME = "log";

	private final LogArguments logArguments;

	public Log_Statement(final LogArguments logArguments) {
		this.logArguments = logArguments;

		if (logArguments != null) {
			logArguments.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_LOG;
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

		if (logArguments == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (logArguments != null) {
			logArguments.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (logArguments != null) {
			logArguments.check(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (logArguments != null) {
			logArguments.updateSyntax(reparser, false);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (logArguments != null) {
			logArguments.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (logArguments != null && !logArguments.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		source.append( "\t\t" );
		source.append( "//TODO this is only temporal implementation!\n" );
		source.append( "System.out.println(" );
		logArguments.generateCode( aData, source );
		source.append( ");\n" );

		//this is a preliminary support to be enabled once runtime support is present
		if (logArguments != null) {
			aData.addCommonLibraryImport("TtcnLogger");

			boolean bufferedMode = true;
			if (logArguments.getNofLogArguments() == 1) {
				LogArgument firstArgument = logArguments.getLogArgumentByIndex(0);
				switch (firstArgument.getRealArgument().getArgumentType()) {
				case String:
					// the argument is a simple string: use non-buffered mode
					//FIXME Code::translate_string is missing for now
					source.append(MessageFormat.format("/*TtcnLogger.log_str(Severity.TTCN_USER, \"{0}\");\n*/", ((String_InternalLogArgument) firstArgument.getRealArgument()).getString()));
					bufferedMode = false;
					break;
				case Macro: {
					Macro_Value value = ((Macro_InternalLogArgument) firstArgument.getRealArgument()).getMacro();
					if (value.canGenerateSingleExpression()) {
						// the argument is a simple macro call: use non-buffered mode
						source.append(MessageFormat.format("/*TtcnLogger.log_str(Severity.TTCN_USER, \"{0}\");\n*/", value.generateSingleExpression(aData)));
						bufferedMode = false;
					}
					break;
				}
				default:
					break;
				}
			}

			if (bufferedMode) {
				// the argument is a complicated construct: use buffered mode
				source.append("/*try {\n");
				source.append("TtcnLogger.begin_event(Severity.TTCN_USER);\n");
				logArguments.generateCodeLog(aData, source);
				source.append("TtcnLogger.end_event();\n");
				source.append("} catch (Exception exception) {\n");
				source.append("TtcnLogger.finish_event();\n");
				source.append("throw exception;\n");
				source.append("}\n*/");
			}
		} else {
			source.append("/*TTCN_Logger.log_str(Severity.TTCN_USER,\"<empty log statement>\");\n*/");
		}
	}
}
