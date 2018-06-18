/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingType;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
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

		targetType.check(timestamp);
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
		if (lastTimeBuilt != null && !lastTimeBuilt.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeBuilt = aData.getBuildTimstamp();

		aData.addBuiltinTypeImport("IDecode_Match");
		aData.addBuiltinTypeImport("TTCN_Buffer");
		aData.addBuiltinTypeImport("TitanOctetString");
		aData.addCommonLibraryImport("TtcnError");
		aData.addCommonLibraryImport("TtcnLogger");
		aData.addBuiltinTypeImport("Base_Type.TTCN_Typedescriptor");
		aData.addBuiltinTypeImport("Base_Template.template_sel");

		final String tempVariableName = aData.getTemporaryVariableName();
		final IType targetType = target.getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);

		final String targetTypeName = targetType.getGenNameValue(aData, source, myScope);
		final String targetTemplateName = targetType.getGenNameTemplate(aData, source, myScope);
		source.append(MessageFormat.format("class dec_match_{0} implements IDecode_Match '{'\n", tempVariableName));
		source.append(MessageFormat.format("{0} target;\n", targetTemplateName));
		source.append(MessageFormat.format("{0} dec_val;\n", targetTypeName));

		source.append(MessageFormat.format("public dec_match_{0}(final {1} target) '{'\n", tempVariableName, targetTemplateName));
		source.append("this.target = target;\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public boolean match(final TTCN_Buffer buffer) {\n");
		source.append(MessageFormat.format("dec_val = new {0}();\n", targetTypeName));
		source.append("boolean ret_val;\n");
		source.append("TitanOctetString os = new TitanOctetString();\n");
		source.append("buffer.get_string(os);\n");
		source.append(MessageFormat.format("if ({0}_decoder(os, dec_val, {0}_default_coding).operatorNotEquals(0)) '{'\n", targetType.getGenNameCoder(aData, source, myScope)));
		source.append("TtcnError.TtcnWarning(\"Decoded content matching failed, because the data could not be decoded.\");\n");
		source.append("ret_val = false;\n");
		source.append("} else if (os.lengthOf().operatorNotEquals(0)) {\n");
		source.append("TtcnError.TtcnWarning(MessageFormat.format(\"Decoded content matching failed, because the buffer was not empty after decoding. Remaining octets: {0}.\", os.lengthOf().getInt()));\n");
		source.append("ret_val = false;\n");
		source.append("} else {\n");
		source.append("ret_val = target.match(dec_val, true);\n");
		source.append("}\n");
		source.append("if (!ret_val) {\n");
		source.append("dec_val = null;\n");
		source.append("}\n");
		source.append("return ret_val;\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public void log() {\n");
		source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}: \");\n", targetTypeName));
		source.append("target.log();\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public Object get_dec_res() {\n");
		source.append("return dec_val;\n");
		source.append("}\n");

		source.append("@Override\n");
		source.append("public TTCN_Typedescriptor get_type_descr() {\n");
		source.append(MessageFormat.format("return {0}_descr_;\n", targetType.getGenNameTypeDescriptor(aData, source, myScope)));
		source.append("}\n");
		source.append( "};\n" );

		source.append(MessageFormat.format("{0}.setType(template_sel.DECODE_MATCH, 0);\n", name));
		source.append("{\n");
		// generate the decoding target into a temporary
		final String target_tempID = aData.getTemporaryVariableName();
		if (target.getDerivedReference() == null) {
			source.append(MessageFormat.format("{0} {1} = new {0}();\n", targetTemplateName, target_tempID));
		} else {
			final ExpressionStruct referencedExpression = new ExpressionStruct();
			target.getDerivedReference().generateCode(aData, referencedExpression);
			if (referencedExpression.preamble.length() > 0) {
				source.append(referencedExpression.preamble);
			}

			source.append(MessageFormat.format("{0} {1} = new {0}({2});\n", targetTemplateName, target_tempID, referencedExpression.expression));
			if (referencedExpression.postamble.length() > 0) {
				source.append(referencedExpression.postamble);
			}
		}

		target.getTemplateBody().generateCodeInit(aData, source, target_tempID);

		// the string encoding format might be an expression, generate its preamble here
		final ExpressionStruct codingExpression = new ExpressionStruct();
		if (stringEncoding != null) {
			stringEncoding.generateCodeExpression(aData, codingExpression, true);
			if (codingExpression.preamble.length() > 0 ) {
				source.append(codingExpression.preamble);
			}
		}

		// initialize the decmatch template with an instance of the new class
		// (pass the temporary template to the new instance's constructor) and
		// the encoding format if it's an universal charstring
		source.append(MessageFormat.format("{0}.set_decmatch(new dec_match_{1}({2})", name, tempVariableName, target_tempID));
		if (codingExpression.expression.length() > 0) {
			source.append(", ").append(codingExpression.expression);
		}
		source.append(");\n");
		if (codingExpression.postamble.length() > 0) {
			source.append(codingExpression.postamble);
		}

		source.append("}\n");
	}
}
