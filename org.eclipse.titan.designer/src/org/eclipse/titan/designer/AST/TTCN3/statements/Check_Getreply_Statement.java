/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;

/**
 * @author Kristof Szabados
 * */
public final class Check_Getreply_Statement extends Statement {
	private static final String FULLNAMEPART1 = ".portreference";
	private static final String FULLNAMEPART2 = ".parameter";
	private static final String FULLNAMEPART3 = ".valuematch";
	private static final String FULLNAMEPART4 = ".from";
	private static final String FULLNAMEPART5 = ".redirectValue";
	private static final String FULLNAMEPART6 = ".parameters";
	private static final String FULLNAMEPART7 = ".redirectSender";
	private static final String FULLNAMEPART8 = ".redirectIndex";
	private static final String FULLNAMEPART9 = ".redirectTimestamp";
	private static final String STATEMENT_NAME = "check-getreply";

	private final Reference portReference;
	private final boolean anyFrom;
	private final TemplateInstance parameter;
	private final TemplateInstance valueMatch;
	private final TemplateInstance fromClause;
	private final Value_Redirection redirectValue;
	private final Parameter_Redirection redirectParameter;
	private final Reference redirectSender;
	private final Reference redirectIndex;
	private final Reference redirectTimestamp;

	public Check_Getreply_Statement(final Reference portReference, final boolean anyFrom, final TemplateInstance parameter, final TemplateInstance valueMatch,
			final TemplateInstance fromClause, final Value_Redirection redirectValue, final Parameter_Redirection redirectParameter,
			final Reference redirectSender, final Reference redirectIndex, final Reference redirectTimestamp) {
		this.portReference = portReference;
		this.anyFrom = anyFrom;
		this.parameter = parameter;
		this.valueMatch = valueMatch;
		this.fromClause = fromClause;
		this.redirectValue = redirectValue;
		this.redirectParameter = redirectParameter;
		this.redirectSender = redirectSender;
		this.redirectIndex = redirectIndex;
		this.redirectTimestamp = redirectTimestamp;

		if (portReference != null) {
			portReference.setFullNameParent(this);
		}
		if (parameter != null) {
			parameter.setFullNameParent(this);
		}
		if (valueMatch != null) {
			valueMatch.setFullNameParent(this);
		}
		if (fromClause != null) {
			fromClause.setFullNameParent(this);
		}
		if (redirectValue != null) {
			redirectValue.setFullNameParent(this);
		}
		if (redirectParameter != null) {
			redirectParameter.setFullNameParent(this);
		}
		if (redirectSender != null) {
			redirectSender.setFullNameParent(this);
		}
		if (redirectIndex != null) {
			redirectIndex.setFullNameParent(this);
		}
		if (redirectTimestamp != null) {
			redirectTimestamp.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_CHECK_GETREPLY;
	}

	@Override
	/** {@inheritDoc} */
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (portReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (parameter == child) {
			return builder.append(FULLNAMEPART2);
		} else if (valueMatch == child) {
			return builder.append(FULLNAMEPART3);
		} else if (fromClause == child) {
			return builder.append(FULLNAMEPART4);
		} else if (redirectValue == child) {
			return builder.append(FULLNAMEPART5);
		} else if (redirectParameter == child) {
			return builder.append(FULLNAMEPART6);
		} else if (redirectSender == child) {
			return builder.append(FULLNAMEPART7);
		} else if (redirectSender == child) {
			return builder.append(FULLNAMEPART8);
		} else if (redirectTimestamp == child) {
			return builder.append(FULLNAMEPART9);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (portReference != null) {
			portReference.setMyScope(scope);
		}
		if (parameter != null) {
			parameter.setMyScope(scope);
		}
		if (valueMatch != null) {
			valueMatch.setMyScope(scope);
		}
		if (fromClause != null) {
			fromClause.setMyScope(scope);
		}
		if (redirectValue != null) {
			redirectValue.setMyScope(scope);
		}
		if (redirectParameter != null) {
			redirectParameter.setMyScope(scope);
		}
		if (redirectSender != null) {
			redirectSender.setMyScope(scope);
		}
		if (redirectIndex != null) {
			redirectIndex.setMyScope(scope);
		}
		if (redirectTimestamp != null) {
			redirectTimestamp.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (portReference != null) {
			portReference.setCodeSection(codeSection);
		}
		if (parameter != null) {
			parameter.setCodeSection(codeSection);
		}
		if (valueMatch != null) {
			valueMatch.setCodeSection(codeSection);
		}
		if (fromClause != null) {
			fromClause.setCodeSection(codeSection);
		}
		if (redirectValue != null) {
			redirectValue.setCodeSection(codeSection);
		}
		if (redirectParameter != null) {
			redirectParameter.setCodeSection(codeSection);
		}
		if (redirectSender != null) {
			redirectSender.setCodeSection(codeSection);
		}
		if (redirectIndex != null) {
			redirectIndex.setCodeSection(codeSection);
		}
		if (redirectTimestamp != null) {
			redirectTimestamp.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasReceivingStatement() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canRepeat() {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		Getreply_Statement.checkGetreply(timestamp, this, "check-getreply", portReference, anyFrom, parameter, valueMatch, fromClause, redirectValue,
				redirectParameter, redirectSender, redirectIndex, redirectTimestamp);

		if (redirectValue != null) {
			//redirectValue.setUsedOnLeftHandSide();
		}
		if (redirectSender != null) {
			redirectSender.setUsedOnLeftHandSide();
		}
		if (redirectIndex != null) {
			redirectIndex.setUsedOnLeftHandSide();
		}
		if (redirectTimestamp != null) {
			redirectTimestamp.setUsedOnLeftHandSide();
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (redirectSender != null) {
			return null;
		}

		final List<Integer> result = new ArrayList<Integer>();
		result.add(Ttcn3Lexer.SENDER);

		if (redirectParameter != null) {
			return result;
		}

		result.add(Ttcn3Lexer.PARAM);

		if (redirectValue != null) {
			return result;
		}

		result.add(Ttcn3Lexer.PORTREDIRECTSYMBOL);

		if (fromClause != null) {
			return result;
		}

		result.add(Ttcn3Lexer.FROM);

		if (parameter != null) {
			return result;
		}

		result.add(Ttcn3Lexer.LPAREN);

		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (portReference != null) {
			portReference.updateSyntax(reparser, false);
			reparser.updateLocation(portReference.getLocation());
		}

		if (parameter != null) {
			parameter.updateSyntax(reparser, false);
			reparser.updateLocation(parameter.getLocation());
		}

		if (valueMatch != null) {
			valueMatch.updateSyntax(reparser, false);
			reparser.updateLocation(valueMatch.getLocation());
		}

		if (fromClause != null) {
			fromClause.updateSyntax(reparser, false);
			reparser.updateLocation(fromClause.getLocation());
		}

		if (redirectValue != null) {
			redirectValue.updateSyntax(reparser, false);
			reparser.updateLocation(redirectValue.getLocation());
		}

		if (redirectParameter != null) {
			redirectParameter.updateSyntax(reparser, false);
			reparser.updateLocation(redirectParameter.getLocation());
		}

		if (redirectSender != null) {
			redirectSender.updateSyntax(reparser, false);
			reparser.updateLocation(redirectSender.getLocation());
		}

		if (redirectIndex != null) {
			redirectIndex.updateSyntax(reparser, false);
			reparser.updateLocation(redirectIndex.getLocation());
		}

		if (redirectTimestamp != null) {
			redirectTimestamp.updateSyntax(reparser, false);
			reparser.updateLocation(redirectTimestamp.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (portReference != null) {
			portReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (parameter != null) {
			parameter.findReferences(referenceFinder, foundIdentifiers);
		}
		if (valueMatch != null) {
			valueMatch.findReferences(referenceFinder, foundIdentifiers);
		}
		if (fromClause != null) {
			fromClause.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectValue != null) {
			redirectValue.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectParameter != null) {
			redirectParameter.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectSender != null) {
			redirectSender.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectIndex != null) {
			redirectIndex.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectTimestamp != null) {
			redirectTimestamp.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (portReference != null && !portReference.accept(v)) {
			return false;
		}
		if (parameter != null && !parameter.accept(v)) {
			return false;
		}
		if (valueMatch != null && !valueMatch.accept(v)) {
			return false;
		}
		if (fromClause != null && !fromClause.accept(v)) {
			return false;
		}
		if (redirectValue != null && !redirectValue.accept(v)) {
			return false;
		}
		if (redirectParameter != null && !redirectParameter.accept(v)) {
			return false;
		}
		if (redirectSender != null && !redirectSender.accept(v)) {
			return false;
		}
		if (redirectIndex != null && !redirectIndex.accept(v)) {
			return false;
		}
		if (redirectTimestamp != null && !redirectTimestamp.accept(v)) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		final ExpressionStruct expression = new ExpressionStruct();
		generateCodeExpression(aData, expression, null);

		source.append(expression.preamble);
		PortGenerator.generateCodeStandalone(aData, source, expression.expression.toString(), getStatementName(), canRepeat(), getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final String callTimer) {
		if (portReference != null) {
			portReference.generateCode(aData, expression);
			expression.expression.append(".check_getreply(");
			if (parameter != null) {
				final boolean hasDecodedParamRedirect = redirectParameter != null && redirectParameter.hasDecodedModifier();
				final int parameterExpressionStart = expression.expression.length();
				parameter.generateCode(aData, expression, Restriction_type.TR_NONE, hasDecodedParamRedirect);
				final String lastGenParExpression = expression.expression.substring(parameterExpressionStart);
				String lastGenValueExpression = null;
				final IType signature = parameter.getTemplateBody().getMyGovernor();
				final IType signatureType = signature.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
				final IType returnType = ((Signature_Type) signatureType).getSignatureReturnType();
				if (returnType != null) {
					expression.expression.append(".set_value_template(");
					if (valueMatch != null) {
						final boolean hasDecodedValueRedirect = redirectValue != null && redirectValue.hasDecodedModifier();
						final int valueExpressionStart = expression.expression.length();
						valueMatch.generateCode(aData, expression, Restriction_type.TR_NONE, hasDecodedValueRedirect);
						lastGenValueExpression = expression.expression.substring(valueExpressionStart);
					} else {
						// the value match is not present
						// we must substitute it with ? in the signature template
						expression.expression.append(MessageFormat.format("new {0}(template_sel.ANY_VALUE)", returnType.getGenNameTemplate(aData, expression.expression)));
					}
					expression.expression.append(')');
				}

				expression.expression.append(", ");
				generateCodeExprFromclause(aData, expression);
				if (hasDecodedParamRedirect) {
					final String tempID = aData.getTemporaryVariableName();
					redirectParameter.generateCodeDecoded(aData, expression.preamble, parameter, tempID, true);
					final IType lastSignatureType = signature.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
					final String signatureName = signature.getGenNameValue(aData, expression.expression);
					expression.expression.append(MessageFormat.format(", new {0}_reply_redirect_{1}(", signatureName, tempID));
				} else {
					expression.expression.append(MessageFormat.format(", new {0}_reply_redirect(", signature.getGenNameValue(aData, expression.expression)));
				}

				if (returnType != null) {
					if (redirectValue == null) {
						expression.expression.append("null");
					} else {
						redirectValue.generateCode(aData, expression, valueMatch, lastGenValueExpression);
					}
					if (redirectParameter != null) {
						expression.expression.append(", ");
					}
				}

				if (redirectParameter != null) {
					redirectParameter.generateCode(aData, expression, parameter, lastGenParExpression, true);
				}

				expression.expression.append("), ");
				if (redirectSender == null) {
					expression.expression.append("null");
				} else {
					redirectSender.generateCode(aData, expression);
				}
			} else {
				// the signature template is not present
				generateCodeExprFromclause(aData, expression);
				expression.expression.append(", ");
				if (redirectSender == null) {
					expression.expression.append("null");
				} else {
					redirectSender.generateCode(aData, expression);
				}
			}

			expression.expression.append(", ");
			if (redirectTimestamp == null) {
				expression.expression.append("null");
			}else {
				redirectTimestamp.generateCode(aData, expression);
			}
			expression.expression.append(",");
			if (redirectIndex == null) {
				expression.expression.append("null");
			} else {
				generateCodeIndexRedirect(aData, expression, redirectIndex, getMyScope());
			}
		} else {
			// the operation refers to any port
			expression.expression.append("TitanPort.any_check_getreply(");
			generateCodeExprFromclause(aData, expression);
			expression.expression.append(", ");
			if (redirectSender == null) {
				expression.expression.append("null");
			} else {
				redirectSender.generateCode(aData, expression);
			}
			expression.expression.append(", ");
			if (redirectTimestamp == null) {
				expression.expression.append("null");
			}else {
				redirectTimestamp.generateCode(aData, expression);
			}
		}
		expression.expression.append(')');
	}

	/**
	 * helper to generate the from part.
	 *
	 * originally generate_code_expr_fromclause
	 * */
	private void generateCodeExprFromclause(final JavaGenData aData, final ExpressionStruct expression) {
		if (fromClause != null) {
			fromClause.generateCode(aData, expression, Restriction_type.TR_NONE);
		} else if (redirectSender != null) {
			final IType varType = redirectSender.checkVariableReference(CompilationTimeStamp.getBaseTimestamp());
			if (varType == null) {
				ErrorReporter.INTERNAL_ERROR("Encountered a redirection with unknown type `" + getFullName() + "''");
			}
			if (varType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetype()==Type_type.TYPE_COMPONENT) {
				aData.addBuiltinTypeImport("TitanComponent_template");
				expression.expression.append("TitanComponent_template.any_compref");
			} else {
				expression.expression.append(MessageFormat.format("new {0}(template_sel.ANY_VALUE)", varType.getGenNameTemplate(aData, expression.expression)));
			}
		} else {
			// neither from clause nor sender redirect is present
			// the operation cannot refer to address type
			aData.addBuiltinTypeImport("TitanComponent_template");
			expression.expression.append("TitanComponent_template.any_compref");
		}
	}
}
