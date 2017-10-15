/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Integer_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a length restriction for a range.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class RangeLenghtRestriction extends LengthRestriction {
	private static final String FULLNAMEPART1 = ".<lower>";
	private static final String FULLNAMEPART2 = ".<upper>";

	private final Value lower;
	private final Value upper;

	/** The time when this restriction was check the last time. */
	private CompilationTimeStamp lastTimeChecked;

	public RangeLenghtRestriction(final Value lower, final Value upper) {
		super();
		this.lower = lower;
		this.upper = upper;

		if (lower != null) {
			lower.setFullNameParent(this);
		}
		if (upper != null) {
			upper.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		if (lower == null) {
			return "<erroneous length restriction>";
		}

		final StringBuilder builder = new StringBuilder("length(");
		builder.append(lower.createStringRepresentation());
		builder.append(" .. ");
		if (upper != null) {
			builder.append(upper.createStringRepresentation());
		} else {
			builder.append("infinity");
		}
		builder.append(')');

		return builder.toString();
	}

	public IValue getLowerValue(final CompilationTimeStamp timestamp) {
		if (lower == null) {
			return null;
		}

		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = lower.getValueRefdLast(timestamp, chain);
		chain.release();

		return last;
	}

	public IValue getUpperValue(final CompilationTimeStamp timestamp) {
		if (upper == null) {
			return null;
		}

		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = upper.getValueRefdLast(timestamp, chain);
		chain.release();

		return last;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (lower != null) {
			lower.setMyScope(scope);
		}
		if (upper != null) {
			upper.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (lower != null) {
			lower.setCodeSection(codeSection);
		}
		if (upper != null) {
			upper.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (lower == child) {
			return builder.append(FULLNAMEPART1);
		} else if (upper == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		lastTimeChecked = timestamp;

		final Integer_Type integer = new Integer_Type();
		lower.setMyGovernor(integer);
		IValue last = integer.checkThisValueRef(timestamp, lower);
		integer.checkThisValueLimit(timestamp, last, expectedValue, false, false, true, false);

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue valueLower = last.getValueRefdLast(timestamp, chain);
		chain.release();

		if (last.getIsErroneous(timestamp)) {
			return;
		}

		BigInteger lowerInt;
		switch (valueLower.getValuetype()) {
		case INTEGER_VALUE: {
			lowerInt = ((Integer_Value) valueLower).getValueValue();
			if (lowerInt.compareTo(BigInteger.ZERO) == -1) {
				final String message = MessageFormat.format(
						"The lower boundary of the length restriction must be a non-negative integer value instead of {0}",
						lowerInt);
				valueLower.getLocation().reportSemanticError(message);
			}
			break;
		}
		default:
			lowerInt = BigInteger.ZERO;
			break;
		}

		if (upper == null) {
			return;
		}

		upper.setMyGovernor(integer);
		last = integer.checkThisValueRef(timestamp, upper);
		integer.checkThisValueLimit(timestamp, last, expectedValue, false, false, true, false);

		chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue valueUpper = last.getValueRefdLast(timestamp, chain);
		chain.release();

		if (last.getIsErroneous(timestamp)) {
			return;
		}

		BigInteger upperInt;
		switch (valueUpper.getValuetype()) {
		case INTEGER_VALUE: {
			upperInt = ((Integer_Value) valueUpper).getValueValue();
			if (upperInt.compareTo(BigInteger.ZERO) == -1) {
				final String message = MessageFormat.format(
						"The upper boundary of the length restriction must be a non-negative integer value instead of {0}",
						upperInt);
				valueUpper.getLocation().reportSemanticError(message);
			} else if (upperInt.compareTo(lowerInt) == -1) {
				getLocation().reportSemanticError(
						MessageFormat.format(
								"The upper boundary of the length restriction ({0}) cannot be smaller than the lower boundary {1}",
								upperInt, lowerInt));
			}
			break;
		}
		default:
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkArraySize(final CompilationTimeStamp timestamp, final ArrayDimension dimension) {
		if (lastTimeChecked == null || dimension.getIsErroneous(timestamp)) {
			return;
		}

		boolean errorFlag = false;
		final long arraySize = dimension.getSize();

		IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue lowerLast = lower.getValueRefdLast(timestamp, chain);
		chain.release();

		if (Value_type.INTEGER_VALUE.equals(lowerLast.getValuetype()) && !lowerLast.getIsErroneous(timestamp)) {
			final BigInteger length = ((Integer_Value) lowerLast).getValueValue();
			if (length.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
				final String message = MessageFormat
						.format("An integer value less then `{0}'' was expected as the lower boundary of the length restriction instead of `{1}''",
								Integer.MAX_VALUE, length);
				lower.getLocation().reportSemanticError(message);
				errorFlag = true;
			} else if (length.compareTo(BigInteger.valueOf(arraySize)) == 1) {
				final String message = MessageFormat
						.format("There number of elements allowed by the length restriction (at least {0}) contradicts the array size ({1})",
								length, arraySize);
				lower.getLocation().reportSemanticError(message);
				errorFlag = true;
			}
		}

		if (upper != null) {
			chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue upperLast = upper.getValueRefdLast(timestamp, chain);
			chain.release();

			if (Value_type.INTEGER_VALUE.equals(upperLast.getValuetype()) && !upperLast.getIsErroneous(timestamp)) {
				final BigInteger length = ((Integer_Value) upperLast).getValueValue();
				if (length.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
					final String message = MessageFormat.format(
							"An integer value less then `{0}'' was expected as the upper boundary of the length restriction instead of `{1}''",
							Integer.MAX_VALUE, length);
					upper.getLocation()
					.reportSemanticError(message
							);
					errorFlag = true;
				} else if (length.compareTo(BigInteger.valueOf(arraySize)) == 1) {
					final String message = MessageFormat
							.format("There number of elements allowed by the length restriction (at most {0}) contradicts the array size ({1})",
									length, arraySize);
					upper.getLocation().reportSemanticError(message);
					errorFlag = true;
				}
			}
		}

		if (!errorFlag) {
			getLocation().reportSemanticWarning("Length restriction is useless for an array template");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkNofElements(final CompilationTimeStamp timestamp, final int nofElements, final boolean lessAllowed,
			final boolean moreAllowed, final boolean hasAnyornone, final ILocateableNode locatable) {
		if (lower == null) {
			return;
		}

		if (!lessAllowed) {
			final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue last = lower.getValueRefdLast(timestamp, chain);
			chain.release();

			if (Value_type.INTEGER_VALUE.equals(last.getValuetype()) && !last.getIsErroneous(timestamp)) {
				final BigInteger length = ((Integer_Value) last).getValueValue();
				if (length.compareTo(BigInteger.valueOf(nofElements)) == 1) {
					final String message = MessageFormat.format(
							"There are fewer ({0}) elements than it is allowed by the length restriction (at least {1})",
							nofElements, length);
					locatable.getLocation().reportSemanticError(message);
				}
			}
		}

		if (upper == null) {
			return;
		}

		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = upper.getValueRefdLast(timestamp, chain);
		chain.release();

		if (Value_type.INTEGER_VALUE.equals(last.getValuetype()) && !last.getIsErroneous(timestamp)) {
			final BigInteger length = ((Integer_Value) last).getValueValue();
			if (length.compareTo(BigInteger.valueOf(nofElements)) == -1 && !moreAllowed) {
				final String message = MessageFormat.format(
						"There are more ({0} {1}) elements than it is allowed by the length restriction ({2})",
						hasAnyornone ? "at least" : "", nofElements, length);
				locatable.getLocation().reportSemanticError(message);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (lower != null) {
			lower.updateSyntax(reparser, false);
			reparser.updateLocation(lower.getLocation());
		}

		if (upper != null) {
			upper.updateSyntax(reparser, false);
			reparser.updateLocation(upper.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (lower != null) {
			lower.findReferences(referenceFinder, foundIdentifiers);
		}
		if (upper != null) {
			upper.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (lower != null && !lower.accept(v)) {
			return false;
		}
		if (upper != null && !upper.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (lower != null) {
			lower.reArrangeInitCode(aData, source, usageModule);
		}
		if (upper != null) {
			upper.reArrangeInitCode(aData, source, usageModule);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		if (lower != null && !isInfinity(lower)) {
			final ExpressionStruct expression = new ExpressionStruct();
			lower.generateCodeExpression(aData, expression);
			source.append(MessageFormat.format("{0}.set_min_length({1});\n", name, expression.expression));
		}
		if (upper != null && !isInfinity(upper)) {
			final ExpressionStruct expression = new ExpressionStruct();
			upper.generateCodeExpression(aData, expression);
			source.append(MessageFormat.format("{0}.set_max_length({1});\n", name, expression.expression));
		}
	}

	/**
	 * @param value possible infinity value
	 * @return true if value is negative or positive infinity
	 */
	private static boolean isInfinity( final Value value ) {
		//INFINITY value is stored as a Real_Value
		//see Ttcn3Parser.g4 pr_FloatValue
		if ( !(value instanceof Real_Value) ) {
			return false;
		}
		final Real_Value real = (Real_Value)value;
		return Double.isInfinite( real.getValue() );
	}
}
