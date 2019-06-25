/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

/**
 * This class is used to generate proper Java code,
 *  in situation where pre- or post- calculations are needed.
 * For example short circuit evaluation of conditions,
 *  assignment of complex values/template
 *
 * @author Kristof Szabados
 * */
public class ExpressionStruct {
	public StringBuilder preamble = new StringBuilder();
	public StringBuilder expression = new StringBuilder();
	public StringBuilder postamble = new StringBuilder();

	/**
	 * Merge this expression into the provided stringbuilder.
	 * Also putting scoping brackets around it.
	 *
	 * merge_free_expr in compiler
	 *
	 * @param source the stringbuilder to extend.
	 *
	 * @return the source parameter.
	 * */
	public StringBuilder mergeExpression(final StringBuilder source) {
		if(preamble.length() > 0 || postamble.length() > 0) {
			source.append("{\n");
			source.append(preamble);
		}

		source.append(expression);
		source.append(";\n");

		if(preamble.length() > 0 || postamble.length() > 0) {
			source.append(postamble);
			source.append("}\n");
		}

		return source;
	}

	/**
	 * Merge this expression into the provided stringbuilder.
	 * Without putting scoping brackets around it.
	 *
	 * @param source the stringbuilder to extend.
	 *
	 * @return the source parameter.
	 * */
	public StringBuilder openMergeExpression(final StringBuilder source) {
		if(preamble.length() > 0) {
			source.append(preamble);
		}

		source.append(expression);

		if(postamble.length() > 0) {
			source.append(postamble);
		}

		return source;
	}
}
