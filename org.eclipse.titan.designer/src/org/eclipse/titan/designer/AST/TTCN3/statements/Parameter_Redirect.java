/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.templates.DecodeMatch_template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents the parameter redirection of a getcall/getreply operation.
 *
 * @author Kristof Szabados
 * */
public abstract class Parameter_Redirect extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	protected static final String SIGNATUREWITHOUTPARAMETERS = "Parameter redirect cannot be used because signature `{0}'' does not have parameters";

	private Location location = NULL_Location.INSTANCE;

	/** the time when this was checked the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	@Override
	/** {@inheritDoc} */
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public final Location getLocation() {
		return location;
	}

	/**
	 * Sets the code_section attribute for the parameter redirection to the
	 * provided value.
	 *
	 * @param codeSection
	 *                the code section where these statements should be
	 *                generated.
	 * */
	public abstract void setCodeSection(final CodeSectionType codeSection);

	/**
	 * @return {@code true} if at least one of the value redirects has the
	 *         '@decoded' modifier
	 */
	public abstract boolean has_decoded_modifier();

	/**
	 * Does the semantic checking of the redirected parameter.
	 * <p>
	 * Does report errors, should only be called if there were errors found
	 * before.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public abstract void checkErroneous(final CompilationTimeStamp timestamp);

	/**
	 * Check whether the reference points to a variable of the provided
	 * type.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference to check
	 * @param type
	 *                the type the parameter is expected to have.
	 * */
	public final void checkVariableReference(final CompilationTimeStamp timestamp, final Reference reference, final IType type) {
		if (reference == null) {
			return;
		}

		final IType variableType = reference.checkVariableReference(timestamp);
		if (type != null && variableType != null && !type.isIdentical(timestamp, variableType)) {
			final String message = MessageFormat.format(
					"Type mismatch in parameter redirect: A variable of type `{0}'' was expected instead of `{1}''",
					type.getTypename(), variableType.getTypename());
			reference.getLocation().reportSemanticError(message);
			return;
		}

		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment != null) {
			switch (assignment.getAssignmentType()) {
			case A_PAR_VAL:
			case A_PAR_VAL_OUT:
			case A_PAR_VAL_INOUT:
				((FormalParameter) assignment).setWritten();
				break;
			case A_VAR:
				((Def_Var) assignment).setWritten();
				break;
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				((FormalParameter) assignment).setWritten();
				break;
			case A_VAR_TEMPLATE:
				((Def_Var_Template) assignment).setWritten();
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Does the semantic checking of the redirected parameter.
	 * <p>
	 * Does not report errors, that is done by check_erroneous.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param signature
	 *                the signature the parameter redirection belongs to.
	 * @param isOut
	 *                tells if this parameter is an out parameter, or not.
	 * */
	public abstract void check(CompilationTimeStamp timestamp, Signature_Type signature, boolean isOut);

	/**
	 * Add generated java code for parameter redirection.
	 * 
	 * @param aData
	 *                only used to update imports if needed
	 * @param expression
	 *                the expression for code generated
	 * @param matched_ti
	 *                the template instance matched by the original
	 *                statement.
	 * @param is_out
	 *                {@code true} if the parameters have out direction,
	 *                {@code false} otherwise.
	 */
	public abstract void generateCode( final JavaGenData aData, final ExpressionStruct expression, final TemplateInstance matched_ti, final String lastGenTIExpression, final boolean is_out);

	/**
	 * Internal version of the java code generation for parameter
	 * redirection.
	 * 
	 * @param aData
	 *                only used to update imports if needed
	 * @param expression
	 *                the expression for code generated
	 * @param entries
	 *                the variable entries to use for code generation.
	 * @param matched_ti
	 *                the template instance matched by the original
	 *                statement.
	 * @param is_out
	 *                {@code true} if the parameters have out direction,
	 *                {@code false} otherwise.
	 */
	protected void internalGenerateCode( final JavaGenData aData, final ExpressionStruct expression, final Variable_Entries entries, final TemplateInstance matched_ti, final String lastGenTIExpression, final boolean is_out) {
		if (has_decoded_modifier()) {
			expression.expression.append(MessageFormat.format("{0}, ", lastGenTIExpression));
		}

		for (int i = 0; i < entries.getNofEntries(); i++) {
			if (i > 0) {
				expression.expression.append(", ");
			}

			final Variable_Entry entry = entries.getEntryByIndex(i);
			Value stringEncoding = null;
			if (entry.isDecoded() && entry.getStringEncoding() != null && entry.getStringEncoding().isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
				stringEncoding = entry.getStringEncoding();
			}

			final Reference ref = entry.getReference();
			if (ref == null) {
				expression.expression.append("null");
				if (stringEncoding != null) {
					expression.expression.append(", TitanCharString()");
				}
			} else {
				ref.generateCode(aData, expression);
				if (stringEncoding != null) {
					expression.expression.append(", ");
					stringEncoding.generateCodeExpression(aData, expression, true);
				}
			}
		}
	}

	/**
	 * Generate a helper class that is needed for parameter redirections
	 * that also have at least one parameter redirection with decoding.
	 * 
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source to append.
	 * @param matched_ti
	 *                the template instance matched by the original
	 *                statement.
	 * @param tempID
	 *                the temporary id to be used for naming the class.
	 * @param is_out
	 *                {@code true} if the parameters have out direction,
	 *                {@code false} otherwise.
	 */
	public abstract void generateCodeDecoded(final JavaGenData aData, final StringBuilder source, final TemplateInstance matched_ti, final String tempID, final boolean is_out);

	/**
	 * Generate a helper class that is needed for parameter redirections
	 * that also have at least one parameter redirection with decoding.
	 * 
	 * @param aData
	 *                only used to update imports if needed
	 * @param source
	 *                the source to append.
	 * @param entries
	 *                the variable entries to use for code generation.
	 * @param matched_ti
	 *                the template instance matched by the original
	 *                statement.
	 * @param tempID
	 *                the temporary id to be used for naming the class.
	 * @param is_out
	 *                {@code true} if the parameters have out direction,
	 *                {@code false} otherwise.
	 */
	public void internalGenerateCodeDecoded(JavaGenData aData, StringBuilder source, final Variable_Entries entries, TemplateInstance matched_ti, String tempID, boolean is_out) {
		// TODO check to see how is different from the redirection's scope.
		Scope scope = null;
		for (int i = 0 ; i < entries.getNofEntries(); i++) {
			Reference reference = entries.getEntryByIndex(i).getReference();
			if (reference != null) {
				scope = reference.getMyScope();
				break;
			}
		}

		StringBuilder membersString = new StringBuilder();
		StringBuilder constructorParameters = new StringBuilder();
		StringBuilder baseConstructorParameters = new StringBuilder();
		StringBuilder constructorInitList = new StringBuilder();
		StringBuilder setParametersString = new StringBuilder();

		IType sigType = matched_ti.getTemplateBody().getMyGovernor().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		Type returnType = ((Signature_Type)sigType).getSignatureReturnType();
		if (returnType != null && is_out) {
			constructorParameters.append("Value_Redirect_Interface return_redirect, ");
			baseConstructorParameters.append("return_redirect");
		}

		membersString.append(MessageFormat.format("{0} ptr_matched_temp;\n", sigType.getGenNameTemplate(aData, source, scope)));
		constructorParameters.append(MessageFormat.format("{0} par_matched_temp", sigType.getGenNameTemplate(aData, source, scope)));
		constructorInitList.append("ptr_matched_temp = par_matched_temp;\n");

		SignatureFormalParameterList parList = ((Signature_Type)sigType).getParameterList();
		for (int i = 0 ; i < entries.getNofEntries(); i++) {
			final Variable_Entry variableEntry = entries.getEntryByIndex(i);

			SignatureFormalParameter parameter = is_out ? parList.getOutParameterByIndex(i) : parList.getInParameterByIndex(i);
			String parameterName = parameter.getIdentifier().getName();
			if (constructorParameters.length() > 0) {
				constructorParameters.append(", ");
			}
			if (baseConstructorParameters.length() > 0) {
				baseConstructorParameters.append(", ");
			}

			if (variableEntry.isDecoded()) {
				// TODO extract common parts, in the compiler too.
				membersString.append(MessageFormat.format("private {0} ptr_{1}_dec;\n", variableEntry.getDeclarationType().getGenNameValue(aData, source, scope), parameterName));
				constructorParameters.append(MessageFormat.format("{0} par_{1}_dec", variableEntry.getDeclarationType().getGenNameValue(aData, source, scope), parameterName));
				baseConstructorParameters.append("null");
				setParametersString.append(MessageFormat.format("if (ptr_{0}_dec != null) '{'\n", parameterName));

				NamedTemplate matchedNamedTemplate = null;
				if (matched_ti.getTemplateBody().getTemplatetype() == Template_type.NAMED_TEMPLATE_LIST) {
					matchedNamedTemplate = ((Named_Template_List)matched_ti.getTemplateBody()).getNamedTemplate(parameter.getIdentifier());
				}

				ITTCN3Template matchedTemplate = null;
				if (matchedNamedTemplate != null) {
					matchedTemplate = matchedNamedTemplate.getTemplate().getTemplateReferencedLast(CompilationTimeStamp.getBaseTimestamp());
				}

				boolean useDecmatchResult = matchedTemplate != null && matchedTemplate.getTemplatetype() == Template_type.DECODE_MATCH;
				boolean needsDecode = true;
				if (parameter.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetypeTtcn3() == Type_type.TYPE_UCHARSTRING) {
					//FIXME implement
					setParametersString.append("//FIXME decoded parameter redirection for universal charstrings are not yet supported\n");
				}
				if (useDecmatchResult) {
					// if the redirected parameter was matched using a decmatch template,
					// then the parameter redirect class should use the decoding result 
					// from the template instead of decoding the parameter again
					needsDecode = false;
					IType decmatchType = ((DecodeMatch_template)matchedTemplate).getDecodeTarget().getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE).getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
					if (variableEntry.getDeclarationType() != decmatchType) {
						// the decmatch template and this value redirect decode two
						// different types, so just decode the value
						needsDecode = true;
						useDecmatchResult = false;
					} else if (parameter.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetypeTtcn3() == Type_type.TYPE_UCHARSTRING) {
						//FIXME implement
						setParametersString.append("//FIXME usedecmatch1 not yet supported\n");
					}
				} else {
					setParametersString.append("//FIXME usedecmatch2 not yet supported\n");
				}
				if (useDecmatchResult) {
					setParametersString.append(MessageFormat.format("ptr_{0}_dec.operator_assign(({1})ptr_matched_temp.constGet_field_{2}().get_decmatch_dec_res());\n", parameterName, variableEntry.getDeclarationType().getGenNameValue(aData, setParametersString, scope), parameterName));
				}
				if (needsDecode) {
					setParametersString.append("//FIXME needs decode part of decoded parameter redirection are not yet supported\n");
					//FIXME implement
				}
				//FIXME implement
				setParametersString.append("//FIXME decoded parameter redirection not yet supported.\n");
				setParametersString.append("}\n");
			} else {
				constructorParameters.append(MessageFormat.format("{0} par_{1}", parameter.getType().getGenNameValue(aData, source, scope), parameterName));
				baseConstructorParameters.append(MessageFormat.format("par_{0}", parameterName));
			}
		}

		//
		final String qualifiedSignatureName = sigType.getGenNameValue(aData, source, scope);
		//TODO sigType is already a refdlast type.
		final String unqualifiedSignatureName = sigType.getGenNameValue(aData, source, sigType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getMyScope());
		final String opName = is_out ? "reply" : "call";
		source.append(MessageFormat.format("class {0}_{1}_redirect_{2} extends {3}_{1}_redirect '{'\n", unqualifiedSignatureName, opName, tempID, qualifiedSignatureName));
		source.append(membersString);
		source.append(MessageFormat.format("public {0}_{1}_redirect_{2}({3}) '{'\n", unqualifiedSignatureName, opName, tempID, constructorParameters));
		source.append(MessageFormat.format("super({0});\n", baseConstructorParameters));
		source.append(constructorInitList);
		source.append("};\n");
		source.append(MessageFormat.format("public void set_parameters({0}_{1} par) '{'\n", qualifiedSignatureName, opName));
		source.append(setParametersString);
		source.append("super.set_parameters(par);\n");
		source.append("};\n");
		source.append("};\n");
	}
}
