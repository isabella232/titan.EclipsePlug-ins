/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a decode match template.
 *
 * @author Kristof Szabados
 * 
 * */
public class DecodeMatch_template extends TTCN3Template {
	final Value stringEncoding;
	final TemplateInstance target;

	public DecodeMatch_template(final Value stringEncoding, final TemplateInstance target) {
		this.stringEncoding = stringEncoding;
		this.target = target;

		if (stringEncoding != null) {
			stringEncoding.setFullNameParent(this);
		}
		if (target != null) {
			target.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.DECODE_MATCH;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous decode match";
		}

		return "decode match";
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		if (target == null) {
			return "<erroneous template>";
		}

		final StringBuilder builder = new StringBuilder();
		builder.append("decmatch ");

		if (stringEncoding != null) {
			builder.append('(');
			builder.append(stringEncoding.createStringRepresentation());
			builder.append(')');
		}

		builder.append(target.getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE).getTypename());
		builder.append(": ");
		target.getTemplateBody().createStringRepresentation();

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
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (stringEncoding == child) {
			return builder.append(".<string_encoding>");
		} else if (target == child) {
			return builder.append(".<decoding_target>");
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (stringEncoding != null) {
			stringEncoding.setMyScope(scope);
		}
		if (target != null) {
			target.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);
		if (stringEncoding != null) {
			stringEncoding.setCodeSection(codeSection);
		}
		if (target != null) {
			target.setCodeSection(codeSection);
		}
		if (lengthRestriction != null) {
			lengthRestriction.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (target != null) {
			target.checkRecursions(timestamp, referenceChain);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReferenceTemplate(final CompilationTimeStamp timestamp, final Assignment lhs) {
		if (target != null) {
			return target.getTemplateBody().checkExpressionSelfReferenceTemplate(timestamp, lhs);
		}

		return false;
	}


	@Override
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of a decode match");
	}

	/**
	 * Checks if this template is valid for the provided type.
	 * <p>
	 * The type must be equivalent with the TTCN-3 string type
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param type the string type used for the check.
	 * @param implicitOmit true if the implicit omit optional attribute was set for the template, false otherwise.
	 * @param lhs the assignment to check against.
	 *
	 * @return true if the value contains a reference to lhs
	 * */
	public boolean checkThisTemplateString(final CompilationTimeStamp timestamp, final IType type, final boolean implicitOmit, final Assignment lhs) {
		target.getTemplateBody().setLoweridToReference(timestamp);
		IType targetType = target.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_TEMPLATE);
		if (targetType == null) {
			target.getLocation().reportSemanticError("Type of template instance cannot be determined");
			return false;
		}

		if (target.getType() != null && targetType instanceof IReferencingType) {
			targetType = targetType.getTypeRefdLast(timestamp);
		}

		boolean selfReference = target.getTemplateBody().checkThisTemplateGeneric(timestamp, targetType, target.getDerivedReference() == null ? false : true, false, true, true, implicitOmit, lhs);
		targetType.checkCoding(timestamp, false, getMyScope().getModuleScope(), false);

		if (stringEncoding != null) {
			if (type.getTypetype() != Type_type.TYPE_UCHARSTRING) {
				stringEncoding.getLocation().reportSemanticError("The encoding format parameter is only available to universal charstring templates");
				return selfReference;
			}
			selfReference |= stringEncoding.checkStringEncoding(timestamp, lhs);
		}

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public boolean needsTemporaryReference() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		//FIXME implement
		source.append( "\t//TODO: " );
		source.append( getClass().getSimpleName() );
		source.append( ".generateCodeInit() is not implemented!\n" );
	}

	
}
