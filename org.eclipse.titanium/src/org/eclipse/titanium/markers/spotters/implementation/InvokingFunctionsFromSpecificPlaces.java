/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Check_Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Invoke_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Operation_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Receive_Port_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Referenced_Altguard;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AllComponentAliveExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AllComponentRunningExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AllPortCheckSateExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AnyComponentAliveExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AnyComponentRunningExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AnyPortCheckStateExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.AnyTimerRunningExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.CheckStateExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ComponentAliveExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ComponentRunningExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RNDExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.RNDWithValueExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.TimerReadExpression;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.TimerRunningExpression;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class InvokingFunctionsFromSpecificPlaces extends BaseModuleCodeSmellSpotter {
	//private static final String ERROR_MESSAGE = "Usage of goto and label statements is not recommended "
	//		+ "as they usually break the structure of the code";

	public InvokingFunctionsFromSpecificPlaces() {
		super(CodeSmellType.INVOKING_FUNCTIONS_FROM_SPECIFIC_PLACES);
	}

	protected static class FunctionVisitor extends ASTVisitor {

		protected final Problems problems;

		public FunctionVisitor(final Problems problems) {
			super();
			this.problems = problems;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if(node instanceof Referenced_Value) {
				Referenced_Value value = (Referenced_Value) node;
				Assignment_type asst = value.getReference().getAssOld().getAssignmentType();
				if(
						asst == Assignment_type.A_FUNCTION_RVAL ||
						asst == Assignment_type.A_EXT_FUNCTION_RVAL ||
						asst == Assignment_type.A_FUNCTION_RTEMP ||
						asst == Assignment_type.A_EXT_FUNCTION_RTEMP

						) {
					problems.report(value.getLocation(), "Invoking function may change the actual snapshot");
				}
			}

			if(node instanceof RNDExpression || node instanceof RNDWithValueExpression ) {
				Expression_Value exp = (Expression_Value) node;
				problems.report(exp.getLocation(), "Random number generation change the actual snapshot");
			}

			if(node instanceof AllComponentAliveExpression || node instanceof AllComponentRunningExpression ||
					node instanceof AnyComponentAliveExpression || node instanceof AnyComponentRunningExpression ||
					node instanceof ComponentAliveExpression || node instanceof ComponentRunningExpression ) {
				Expression_Value exp = (Expression_Value) node;
				problems.report(exp.getLocation(), "State of component may change during the actual snapshot");
			}

			if(node instanceof AnyPortCheckStateExpression ||node instanceof AllPortCheckSateExpression || node instanceof  CheckStateExpression) {
				Expression_Value exp = (Expression_Value) node;
				problems.report(exp.getLocation(), "State of port may change during the actual snapshot");
			}

			if(node instanceof AnyTimerRunningExpression || 
					node instanceof TimerRunningExpression || 
					node instanceof TimerReadExpression) {
				Expression_Value exp = (Expression_Value) node;
				problems.report(exp.getLocation(), "State of timer may change during the actual snapshot");
			}
			return V_CONTINUE;
		}

	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		FunctionVisitor visitor = new FunctionVisitor(problems);

		if (node instanceof AltGuard) {
			final AltGuard altGuard = (AltGuard) node;
			if (altGuard.getGuardExpression() != null) {
				altGuard.getGuardExpression().accept(visitor);
			}
		}

		if(
				node instanceof Receive_Port_Statement ||
				node instanceof Check_Receive_Port_Statement ||
				node instanceof Check_Port_Statement
				) {
			node.accept(visitor);
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = 
				new ArrayList<Class<? extends IVisitableNode>>(6);
		ret.add(Operation_Altguard.class);
		ret.add(Invoke_Altguard.class);
		ret.add(Referenced_Altguard.class);

		ret.add(Receive_Port_Statement.class);
		ret.add(Check_Receive_Port_Statement.class);
		ret.add(Check_Port_Statement.class);
		return ret;
	}
}
