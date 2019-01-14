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
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.Verdict_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the value redirection of several operations.
 * TODO list of operations.
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

	public Value_Redirection() {
		valueRedirections = new ArrayList<Single_ValueRedirection>();
	}

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
	public boolean has_decoded_modifier() {
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
		//FIXME implement
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

	public void generateCode( final JavaGenData aData, final ExpressionStruct expression, final TemplateInstance matchedTi, final String lastGenTIExpression ) {
		if (verdictOnly) {
			//verdict only case
			if (valueRedirections.size() == 1) {
				valueRedirections.get(0).getVariableReference().generateCode(aData, expression);
			}
		} else {
			aData.addBuiltinTypeImport("Value_Redirect_Interface");
			//FIXME implement fully
			StringBuilder membersString = new StringBuilder();
			StringBuilder constructorParameters = new StringBuilder();
			StringBuilder constructorInitializers = new StringBuilder();
			StringBuilder instanceParameterList = new StringBuilder();
			StringBuilder setValuesString = new StringBuilder();

			if (matchedTi != null && has_decoded_modifier()) {
				// store a pointer to the matched template, the decoding results from
				// decmatch templates might be reused to optimize decoded value redirects
				instanceParameterList.append(MessageFormat.format("{0}, ", lastGenTIExpression));
				String templateName = valueType.getGenNameTemplate(aData, expression.expression, getMyScope());
				membersString.append(MessageFormat.format("{0} ptr_matched_temp;\n", templateName));
				constructorParameters.append(MessageFormat.format("{0} par_matched_temp, ", templateName));
				constructorInitializers.append("ptr_matched_temp = par_matched_temp;\n");
			}

			for (int i = 0 ; i < valueRedirections.size(); i++) {
				if (i > 0) {
					constructorParameters.append(", ");
					instanceParameterList.append(", ");
				}

				Single_ValueRedirection redirection = valueRedirections.get(i);
				ExpressionStruct variableReferenceExpression = new ExpressionStruct();
				Reference variableReference = redirection.getVariableReference();
				redirection.getVariableReference().generateCode(aData, variableReferenceExpression);
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
					ArrayList<ISubReference> subreferences = redirection.getSubreferences();
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
				IType memberType = redirection.isDecoded() ? redirection.getDeclarationType() : referenceType;
				String typeName = memberType.getGenNameValue(aData, expression.expression, myScope);

				membersString.append(MessageFormat.format("{0} ptr_{1};\n", typeName, i));
				constructorParameters.append(MessageFormat.format("{0} par_{1}", typeName, i));
				constructorInitializers.append(MessageFormat.format("ptr_{0} = par_{0};\n", i));
				//FIXME handle subreferences 10633

				//FIXME this is only good for the most simple cases
				setValuesString.append(MessageFormat.format("ptr_{0}.operator_assign(({1}){2});\n", i, typeName, "values"));
			}

			final String tempClassName = aData.getTemporaryVariableName();
			expression.preamble.append(MessageFormat.format("class Value_Redirect_{0} implements Value_Redirect_Interface '{'\n", tempClassName));
			expression.preamble.append(membersString);
			expression.preamble.append(MessageFormat.format("public Value_Redirect_{0}({1}) '{'\n", tempClassName, constructorParameters));
			expression.preamble.append(constructorInitializers);
			expression.preamble.append("}\n");
			expression.preamble.append("\t@Override\n");
			expression.preamble.append("\tpublic void set_values(final Base_Type values) {\n");
			//TODO can save on the casting!
			expression.preamble.append(setValuesString);
			expression.preamble.append("//FIXME for the time being not yet supported\n");

			expression.preamble.append("\t}\n");
			expression.preamble.append("}\n");

			final String tempVariableName = aData.getTemporaryVariableName();
			expression.preamble.append(MessageFormat.format("Value_Redirect_{0} {1} = new Value_Redirect_{0}({2});\n", tempClassName, tempVariableName, instanceParameterList));

			expression.expression.append(tempVariableName);
		}
	}
}
