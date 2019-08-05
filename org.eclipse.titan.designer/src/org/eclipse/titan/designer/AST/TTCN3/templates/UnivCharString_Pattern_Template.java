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

//FIXME implement as soon as charstring pattern templates become handled
/**
 * Represents a template that holds a charstring pattern.
 *
 * @author Kristof Szabados
 * */
public final class UnivCharString_Pattern_Template extends TTCN3Template {

	private final PatternString patternstring;

	public UnivCharString_Pattern_Template() {
		patternstring = new PatternString(PatternType.UNIVCHARSTRING_PATTERN);
		patternstring.setFullNameParent(this);
	}

	public UnivCharString_Pattern_Template(final PatternString ps) {
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
		return Template_type.USTR_PATTERN;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous universal character string pattern";
		}

		return "universal character string pattern";
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
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return Type_type.TYPE_UCHARSTRING;
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
		getLocation().reportSemanticError("A specific value expected instead of a universal charstring pattern");
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

		final StringBuilder preamble = new StringBuilder();

		aData.addBuiltinTypeImport( "TitanCharString" );
		aData.addBuiltinTypeImport( "Base_Template.template_sel" );

		String patternStr = patternstring.create_charstring_literals(aData, myScope.getModuleScopeGen(), preamble);
		source.append(preamble);
		source.append(MessageFormat.format("{0}.operator_assign(new {1});\n", name, patternStr));

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
		String patternStr = patternstring.create_charstring_literals(aData, myScope.getModuleScopeGen(), preamble);
		result.append(preamble);
		result.append( MessageFormat.format( "new {0}", patternStr));

		//TODO handle cast needed

		return result;
	}
}
