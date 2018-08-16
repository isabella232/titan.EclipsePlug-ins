/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter.parameterEvaluationType;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.Referenced_Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * helper class for generating code to lazy and fuzzy formal parameters.
 *
 * @author Kristof Szabados
 * */
public class LazyFuzzyParamData {
	//recursive code generation: calling a func. with lazy/fuzzy param inside a lazy/fuzzy param
	private static int depth = 0;
	private static boolean used_as_lvalue = false;
	private static ArrayList<String> typeVector = null;
	private static ArrayList<String> referedVector = null;

	public static void init(final boolean used_as_lvalue) {
		if (depth == 0) {
			LazyFuzzyParamData.used_as_lvalue = used_as_lvalue;
			typeVector = new ArrayList<String>();
			referedVector = new ArrayList<String>();
		}

		depth++;
	}

	public static void clean() {
		if (depth == 1) {
			typeVector.clear();
			typeVector = null;
			referedVector.clear();
			referedVector = null;
		}

		depth--;
	}

	public static boolean inLazyOrFuzzy() {
		return depth > 0;
	}

	public static String addReferenceGenname(final JavaGenData aData, final StringBuilder source, final Assignment assignment, final Scope scope) {
		final StringBuilder typeString = new StringBuilder();
		switch (assignment.getAssignmentType()) {
		case A_MODULEPAR_TEMPLATE:
		case A_TEMPLATE:
		case A_VAR_TEMPLATE:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
			typeString.append(assignment.getType(CompilationTimeStamp.getBaseTimestamp()).getGenNameTemplate(aData, source, scope));
			break;
		default:
			typeString.append(assignment.getType(CompilationTimeStamp.getBaseTimestamp()).getGenNameValue(aData, source, scope));
			break;
		}

		boolean parIsLazyOrFuzzy = false;
		switch (assignment.getAssignmentType()) {
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
			parIsLazyOrFuzzy = ((FormalParameter)assignment).getEvaluationType() != parameterEvaluationType.NORMAL_EVAL;
			if (parIsLazyOrFuzzy) {
				typeString.insert(0, "Lazy_Fuzzy_ValueExpr<");
				typeString.append('>');
			}
			break;
		case A_PAR_TEMP_IN:
			parIsLazyOrFuzzy = ((FormalParameter)assignment).getEvaluationType() != parameterEvaluationType.NORMAL_EVAL;
			if (parIsLazyOrFuzzy) {
				typeString.insert(0, "Lazy_Fuzzy_TemplateExpr<");
				typeString.append('>');
			}
			break;
		default:
			break;
		}

		typeVector.add(typeString.toString());
		referedVector.add(assignment.getGenNameFromScope(aData, source, scope, ""));
		if (parIsLazyOrFuzzy) {
			return getMemberName(referedVector.size() - 1) + ".evaluate()";
		} else {
			return getMemberName(referedVector.size() - 1);
		}
	}

	public static String getMemberName(final int index) {
		return "lpm_" + index;
	}

	private static void generateCodeForValue(final JavaGenData aData, final ExpressionStruct valueExpression, final IValue value, final Scope scope) {
		value.generateCodeExpression(aData, valueExpression, false);
	}

	private static void generateCodeForTemplate(JavaGenData aData, ExpressionStruct templateExpression, TemplateInstance template,
			Restriction_type genRestrictionCheck, Scope scope) {
		template.generateCode(aData, templateExpression, genRestrictionCheck);
	}

	public static void generateCode(final JavaGenData aData, final ExpressionStruct expression, final IValue value, final Scope scope, final boolean lazy) {
		if (depth > 1) {
			// if a function with lazy parameter(s) was called inside a lazy parameter then don't generate code for
			// lazy parameter inside a lazy parameter, call the function as a normal call
			// wrap the calculated parameter value inside a special constructor which calculates the value of its cache immediately
			final ExpressionStruct valueExpression = new ExpressionStruct();
			generateCodeForValue(aData, valueExpression, value, scope);
			// the id of the instance of Lazy_Fuzzy_Expr, which will be used as the actual parameter
			final String paramId = aData.getTemporaryVariableName();
			if (valueExpression.preamble.length() > 0) {
				expression.preamble.append(valueExpression.preamble);
			}

			aData.addBuiltinTypeImport("Lazy_Fuzzy_ValueExpr");
			expression.preamble.append(MessageFormat.format("final Lazy_Fuzzy_ValueExpr<{0}> {1} = new Lazy_Fuzzy_ValueExpr<{0}>({2}, {3});\n", value.getMyGovernor().getGenNameValue(aData, expression.preamble, scope), paramId, lazy ? "false" : "true", valueExpression.expression));
			expression.expression.append(paramId);

			return;
		}

		// only if the formal parameter is *not* used as lvalue
		if (!used_as_lvalue && value.getValuetype() == Value_type.REFERENCED_VALUE && ((Referenced_Value)value).getReference().getSubreferences().size() <= 1) {
			final Reference reference = ((Referenced_Value)value).getReference();
			final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
			if (assignment != null) {
				parameterEvaluationType eval = parameterEvaluationType.NORMAL_EVAL;
				switch (assignment.getAssignmentType()) {
				case A_PAR_VAL:
				case A_PAR_VAL_IN:
				case A_PAR_TEMP_IN:
					eval = ((FormalParameter)assignment).getEvaluationType();
					break;
				default:
					break;
				}

				if (eval != parameterEvaluationType.NORMAL_EVAL) {
					final String refdString = assignment.getGenNameFromScope(aData, expression.expression, scope, "");
					if ((eval == parameterEvaluationType.LAZY_EVAL && !lazy) ||
							(eval == parameterEvaluationType.FUZZY_EVAL && lazy)) {
						expression.preamble.append(MessageFormat.format("{0}.change();\n", refdString));
						expression.postamble.append(MessageFormat.format("{0}.revert();\n", refdString));
					}

					expression.expression.append(refdString);

					return;
				}
			}
		}

		// generate the code for value in a temporary expr structure, this code is put inside the evaluate() member function
		aData.addBuiltinTypeImport("Lazy_Fuzzy_ValueExpr");
		final ExpressionStruct valueExpression = new ExpressionStruct();
		generateCodeForValue(aData, valueExpression, value, scope);
		final String param_id = aData.getTemporaryVariableName();
		final String typeName = value.getMyGovernor().getGenNameValue(aData, expression.expression, scope);
		generateCodeParameterClass(aData, expression, valueExpression, param_id, typeName, true, lazy);
	}

	public static void generateCode(final JavaGenData aData, final ExpressionStruct expression, final TemplateInstance template,
			final Restriction_type genRestrictionCheck, final Scope scope, final boolean lazy) {
		if (depth > 1) {
			// if a function with lazy parameter(s) was called inside a lazy parameter then don't generate code for
			// lazy parameter inside a lazy parameter, call the function as a normal call
			// wrap the calculated parameter value inside a special constructor which calculates the value of its cache immediately
			final ExpressionStruct templateExpression = new ExpressionStruct();
			generateCodeForTemplate(aData, templateExpression, template, genRestrictionCheck, scope);
			// the id of the instance of Lazy_Fuzzy_Expr, which will be used as the actual parameter
			final String paramId = aData.getTemporaryVariableName();
			if (templateExpression.preamble.length() > 0) {
				expression.preamble.append(templateExpression.preamble);
			}

			aData.addBuiltinTypeImport("Lazy_Fuzzy_TemplateExpr");
			expression.preamble.append(MessageFormat.format("final Lazy_Fuzzy_TemplateExpr<{0}> {1} = new Lazy_Fuzzy_TemplateExpr<{0}>({2}, {3});\n", template.getTemplateBody().getMyGovernor().getGenNameTemplate(aData, expression.preamble, scope), paramId, lazy ? "false" : "true", templateExpression.expression));
			expression.expression.append(paramId);

			return;
		}

		// only if the formal parameter is *not* used as lvalue
		if (!used_as_lvalue && template.getTemplateBody().getTemplatetype() == Template_type.TEMPLATE_REFD && ((Referenced_Template)template.getTemplateBody()).getReference().getSubreferences().size() <= 1) {
			final Reference reference = ((Referenced_Template)template.getTemplateBody()).getReference();
			final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
			if (assignment != null) {
				parameterEvaluationType eval = parameterEvaluationType.NORMAL_EVAL;
				switch (assignment.getAssignmentType()) {
				case A_PAR_VAL:
				case A_PAR_VAL_IN:
				case A_PAR_TEMP_IN:
					eval = ((FormalParameter)assignment).getEvaluationType();
					break;
				default:
					break;
				}

				if (eval != parameterEvaluationType.NORMAL_EVAL) {
					final String refdString = assignment.getGenNameFromScope(aData, expression.expression, scope, "");
					if ((eval == parameterEvaluationType.LAZY_EVAL && !lazy) ||
							(eval == parameterEvaluationType.FUZZY_EVAL && lazy)) {
						expression.preamble.append(MessageFormat.format("{0}.change();\n", refdString));
						expression.postamble.append(MessageFormat.format("{0}.revert();\n", refdString));
					}

					expression.expression.append(refdString);

					return;
				}
			}
		}

		// generate the code for template in a temporary expr structure, this code is put inside the evaluate() member function
		aData.addBuiltinTypeImport("Lazy_Fuzzy_TemplateExpr");
		final ExpressionStruct templateExpression = new ExpressionStruct();
		generateCodeForTemplate(aData, templateExpression, template, genRestrictionCheck, scope);
		final String param_id = aData.getTemporaryVariableName();
		final String typeName = template.getTemplateBody().getMyGovernor().getGenNameTemplate(aData, expression.expression, scope);
		generateCodeParameterClass(aData, expression, templateExpression, param_id, typeName, false, lazy);

	}

	private static void generateCodeParameterClass(final JavaGenData aData, final ExpressionStruct expression, final ExpressionStruct paramExpression,
			final String param_id, final String typeName, final boolean isValue, final boolean lazy) {
		expression.preamble.append(MessageFormat.format("Lazy_Fuzzy_{0}Expr<{1}> {2} = new Lazy_Fuzzy_{0}Expr<{1}>({3}) '{'\n", isValue? "Value" : "Template", typeName, param_id, !lazy));
		for (int i = 0; i < typeVector.size(); i++) {
			expression.preamble.append(MessageFormat.format("private {0} {1} = {2};\n", typeVector.get(i), getMemberName(i), referedVector.get(i)));
		}

		expression.preamble.append("@Override\n");
		expression.preamble.append("protected void evaluate_expression() {\n");
		if (paramExpression.preamble.length() > 0) {
			expression.preamble.append(paramExpression.preamble);
		}
		expression.preamble.append(MessageFormat.format("expr_cache = {0};\n", paramExpression.expression));
		if (paramExpression.postamble.length() > 0) {
			expression.postamble.append(paramExpression.postamble);
		}

		expression.preamble.append("}\n");
		expression.preamble.append("};\n");
		expression.expression.append(param_id);
	}
}
