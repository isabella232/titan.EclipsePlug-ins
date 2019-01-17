/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.TypeOwner_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.templates.DecodeMatch_template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.Verdict_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the value redirection of several operations (done, port check,
 * check-catch, check-getreply, check-receive).
 *
 * @author Kristof Szabados
 * */
public class Value_Redirection extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	final private ArrayList<Single_ValueRedirection> valueRedirections;

	// pointer to the type of the redirected value, not owned here
	private IType valueType = null;

	/**
	 * Indicates whether the value redirect is restricted to only one value of
	 * type 'verdicttype' */
	private boolean verdictOnly = false;

	private Location location = NULL_Location.INSTANCE;

	/** the time when this was checked the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	/**
	 * Constructs the value style redirection with noe redirections by
	 * default.
	 * */
	public Value_Redirection() {
		valueRedirections = new ArrayList<Single_ValueRedirection>();
	}

	/**
	 * Adds a single value redirection to the list of redirections managed
	 * here.
	 *
	 * @param single_ValueRedirect
	 *                the redirection to add.
	 * */
	public void add(final Single_ValueRedirection single_ValueRedirect){
		if (single_ValueRedirect != null) {
			single_ValueRedirect.setFullNameParent(this);

			valueRedirections.add(single_ValueRedirect);
		}
	}

	/**
	 * @return {@code true} if at least one of the value redirects has the
	 * '@decoded' modifier
	 */
	public boolean hasDecodedModifier() {
		for (final Single_ValueRedirection redirect : valueRedirections) {
			if (redirect.isDecoded()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Attempts to identify the type of the redirected value. Only those
	 * single redirects are checked, which redirect the whole value, not
	 * just a field. If multiple whole-value-redirects of separate types are
	 * found, then an error is displayed.
	 *
	 * @return the found type, if any.
	 */
	public IType getType(final CompilationTimeStamp timestamp) {
		IType returnValue = null;
		for (int i = 0; i < valueRedirections.size(); i++) {
			final Single_ValueRedirection redirect = valueRedirections.get(i);

			if (redirect.getSubreferences() == null) {
				final IType variableType = redirect.getVariableReference().checkVariableReference(timestamp);
				if (variableType != null) {
					if (returnValue == null) {
						returnValue = variableType;
					} else {
						if (!returnValue.isIdentical(timestamp, variableType)) {
							getLocation().reportSemanticError("The variable references the whole value is redirected to should be of the same type");

							return null;
						}
					}
				}
			}
		}

		return valueType;
	}

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

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < valueRedirections.size(); i++) {
			final Single_ValueRedirection redirect = valueRedirections.get(i);

			if (redirect == child) {
				return builder.append(".redirect_").append(i + 1);
			}
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (final Single_ValueRedirection redirect : valueRedirections) {
			redirect.setMyScope(scope);
		}
	}

	/**
	 * Sets the code_section attribute for the statements in this parameter assignment to the provided value.
	 *
	 * @param codeSection the code section where these statements should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		for (final Single_ValueRedirection redirect : valueRedirections) {
			redirect.getVariableReference().setCodeSection(codeSection);;
		}
	}

	/**
	 * Checks this value redirection construct, according to the provided
	 * type. This type needs to be provided by the statement this
	 * redirection is attached to, as it needs to be the type of the return
	 * value of the used expression.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param type
	 *                the type to check the value redirection against (for
	 *                example the type of the received value in case of a
	 *                receive statement).
	 * */
	public void check(final CompilationTimeStamp timestamp, final IType type) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (verdictOnly) {
			lastTimeChecked = timestamp;

			return;
		}

		boolean invalidType = type.getIsErroneous(timestamp);
		if (!invalidType) {
			final Type_type tt = type.getTypeRefdLast(timestamp).getTypetypeTtcn3();
			if (tt != Type_type.TYPE_TTCN3_SEQUENCE && tt != Type_type.TYPE_TTCN3_SET) {
				for (int i = 0; i < valueRedirections.size(); i++) {
					final Single_ValueRedirection redirect = valueRedirections.get(i);
					if (redirect.getSubreferences() != null) {
						invalidType = true;
						redirect.getLocation().reportSemanticError(MessageFormat.format("Cannot redirect fields of type `{0}'', because it is not a record or set", type.getTypename()));
					}
				}
			}
		}

		if (invalidType) {
			checkErroneous(timestamp);
			lastTimeChecked = timestamp;

			return;
		}

		valueType = type.getTypeRefdLast(timestamp);
		for (int i = 0; i < valueRedirections.size(); i++) {
			final Single_ValueRedirection redirect = valueRedirections.get(i);

			final Reference variableReference = redirect.getVariableReference();
			final IType varType = variableReference.checkVariableReference(timestamp);
			final ArrayList<ISubReference> subreferences = redirect.getSubreferences();
			IType expectedType = null;
			if (subreferences == null) {
				// the whole value is redirected to the referenced variable
				expectedType = type;
			} else {
				// a field of the value is redirected to the referenced variable
				final Reference reference = new Reference(null);
				//first field is only used to not have a single element subreference list.
				reference.addSubReference(new FieldSubReference(variableReference.getId()));
				for (int j = 0; j < subreferences.size(); j++) {
					reference.addSubReference(subreferences.get(j));
				}

				final IType fieldType = type.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				if (fieldType != null) {
					if (redirect.isDecoded()) {
						final Value stringEncoding = redirect.getStringEncoding();
						boolean isErroneous = false;
						final IType refdLast = fieldType.getTypeRefdLast(timestamp);
						switch (refdLast.getTypetypeTtcn3()) {
						case TYPE_BITSTRING:
						case TYPE_HEXSTRING:
						case TYPE_OCTETSTRING:
						case TYPE_CHARSTRING:
							if (stringEncoding != null) {
								stringEncoding.getLocation().reportSemanticError("The encoding format parameter for the '@decoded' modifier is only available to value redirects of universal charstrings");
								isErroneous = true;
							}
							break;
						case TYPE_UCHARSTRING:
							if (stringEncoding != null) {
								stringEncoding.checkStringEncoding(timestamp, null);
							}
							break;
						default:
							redirect.getLocation().reportSemanticError("The '@decoded' modifier is only available to value redirects of string types.");
							isErroneous = true;
							break;
						}

						if (!isErroneous && varType != null) {
							// store the variable type in case it's decoded (since this cannot
							// be extracted from the value type with the sub-references)
							final IType declarationType = varType.getTypeRefdLast(timestamp);
							redirect.setDeclarationType(declarationType);
							declarationType.checkCoding(timestamp, false, variableReference.getMyScope().getModuleScope(), false);
						}
					} else {
						expectedType = fieldType;
					}
				}
			}

			if (expectedType != null && varType != null) {
				//TODO support for type compatibility
				if (!varType.isIdentical(timestamp, expectedType)) {
					redirect.getLocation().reportSemanticError(MessageFormat.format("Type mismatch in value redirect: A variable of type `{0}'' was expected instead of `{1}''", expectedType.getTypename(), varType.getTypename()));
				}
			}
		}

		lastTimeChecked = timestamp;
	}

	/**
	 * Special checking that is only called once the value redirection was
	 * already found to be erroneous. Tries to check the remaining parts
	 * that can be checked on their own.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * */
	public void checkErroneous(final CompilationTimeStamp timestamp) {
		for (int i = 0; i < valueRedirections.size(); i++) {
			final Single_ValueRedirection redirect = valueRedirections.get(i);

			redirect.getVariableReference().checkVariableReference(timestamp);
			final Value stringEncoding = redirect.getStringEncoding();
			if (stringEncoding != null) {
				stringEncoding.checkStringEncoding(timestamp, null);
			}
		}
	}

	/**
	 * A special version of the check functionality used by the done
	 * statement. There only verdict types can be received.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param type
	 *                the type to check the value redirection against (for
	 *                example the type of the received value in case of a
	 *                receive statement).
	 * */
	public void checkVerdictOnly(final CompilationTimeStamp timestamp) {
		verdictOnly = true;
		check(timestamp, new Verdict_Type());
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for(final Single_ValueRedirection redirect : valueRedirections) {
			if (redirect != null) {
				redirect.updateSyntax(reparser, false);
				reparser.updateLocation(redirect.getLocation());
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (valueRedirections == null) {
			return;
		}

		for (final Single_ValueRedirection redirect : valueRedirections) {
			redirect.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}

		if (valueRedirections != null) {
			for (final Single_ValueRedirection redirect : valueRedirections) {
				if (!redirect.accept(v)) {
					return false;
				}
			}
		}

		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}

		return true;
	}

	/**
	 * Generate the code for the value redirection handling.
	 * In case of a done statement this is only a verdict reference.
	 * In other cases a temporary class to handle the complications.
	 * 
	 * @param aData
	 *                only used to update imports if needed
	 * @param expression
	 *                the expression to append.
	 * @param matchedTi
	 *                the template instance matched by the original
	 *                statement.
	 * @param lastGenTIExpression
	 *                the string last generated for the provided matchedTi,
	 *                so that it does not need to be generated again.
	 */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression, final TemplateInstance matchedTi, final String lastGenTIExpression ) {
		if (verdictOnly) {
			//verdict only case
			if (valueRedirections.size() == 1) {
				valueRedirections.get(0).getVariableReference().generateCode(aData, expression);
			}
		} else {
			aData.addBuiltinTypeImport("Value_Redirect_Interface");

			final Scope scope = valueRedirections.get(0).getVariableReference().getMyScope();
			final StringBuilder membersString = new StringBuilder();
			final StringBuilder constructorParameters = new StringBuilder();
			final StringBuilder constructorInitializers = new StringBuilder();
			final StringBuilder instanceParameterList = new StringBuilder();
			final StringBuilder setValuesString = new StringBuilder();

			if (matchedTi != null && hasDecodedModifier()) {
				// store a pointer to the matched template, the decoding results from
				// decmatch templates might be reused to optimize decoded value redirects
				instanceParameterList.append(MessageFormat.format("{0}, ", lastGenTIExpression));
				final String templateName = valueType.getGenNameTemplate(aData, expression.expression, getMyScope());
				membersString.append(MessageFormat.format("{0} ptr_matched_temp;\n", templateName));
				constructorParameters.append(MessageFormat.format("{0} par_matched_temp, ", templateName));
				constructorInitializers.append("ptr_matched_temp = par_matched_temp;\n");
			}

			boolean needPar = false;
			for (int i = 0 ; i < valueRedirections.size(); i++) {
				if (i > 0) {
					constructorParameters.append(", ");
					instanceParameterList.append(", ");
				}

				final Single_ValueRedirection redirection = valueRedirections.get(i);
				final ExpressionStruct variableReferenceExpression = new ExpressionStruct();
				final Reference variableReference = redirection.getVariableReference();
				variableReference.generateCode(aData, variableReferenceExpression);
				instanceParameterList.append(variableReferenceExpression.expression);
				if (variableReferenceExpression.preamble != null) {
					expression.preamble.append(variableReferenceExpression.preamble);
				}
				if (variableReferenceExpression.postamble != null) {
					expression.postamble.append(variableReferenceExpression.postamble);
				}

				IType redirectionType;
				if (redirection.getSubreferences() == null) {
					redirectionType = valueType;
				} else {
					final ArrayList<ISubReference> subreferences = redirection.getSubreferences();
					final Reference reference = new Reference(null);
					//first field is only used to not have a single element subreference list.
					reference.addSubReference(new FieldSubReference(variableReference.getId()));
					for (int j = 0; j < subreferences.size(); j++) {
						reference.addSubReference(subreferences.get(j));
					}
					redirectionType = valueType.getFieldType(CompilationTimeStamp.getBaseTimestamp(), reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
				}

				redirectionType = redirectionType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				//TODO not a good idea to do checks during code generation.
				IType referenceType = variableReference.checkVariableReference(CompilationTimeStamp.getBaseTimestamp());
				referenceType = referenceType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				final IType memberType = redirection.isDecoded() ? redirection.getDeclarationType() : referenceType;
				final String typeName = memberType.getGenNameValue(aData, expression.expression, myScope);
				membersString.append(MessageFormat.format("{0} ptr_{1};\n", typeName, i));
				constructorParameters.append(MessageFormat.format("{0} par_{1}", typeName, i));
				constructorInitializers.append(MessageFormat.format("ptr_{0} = par_{0};\n", i));

				final ExpressionStruct subrefExpression = new ExpressionStruct();
				String optionalSuffix = "";
				if (redirection.getSubreferences() != null) {
					final ArrayList<ISubReference> subreferences = redirection.getSubreferences();
					Reference.generateCode(aData, subrefExpression, subreferences, 0, false, true, valueType);

					if (redirectionType.getOwnertype() == TypeOwner_type.OT_COMP_FIELD) {
						final CompField cf = (CompField)redirectionType.getOwner();
						if (cf.isOptional()) {
							optionalSuffix = ".get()";
						}
					}
				}

				if (subrefExpression.preamble.length() > 0) {
					setValuesString.append(subrefExpression.preamble);
				}
				final String subrefsString = subrefExpression.expression.length() > 0 ? subrefExpression.expression.toString() : "";
				if (redirection.isDecoded()) {
					ITTCN3Template matchedTemplate = null;
					if (matchedTi != null) {
						final ArrayList<ISubReference> subreferences = redirection.getSubreferences();
						final Reference reference = new Reference(null);
						//first field is only used to not have a single element subreference list.
						reference.addSubReference(new FieldSubReference(variableReference.getId()));
						for (int j = 0; j < subreferences.size(); j++) {
							reference.addSubReference(subreferences.get(j));
						}
						matchedTemplate = matchedTi.getTemplateBody().getReferencedSubTemplate(CompilationTimeStamp.getBaseTimestamp(), reference, null, true);
					}
					if (matchedTemplate != null) {
						matchedTemplate = matchedTemplate.getTemplateReferencedLast(CompilationTimeStamp.getBaseTimestamp());
					}
	
					boolean useDecmatchResult = matchedTemplate != null && matchedTemplate.getTemplatetype() == Template_type.DECODE_MATCH;
					boolean needsDecode = true;
					final ExpressionStruct redirCodingExpression = new ExpressionStruct();
					if (redirectionType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetypeTtcn3() == Type_type.TYPE_UCHARSTRING) {
						aData.addBuiltinTypeImport("TitanCharString.CharCoding");

						IValue temp = (IValue)redirection.getStringEncoding();
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
							redirCodingExpression.preamble.append(MessageFormat.format("CharCoding coding = TitanUniversalCharString.get_character_coding(enc_fmt_{0}.get_value().toString(), \"decoded parameter redirect\");\n", i));
							redirCodingExpression.expression.append("coding");
						}
					}

					if (useDecmatchResult) {
						// if the redirected value was matched using a decmatch template,
						// then the value redirect class should use the decoding result 
						// from the template instead of decoding the value again
						needsDecode = false;
						final IType decmatchType = ((DecodeMatch_template)matchedTemplate).getDecodeTarget().getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE).getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
						if (redirection.getDeclarationType() != decmatchType) {
							// the decmatch template and this value redirect decode two
							// different types, so just decode the value
							needsDecode = true;
							useDecmatchResult = false;
						} else if (redirectionType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetypeTtcn3() == Type_type.TYPE_UCHARSTRING) {
							// for universal charstrings the situation could be trickier
							// compare the string encodings
							boolean differentUstrEncoding = false;
							boolean unkonwnUstrEncodings = false;
							if (redirection.getStringEncoding() == null) {
								final Value tempStringEncoding = ((DecodeMatch_template)matchedTemplate).getStringEncoding();
								if (tempStringEncoding != null) {
									if (tempStringEncoding.isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
										unkonwnUstrEncodings = true;
									} else {
										final Charstring_Value stringEncoding = (Charstring_Value)tempStringEncoding.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
										if (!"UTF-8".equals(stringEncoding.getValue())) {
											differentUstrEncoding = true;
										}
									}
								}
							} else if (redirection.getStringEncoding().isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
								unkonwnUstrEncodings = true;
							} else if (((DecodeMatch_template)matchedTemplate).getStringEncoding() == null) {
								final IValue temp = redirection.getStringEncoding().getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
								if ("UTF-8".equals(((Charstring_Value)temp).getValue())) {
									differentUstrEncoding = true;
								}
							} else if (((DecodeMatch_template)matchedTemplate).getStringEncoding().isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
								unkonwnUstrEncodings = true;
							} else {
								final IValue redirectionTemp = redirection.getStringEncoding().getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
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
								setValuesString.append(MessageFormat.format("{0}if ( {1} == ptr_matched_temp{2}.get_decmatch_str_enc()) '{'\n", redirCodingExpression.preamble, redirCodingExpression.expression, subrefsString));
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

						if (unfoldable && matchedTi != null) {
							// the decmatch-check must be done at runtime
							useDecmatchResult = true;
							if (redirCodingExpression.preamble.length() > 0) {
								setValuesString.append(redirCodingExpression.preamble);
							}

							setValuesString.append("if (ptr_matched_temp.get_selection() == template_sel.SPECIFIC_VALUE && ");
							// go through the already generated subreference string, append
							// one reference at a time, and check if the referenced template
							// is a specific value
							final StringBuilder currentRef = new StringBuilder("ptr_matched_temp");
							final int length = subrefsString.length();
							int start = 0;
							for (int j = 0; j < length; j++) {
								if (subrefsString.charAt(j) == '.' || subrefsString.charAt(j) == '[') {
									currentRef.append(subrefsString.substring(start, j));
									setValuesString.append(MessageFormat.format("{0}.get_selection() == template_sel.SPECIFIC_VALUE && ", currentRef));
									start = j;
								}
							}

							setValuesString.append(MessageFormat.format("ptr_matched_temp{0}.get_selection() == template_sel.DECODE_MATCH && ", subrefsString));
							setValuesString.append(MessageFormat.format("{0}_descr_ == ptr_matched_temp{1}.get_decmatch_type_descr()", redirection.getDeclarationType().getGenNameTypeDescriptor(aData, setValuesString, scope), subrefsString));
							if (redirCodingExpression.expression.length() > 0) {
								setValuesString.append(MessageFormat.format(" && {0} == ptr_matched_temp{1}.get_decmatch_str_enc()", redirCodingExpression.expression, subrefsString));
							}
							setValuesString.append(") {\n");
						}
					}
					if (useDecmatchResult) {
						setValuesString.append(MessageFormat.format("ptr_{0}.operator_assign(({1})ptr_matched_temp{2}.get_decmatch_dec_res());\n", i, typeName, subrefsString));
					}
					if (needsDecode) {
						needPar = true;
						if (useDecmatchResult) {
							setValuesString.append("} else {\n");
						}

						final Type_type tt = redirectionType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetypeTtcn3();
						//legacy encoding does not need to be supported
						aData.addBuiltinTypeImport("TitanOctetString");
						aData.addBuiltinTypeImport("AdditionalFunctions");

						setValuesString.append(MessageFormat.format("TitanOctetString buff_{0} = new TitanOctetString(", i));
						switch(tt) {
						case TYPE_BITSTRING:
							setValuesString.append(MessageFormat.format("AdditionalFunctions.bit2oct(par{0}{1})", subrefsString, optionalSuffix));
							break;
						case TYPE_HEXSTRING:
							setValuesString.append(MessageFormat.format("AdditionalFunctions.hex2oct(par{0}{1})", subrefsString, optionalSuffix));
							break;
						case TYPE_OCTETSTRING:
							setValuesString.append(MessageFormat.format("par{0}{1}", subrefsString, optionalSuffix));
							break;
						case TYPE_CHARSTRING:
							setValuesString.append(MessageFormat.format("AdditionalFunctions.char2oct(par{0}{1})", subrefsString, optionalSuffix));
							break;
						case TYPE_UCHARSTRING:
							setValuesString.append(MessageFormat.format("AdditionalFunctions.unichar2oct(par{0}{1}, ", subrefsString, optionalSuffix));
							if (redirection.getStringEncoding() == null) {
								setValuesString.append("\"UTF-8\"");
							} else if (!redirection.getStringEncoding().isUnfoldable(CompilationTimeStamp.getBaseTimestamp())) {
								IValue temp = redirection.getStringEncoding();
								temp = temp.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), null);
								setValuesString.append(MessageFormat.format("\"{0}\"", ((Charstring_Value)temp).getValue()));
							} else {
								// the encoding format is not known at compile-time, so an extra
								// member and constructor parameter is needed to store it
								instanceParameterList.append(", ");
								final ExpressionStruct stringEncodingExpression = new ExpressionStruct();
								redirection.getStringEncoding().generateCodeExpression(aData, stringEncodingExpression, false);
								instanceParameterList.append(stringEncodingExpression.expression);
								if (stringEncodingExpression.preamble.length() > 0) {
									expression.preamble.append(stringEncodingExpression.preamble);
								}
								if (stringEncodingExpression.postamble.length() > 0) {
									expression.postamble.append(stringEncodingExpression.postamble);
								}

								membersString.append(MessageFormat.format("TitanCharString enc_fmt_{0};\n", i));
								constructorParameters.append(MessageFormat.format(", TitanCharString par_fmt_{0}", i));
								constructorInitializers.append(MessageFormat.format("enc_fmt_{0} = par_fmt_{0};\n", i));
								setValuesString.append(MessageFormat.format("enc_fmt_{0}", i));
							}
							setValuesString.append(')');
							break;
						default:
							//FATAL ERROR
							break;
						}
						setValuesString.append(");\n");
						setValuesString.append(MessageFormat.format("if ({0}_decoder(buff_{1}, ptr_{1}, {2}_default_coding).operator_not_equals(0)) '{'\n", memberType.getGenNameCoder(aData, setValuesString, scope), i, memberType.getGenNameDefaultCoding(aData, setValuesString, scope)));
						setValuesString.append(MessageFormat.format("throw new TtcnError(\"Decoding failed in value redirect #{0}.\");\n", i+1));
						setValuesString.append("}\n");
						setValuesString.append(MessageFormat.format("if (buff_{0}.lengthof().operator_not_equals(0)) '{'\n", i));
						setValuesString.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"Value redirect #{0} failed, because the buffer was not empty after decoding. Remaining octets: '{'0'}'\", buff_{1}.lengthof().get_int()));\n", i+1, i));
						setValuesString.append("}\n");

						if (useDecmatchResult) {
							setValuesString.append("}\n");
						}
					}
				} else {
					needPar = true;
					if (referenceType.isIdentical(CompilationTimeStamp.getBaseTimestamp(), redirectionType)) {
						setValuesString.append(MessageFormat.format("ptr_{0}.operator_assign(par{1}{2});\n", i, subrefsString, optionalSuffix));
					} else {
						//FIXME implement 
						setValuesString.append("//FIXME type conversion is not yet supported\n");
					}
				}
				if (subrefExpression.postamble.length() > 0) {
					setValuesString.append(subrefExpression.postamble);
				}
			}

			final String tempClassName = aData.getTemporaryVariableName();
			expression.preamble.append(MessageFormat.format("class Value_Redirect_{0} implements Value_Redirect_Interface '{'\n", tempClassName));
			expression.preamble.append(membersString);
			expression.preamble.append(MessageFormat.format("public Value_Redirect_{0}({1}) '{'\n", tempClassName, constructorParameters));
			expression.preamble.append(constructorInitializers);
			expression.preamble.append("}\n");
			expression.preamble.append("\t@Override\n");
			expression.preamble.append("\tpublic void set_values(final Base_Type values) {\n");
			if (needPar) {
				expression.preamble.append(MessageFormat.format("final {0} par = ({0})values;\n", valueType.getGenNameValue(aData, expression.preamble, scope)));
			}

			expression.preamble.append(setValuesString);
			expression.preamble.append("\t}\n");
			expression.preamble.append("}\n");

			final String tempVariableName = aData.getTemporaryVariableName();
			expression.preamble.append(MessageFormat.format("Value_Redirect_{0} {1} = new Value_Redirect_{0}({2});\n", tempClassName, tempVariableName, instanceParameterList));

			expression.expression.append(tempVariableName);
		}
	}
}
