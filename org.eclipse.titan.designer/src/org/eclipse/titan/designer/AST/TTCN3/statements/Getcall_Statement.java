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

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.OperationModes;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeSet;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;

/**
 * @author Kristof Szabados
 * */
public final class Getcall_Statement extends Statement {
	private static final String SIGNATUREPARAMETEREXPECTED = "The type of parameter is `{0}'', which is not a signature";
	private static final String ANYWITHREDIRECT = "Operation `any port.{0}'' cannot have parameter redirect";
	private static final String ANYWITHPARAMETER = "operation `any port.{0}'' cannot have parameter";
	private static final String SIGNATURENOTPRESENT = "Signature `{0}'' is not present on the incoming list of port type `{1}''";
	private static final String UNKNOWNSIGNATURETYPE = "Cannot determine the type of the signature";
	private static final String MESSAGEBASEDPORT = "Procedure-based operation `{0}'' is not applicable to a massage-based port of type `{1}''";
	private static final String NOINSIGNATURES = "Port type `{0}'' does not have any incoming signatures";
	private static final String REDIRECTWITHOUTSIGNATURE = "Parameter redirect cannot be used without signature template";

	private static final String FULLNAMEPART1 = ".portreference";
	private static final String FULLNAMEPART2 = ".parameter";
	private static final String FULLNAMEPART3 = ".from";
	private static final String FULLNAMEPART4 = ".parameters";
	private static final String FULLNAMEPART5 = ".redirectSender";
	private static final String FULLNAMEPART6 = ".redirectIndex";
	private static final String FULLNAMEPART7 = ".redirectTimestamp";
	private static final String STATEMENT_NAME = "getcall";

	private final Reference portReference;
	private final boolean anyFrom;
	private final TemplateInstance parameter;
	private final TemplateInstance fromClause;
	private final Parameter_Redirect redirectParameter;
	private final Reference redirectSender;
	private final Reference redirectIndex;
	private final Reference redirectTimestamp;

	public Getcall_Statement(final Reference portReference, final boolean anyFrom, final TemplateInstance parameter, final TemplateInstance fromClause,
			final Parameter_Redirect redirectParameter, final Reference redirectSender, final Reference redirectIndex, final Reference redirectTimestamp) {
		this.portReference = portReference;
		this.anyFrom = anyFrom;
		this.parameter = parameter;
		this.fromClause = fromClause;
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
		if (fromClause != null) {
			fromClause.setFullNameParent(this);
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
		return Statement_type.S_GETCALL;
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
		} else if (fromClause == child) {
			return builder.append(FULLNAMEPART3);
		} else if (redirectParameter == child) {
			return builder.append(FULLNAMEPART4);
		} else if (redirectSender == child) {
			return builder.append(FULLNAMEPART5);
		} else if (redirectIndex == child) {
			return builder.append(FULLNAMEPART6);
		} else if (redirectTimestamp == child) {
			return builder.append(FULLNAMEPART7);
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
		if (fromClause != null) {
			fromClause.setMyScope(scope);
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
		if (fromClause != null) {
			fromClause.setCodeSection(codeSection);
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

		checkGetcallStatement(timestamp, this, "getcall", portReference, anyFrom, parameter, fromClause, redirectParameter, redirectSender,
				redirectIndex, redirectTimestamp);

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

	public static void checkGetcallStatement(final CompilationTimeStamp timestamp, final Statement statement, final String statementName,
			final Reference portReference, final boolean anyFrom, final TemplateInstance parameter, final TemplateInstance fromClause,
			final Parameter_Redirect redirectParameter, final Reference redirectSender, final Reference redirectIndex, final Reference redirectTimestamp) {
		final Port_Type portType = Port_Utility.checkPortReference(timestamp, statement, portReference, anyFrom);

		if (parameter == null) {
			if (portType != null) {
				final PortTypeBody body = portType.getPortBody();
				if (OperationModes.OP_Message.equals(body.getOperationMode())) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(MESSAGEBASEDPORT, statementName, portType.getTypename()));
				} else if (body.getInSignatures() == null) {
					portReference.getLocation().reportSemanticError(MessageFormat.format(NOINSIGNATURES, portType.getTypename()));
				}
			}

			if (redirectParameter != null) {
				redirectParameter.getLocation().reportSemanticError(REDIRECTWITHOUTSIGNATURE);
				redirectParameter.checkErroneous(timestamp);
			}
		} else {
			IType signature = null;
			boolean signatureDetermined = false;
			if (portType != null) {
				final PortTypeBody body = portType.getPortBody();
				final TypeSet inSignatures = body.getInSignatures();
				if (OperationModes.OP_Message.equals(body.getOperationMode())) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(MESSAGEBASEDPORT, statementName, portType.getTypename()));
				} else if (inSignatures != null) {
					if (inSignatures.getNofTypes() == 1) {
						signature = inSignatures.getTypeByIndex(0);
					} else {
						signature = Port_Utility.getOutgoingType(timestamp, parameter);

						if (signature == null) {
							parameter.getLocation().reportSemanticError(UNKNOWNSIGNATURETYPE);
						} else {
							if (!inSignatures.hasType(timestamp, signature)) {
								parameter.getLocation().reportSemanticError(
										MessageFormat.format(SIGNATURENOTPRESENT, signature.getTypename(),
												portType.getTypename()));
							}
						}
					}

					signatureDetermined = true;
				} else {
					portReference.getLocation().reportSemanticError(MessageFormat.format(NOINSIGNATURES, portType.getTypename()));
				}
			} else if (portReference == null) {
				// any port is referenced, or there was a syntax
				// error
				parameter.getLocation().reportSemanticError(MessageFormat.format(ANYWITHPARAMETER, statementName));
				if (redirectParameter != null) {
					redirectParameter.getLocation().reportSemanticError(MessageFormat.format(ANYWITHREDIRECT, statementName));
				}
			}

			if (!signatureDetermined) {
				signature = Port_Utility.getOutgoingType(timestamp, parameter);
			}

			if (signature == null) {
				if (redirectParameter != null) {
					redirectParameter.checkErroneous(timestamp);
				}
			} else {
				parameter.check(timestamp, signature);

				signature = signature.getTypeRefdLast(timestamp);
				switch (signature.getTypetype()) {
				case TYPE_SIGNATURE:
					((Signature_Type) signature).checkThisTemplate(timestamp, parameter.getTemplateBody(), false, false, null);
					if (redirectParameter != null) {
						redirectParameter.check(timestamp, (Signature_Type) signature, false);
					}
					break;
				default:
					parameter.getLocation().reportSemanticError(
							MessageFormat.format(SIGNATUREPARAMETEREXPECTED, signature.getTypename()));
					if (redirectParameter != null) {
						redirectParameter.checkErroneous(timestamp);
					}
					break;
				}
			}
		}

		Port_Utility.checkFromClause(timestamp, statement, portType, fromClause, redirectSender);

		if (redirectIndex != null && portReference != null) {
			final Assignment assignment = portReference.getRefdAssignment(timestamp, false);
			checkIndexRedirection(timestamp, redirectIndex, assignment == null ? null : ((Def_Port)assignment).getDimensions(), anyFrom, "port");
		}

		Port_Utility.checkTimestampRedirect(timestamp, portType, redirectTimestamp);
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

		if (fromClause != null) {
			fromClause.updateSyntax(reparser, false);
			reparser.updateLocation(fromClause.getLocation());
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
		if (fromClause != null) {
			fromClause.findReferences(referenceFinder, foundIdentifiers);
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
		if (fromClause != null && !fromClause.accept(v)) {
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
			// the operation refers to a specific port
			portReference.generateCode(aData, expression);
			expression.expression.append(".getcall(");
			if (parameter != null) {
				final boolean hasDecodedRedirect = redirectParameter != null && redirectParameter.has_decoded_modifier();
				final int expressionStart = expression.expression.length();
				parameter.generateCode(aData, expression, Restriction_type.TR_NONE, hasDecodedRedirect);
				final String lastGenExpression = expression.expression.substring(expressionStart);
				expression.expression.append(", ");
				generateCodeExprFromclause(aData, expression);
				final IType signature = parameter.getTemplateBody().getMyGovernor();
				if (hasDecodedRedirect) {
					final String tempID = aData.getTemporaryVariableName();
					redirectParameter.generateCodeDecoded(aData, expression.preamble, parameter, tempID, false);
					final IType lastSignatureType = signature.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
					final String signatureName = signature.getGenNameValue(aData, expression.expression, lastSignatureType.getMyScope());
					expression.expression.append(MessageFormat.format(", new {0}_call_redirect_{1}(", signatureName, tempID));
				} else {
					expression.expression.append(MessageFormat.format(", new {0}_call_redirect(", signature.getGenNameValue(aData, expression.expression, myScope)));
				}
				if (redirectParameter != null) {
					redirectParameter.generateCode(aData, expression, parameter, lastGenExpression, false);
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
			expression.expression.append("TitanPort.any_getcall(");
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
				expression.expression.append(MessageFormat.format("new {0}(template_sel.ANY_VALUE)", varType.getGenNameTemplate(aData, expression.expression, myStatementBlock)));
			}
		} else {
			// neither from clause nor sender redirect is present
			// the operation cannot refer to address type
			aData.addBuiltinTypeImport("TitanComponent_template");
			expression.expression.append("TitanComponent_template.any_compref");
		}
	}
}
