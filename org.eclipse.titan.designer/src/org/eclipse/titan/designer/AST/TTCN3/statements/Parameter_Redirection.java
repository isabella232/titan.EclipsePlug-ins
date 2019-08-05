/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.IValue;
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
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents the parameter redirection of a getcall/getreply operation.
 *
 * @author Kristof Szabados
 * */
public abstract class Parameter_Redirection extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
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
	public abstract boolean hasDecodedModifier();

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
		if (hasDecodedModifier()) {
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
	public void internalGenerateCodeDecoded(final JavaGenData aData, final StringBuilder source, final Variable_Entries entries, final TemplateInstance matched_ti, final String tempID, final boolean is_out) {
		// does know about its own scope
		final Scope scope = getMyScope();

		final StringBuilder membersString = new StringBuilder();
		final StringBuilder constructorParameters = new StringBuilder();
		final StringBuilder baseConstructorParameters = new StringBuilder();
		final StringBuilder constructorInitList = new StringBuilder();
		final StringBuilder setParametersString = new StringBuilder();

		final IType sigType = matched_ti.getTemplateBody().getMyGovernor().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		final Type returnType = ((Signature_Type)sigType).getSignatureReturnType();
		if (returnType != null && is_out) {
			constructorParameters.append("Value_Redirect_Interface return_redirect, ");
			baseConstructorParameters.append("return_redirect");
		}

		final String sigTypeGenTempName = sigType.getGenNameTemplate(aData, source);
		membersString.append(MessageFormat.format("{0} ptr_matched_temp;\n", sigTypeGenTempName));
		constructorParameters.append(MessageFormat.format("{0} par_matched_temp", sigTypeGenTempName));
		constructorInitList.append("ptr_matched_temp = par_matched_temp;\n");

		final SignatureFormalParameterList parList = ((Signature_Type)sigType).getParameterList();
		for (int i = 0 ; i < entries.getNofEntries(); i++) {
			final Variable_Entry variableEntry = entries.getEntryByIndex(i);

			final SignatureFormalParameter parameter = is_out ? parList.getOutParameterByIndex(i) : parList.getInParameterByIndex(i);
			final String parameterName = parameter.getIdentifier().getName();
			if (constructorParameters.length() > 0) {
				constructorParameters.append(", ");
			}
			if (baseConstructorParameters.length() > 0) {
				baseConstructorParameters.append(", ");
			}

			if (variableEntry.isDecoded()) {
				final IType declarationType = variableEntry.getDeclarationType();
				final String veGenName = declarationType.getGenNameValue(aData, source);
				membersString.append(MessageFormat.format("private {0} ptr_{1}_dec;\n", veGenName, parameterName));
				constructorParameters.append(MessageFormat.format("{0} par_{1}_dec", veGenName, parameterName));
				baseConstructorParameters.append("null");
				setParametersString.append(MessageFormat.format("if (ptr_{0}_dec != null) '{'\n", parameterName));

				final TTCN3Template lastMatchedTemplate = matched_ti.getTemplateBody().getTemplateReferencedLast(CompilationTimeStamp.getBaseTimestamp());
				NamedTemplate matchedNamedTemplate = null;
				if (lastMatchedTemplate.getTemplatetype() == Template_type.NAMED_TEMPLATE_LIST) {
					matchedNamedTemplate = ((Named_Template_List)lastMatchedTemplate).getNamedTemplate(parameter.getIdentifier());
				}

				ITTCN3Template matchedTemplate = null;
				if (matchedNamedTemplate != null) {
					matchedTemplate = matchedNamedTemplate.getTemplate().getTemplateReferencedLast(CompilationTimeStamp.getBaseTimestamp());
				}

				boolean useDecmatchResult = matchedTemplate != null && matchedTemplate.getTemplatetype() == Template_type.DECODE_MATCH;
				boolean needsDecode = true;
				final ExpressionStruct redirCodingExpression = new ExpressionStruct();
				final IType paramLastType = parameter.getType().getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				if (paramLastType.getTypetypeTtcn3() == Type_type.TYPE_UCHARSTRING) {
					aData.addBuiltinTypeImport("TitanCharString.CharCoding");

					IValue temp = (IValue)variableEntry.getStringEncoding();
					if (temp != null) {
						temp = temp.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
					}
					if (temp == null || !temp.isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
						final Charstring_Value stringEncoding = (Charstring_Value)temp;
						String redirCodingString;
						if (stringEncoding == null || "UTF-8".equals(stringEncoding.getValue())) {
							redirCodingString = "UTF_8";
						} else if ("UTF-16".equals(stringEncoding.getValue()) || "UTF-16BE".equals(stringEncoding.getValue())) {
							redirCodingString = "UTF16BE";
						} else if ("UTF-16LE".equals(stringEncoding.getValue())) {
							redirCodingString = "UTF16LE";
						} else if ("UTF-32LE".equals(stringEncoding.getValue())) {
							redirCodingString = "UTF32LE";
						} else {
							redirCodingString = "UTF32BE";
						}
						redirCodingExpression.expression.append(MessageFormat.format("CharCoding.{0}", redirCodingString));
					} else {
						redirCodingExpression.preamble.append(MessageFormat.format("CharCoding coding = TitanUniversalCharString.get_character_coding(enc_fmt_{0}.get_value().toString(), \"decoded parameter redirect\");\n", parameterName));
						redirCodingExpression.expression.append("coding");
					}
				}
				if (useDecmatchResult) {
					// if the redirected parameter was matched using a decmatch template,
					// then the parameter redirect class should use the decoding result
					// from the template instead of decoding the parameter again
					needsDecode = false;
					final TemplateInstance decodeTarget = ((DecodeMatch_template)matchedTemplate).getDecodeTarget();
					final IType targetGovernor = decodeTarget.getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
					final IType decmatchType = targetGovernor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
					if (declarationType != decmatchType) {
						// the decmatch template and this value redirect decode two
						// different types, so just decode the value
						needsDecode = true;
						useDecmatchResult = false;
					} else if (paramLastType.getTypetypeTtcn3() == Type_type.TYPE_UCHARSTRING) {
						// for universal charstrings the situation could be trickier
						// compare the string encodings
						boolean differentUstrEncoding = false;
						boolean unkonwnUstrEncodings = false;
						if (variableEntry.getStringEncoding() == null) {
							if (((DecodeMatch_template)matchedTemplate).getStringEncoding() != null) {
								final Value temp = ((DecodeMatch_template)matchedTemplate).getStringEncoding();
								if (temp.isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
									unkonwnUstrEncodings = true;
								} else {
									final Charstring_Value stringEncoding = (Charstring_Value)temp.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
									if (!"UTF-8".equals(stringEncoding.getValue())) {
										differentUstrEncoding = true;
									}
								}
							}
						} else if (variableEntry.getStringEncoding().isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
							unkonwnUstrEncodings = true;
						} else if (((DecodeMatch_template)matchedTemplate).getStringEncoding() == null) {
							final IValue temp = variableEntry.getStringEncoding().getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
							if ("UTF-8".equals(((Charstring_Value)temp).getValue())) {
								differentUstrEncoding = true;
							}
						} else if (((DecodeMatch_template)matchedTemplate).getStringEncoding().isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
							unkonwnUstrEncodings = true;
						} else {
							final IValue redirectionTemp = variableEntry.getStringEncoding().getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
							final Value templateTemp = ((DecodeMatch_template)matchedTemplate).getStringEncoding();
							final Charstring_Value tempStringEncoding = (Charstring_Value)templateTemp.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
							if (!((Charstring_Value)redirectionTemp).getValue().equals(tempStringEncoding.getValue())) {
								differentUstrEncoding = true;
							}
						}

						if (unkonwnUstrEncodings) {
							// the decision of whether to use the decmatch result or to decode
							// the value is made at runtime
							needsDecode = true;
							setParametersString.append(MessageFormat.format("{0}if ( {1} == ptr_matched_temp.constGet_field_{2}().get_decmatch_str_enc()) '{'\n", redirCodingExpression.preamble, redirCodingExpression.expression, parameterName));
						} else if (differentUstrEncoding) {
							// if the encodings are different, then ignore the decmatch result
							// and just generate the decoding code as usual
							needsDecode = true;
							useDecmatchResult = false;
						}
					}
				} else {
					// it might still be a decmatch template if it's not known at compile-time
					boolean unfoldable = matchedTemplate == null;
					if (!unfoldable) {
						switch (matchedTemplate.getTemplatetype()) {
						case ANY_VALUE:
						case ANY_OR_OMIT:
						case BSTR_PATTERN:
						case CSTR_PATTERN:
						case HSTR_PATTERN:
						case OSTR_PATTERN:
						case USTR_PATTERN:
						case COMPLEMENTED_LIST:
						case VALUE_LIST:
						case VALUE_RANGE:
							// it's known at compile-time, and not a decmatch template
							break;
						default:
							// needs runtime check
							unfoldable = true;
							break;
						}
					}

					if (unfoldable) {
						// the decmatch-check must be done at runtime
						useDecmatchResult = true;
						if (redirCodingExpression.preamble.length() > 0) {
							setParametersString.append(redirCodingExpression.preamble);
						}

						final String typeDescriptorName = declarationType.getGenNameTypeDescriptor(aData, setParametersString);
						setParametersString.append(MessageFormat.format("if (ptr_matched_temp.constGet_field_{0}().get_selection() == template_sel.DECODE_MATCH && {1}_descr_ == ptr_matched_temp.constGet_field_{0}().get_decmatch_type_descr()", parameterName, typeDescriptorName));
						if (redirCodingExpression.expression.length() > 0) {
							setParametersString.append(MessageFormat.format("&& {0} == ptr_matched_temp.constGet_field_{1}().get_decmatch_str_enc()", redirCodingExpression.expression, parameterName));
						}
						setParametersString.append(") {\n");
					}
				}
				if (useDecmatchResult) {
					setParametersString.append(MessageFormat.format("ptr_{0}_dec.operator_assign(({1})ptr_matched_temp.constGet_field_{2}().get_decmatch_dec_res());\n", parameterName, declarationType.getGenNameValue(aData, setParametersString), parameterName));
				}
				if (needsDecode) {
					if (useDecmatchResult) {
						setParametersString.append("} else {\n");
					}

					//legacy encoding does not need to be supported
					aData.addBuiltinTypeImport("TitanOctetString");
					aData.addBuiltinTypeImport("AdditionalFunctions");

					setParametersString.append("TitanOctetString buff = new TitanOctetString(");
					final Type_type tt = paramLastType.getTypetypeTtcn3();
					switch(tt) {
					case TYPE_BITSTRING:
						setParametersString.append(MessageFormat.format("AdditionalFunctions.bit2oct(par.constGet_field_{0}())", parameterName));
						break;
					case TYPE_HEXSTRING:
						setParametersString.append(MessageFormat.format("AdditionalFunctions.hex2oct(par.constGet_field_{0}())", parameterName));
						break;
					case TYPE_OCTETSTRING:
						setParametersString.append(MessageFormat.format("par.{0}()", parameterName));
						break;
					case TYPE_CHARSTRING:
						setParametersString.append(MessageFormat.format("AdditionalFunctions.char2oct(par.constGet_field_{0}())", parameterName));
						break;
					case TYPE_UCHARSTRING:
						setParametersString.append(MessageFormat.format("AdditionalFunctions.unichar2oct(par.constGet_field_{0}(), ", parameterName));
						if (variableEntry.getStringEncoding() == null) {
							setParametersString.append("\"UTF-8\"");
						} else if (!variableEntry.getStringEncoding().isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
							IValue temp = variableEntry.getStringEncoding();
							temp = temp.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
							setParametersString.append(MessageFormat.format("\"{0}\"", ((Charstring_Value)temp).getValue()));
						} else {
							// the encoding format is not known at compile-time, so an extra
							// member and constructor parameter is needed to store it
							membersString.append(MessageFormat.format("TitanCharString enc_fmt_{0};\n", parameterName));
							constructorParameters.append(MessageFormat.format(", TitanCharString par_fmt_{0}", parameterName));
							constructorInitList.append(MessageFormat.format("enc_fmt_{0} = par_fmt_{0};\n", parameterName));
							setParametersString.append(MessageFormat.format("enc_fmt_{0}", parameterName));
						}
						setParametersString.append(')');
						break;
					default:
						//FATAL ERROR
						break;
					}

					setParametersString.append(");\n");

					final String coderName = variableEntry.getDeclarationType().getGenNameCoder(aData, setParametersString, scope);
					final String codingName = declarationType.getGenNameDefaultCoding(aData, setParametersString, scope);
					setParametersString.append(MessageFormat.format("if ({0}_decoder(buff, ptr_{1}_dec, {2}_default_coding).operator_not_equals(0)) '{'\n", coderName, parameterName, codingName));
					setParametersString.append(MessageFormat.format("throw new TtcnError(\"Decoding failed in parameter (for parameter `{0}'').\");\n", parameterName));
					setParametersString.append("}\n");
					setParametersString.append("if (buff.lengthof().operator_not_equals(0)) {\n");
					setParametersString.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Parameter redirect (for parameter `{0}'' failed, because the buffer was not empty after decoding. Remaining octets: '{'0'}'\", buff.lengthof().get_int()));\n", parameterName));
					setParametersString.append("}\n");

					if (useDecmatchResult) {
						setParametersString.append("}\n");
					}
				}

				setParametersString.append("}\n");
			} else {
				constructorParameters.append(MessageFormat.format("{0} par_{1}", parameter.getType().getGenNameValue(aData, source), parameterName));
				baseConstructorParameters.append(MessageFormat.format("par_{0}", parameterName));
			}
		}

		final String qualifiedSignatureName = sigType.getGenNameValue(aData, source);
		final String unqualifiedSignatureName = sigType.getGenNameValue(aData, source);
		final String opName = is_out ? "reply" : "call";
		source.append(MessageFormat.format("class {0}_{1}_redirect_{2} extends {3}_{1}_redirect '{'\n", unqualifiedSignatureName, opName, tempID, qualifiedSignatureName));
		source.append(membersString);
		source.append(MessageFormat.format("public {0}_{1}_redirect_{2}({3}) '{'\n", unqualifiedSignatureName, opName, tempID, constructorParameters));
		source.append(MessageFormat.format("super({0});\n", baseConstructorParameters));
		source.append(constructorInitList);
		source.append("}\n");
		source.append(MessageFormat.format("public void set_parameters({0}_{1} par) '{'\n", qualifiedSignatureName, opName));
		source.append(setParametersString);
		source.append("super.set_parameters(par);\n");
		source.append("}\n");
		source.append("};\n");
	}
}
