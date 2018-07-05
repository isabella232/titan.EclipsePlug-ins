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
import org.eclipse.titan.designer.AST.IType.Type_type;
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
import org.eclipse.titan.designer.AST.TTCN3.types.TypeSet;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;

/**
 * @author Kristof Szabados
 *
 * FIXME add support for translate
 * */
public final class Receive_Port_Statement extends Statement {
	private static final String MESSAGEBASEOPERATIONONPROCEDUREPORT = "Massage-based operation `{0}'' is not applicable"
			+ " to a procedure-based port of type `{1}''";
	private static final String NOINCOMINGMESSAGETYPES = "Port type `{0}'' does not have any incoming message types";
	private static final String VALUEREDIRECTWITHOUTRECEIVEPARAMETER = "Value redirect cannot be used without receive parameter";
	private static final String RECEIVEONPORT = "Message-based operation `{0}'' is not applicable to a procedure-based port of type `{1}''";
	private static final String UNKNOWNINCOMINGMESSAGE = "Cannot determine the type of the incoming message";
	private static final String TYPENOTPRESENT = "Message type `{0}'' is not present on the incoming list of port of type `{1}''";
	private static final String TYPEISAMBIGUOUS = "The type of the message is ambiguous:"
			+ " `{0}'' is compatible with more than one incoming message types of port type `{1}''";
	private static final String ANYPORTWITHPARAMETER = "Operation `any port.{0}'' cannot have parameter";
	private static final String RECEIVEWITHVALUEREDIRECT = "Operation `any port. {0}'' cannot have value redirect";

	private static final String FULLNAMEPART1 = ".portreference";
	private static final String FULLNAMEPART2 = ".receiveparameter";
	private static final String FULLNAMEPART3 = ".from";
	private static final String FULLNAMEPART4 = ".redirectvalue";
	private static final String FULLNAMEPART5 = ".redirectsender";
	private static final String FULLNAMEPART6 = ".redirectIndex";
	private static final String STATEMENT_NAME = "receive";

	private final Reference portReference;
	private final boolean anyFrom;
	private final TemplateInstance receiveParameter;
	private final TemplateInstance fromClause;
	private final Reference redirectValue;
	private final Reference redirectSender;
	private final Reference redirectIndex;

	public Receive_Port_Statement(final Reference portReference, final boolean anyFrom, final TemplateInstance receiveParameter, final TemplateInstance fromClause,
			final Reference redirectValue, final Reference redirectSender, final Reference redirectIndex, final boolean translate) {
		this.portReference = portReference;
		this.anyFrom = anyFrom;
		this.receiveParameter = receiveParameter;
		this.fromClause = fromClause;
		this.redirectValue = redirectValue;
		this.redirectSender = redirectSender;
		this.redirectIndex = redirectIndex;

		if (portReference != null) {
			portReference.setFullNameParent(this);
		}
		if (receiveParameter != null) {
			receiveParameter.setFullNameParent(this);
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

	public Reference getPort() {
		return portReference;
	}

	/**
	 * @return the type of the port used for this receive, or
	 *         <code>null</code> if used without port.
	 */
	public Port_Type getPortType() {
		return Port_Utility.checkPortReference(CompilationTimeStamp.getBaseTimestamp(), this, portReference, anyFrom);
	}

	/**
	 * @return the reference to redirect the received value, or
	 *         <code>null</code> if the statement does not redirect the
	 *         value.
	 */
	public Reference getRedirectValue() {
		return redirectValue;
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_RECEIVE;
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
		} else if (receiveParameter == child) {
			return builder.append(FULLNAMEPART2);
		} else if (fromClause == child) {
			return builder.append(FULLNAMEPART3);
		} else if (redirectValue == child) {
			return builder.append(FULLNAMEPART4);
		} else if (redirectSender == child) {
			return builder.append(FULLNAMEPART5);
		} else if (redirectIndex == child) {
			return builder.append(FULLNAMEPART6);
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
		if (receiveParameter != null) {
			receiveParameter.setMyScope(scope);
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

		checkReceivingStatement(timestamp, this, "receive", portReference, anyFrom, receiveParameter, fromClause, redirectValue, redirectSender, redirectIndex);

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

	/**
	 * Checks a port receiving statement.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param origin
	 *                the original statement.
	 * @param statementName
	 *                the name of the original statement.
	 * @param portReference
	 *                the port reference.
	 * @param receiveParameter
	 *                the receiving parameter.
	 * @param fromClause
	 *                the from clause of the statement
	 * @param redirectValue
	 *                the redirection value of the statement.
	 * @param redirectSender
	 *                the sender redirection of the statement.
	 * */
	public static void checkReceivingStatement(final CompilationTimeStamp timestamp, final Statement origin, final String statementName,
			final Reference portReference, final boolean anyFrom, final TemplateInstance receiveParameter, final TemplateInstance fromClause,
			final Reference redirectValue, final Reference redirectSender, final Reference redirectIndex) {
		final Port_Type portType = Port_Utility.checkPortReference(timestamp, origin, portReference, anyFrom);

		if (receiveParameter == null) {
			if (portType != null && Type_type.TYPE_PORT.equals(portType.getTypetype())) {
				final PortTypeBody body = portType.getPortBody();
				if (OperationModes.OP_Procedure.equals(body.getOperationMode())) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(MESSAGEBASEOPERATIONONPROCEDUREPORT, statementName,
									portType.getTypename()));
				} else if (body.getInMessages() == null) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(NOINCOMINGMESSAGETYPES, portType.getTypename()));
				}
			}

			if (redirectValue != null) {
				redirectValue.getLocation().reportSemanticError(VALUEREDIRECTWITHOUTRECEIVEPARAMETER);
				Port_Utility.checkValueRedirect(timestamp, redirectValue, null);
			}
		} else {
			// determine the type of the incoming message
			IType messageType = null;
			boolean messageTypeDetermined = false;
			final boolean[] valueRedirectChecked = new boolean[] { false };

			if (portType != null) {
				// the port type is known
				final PortTypeBody portTypeBody = portType.getPortBody();
				final TypeSet inMessages = portTypeBody.getInMessages();
				if (OperationModes.OP_Procedure.equals(portTypeBody.getOperationMode())) {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(RECEIVEONPORT, statementName, portType.getTypename()));
				} else if (inMessages != null) {
					if (inMessages.getNofTypes() == 1) {
						messageType = inMessages.getTypeByIndex(0);
					} else {
						messageType = Port_Utility.getIncomingType(timestamp, receiveParameter, redirectValue,
								valueRedirectChecked);
						if (messageType == null) {
							receiveParameter.getLocation().reportSemanticError(UNKNOWNINCOMINGMESSAGE);
						} else {
							final int nofCompatibleTypes = inMessages.getNofCompatibleTypes(timestamp, messageType);
							if (nofCompatibleTypes == 0) {
								receiveParameter.getLocation().reportSemanticError(
										MessageFormat.format(TYPENOTPRESENT, messageType.getTypename(),
												portType.getTypename()));
							} else if (nofCompatibleTypes > 1) {
								receiveParameter.getLocation().reportSemanticError(
										MessageFormat.format(TYPEISAMBIGUOUS, messageType.getTypename(),
												portType.getTypename()));
							}
						}
					}

					messageTypeDetermined = true;
				} else {
					portReference.getLocation().reportSemanticError(
							MessageFormat.format(NOINCOMINGMESSAGETYPES, portType.getTypename()));
				}
			} else if (portReference == null) {
				// any port
				receiveParameter.getLocation().reportSemanticError(MessageFormat.format(ANYPORTWITHPARAMETER, statementName));
				if (redirectValue != null) {
					redirectValue.getLocation().reportSemanticError(
							MessageFormat.format(RECEIVEWITHVALUEREDIRECT, statementName));
				}
			}

			if (!messageTypeDetermined) {
				messageType = Port_Utility.getIncomingType(timestamp, receiveParameter, redirectValue, valueRedirectChecked);
			}

			if (messageType != null) {
				receiveParameter.check(timestamp, messageType);
				if (!valueRedirectChecked[0]) {
					Port_Utility.checkValueRedirect(timestamp, redirectValue, messageType);
				}
			}
		}

		Port_Utility.checkFromClause(timestamp, origin, portType, fromClause, redirectSender);

		if (redirectIndex != null && portReference != null) {
			final Assignment assignment = portReference.getRefdAssignment(timestamp, false);
			checkIndexRedirection(timestamp, redirectIndex, assignment == null ? null : ((Def_Port)assignment).getDimensions(), anyFrom, "port");
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

		if (receiveParameter != null) {
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

		if (receiveParameter != null) {
			receiveParameter.updateSyntax(reparser, false);
			reparser.updateLocation(receiveParameter.getLocation());
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
		if (receiveParameter != null) {
			receiveParameter.findReferences(referenceFinder, foundIdentifiers);
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
		if (receiveParameter != null && !receiveParameter.accept(v)) {
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
		//FIXME handle translation too
		if (portReference != null) {
			portReference.generateCode(aData, expression);
			expression.expression.append(".receive(");
			if (receiveParameter != null) {
				receiveParameter.generateCode(aData, expression, Restriction_type.TR_NONE);
				expression.expression.append(", ");
				if (redirectValue == null) {
					expression.expression.append("null");
				} else {
					redirectValue.generateCode(aData, expression);
				}
				expression.expression.append(", ");
			}
		} else {
			aData.addBuiltinTypeImport("TitanPort");
			expression.expression.append("TitanPort.any_receive(");
		}

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
			aData.addBuiltinTypeImport("TitanComponent_template");
			expression.expression.append("TitanComponent_template.any_compref");
		}

		expression.expression.append(", ");
		if (redirectSender == null) {
			expression.expression.append("null");
		}else {
			redirectSender.generateCode(aData, expression);
		}

		//FIXME also if translate
		if (portReference != null) {
			expression.expression.append(",");
			if (redirectIndex == null) {
				expression.expression.append("null");
			} else {
				generateCodeIndexRedirect(aData, expression, redirectIndex, getMyScope());
			}
		}

		expression.expression.append( ')' );
	}
}
