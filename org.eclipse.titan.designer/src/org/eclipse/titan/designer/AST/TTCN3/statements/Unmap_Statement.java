/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.PortReference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.PortType_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Unmap_Statement extends Statement {
	private static final String FULLNAMEPART1 = ".componentreference1";
	private static final String FULLNAMEPART2 = ".portreference1";
	private static final String FULLNAMEPART3 = ".componentreference2";
	private static final String FULLNAMEPART4 = ".portreference2";
	private static final String STATEMENT_NAME = "unmap";

	private final Value componentReference1;
	private final PortReference portReference1;
	private final Value componentReference2;
	private final PortReference portReference2;
	private boolean translate = false;

	public Unmap_Statement(final Value componentReference1, final PortReference portReference1, final Value componentReference2,
			final PortReference portReference2) {
		this.componentReference1 = componentReference1;
		this.portReference1 = portReference1;
		this.componentReference2 = componentReference2;
		this.portReference2 = portReference2;

		if (componentReference1 != null) {
			componentReference1.setFullNameParent(this);
		}
		if (portReference1 != null) {
			portReference1.setFullNameParent(this);
		}
		if (componentReference2 != null) {
			componentReference2.setFullNameParent(this);
		}
		if (portReference2 != null) {
			portReference2.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_UNMAP;
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

		if (componentReference1 == child) {
			return builder.append(FULLNAMEPART1);
		} else if (portReference1 == child) {
			return builder.append(FULLNAMEPART2);
		} else if (componentReference2 == child) {
			return builder.append(FULLNAMEPART3);
		} else if (portReference2 == child) {
			return builder.append(FULLNAMEPART4);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (componentReference1 != null) {
			componentReference1.setMyScope(scope);
		}
		if (componentReference2 != null) {
			componentReference2.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		IType portType1;
		IType portType2;
		PortTypeBody body1;
		PortTypeBody body2;
		boolean cref1IsTestcomponents = false;
		boolean cref1IsSystem = false;
		boolean cref2IsTestcomponent = false;
		boolean cref2IsSystem = false;

		portType1 = Port_Utility.checkConnectionEndpoint(timestamp, this, componentReference1, portReference1, true);
		if (portType1 == null) {
			body1 = null;
		} else {
			body1 = ((Port_Type) portType1).getPortBody();
			if (body1.isInternal()) {
				componentReference1.getLocation().reportSemanticWarning(
						MessageFormat.format("Port type `{0}'' was marked as `internal''", portType1.getTypename()));
			}
		}

		final IValue configReference1 = componentReference1.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
		if (Value_type.EXPRESSION_VALUE.equals(configReference1.getValuetype())) {
			switch (((Expression_Value) configReference1).getOperationType()) {
			case MTC_COMPONENT_OPERATION:
				cref1IsTestcomponents = true;
				break;
			case SELF_COMPONENT_OPERATION:
				cref1IsTestcomponents = true;
				break;
			case COMPONENT_CREATE_OPERATION:
				cref1IsTestcomponents = true;
				break;
			case SYSTEM_COMPONENT_OPERATION:
				cref1IsSystem = true;
				break;
			default:
				break;
			}
		}

		portType2 = Port_Utility.checkConnectionEndpoint(timestamp, this, componentReference2, portReference2, true);
		if (portType2 == null) {
			body2 = null;
		} else {
			body2 = ((Port_Type) portType2).getPortBody();
			if (body2.isInternal()) {
				componentReference2.getLocation().reportSemanticWarning(
						MessageFormat.format("Port type `{0}'' was marked as `internal''", portType2.getTypename()));
			}
		}

		final IValue configReference2 = componentReference2.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
		if (Value_type.EXPRESSION_VALUE.equals(configReference2.getValuetype())) {
			switch (((Expression_Value) configReference2).getOperationType()) {
			case MTC_COMPONENT_OPERATION:
				cref2IsTestcomponent = true;
				break;
			case SELF_COMPONENT_OPERATION:
				cref2IsTestcomponent = true;
				break;
			case COMPONENT_CREATE_OPERATION:
				cref2IsTestcomponent = true;
				break;
			case SYSTEM_COMPONENT_OPERATION:
				cref2IsSystem = true;
				break;
			default:
				break;
			}
		}

		lastTimeChecked = timestamp;

		if (cref1IsTestcomponents && cref2IsTestcomponent) {
			location.reportSemanticError(Map_Statement.BOTHENDSARETESTCOMPONENTPORTS);
			return;
		}

		if (cref1IsSystem && cref2IsSystem) {
			location.reportSemanticError(Map_Statement.BOTHENDSARESYSTEMPORTS);
			return;
		}

		if (body1 == null || body2 == null) {
			if (body1 != null && body1.isInternal()) {
				portReference1.getLocation().reportSemanticWarning(MessageFormat.format("Port type `{0}'' was marked as `internal''", portType1.getTypename()));
			}
			if (body2 != null && body2.isInternal()) {
				portReference2.getLocation().reportSemanticWarning(MessageFormat.format("Port type `{0}'' was marked as `internal''", portType2.getTypename()));
			}

			if ((body1 != null && !body1.isLegacy() && body1.getPortType() == PortType_type.PT_USER) ||
					(body2 != null && !body2.isLegacy() && body2.getPortType() == PortType_type.PT_USER)) {
				getLocation().reportSemanticWarning(MessageFormat.format("This mapping is not done in translation mode, because the {0} endpoint is unknown", body1 != null ? "second" : "first"));
			}

			return;
		}

		if (cref1IsTestcomponents || cref2IsSystem) {
			translate = !body1.isLegacy() && body1.isTranslate(body2);
			if (!translate && !body1.isMappable(timestamp, body2)) {
				location.reportSemanticError(MessageFormat.format(Map_Statement.INCONSISTENTMAPPING1, portType1.getTypename(),
						portType2.getTypename()));
				body1.reportMappingErrors(timestamp, body2);
			}
			if (!translate && !body1.mapCanReceiveOrSend(body2)) {
				getLocation().reportSemanticWarning(MessageFormat.format("Port type `{0}'' cannot send or receive from system port type `{1}''.", portType1.getTypename(), portType2.getTypename()));
			}
		} else if (cref2IsTestcomponent || cref1IsSystem) {
			translate = !body2.isLegacy() && body2.isTranslate(body1);
			if (!translate && !body2.isMappable(timestamp, body1)) {
				location.reportSemanticError(MessageFormat.format(Map_Statement.INCONSISTENTMAPPING2, portType1.getTypename(),
						portType2.getTypename()));
				body2.reportMappingErrors(timestamp, body1);
			}
			if (!translate && !body2.mapCanReceiveOrSend(body1)) {
				getLocation().reportSemanticWarning(MessageFormat.format("Port type `{0}'' cannot send or receive from system port type `{1}''.", portType2.getTypename(), portType1.getTypename()));
			}
		} else {
			// we don't know which one is the system port
			boolean firstMappedToSecond = !body1.isLegacy() && body1.isTranslate(body2);
			boolean secondMappedToFirst = !body2.isLegacy() && body2.isTranslate(body1);;
			translate = firstMappedToSecond || secondMappedToFirst;

			if (!translate && !body1.isMappable(timestamp, body2) && !body2.isMappable(timestamp, body1)) {
				location.reportSemanticError(MessageFormat.format(Map_Statement.INCONSISTENTMAPPING3, portType1.getTypename(),
						portType2.getTypename()));
			}
		}

		if (!translate) {
			if (body1.isInternal()) {
				portReference1.getLocation().reportSemanticWarning(MessageFormat.format("Port type `{0}'' was marked as `internal''", portType1.getTypename()));
			}
			if (body2.isInternal()) {
				portReference2.getLocation().reportSemanticWarning(MessageFormat.format("Port type `{0}'' was marked as `internal''", portType2.getTypename()));
			}
			if ((!body1.isLegacy() && body1.getPortType() == PortType_type.PT_USER) ||
					(!body2.isLegacy() && body2.getPortType() == PortType_type.PT_USER)) {
				getLocation().reportSemanticWarning("This mapping is not done in translation mode");
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (componentReference1 != null) {
			componentReference1.updateSyntax(reparser, false);
			reparser.updateLocation(componentReference1.getLocation());
		}

		if (portReference1 != null) {
			portReference1.updateSyntax(reparser, false);
			reparser.updateLocation(portReference1.getLocation());
		}

		if (componentReference2 != null) {
			componentReference2.updateSyntax(reparser, false);
			reparser.updateLocation(componentReference2.getLocation());
		}

		if (portReference2 != null) {
			portReference2.updateSyntax(reparser, false);
			reparser.updateLocation(portReference2.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReference1 != null) {
			componentReference1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (portReference1 != null) {
			portReference1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (componentReference2 != null) {
			componentReference2.findReferences(referenceFinder, foundIdentifiers);
		}
		if (portReference2 != null) {
			portReference2.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (componentReference1 != null && !componentReference1.accept(v)) {
			return false;
		}
		if (portReference1 != null && !portReference1.accept(v)) {
			return false;
		}
		if (componentReference2 != null && !componentReference2.accept(v)) {
			return false;
		}
		if (portReference2 != null && !portReference2.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		final ExpressionStruct expression = new ExpressionStruct();

		//TODO why the checks here?
		expression.expression.append("TTCN_Runtime.unmap_port(");
		componentReference1.generateCodeExpression(aData, expression, true);
		expression.expression.append(", ");
		if (componentReference1.getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_DYNAMIC_VALUE) == null) {
			Port_Utility.generate_code_portref(aData, expression, portReference1);
		} else {
			portReference1.generateCode(aData, expression);
			expression.expression.append(".get_name(), ");
		}

		componentReference2.generateCodeExpression(aData, expression, true);
		expression.expression.append(", ");
		if (componentReference2.getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_DYNAMIC_VALUE) == null) {
			Port_Utility.generate_code_portref(aData, expression, portReference2);
		} else {
			portReference2.generateCode(aData, expression);
			expression.expression.append(".get_name()");
		}
		if (translate) {
			expression.expression.append(", true)");
		} else {
			expression.expression.append(", false)");
		}

		expression.mergeExpression(source);
	}
}
