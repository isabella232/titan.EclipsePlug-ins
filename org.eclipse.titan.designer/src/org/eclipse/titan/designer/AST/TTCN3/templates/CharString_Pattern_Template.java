/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString.PatternType;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template that holds a charstring pattern.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class CharString_Pattern_Template extends TTCN3Template {

	private final PatternString patternstring;

	// if assigned to a universal charstring the semantic checking will create a converted value.
	private TTCN3Template converted = null;

	public CharString_Pattern_Template() {
		patternstring = new PatternString(PatternType.CHARSTRING_PATTERN);
		patternstring.setFullNameParent(this);
	}

	public CharString_Pattern_Template(final PatternString ps) {
		patternstring = ps;

		if (patternstring != null) {
			patternstring.setFullNameParent(this);
		}

	}

	public PatternString getPatternstring() {
		return patternstring;
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.CSTR_PATTERN;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous character string pattern";
		}

		return "character string pattern";
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("pattern \"");
		builder.append(patternstring.getFullString());
		builder.append('"');

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
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (patternstring != null) {
			patternstring.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);
		patternstring.setCodeSection(codeSection);
		if (lengthRestriction != null) {
			lengthRestriction.setCodeSection(codeSection);
		}
	}

	public boolean patternContainsAnyornoneSymbol() {
		return true;
	}

	public int getMinLengthOfPattern() {
		// TODO maybe we can say something more precise
		return 0;
	}

	@Override
	/** {@inheritDoc} */
	public TTCN3Template setTemplatetype(final CompilationTimeStamp timestamp, final Template_type newType) {
		switch (newType) {
		case USTR_PATTERN:
			converted = new UnivCharString_Pattern_Template(patternstring);
			converted.copyGeneralProperties(this);
			break;
		default:
			return super.setTemplatetype(timestamp, newType);
		}

		return converted;
	}

	/**
	 * Calculates the referenced template, and while doing so checks the
	 * reference too.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the reference chain used to detect cyclic references.
	 *
	 * @return the template referenced
	 * */
	public TTCN3Template getTemplateReferencedLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (converted == null || converted.getIsErroneous(timestamp)) {
			return this;
		}

		return converted.getTemplateReferencedLast(timestamp, referenceChain);
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		if (converted != null) {
			return converted.getExpressionReturntype(timestamp, expectedValue);
		}

		return Type_type.TYPE_CHARSTRING;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReferenceTemplate(final CompilationTimeStamp timestamp, final Assignment lhs) {
		//FIXME implement once patterns are supported

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of an charstring pattern");
	}

	@Override
	/** {@inheritDoc} */
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		if (Type_type.TYPE_CHARSTRING.equals(typeType) || Type_type.TYPE_UCHARSTRING.equals(typeType)) {
			final boolean hasAnyOrNone = patternContainsAnyornoneSymbol();
			lengthRestriction.checkNofElements(timestamp, getMinLengthOfPattern(), hasAnyOrNone, false, hasAnyOrNone, this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && patternstring != null) {
			patternstring.checkRecursions(timestamp, referenceChain);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (patternstring != null && !patternstring.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		if (converted != null) {
			return converted.hasSingleExpression();
		}

		if (lengthRestriction != null || isIfpresent /* TODO:  || get_needs_conversion()*/) {
			return false;
		}

		//TODO maybe can be optimized by analyzing the pattern
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		lastTimeBuilt = aData.getBuildTimstamp();

		if (converted != null) {
			converted.generateCodeInit(aData, source, name);
			return;
		}

		final StringBuilder preamble = new StringBuilder();

		aData.addBuiltinTypeImport( "TitanCharString" );
		aData.addBuiltinTypeImport( "Base_Template.template_sel" );

		
		String patternString = patternstring.create_charstring_literals(aData,myScope.getModuleScopeGen(),preamble);
		source.append(preamble);
		source.append(MessageFormat.format("{0}.operator_assign(new {1});\n", name, patternString));
		if (lengthRestriction != null) {
			if(getCodeSection() == CodeSectionType.CS_POST_INIT) {
				lengthRestriction.reArrangeInitCode(aData, source, myScope.getModuleScopeGen());
			}
			lengthRestriction.generateCodeInit(aData, source, name);
		}

		if (isIfpresent) {
			source.append(name);
			source.append(".set_ifPresent();\n");
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getSingleExpression(final JavaGenData aData, final boolean castIsNeeded) {
		if (converted != null) {
			return converted.getSingleExpression(aData, castIsNeeded);
		}

		final StringBuilder result = new StringBuilder();

		if (castIsNeeded && (lengthRestriction != null || isIfpresent)) {
			ErrorReporter.INTERNAL_ERROR("FATAL ERROR while processing string pattern template `" + getFullName() + "''");
			return result;
		}

		if (myGovernor == null ) {
			ErrorReporter.INTERNAL_ERROR("FATAL ERROR while processing string pattern template `" + getFullName() + "''");
			return result;
		}

		aData.addBuiltinTypeImport( "TitanCharString" );
		aData.addBuiltinTypeImport( "Base_Template.template_sel" );
		
		StringBuilder preamble = new StringBuilder();
		String patternString = patternstring.create_charstring_literals(aData,myScope.getModuleScopeGen(),preamble);
		result.append(preamble);
		result.append( MessageFormat.format( "new {0}", patternString));	

		//TODO handle cast needed

		return result;
	}

	//TODO: comments
	public void generateCodeStrPattern(final JavaGenData aData, final StringBuilder source) {
		source.append(patternstring.create_charstring_literals(aData,myScope.getModuleScopeGen(),source));
	}
}
