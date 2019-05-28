/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.TTCN3.Code;
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
	public void setCodeSection(final CodeSectionType codeSection) {
		//Do nothing
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
			//TODO this will be the final generated code
			if (argument.length() == 0) {
				// the string is empty: do not generate any code
				return;
			} else if (argument.length() == 1) {
				// the string has one character: use log_char member
				final String escapedChar = Code.translate_character(argument.charAt(0));
				expression.expression.append(MessageFormat.format("TTCN_Logger.log_char(\'\'{0}\'\')", escapedChar));
			} else {
				final String escaped = Code.translate_string(argument);
				expression.expression.append(MessageFormat.format("TTCN_Logger.log_event_str(\"{0}\")", escaped));
			}
		}
	}
}
