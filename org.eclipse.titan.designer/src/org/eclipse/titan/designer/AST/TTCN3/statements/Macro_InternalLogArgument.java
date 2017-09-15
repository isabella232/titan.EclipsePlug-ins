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
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.TTCN3.values.Macro_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public final class Macro_InternalLogArgument extends InternalLogArgument {
	private final Macro_Value value;

	public Macro_InternalLogArgument(final Macro_Value value) {
		super(ArgumentType.Macro);
		this.value = value;
	}

	public Macro_Value getMacro() {
		return value;
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (value != null) {
			value.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (value == null) {
			return;
		}

		value.checkRecursions(timestamp, referenceChain);
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression ) {
		//FIXME somewhat more complicated
		if (value != null) {
			//TODO this will be the final code generator
			if (value.canGenerateSingleExpression()) {
				expression.expression.append(MessageFormat.format("TtcnLogger.log_event_Str({0})", value.generateSingleExpression(aData)));
			} else {
				value.generateCodeExpression(aData, expression);
				expression.expression.append(".log()");
			}
		}
	}
}
