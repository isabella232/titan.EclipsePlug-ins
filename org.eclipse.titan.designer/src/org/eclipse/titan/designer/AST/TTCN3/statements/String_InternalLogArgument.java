/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
/**
 * @author Kristof Szabados
 * */
public final class String_InternalLogArgument extends InternalLogArgument {
	private final String argument;

	public String_InternalLogArgument(final String argument) {
		super(ArgumentType.String);
		this.argument = argument;
	}

	public String getString() {
		return argument;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression ) {
		//FIXME somewhat more complicated
		if (argument != null) {
			expression.expression.append(MessageFormat.format("\"{0}\"", argument));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeLog( final JavaGenData aData, final ExpressionStruct expression ) {
		//FIXME somewhat more complicated
		if (argument != null) {
			//TODO this will be the final generated code
			if (argument.length() == 0) {
				// the string is empty: do not generate any code
				return;
			} else if (argument.length() == 1) {
				// the string has one character: use log_char member
				//FIXME needs to use Code::translate_character
				expression.expression.append(MessageFormat.format("TtcnLogger.log_char('{0}')", argument.charAt(0)));
			} else {
				//FIXME needs to use Code::translate_string
				expression.expression.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}\")", argument));
			}
		}
	}
}
