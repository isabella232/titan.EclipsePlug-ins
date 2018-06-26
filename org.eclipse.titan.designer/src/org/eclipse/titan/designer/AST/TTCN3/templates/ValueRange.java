/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Class to represent a TTCN-3 ValueRange objects.
 *
 * @author Kristof Szabados
 * */
public final class ValueRange extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART1 = ".<lower_boundary>";
	private static final String FULLNAMEPART2 = ".<lower_boundary>";

	private final Value min;
	private final boolean minExclusive;
	private final Value max;
	private final boolean maxExclusive;

	private Type_type typeType;

	public ValueRange(final Value min, final boolean minExclusive, final Value max, final boolean maxExclusive) {
		super();
		this.min = min;
		this.minExclusive = minExclusive;
		this.max = max;
		this.maxExclusive = maxExclusive;

		if (min != null) {
			min.setFullNameParent(this);
		}
		if (max != null) {
			max.setFullNameParent(this);
		}
	}

	public Value getMin() {
		return min;
	}

	public boolean isMinExclusive() {
		return minExclusive;
	}

	public Value getMax() {
		return max;
	}

	public boolean isMaxExclusive() {
		return maxExclusive;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (min != null) {
			min.setMyScope(scope);
		}
		if (max != null) {
			max.setMyScope(scope);
		}
	}

	/**
	 * Sets the code_section attribute for this range to the provided value.
	 *
	 * @param codeSection the code section where this range should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (min != null) {
			min.setCodeSection(codeSection);
		}
		if (max != null) {
			max.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (min == child) {
			return builder.append(FULLNAMEPART1);
		} else if (max == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	/**
	 * Creates and returns a string representation if the range.
	 *
	 * @return the string representation of the range.
	 * */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append('(');
		if (min == null) {
			builder.append("-infinity");
		} else {
			builder.append(min.createStringRepresentation());
		}
		builder.append(" .. ");
		if (max == null) {
			builder.append("infinity");
		} else {
			builder.append(max.createStringRepresentation());
		}
		builder.append(')');
		return builder.toString();
	}

	/**
	 * Calculates the governor of this value range.
	 *
	 * @param timestamp
	 *                the actual semantic checking cycle
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 *
	 * @return the governor of the value range
	 * */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (min != null) {
			final IType type = min.getExpressionGovernor(timestamp, expectedValue);
			if (type != null) {
				return type;
			}
		}

		if (max != null) {
			final IType type = max.getExpressionGovernor(timestamp, expectedValue);
			if (type != null) {
				return type;
			}
		}

		return null;
	}

	/**
	 * Calculates the returning type of this value range.
	 *
	 * @param timestamp
	 *                the actual semantic checking cycle
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 *
	 * @return the returning type of the value range
	 * */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (min != null) {
			final Type_type type = min.getExpressionReturntype(timestamp, expectedValue);
			if (!Type_type.TYPE_UNDEFINED.equals(type)) {
				return type;
			}
		}

		if (max != null) {
			final Type_type type = max.getExpressionReturntype(timestamp, expectedValue);
			if (!Type_type.TYPE_UNDEFINED.equals(type)) {
				return type;
			}
		}

		return Type_type.TYPE_UNDEFINED;
	}

	/**
	 * Check that the value range - being used as the RHS - refers to the LHS of the assignment.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param lhs
	 *                the assignment to check against
	 * @return true if self-assignment
	 * */
	public boolean checkExpressionSelfReferenceValue(final CompilationTimeStamp timestamp, final Assignment lhs) {
		if (min != null && min.checkExpressionSelfReferenceValue(timestamp, lhs)) {
			return true;
		}
		if (max != null && max.checkExpressionSelfReferenceValue(timestamp, lhs)) {
			return true;
		}

		return false;
	}

	/**
	 * Handles the incremental parsing of this value range.
	 *
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (min != null) {
			min.updateSyntax(reparser, false);
			reparser.updateLocation(min.getLocation());
		}

		if (max != null) {
			max.updateSyntax(reparser, false);
			reparser.updateLocation(max.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (min != null) {
			min.findReferences(referenceFinder, foundIdentifiers);
		}
		if (max != null) {
			max.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (min != null && !min.accept(v)) {
			return false;
		}
		if (max != null && !max.accept(v)) {
			return false;
		}
		return true;
	}

	public void setTypeType(final Type_type typeType) {
		this.typeType = typeType;
	}

	/**
	 * Walks through the range limits recursively and appends the java
	 * initialization sequence of all (directly or indirectly) referenced
	 * non-parameterized templates and the default values of all
	 * parameterized templates to source and returns the resulting string.
	 * Only objects belonging to module usageModule are initialized.
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source for code generated
	 * @param usageModule the module where the template is to be used.
	 * */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (min != null) {
			min.reArrangeInitCode(aData, source, usageModule);
		}

		if (max != null) {
			max.reArrangeInitCode(aData, source, usageModule);
		}
	}

	/**
	 * Add generated java code for initializing a template
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source for code generated
	 * @param name the name to init
	 */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		aData.addBuiltinTypeImport( "Base_Template.template_sel" );

		final ExpressionStruct expression = new ExpressionStruct();
		final StringBuilder initStatement = new StringBuilder();
		initStatement.append(name);
		initStatement.append(".setType( template_sel.VALUE_RANGE );\n");
		if(min != null) {
			final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue last = min.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), chain);
			chain.release();

			if (!last.getValuetype().equals(Value_type.REAL_VALUE) || ((Real_Value) last).getValue() != Double.NEGATIVE_INFINITY) {
				last.generateCodeExpression(aData, expression, false);
				initStatement.append(name);
				initStatement.append(".setMin( ");
				initStatement.append(expression.expression);
				initStatement.append(" );\n");
			}
		}
		if(minExclusive) {
			switch(typeType) {
			case TYPE_INTEGER:
			case TYPE_REAL:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
				initStatement.append(name);
				initStatement.append(".setMinExclusive(true);\n");
				break;
			default:
				ErrorReporter.INTERNAL_ERROR("FATAL ERROR while processing lower bound `" + getFullName() + "''");
			}
		}

		if(max != null) {
			final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue last = max.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), chain);
			chain.release();

			if (!last.getValuetype().equals(Value_type.REAL_VALUE) || ((Real_Value) last).getValue() != Double.POSITIVE_INFINITY) {
				expression.expression = new StringBuilder();
				last.generateCodeExpression(aData, expression, false);
				initStatement.append(name);
				initStatement.append(".setMax( ");
				initStatement.append(expression.expression);
				initStatement.append(" );\n");
			}
		}
		if(maxExclusive) {
			switch(typeType) {
			case TYPE_INTEGER:
			case TYPE_REAL:
			case TYPE_CHARSTRING:
			case TYPE_UCHARSTRING:
				initStatement.append(name);
				initStatement.append(".setMaxExclusive(true);\n");
				break;
			default:
				ErrorReporter.INTERNAL_ERROR("FATAL ERROR while processing upper bound `" + getFullName() + "''");
			}
		}

		if(expression.preamble.length() > 0 || expression.postamble.length() > 0) {
			source.append("{\n");
			source.append(expression.preamble);
			source.append(initStatement);
			source.append(expression.postamble);
			source.append("}\n");
		} else {
			source.append(initStatement);
		}
	}
}
