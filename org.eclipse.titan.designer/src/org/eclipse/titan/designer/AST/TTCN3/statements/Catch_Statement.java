/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.OperationModes;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureExceptions;
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
public final class Catch_Statement extends Statement {
	private static final String EXCEPTIONPARAMEXPECTED3 = "The type of catch parameter is the `default'' type, which cannot be an exception type";
	private static final String EXCEPTIONPARAMEXPECTED2 = "The type of catch parameter is port type `{0}'', which cannot be an exception type";
	private static final String EXCEPTIONPARAMEXPECTED1 = "The type of catch parameter is signature `{0}'', which cannot be an exception type";
	private static final String AMBIGUOUSEXCEPTIONTYPE = "Type of the exception is ambigous:"
			+ " `{0}'' is compatible with more than one exception type of signature `{1}''";
	private static final String MISSINGEXCEPTIONTYPE = "Type `{0}'' is not present on the exception list of signature `{1}''";
	private static final String UNKNOWNEXCEPTIONTYPE = "Cannot determine the type of the exception";
	private static final String SIGNATUREWITHOUTEXCEPTIONS = "Signature `{0}'' does not have exceptions";
	private static final String MISSINGSIGNATURE = "Signature `{0}'' is not present on the outgoing list of port type `{1}''";
	private static final String ANYPORTWITHVALUEREDIRECT = "operation `any port.{0}'' cannot have value redirect";
	private static final String ANYPORTWITHPARAMETER = "operation`any port.{0}'' cannot have parameter";
	private static final String VALUEREDIRECTWITHOUTPARAMETER = "Value redirect cannot be used without signature and parameter";
	private static final String PORTWITHOUTEXCEPTIONSUPPORT = "Port type `{0}'' does not have any outgoing signatures that support exceptions";
	private static final String MESSAGEPORT = "Procedure-based operation `{0}'' is not applicable to a message-based port of type `{1}''";
	private static final String TIMEOUTWITHSENDERREDIRECT = "operation `catch(timeout)'' cannot have a sender redirect";
	private static final String TIMEOUTWITHFROM = "operation `catch(timeout)'' cannot have a from caluse";
	private static final String TIMEOUTWITHOUTOUTSIGNATURES = "Timeout exception cannot be cought on a port of type `{0}'',"
			+ " which does not have any outgoing signatures that allow blocking calls";
	private static final String TIMEOUTONMESSAGEPORT = "Timeout exception cannot be cought on a message-based port of type `{0}''";
	private static final String TIMEOUTONANYPORT = "Timeout exception cannot be cought on `any port''";
	private static final String TIMEOUTNOTPERMITTED1 = "Catching of `timeout' exception is not allowed in this context."
			+ " It is permitted only in the response and exception handling part of `call' operations";
	private static final String TIMEOUTNOTPERMITTED2 = "Catching of `timeout' exception is not allowed"
			+ " because the previous `call' operation does not have timer";

	private static final String FULLNAMEPART1 = ".portreference";
	private static final String FULLNAMEPART2 = ".signaturereference";
	private static final String FULLNAMEPART3 = ".parameter";
	private static final String FULLNAMEPART4 = ".from";
	private static final String FULLNAMEPART5 = ".redirecvalue";
	private static final String FULLNAMEPART6 = ".redirectSender";
	private static final String FULLNAMEPART7 = ".redirectIndex";
	private static final String STATEMENT_NAME = "catch";

	private final Reference portReference;
	private final boolean anyfrom;
	private final Reference signatureReference;
	private final TemplateInstance parameter;
	private final boolean timeout;
	private final TemplateInstance fromClause;
	private final Reference redirectValue;
	private final Reference redirectSender;
	private final Reference redirectIndex;
	private boolean inCall = false;
	private boolean callHasTimer = false;
	

	// calculated field
	private Signature_Type signature;

	public Catch_Statement(final Reference portReference, final boolean anyFrom, final Reference signatureReference, final TemplateInstance parameter,
			final boolean timeout, final TemplateInstance fromClause, final Reference redirectValue, final Reference redirectSender, final Reference redirectIndex) {
		this.portReference = portReference;
		this.anyfrom = anyFrom;
		this.signatureReference = signatureReference;
		this.parameter = parameter;
		this.timeout = timeout;
		this.fromClause = fromClause;
		this.redirectValue = redirectValue;
		this.redirectSender = redirectSender;
		this.redirectIndex = redirectIndex;

		if (portReference != null) {
			portReference.setFullNameParent(this);
		}
		if (signatureReference != null) {
			signatureReference.setFullNameParent(this);
		}
		if (parameter != null) {
			parameter.setFullNameParent(this);
		}
		if (fromClause != null) {
			fromClause.setFullNameParent(this);
		}
		if (redirectValue != null) {
			redirectValue.setFullNameParent(this);
		}
		if (redirectSender != null) {
			redirectSender.setFullNameParent(this);
		}
		if (redirectIndex != null) {
			redirectIndex.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_CATCH;
	}

	@Override
	/** {@inheritDoc} */
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	public Reference getPortReference() {
		return portReference;
	}

	public Signature_Type getSignatureType() {
		return signature;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (portReference == child) {
			return builder.append(FULLNAMEPART1);
		} else if (signatureReference == child) {
			return builder.append(FULLNAMEPART2);
		} else if (parameter == child) {
			return builder.append(FULLNAMEPART3);
		} else if (fromClause == child) {
			return builder.append(FULLNAMEPART4);
		} else if (redirectValue == child) {
			return builder.append(FULLNAMEPART5);
		} else if (redirectSender == child) {
			return builder.append(FULLNAMEPART6);
		} else if (redirectIndex == child) {
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
		if (signatureReference != null) {
			signatureReference.setMyScope(scope);
		}
		if (parameter != null) {
			parameter.setMyScope(scope);
		}
		if (fromClause != null) {
			fromClause.setMyScope(scope);
		}
		if (redirectValue != null) {
			redirectValue.setMyScope(scope);
		}
		if (redirectSender != null) {
			redirectSender.setMyScope(scope);
		}
		if (redirectIndex != null) {
			redirectIndex.setMyScope(scope);
		}
	}

	public boolean hasTimeout() {
		return timeout;
	}

	/**
	 * Sets the options that are used when it is used in a catch operation.
	 * */
	public void setCallSettings(final boolean inCall, final boolean callHasTimer) {
		this.inCall = inCall;
		this.callHasTimer = callHasTimer;
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

		if (signatureReference != null) {
			signature = Port_Utility.checkSignatureReference(timestamp, signatureReference);
		}

		checkCatch(timestamp, this, "catch", portReference, anyfrom, signatureReference, parameter, timeout, fromClause, redirectValue, redirectSender, redirectIndex);

		if (redirectValue != null) {
			redirectValue.setUsedOnLeftHandSide();
		}
		if (redirectSender != null) {
			redirectSender.setUsedOnLeftHandSide();
		}
		if (redirectIndex != null) {
			redirectIndex.setUsedOnLeftHandSide();
		}

		lastTimeChecked = timestamp;
	}

	public static void checkCatch(final CompilationTimeStamp timestamp, final Statement statement, final String statementName,
			final Reference portReference, final boolean anyFrom, final Reference signatureReference, final TemplateInstance parameter, final boolean timeout,
			final TemplateInstance fromClause, final Reference redirectValue, final Reference redirectSender, final Reference redirectIndex) {

		final Port_Type portType = Port_Utility.checkPortReference(timestamp, statement, portReference, anyFrom);
		if (signatureReference == null) {
			if (timeout) {
				if (portReference == null) {
					statement.getLocation().reportSemanticError(TIMEOUTONANYPORT);
				} else {
					if (portType != null) {
						final 	PortTypeBody body = portType.getPortBody();
						// the order of checks is wrong
						// in the compiler
						if (OperationModes.OP_Message.equals(body.getOperationMode())) {
							portReference.getLocation().reportSemanticError(
									MessageFormat.format(TIMEOUTONMESSAGEPORT, portType.getTypename()));
						} else if (!body.getreplyAllowed(timestamp)) {
							portReference.getLocation().reportSemanticError(
									MessageFormat.format(TIMEOUTWITHOUTOUTSIGNATURES, portType.getTypename()));
						}
					}
				}

				if (statement instanceof Catch_Statement) {
					final Catch_Statement catchStatement = (Catch_Statement) statement;
					if (!catchStatement.inCall) {
						statement.getLocation().reportSemanticError(TIMEOUTNOTPERMITTED1);
					} else if (!catchStatement.callHasTimer) {
						statement.getLocation().reportSemanticError(TIMEOUTNOTPERMITTED2);
					}
				}

				if (fromClause != null) {
					fromClause.getLocation().reportSemanticError(TIMEOUTWITHFROM);
				}
				if (redirectSender != null) {
					redirectSender.getLocation().reportSemanticError(TIMEOUTWITHSENDERREDIRECT);
				}
			} else {
				if (portType != null) {
					final PortTypeBody body = portType.getPortBody();

					if (OperationModes.OP_Message.equals(body.getOperationMode())) {
						portReference.getLocation().reportSemanticError(
								MessageFormat.format(MESSAGEPORT, statementName, portType.getTypename()));
					} else if (!body.catchAllowed(timestamp)) {
						portReference.getLocation().reportSemanticError(
								MessageFormat.format(PORTWITHOUTEXCEPTIONSUPPORT, portType.getTypename()));
					}
				}
			}

			if (redirectValue != null) {
				redirectValue.getLocation().reportSemanticError(VALUEREDIRECTWITHOUTPARAMETER);
				Port_Utility.checkValueRedirect(timestamp, redirectValue, null);
			}
		} else {
			Signature_Type signature = Port_Utility.checkSignatureReference(timestamp, signatureReference);

			if (portType != null) {
				final PortTypeBody body = portType.getPortBody();

				if (OperationModes.OP_Message.equals(body.getOperationMode())) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(MESSAGEPORT, statementName, portType.getTypename()));
				} else if (body.catchAllowed(timestamp)) {
					final TypeSet outSignatures = body.getOutSignatures();

					if (signature != null) {
						if (!outSignatures.hasType(timestamp, signature)) {
							signatureReference.getLocation().reportSemanticError(
									MessageFormat.format(MISSINGSIGNATURE, signature.getTypename(),
											portType.getTypename()));
						}
					} else if (outSignatures.getNofTypes() == 1) {
						signature = (Signature_Type) outSignatures.getTypeByIndex(0).getTypeRefdLast(timestamp);
					}
				} else {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(PORTWITHOUTEXCEPTIONSUPPORT, portType.getTypename()));
				}
			} else if (portReference == null) {
				if (parameter != null) {
					parameter.getLocation().reportSemanticError(MessageFormat.format(ANYPORTWITHPARAMETER, statementName));
				}
				if (redirectValue != null) {
					redirectValue.getLocation().reportSemanticError(
							MessageFormat.format(ANYPORTWITHVALUEREDIRECT, statementName));
				}
			}

			// the receive parameter must also be present
			IType exceptionType = null;
			boolean exceptionTypeDetermined = false;
			final boolean[] valueRedirectChecked = new boolean[] { false };

			if (signature != null) {
				final SignatureExceptions exceptions = signature.getSignatureExceptions();

				if (exceptions == null) {
					signatureReference.getLocation().reportSemanticError(
							MessageFormat.format(SIGNATUREWITHOUTEXCEPTIONS, signature.getTypename()));
				} else {
					if (exceptions.getNofExceptions() == 1) {
						exceptionType = exceptions.getExceptionByIndex(0);
					} else if (parameter != null) {
						exceptionType = Port_Utility.getIncomingType(timestamp, parameter, redirectValue,
								valueRedirectChecked);

						if (exceptionType == null) {
							parameter.getLocation().reportSemanticError(UNKNOWNEXCEPTIONTYPE);
						} else {
							final int nofCompatibleTypes = exceptions.getNofCompatibleExceptions(timestamp, exceptionType);

							if (nofCompatibleTypes == 0) {
								final String message = MessageFormat.format(MISSINGEXCEPTIONTYPE,
										exceptionType.getTypename(), signature.getTypename());
								parameter.getLocation().reportSemanticError(message);
							} else if (nofCompatibleTypes > 1) {
								final String message = MessageFormat.format(AMBIGUOUSEXCEPTIONTYPE,
										exceptionType.getTypename(), signature.getTypename());
								parameter.getLocation().reportSemanticError(message);
							}
						}
					}

					exceptionTypeDetermined = true;
				}
			}

			if (!exceptionTypeDetermined) {
				exceptionType = Port_Utility.getIncomingType(timestamp, parameter, redirectValue, valueRedirectChecked);
			}

			if (exceptionType != null && parameter != null) {
				parameter.check(timestamp, exceptionType);

				if (!valueRedirectChecked[0]) {
					Port_Utility.checkValueRedirect(timestamp, redirectValue, exceptionType);
				}

				exceptionType = exceptionType.getTypeRefdLast(timestamp);
				switch (exceptionType.getTypetype()) {
				case TYPE_SIGNATURE:
					parameter.getLocation().reportSemanticError(
							MessageFormat.format(EXCEPTIONPARAMEXPECTED1, exceptionType.getTypename()));
					break;
				case TYPE_PORT:
					parameter.getLocation().reportSemanticError(
							MessageFormat.format(EXCEPTIONPARAMEXPECTED2, exceptionType.getTypename()));
					break;
				case TYPE_DEFAULT:
					parameter.getLocation().reportSemanticError(EXCEPTIONPARAMEXPECTED3);
					break;
				default:
					// accept it
					break;
				}
			}
		}

		Port_Utility.checkFromClause(timestamp, statement, portType, fromClause, redirectSender);

		if (redirectIndex != null && portReference != null) {
			final Assignment assignment = portReference.getRefdAssignment(timestamp, false);
			checkIndexRedirection(timestamp, redirectIndex, assignment == null ? null : ((Def_Port)assignment).getDimensions(), anyFrom, "timer");
		}
	}

	@Override
	/** {@inheritDoc} */
	public List<Integer> getPossibleExtensionStarterTokens() {
		if (redirectSender != null) {
			return null;
		}

		final List<Integer> result = new ArrayList<Integer>();
		result.add(Ttcn3Lexer.SENDER);

		if (redirectValue != null) {
			return result;
		}

		result.add(Ttcn3Lexer.PORTREDIRECTSYMBOL);

		if (fromClause != null) {
			return result;
		}

		result.add(Ttcn3Lexer.FROM);

		if (signatureReference == null || !timeout) {
			result.add(Ttcn3Lexer.LPAREN);
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

		if (signatureReference != null) {
			signatureReference.updateSyntax(reparser, false);
			reparser.updateLocation(signatureReference.getLocation());
		}

		if (parameter != null) {
			parameter.updateSyntax(reparser, false);
			reparser.updateLocation(parameter.getLocation());
		}

		if (fromClause != null) {
			fromClause.updateSyntax(reparser, false);
			reparser.updateLocation(fromClause.getLocation());
		}

		if (redirectValue != null) {
			redirectValue.updateSyntax(reparser, false);
			reparser.updateLocation(redirectValue.getLocation());
		}

		if (redirectSender != null) {
			redirectSender.updateSyntax(reparser, false);
			reparser.updateLocation(redirectSender.getLocation());
		}

		if (redirectIndex != null) {
			redirectIndex.updateSyntax(reparser, false);
			reparser.updateLocation(redirectIndex.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (portReference != null) {
			portReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (signatureReference != null) {
			signatureReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (parameter != null) {
			parameter.findReferences(referenceFinder, foundIdentifiers);
		}
		if (fromClause != null) {
			fromClause.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectValue != null) {
			redirectValue.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectSender != null) {
			redirectSender.findReferences(referenceFinder, foundIdentifiers);
		}
		if (redirectIndex != null) {
			redirectIndex.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (portReference != null && !portReference.accept(v)) {
			return false;
		}
		if (signatureReference != null && !signatureReference.accept(v)) {
			return false;
		}
		if (parameter != null && !parameter.accept(v)) {
			return false;
		}
		if (fromClause != null && !fromClause.accept(v)) {
			return false;
		}
		if (redirectValue != null && !redirectValue.accept(v)) {
			return false;
		}
		if (redirectSender != null && !redirectSender.accept(v)) {
			return false;
		}
		if (redirectIndex != null && !redirectIndex.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		final ExpressionStruct expression = new ExpressionStruct();
		generateCodeExpression(aData, expression, null);

		PortGenerator.generateCodeStandalone(aData, source, expression.expression.toString(), getStatementName(), canRepeat(), getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final String callTimer) {
		if (portReference != null) {
			// the operation refers to a specific port
			if (timeout && callTimer != null) {
				// the operation catches the timeout exception
				expression.expression.append(MessageFormat.format("{0}.timeout()", callTimer));
				return;
			}
			portReference.generateCode(aData, expression);
			expression.expression.append(".get_exception(");
			if (signatureReference != null) {
				// the signature reference and the exception template is present
				expression.expression.append(MessageFormat.format("new {0}_exception_template(", signature.getGenNameValue(aData, expression.expression, myScope)));
				//FIXME handle redirection
				parameter.generateCode(aData, expression, Restriction_type.TR_NONE);
				expression.expression.append("), ");
				//FIXME handle value redirection
			}
		} else {
			// the operation refers to any port
			expression.expression.append("TitanPort.any_catch(");
		}

		generateCodeExprFromclause(aData, expression);
		expression.expression.append(", ");
		if (redirectSender == null) {
			expression.expression.append("null");
		} else {
			redirectSender.generateCode(aData, expression);
		}

		if (portReference != null) {
			expression.expression.append(", ");
			if (redirectIndex == null) {
				expression.expression.append("null");
			} else {
				generateCodeIndexRedirect(aData, expression, redirectIndex, getMyScope());
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
			//FIXME handle redirect
		} else {
			// neither from clause nor sender redirect is present
			// the operation cannot refer to address type
			aData.addBuiltinTypeImport("TitanComponent_template");
			expression.expression.append("TitanComponent_template.any_compref");
		}
	}
}
