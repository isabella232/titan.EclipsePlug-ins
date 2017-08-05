/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template that holds a hexstring pattern.
 *
 * @author Kristof Szabados
 * */
public final class HexString_Pattern_Template extends TTCN3Template {

	private final String pattern;

	public HexString_Pattern_Template(final String pattern) {
		this.pattern = pattern;
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.HSTR_PATTERN;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous hexstring pattern";
		}

		return "hexstring pattern";
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("'");
		builder.append(pattern);
		builder.append("'H");

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	public boolean containsAnyornoneSymbol() {
		return pattern.indexOf('*') > -1;
	}

	public int getMinLengthOfPattern() {
		int starCount = 0;
		int index = pattern.indexOf('*', 0);
		while (index != -1) {
			++index;
			++starCount;
			index = pattern.indexOf('*', index);
		}

		return pattern.length() - starCount;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return Type_type.TYPE_HEXSTRING;
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of an hexstring pattern");
	}

	@Override
	/** {@inheritDoc} */
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		if (Type_type.TYPE_HEXSTRING.equals(typeType)) {
			final boolean hasAnyOrNone = containsAnyornoneSymbol();
			lengthRestriction.checkNofElements(timestamp, getMinLengthOfPattern(), hasAnyOrNone, false, hasAnyOrNone, this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// nothing to be done
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		if (lengthRestriction != null || isIfpresent /* TODO:  || get_needs_conversion()*/) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getSingleExpression(final JavaGenData aData, final boolean castIsNeeded) {
		StringBuilder result = new StringBuilder();

		result.append( "\t//TODO:  " );
		result.append( getClass().getSimpleName() );
		result.append( ".getSingleExpression() not yet implement!\n" );
		// TODO: fatal error

		//TODO handle cast needed

		return result;
	}
}
