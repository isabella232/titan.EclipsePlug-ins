/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
/**
 * @author Kristof Szabados
 * */
public final class Stop_Component_Statement extends Statement {
	private static final String FULLNAMEPART = ".componentreference";
	private static final String STATEMENT_NAME = "stop test component";

	private final IValue componentReference;

	public Stop_Component_Statement(final IValue componentReference) {
		this.componentReference = componentReference;

		if (componentReference != null) {
			componentReference.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_STOP_COMPONENT;
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

		if (componentReference == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		if (componentReference != null) {
			componentReference.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		if (componentReference != null) {
			final IValue last = componentReference.getValueRefdLast(timestamp, null);
			if (Value_type.EXPRESSION_VALUE.equals(last.getValuetype())) {
				switch (((Expression_Value) last).getOperationType()) {
				case SELF_COMPONENT_OPERATION:
				case MTC_COMPONENT_OPERATION:
					return true;
				default:
					break;
				}
			}
		}

		return super.isTerminating(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		Port_Utility.checkComponentReference(timestamp, this, componentReference, true, false);

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (componentReference instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) componentReference).updateSyntax(reparser, false);
			reparser.updateLocation(componentReference.getLocation());
		} else if (componentReference != null) {
			throw new ReParseException();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReference == null) {
			return;
		}

		componentReference.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (componentReference != null && !componentReference.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		aData.addCommonLibraryImport("TTCN_Runtime");

		final ExpressionStruct expression = new ExpressionStruct();

		if (componentReference == null) {
			aData.addBuiltinTypeImport("TitanComponent");

			expression.expression.append("TTCN_Runtime.stop_component(TitanComponent.ALL_COMPREF)");
		} else {
			final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue last = componentReference.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), referenceChain);
			referenceChain.release();

			if (last.getValuetype() == Value_type.REFERENCED_VALUE) {
				// the argument is a simple component reference
				last.generateCodeExpressionMandatory(aData, expression, true);
				expression.expression.append(".stop()");
			} else {
				boolean refers_to_self = false;
				if (last.getValuetype() == Value_type.EXPRESSION_VALUE) {
					// the argument is a special component reference (mtc, self, etc.)
					switch(((Expression_Value)last).getOperationType()) {
					case MTC_COMPONENT_OPERATION: {
						final Definition myDefinition = myStatementBlock.getMyDefinition();

						if (myDefinition != null && myDefinition.getAssignmentType() == Assignment_type.A_TESTCASE) {
							refers_to_self = true;
						}
						break;
					}
					case SELF_COMPONENT_OPERATION:
						refers_to_self = true;
					default:
						break;
					}
				}
				if (refers_to_self) {
					expression.expression.append("TTCN_Runtime.stop_execution()");
				} else {
					expression.expression.append("TTCN_Runtime.stop_component(");
					last.generateCodeExpression(aData, expression, false);
					expression.expression.append(')');
				}
			}
		}
		expression.mergeExpression(source);
	}
}
