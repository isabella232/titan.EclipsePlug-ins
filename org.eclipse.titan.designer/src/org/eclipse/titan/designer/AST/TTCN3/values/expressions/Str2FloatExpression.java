/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Str2FloatExpression extends Expression_Value {
	private static final String OPERANDERROR1 = "The operand of the `str2float' operation should be a charstring value";
	private static final String OPERANDERROR2 = "The operand of the `str2float' operation should be a string containing a valid float value";

	private final Value value;

	private static enum str2floatState { S_INITIAL, S_FIRST_M, S_ZERO_M, S_MORE_M, S_FIRST_F, S_MORE_F,
		S_INITIAL_E, S_FIRST_E, S_ZERO_E, S_MORE_E, S_END, S_ERR }

	public Str2FloatExpression(final Value value) {
		this.value = value;

		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.STR2FLOAT_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		return value != null && value.checkExpressionSelfReferenceValue(timestamp, lhs);
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append("str2float(").append(value.createStringRepresentation()).append(')');
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value != null) {
			value.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (value != null) {
			value.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (value == child) {
			return builder.append(OPERAND);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_REAL;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (value == null) {
			return true;
		}

		return value.isUnfoldable(timestamp, expectedValue, referenceChain);
	}

	/**
	 * Checks the parameters of the expression and if they are valid in
	 * their position in the expression or not.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of value expected.
	 * @param referenceChain
	 *                a reference chain to detect cyclic references.
	 * */
	private void checkExpressionOperands(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (value == null) {
			return;
		}

		value.setLoweridToReference(timestamp);
		final Type_type tempType = value.getExpressionReturntype(timestamp, expectedValue);

		switch (tempType) {
		case TYPE_CHARSTRING:
			final IValue last = value.getValueRefdLast(timestamp, expectedValue, referenceChain);
			if (!last.isUnfoldable(timestamp)) {
				String string = ((Charstring_Value) last).getValue();
				string = string.trim();
				if ("infinity".equals(string) || "-infinity".equals(string) || "not_a_number".equals(string)) {
					return;
				}
				str2floatState state = str2floatState.S_INITIAL;
				// state: expected characters
				// S_INITIAL: +, -, first digit of integer part in mantissa,
				//            leading whitespace
				// S_FIRST_M: first digit of integer part in mantissa
				// S_ZERO_M, S_MORE_M: more digits of mantissa, decimal dot, E
				// S_FIRST_F: first digit of fraction
				// S_MORE_F: more digits of fraction, E, trailing whitespace
				// S_INITIAL_E: +, -, first digit of exponent
				// S_FIRST_E: first digit of exponent
				// S_ZERO_E, S_MORE_E: more digits of exponent, trailing whitespace
				// S_END: trailing whitespace
				// S_ERR: error was found, stop
				for (int i = 0; i < string.length(); i++) {
					final char c = string.charAt(i);
					switch (state) {
					case S_INITIAL:
						if(c == '+' || c == '-') {
							state = str2floatState.S_FIRST_M;
						} else if(c == '0') {
							state = str2floatState.S_ZERO_M;
						} else if(c >= '1' && c <= '9') {
							state = str2floatState.S_MORE_M;
						} else if(Character.isWhitespace(c)) {
							value.getLocation().reportSemanticWarning("Leading whitespace was detected and ignored in the operand of operation `str2float''");
						} else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_FIRST_M:  // first mantissa digit
						if(c == '0') {
							state = str2floatState.S_ZERO_M;
						} else if(c >= '1' && c <= '9') {
							state = str2floatState.S_MORE_M;
						} else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_ZERO_M: // leading mantissa zero
						if(c == '.') {
							state = str2floatState.S_FIRST_F;
						} else if (c == 'E' || c == 'e'){
							state = str2floatState.S_INITIAL_E;
						} else if (c >= '0' && c <= '9') {
							value.getLocation().reportSemanticWarning("Leading zero digit was detected and ignored in the operand of operation `str2float''");
							state = str2floatState.S_MORE_M;
						} else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_MORE_M:
						if(c == '.') {
							state = str2floatState.S_FIRST_F;
						} else if (c == 'E' || c == 'e') {
							state = str2floatState.S_INITIAL_E;
						} else if(c >= '0' && c <= '9') {}
						else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_FIRST_F:
						if(c >= '0' && c <= '9') {
							state = str2floatState.S_MORE_F;
						} else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_MORE_F:
						if (c == 'E' || c == 'e') {
							state = str2floatState.S_INITIAL_E;
						} else if (c >= '0' && c <= '9') {}
						else if(Character.isWhitespace(c)) {
							state = str2floatState.S_END;
						} else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_INITIAL_E:
						if (c == '+' || c == '-') {
							state = str2floatState.S_FIRST_E;
						} else if(c == '0') {
							state = str2floatState.S_ZERO_E;
						} else if(c >= '1' && c <= '9') {
							state = str2floatState.S_MORE_E;
						} else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_FIRST_E:
						if(c == '0') {
							state = str2floatState.S_ZERO_E;
						} else if(c >= '1' && c <= '9') {
							state = str2floatState.S_MORE_E;
						} else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_ZERO_E:
						if (c >= '0' && c <= '9') {
							value.getLocation().reportSemanticWarning("Leading zero digit was detected and ignored in the exponent of the operation `str2float''");
							state = str2floatState.S_MORE_E;
						} else if(Character.isWhitespace(c)) {
							state = str2floatState.S_END;
						} else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_MORE_E:
						if (c >= '0' && c <= '9') {}
						else if(Character.isWhitespace(c)) {
							state = str2floatState.S_END;
						} else {
							state = str2floatState.S_ERR;
						}
						break;
					case S_END:
						if(Character.isWhitespace(c)) {
							state = str2floatState.S_ERR;
						}
						break;
					default:
						break;
					}
					if(state == str2floatState.S_ERR) {
						value.getLocation().reportSemanticError(MessageFormat.format("The argument of function str2float(), which is {0}, does not represent a valid float value. Invalid character {1} was found at index {2}. ", string,c,i));
						setIsErroneous(true);
						break;
					}
				}
				switch (state) {
				case S_INITIAL:
					value.getLocation().reportSemanticError(MessageFormat.format("The argument of function str2float(), which is {0}, should be a string containing a valid float value instead of an empty string.", string));
					setIsErroneous(true);
					break;
				case S_FIRST_M:
					value.getLocation().reportSemanticError(MessageFormat.format("The argument of function str2float(), which is {0}, should be a string containing a valid float value, but only a sign character was detected.", string));
					setIsErroneous(true);
					break;
				case S_ZERO_M:
				case S_MORE_M:
					// OK now (decimal dot missing after mantissa)
					break;
				case S_FIRST_F:
					// OK now (fraction part missing)
					break;
				case S_INITIAL_E:
				case S_FIRST_E:
					value.getLocation().reportSemanticError(MessageFormat.format("The argument of function str2float(), which is {0}, should be a string containing a valid float value, but the exponent is missing after the `E' sign.", string));
					setIsErroneous(true);
					break;
				case S_END:
					// trailing whitespace is ok.
					break;
				default:
					break;
				}
			}
			return;
		case TYPE_UNDEFINED:
			setIsErroneous(true);
			return;
		default:
			if (!isErroneous) {
				location.reportSemanticError(OPERANDERROR1);
				setIsErroneous(true);
			}
			return;
		}
	}

	@Override
	/** {@inheritDoc} */
	public IValue evaluateValue(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return lastValue;
		}

		isErroneous = false;
		lastTimeChecked = timestamp;
		lastValue = this;

		if (value == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		if (getIsErroneous(timestamp) || isUnfoldable(timestamp, referenceChain)) {
			return lastValue;
		}

		final IValue last = value.getValueRefdLast(timestamp, referenceChain);
		if (last.getIsErroneous(timestamp)) {
			return lastValue;
		}

		switch (last.getValuetype()) {
		case CHARSTRING_VALUE: {
			String string = ((Charstring_Value) last).getValue();
			string = string.trim();

			double number;
			if ("-infinity".equals(string)) {
				number = Float.NEGATIVE_INFINITY;
			} else if ("infinity".equals(string)) {
				number = Float.POSITIVE_INFINITY;
			} else if ("not_a_number".equals(string)) {
				number = Float.NaN;
			} else {
				try {
					number = Double.parseDouble(string);
				} catch (NumberFormatException e) {
					number = 0;
				}
			}
			lastValue = new Real_Value(number);
			break;
		}
		default:
			return this;
		}

		lastValue.copyGeneralProperties(this);
		return lastValue;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && value != null) {
			referenceChain.markState();
			value.checkRecursions(timestamp, referenceChain);
			referenceChain.previousState();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value == null) {
			return;
		}

		value.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (value != null && !value.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (value != null) {
			value.reArrangeInitCode(aData, source, usageModule);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		return value.canGenerateSingleExpression();
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		aData.addCommonLibraryImport("AdditionalFunctions");

		expression.expression.append("AdditionalFunctions.str2float( ");
		value.generateCodeExpressionMandatory(aData, expression, false);
		expression.expression.append(')');
	}
}
