/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents the any template.
 *
 * @author Kristof Szabados
 * */
public final class Any_Value_Template extends TTCN3Template {
	private static final String SIGNATUREERROR = "Generic wildcard `?'' cannot be used for signature `{0}''";

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.ANY_VALUE;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous any value";
		}

		return "any value";
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("?");
		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReferenceTemplate(final CompilationTimeStamp timestamp, final Assignment lhs) {
		//self reference can not happen
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value was expected instead of any value");
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// nothing to be done
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified,
			final boolean allowOmit, final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit, final Assignment lhs) {
		final IType last = type.getTypeRefdLast(timestamp);
		if (Type_type.TYPE_SIGNATURE.equals(last.getTypetype())) {
			location.reportSemanticError(MessageFormat.format(SIGNATUREERROR, last.getFullName()));
			setIsErroneous(true);
		}

		checkLengthRestriction(timestamp, type);
		if (!allowOmit && isIfpresent) {
			location.reportSemanticError("`ifpresent' is not allowed here");
		}
		if (subCheck) {
			type.checkThisTemplateSubtype(timestamp, this);
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean chkRestrictionNamedListBaseTemplate(final CompilationTimeStamp timestamp, final String definitionName,
			final boolean omitAllowed,
			final Set<String> checkedNames, final int neededCheckedCnt, final Location usageLocation) {
		usageLocation.reportSemanticError(MessageFormat.format(RESTRICTIONERROR, definitionName, getTemplateTypeName()));
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		if (lengthRestriction != null || isIfpresent /*  TODO: || get_needs_conversion()*/) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getSingleExpression(final JavaGenData aData, final boolean castIsNeeded) {
		final StringBuilder result = new StringBuilder();

		if (castIsNeeded && (lengthRestriction != null || isIfpresent)) {
			ErrorReporter.INTERNAL_ERROR("FATAL ERROR while processing any value template `" + getFullName() + "''");
			return result;
		}

		if (myGovernor == null ) {
			ErrorReporter.INTERNAL_ERROR("FATAL ERROR while processing any value template `" + getFullName() + "''");
			return result;
		}

		aData.addBuiltinTypeImport( "Base_Template.template_sel" );
		result.append(MessageFormat.format("new {0}(template_sel.ANY_VALUE)", myGovernor.getGenNameTemplate(aData, result, myScope)));

		//TODO handle cast needed

		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		if (lastTimeBuilt != null && !lastTimeBuilt.isLess(aData.getBuildTimstamp())) {
			return;
		}
		lastTimeBuilt = aData.getBuildTimstamp();

		aData.addBuiltinTypeImport( "Base_Template.template_sel" );

		source.append(MessageFormat.format("{0}.assign({1});\n", name, getSingleExpression(aData, false)));

		if (lengthRestriction != null) {
			if(getCodeSection() == CodeSectionType.CS_POST_INIT) {
				lengthRestriction.reArrangeInitCode(aData, source, myScope.getModuleScope());
			}
			lengthRestriction.generateCodeInit(aData, source, name);
		}

		if (isIfpresent) {
			source.append(name);
			source.append(".set_ifPresent();\n");
		}
	}
}
