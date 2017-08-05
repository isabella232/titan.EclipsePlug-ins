/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class SequenceOf_Value extends Value {
	private static final String NONNEGATIVEINDEXEXPECTED =
			"A non-negative integer value was expected instead of {0} for indexing a value of `record of'' type `{1}''";
	private static final String INDEXOVERFLOW =
			"Index overflow in a value of `record of'' type `{0}'': the index is {1}, but the value has only {2} elements";
	private static final String NOINDEX = "There is no value assigned to index {0} in the value `{1}''";

	private final Values values;

	private Value convertedValue;

	public SequenceOf_Value(final Values values) {
		this.values = values;

		if (values != null) {
			values.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Value_type getValuetype() {
		return Value_type.SEQUENCEOF_VALUE;
	}

	public boolean isIndexed() {
		return values.isIndexed();
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("{");
		if (isIndexed()) {
			for (int i = 0; i < values.getNofIndexedValues(); i++) {
				if (i > 0) {
					builder.append(", ");
				}

				final IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
				builder.append(indexedValue.createStringRepresentation());
			}
		} else {
			for (int i = 0; i < values.getNofValues(); i++) {
				if (i > 0) {
					builder.append(", ");
				}

				final IValue indexedValue = values.getValueByIndex(i);
				builder.append(indexedValue.createStringRepresentation());
			}
		}
		builder.append('}');

		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_UNDEFINED;
	}

	@Override
	/** {@inheritDoc} */
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference,
			final int actualSubReference, final IReferenceChain refChain) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			convertedValue = null;
		}

		if (convertedValue != null && convertedValue != this) {
			final IValue temp = convertedValue.getReferencedSubValue(timestamp, reference, actualSubReference, refChain);
			if (temp != null && temp.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			}

			return temp;
		}

		final IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp)) {
			return null;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			final Value arrayIndex = ((ArraySubReference) subreference).getValue();
			final IValue valueIndex = arrayIndex.getValueRefdLast(timestamp, refChain);
			if (valueIndex.isUnfoldable(timestamp)) {
				return null;
			}

			if (Value_type.INTEGER_VALUE.equals(valueIndex.getValuetype())) {
				final BigInteger index = ((Integer_Value) valueIndex).getValueValue();

				if (index.compareTo(BigInteger.ZERO) == -1) {
					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NONNEGATIVEINDEXEXPECTED, index, type.getTypename()));
					return null;
				}

				if (isIndexed()) {
					for (int i = 0; i < values.getNofIndexedValues(); i++) {
						IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
						indexedValue = indexedValue.getValueRefdLast(timestamp, refChain);

						if (Value_type.INTEGER_VALUE.equals(indexedValue.getValuetype())
								&& ((Integer_Value) indexedValue).getValueValue().compareTo(index) == 0) {
							return values.getIndexedValueByIndex(i).getValue().getReferencedSubValue(
									timestamp, reference, actualSubReference + 1, refChain);
						}
					}

					if (!reference.getUsedInIsbound()) {
						arrayIndex.getLocation().reportSemanticError(
								MessageFormat.format(NOINDEX, index, values.getFullName()));
					}
				} else if (index.compareTo(BigInteger.valueOf(values.getNofValues())) >= 0) {
					if (!reference.getUsedInIsbound()) {
						arrayIndex.getLocation().reportSemanticError(
								MessageFormat.format(INDEXOVERFLOW, type.getTypename(), index, values.getNofValues()));
					}
				} else {
					return values.getValueByIndex(index.intValue()).getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
				}

				return null;
			}

			arrayIndex.getLocation().reportSemanticError(ArraySubReference.INTEGERINDEXEXPECTED);
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(
					FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(), type.getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(ParameterisedSubReference.INVALIDVALUESUBREFERENCE);
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (values.isIndexed()) {
			for (int i = 0, size = values.getNofIndexedValues(); i < size; i++) {
				final IndexedValue temp = values.getIndexedValueByIndex(i);
				final IValue tempValue = temp.getValue();
				if (tempValue == null || tempValue.isUnfoldable(timestamp, expectedValue, referenceChain)) {
					return true;
				}
			}
		} else {
			for (int i = 0, size = values.getNofValues(); i < size; i++) {
				if (values.getValueByIndex(i).isUnfoldable(timestamp, expectedValue, referenceChain)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (convertedValue == null) {
			return this;
		}

		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			return this;
		}

		return convertedValue;
	}

	public int getNofComponents() {
		if (values.isIndexed()) {
			return values.getNofIndexedValues();
		}

		return values.getNofValues();
	}

	public IValue getValueByIndex(final int index) {
		if (values.isIndexed()) {
			return values.getIndexedValueByIndex(index).getValue();
		}

		return values.getValueByIndex(index);
	}

	public IValue getIndexByIndex(final int index) {
		if (values.isIndexed()) {
			return values.getIndexedValueByIndex(index).getIndex().getValue();
		}

		return null;
	}

	public Values getValues() {
		return values;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (values != null) {
			values.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Value setValuetype(final CompilationTimeStamp timestamp, final Value_type newType) {
		switch (newType) {
		case ARRAY_VALUE:
			convertedValue = new Array_Value(this);
			break;
		case SETOF_VALUE:
			convertedValue = new SetOf_Value(this);
			break;
		case SEQUENCE_VALUE:
			convertedValue = Sequence_Value.convert(timestamp, this);
			break;
		case SET_VALUE:
			convertedValue = new Set_Value(new NamedValues());
			if (getNofComponents() > 0) {
				convertedValue.setIsErroneous(true);
			}
			break;
		default:
			convertedValue = super.setValuetype(timestamp, newType);
			break;
		}

		return convertedValue;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (values.isIndexed()) {
				for (int i = 0, size = values.getNofIndexedValues(); i < size; i++) {
					referenceChain.markState();
					values.getIndexedValueByIndex(i).getValue().checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			} else {
				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					referenceChain.markState();
					values.getValueByIndex(i).checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (!Value_type.SEQUENCEOF_VALUE.equals(last.getValuetype())) {
			return false;
		}

		final SequenceOf_Value otherSequence = (SequenceOf_Value) last;
		if (values.isIndexed()) {
			if (otherSequence.isIndexed()) {
				if (values.getNofIndexedValues() != otherSequence.values.getNofIndexedValues()) {
					return false;
				}

				for (int i = 0, size = values.getNofIndexedValues(); i < size; i++) {
					final IndexedValue localTemp = values.getIndexedValueByIndex(i);
					final IValue indexValue = localTemp.getIndex().getValue();
					if (Value_type.INTEGER_VALUE.equals(indexValue.getValuetype())) {
						final Integer_Value integerValue = (Integer_Value) indexValue;
						final IValue otherValue = otherSequence.values.getIndexedValueByRealIndex(integerValue.intValue());
						if (otherValue == null || !localTemp.getValue().checkEquality(timestamp, otherValue)) {
							return false;
						}
					} else {
						return false;
					}
				}
			} else {
				if (values.getNofIndexedValues() != otherSequence.values.getNofValues()) {
					return false;
				}

				for (int i = 0, size = otherSequence.values.getNofValues(); i < size; i++) {
					final IValue value = values.getIndexedValueByRealIndex(i);
					if (value == null || !otherSequence.values.getValueByIndex(i).checkEquality(timestamp, value)) {
						return false;
					}
				}
			}
		} else {
			if (otherSequence.isIndexed()) {
				if (values.getNofValues() != otherSequence.values.getNofIndexedValues()) {
					return false;
				}

				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					final IValue otherValue = otherSequence.values.getIndexedValueByRealIndex(i);
					if (otherValue == null || !values.getValueByIndex(i).checkEquality(timestamp, otherValue)) {
						return false;
					}
				}
			} else {
				if (values.getNofValues() != otherSequence.values.getNofValues()) {
					return false;
				}

				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					if (!values.getValueByIndex(i).checkEquality(timestamp, otherSequence.values.getValueByIndex(i))) {
						return false;
					}
				}
			}
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (values != null) {
			values.updateSyntax(reparser, false);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean evaluateIsvalue(final boolean fromSequence) {
		if (values == null) {
			return true;
		}

		for (int i = 0, size = values.getNofValues(); i < size; i++) {
			if (!values.getValueByIndex(i).evaluateIsvalue(false)) {
				return false;
			}
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean evaluateIsbound(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return true;
		}

		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			convertedValue = null;
		}

		if (convertedValue != null && convertedValue != this) {
			return convertedValue.evaluateIsbound(timestamp, reference, actualSubReference);
		}

		final IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp)) {
			return false;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			final Value arrayIndex = ((ArraySubReference) subreference).getValue();
			IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue valueIndex = arrayIndex.getValueRefdLast(timestamp, referenceChain);
			referenceChain.release();
			if (valueIndex.isUnfoldable(timestamp)) {
				return false;
			}

			if (Value_type.INTEGER_VALUE.equals(valueIndex.getValuetype())) {
				final int index = ((Integer_Value) valueIndex).intValue();

				if (index < 0) {
					return false;
				}

				if (isIndexed()) {
					for (int i = 0; i < values.getNofIndexedValues(); i++) {
						IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
						referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
						indexedValue = indexedValue.getValueRefdLast(timestamp, referenceChain);
						referenceChain.release();

						if (Value_type.INTEGER_VALUE.equals(indexedValue.getValuetype())
								&& ((Integer_Value) indexedValue).intValue() == index) {
							return values.getIndexedValueByIndex(i).getValue().evaluateIsbound(timestamp, reference, actualSubReference + 1);
						}
					}
				} else if (index >= values.getNofValues()) {
					if (!reference.getUsedInIsbound()) {
						arrayIndex.getLocation().reportSemanticError(
								MessageFormat.format(INDEXOVERFLOW, type.getTypename(), index, values.getNofValues()));
					}
				} else {
					return values.getValueByIndex(index).evaluateIsbound(timestamp, reference, actualSubReference + 1);
				}

				return false;
			}

			return false;
		case fieldSubReference:
			return false;
		case parameterisedSubReference:
			return false;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean evaluateIspresent(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return true;
		}

		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			convertedValue = null;
		}

		if (convertedValue != null && convertedValue != this) {
			return convertedValue.evaluateIsbound(timestamp, reference, actualSubReference);
		}

		final IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp)) {
			return false;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			final Value arrayIndex = ((ArraySubReference) subreference).getValue();
			IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue valueIndex = arrayIndex.getValueRefdLast(timestamp, referenceChain);
			referenceChain.release();
			if (valueIndex.isUnfoldable(timestamp)) {
				return false;
			}

			if (Value_type.INTEGER_VALUE.equals(valueIndex.getValuetype())) {
				final int index = ((Integer_Value) valueIndex).intValue();

				if (index < 0) {
					return false;
				}

				if (isIndexed()) {
					for (int i = 0; i < values.getNofIndexedValues(); i++) {
						IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
						referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
						indexedValue = indexedValue.getValueRefdLast(timestamp, referenceChain);
						referenceChain.release();

						if (Value_type.INTEGER_VALUE.equals(indexedValue.getValuetype())
								&& ((Integer_Value) indexedValue).intValue() == index) {
							return values.getIndexedValueByIndex(i).getValue().evaluateIspresent(timestamp, reference, actualSubReference + 1);
						}
					}
				} else if (index >= values.getNofValues()) {
					if (!reference.getUsedInIsbound()) {
						arrayIndex.getLocation().reportSemanticError(
								MessageFormat.format(INDEXOVERFLOW, type.getTypename(), index, values.getNofValues()));
					}
				} else {
					return values.getValueByIndex(index).evaluateIspresent(timestamp, reference, actualSubReference + 1);
				}

				return false;
			}

			return false;
		case fieldSubReference:
			return false;
		case parameterisedSubReference:
			return false;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (values == null) {
			return;
		}

		values.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (values!=null && !values.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void setGenNamePrefix(final String prefix) {
		super.setGenNamePrefix(prefix);

		if (convertedValue != null) {
			convertedValue.setGenNamePrefix(prefix);
			return;
		}

		if (isIndexed()) {
			for (int i = 0; i < values.getNofIndexedValues(); i++) {
				values.getIndexedValueByIndex(i).getValue().setGenNamePrefix(prefix);
			}
		} else {
			for (int i = 0; i < values.getNofValues(); i++) {
				values.getValueByIndex(i).setGenNamePrefix(prefix);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setGenNameRecursive(final String parameterGenName) {
		super.setGenNameRecursive(parameterGenName);

		if (convertedValue != null) {
			convertedValue.setGenNameRecursive(parameterGenName);
			return;
		}

		if (isIndexed()) {
			for (int i = 0; i < values.getNofIndexedValues(); i++) {
				StringBuilder embeddedName = new StringBuilder(parameterGenName);
				embeddedName.append('[').append(i).append(']');
				values.getIndexedValueByIndex(i).getValue().setGenNameRecursive(embeddedName.toString());
			}
		} else {
			for (int i = 0; i < values.getNofValues(); i++) {
				StringBuilder embeddedName = new StringBuilder(parameterGenName);
				embeddedName.append('[').append(i).append(']');
				values.getValueByIndex(i).setGenNameRecursive(embeddedName.toString());
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		if (convertedValue != null) {
			return convertedValue.canGenerateSingleExpression();
		}

		if (values == null) {
			return false;
		}

		if (values.isIndexed()) {
			return values.getNofIndexedValues() == 0;
		}

		return values.getNofValues() == 0;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateSingleExpression(final JavaGenData aData) {
		if (convertedValue != null) {
			return convertedValue.generateSingleExpression(aData);
		}

		//TODO the empty record is so frequent that it is worth to handle in the library
		return new StringBuilder("NULL_VALUE");
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		if (convertedValue != null) {
			return convertedValue.generateCodeInit(aData, source, name);
		}

		if (isIndexed()) {
			final int nofIndexedValues = values.getNofIndexedValues();
			if (nofIndexedValues == 0) {
				source.append(MessageFormat.format("{0}.assign(null);\n", name)); //FIXME actual NULL_VALUE
			} else {
				final IType ofType = values.getIndexedValueByIndex(0).getValue().getMyGovernor();
				final String ofTypeName = ofType.getGenNameValue(aData, source, myScope);
				for (int i = 0; i < nofIndexedValues; i++) {
					final IndexedValue indexedValue = values.getIndexedValueByIndex(i);
					final String tempId1 = aData.getTemporaryVariableName();
					source.append("{\n");
					final Value index = indexedValue.getIndex().getValue();
					if (index.getValuetype().equals(Value_type.INTEGER_VALUE)) {
						source.append(MessageFormat.format("{0} {1} = {2}.getAt({3});\n", ofTypeName, tempId1, name, ((Integer_Value) index).getValue()));
					} else {
						final String tempId2 = aData.getTemporaryVariableName();
						source.append(MessageFormat.format("int {0};\n", tempId2));
						index.generateCodeInit(aData, source, tempId2);
						source.append(MessageFormat.format("{0} {1} = {2}.getAt({3});\n", ofTypeName, tempId1, name, tempId2));
					}
					indexedValue.getValue().generateCodeInit(aData, source, tempId1);
					source.append("}\n");
				}
			}
		} else {
			final int nofValues = values.getNofValues();
			if (nofValues == 0) {
				source.append(MessageFormat.format("{0}.assign(null);\n", name)); //FIXME actual NULL_VALUE
			} else {
				source.append(MessageFormat.format("{0}.setSize({1});\n", name, nofValues));
				final IType ofType = values.getValueByIndex(0).getMyGovernor();
				final String embeddedType = ofType.getGenNameValue(aData, source, myScope);
				
				for (int i = 0; i < nofValues; i++) {
					final IValue value = values.getValueByIndex(i);
					if (value.getValuetype().equals(Value_type.NOTUSED_VALUE)) {
						continue;
					} else //FIXME needs temporary reference branch (needs_temp_ref function missing)
					{
						final String embeddedName = MessageFormat.format("{0}.getAt({1})", name, i);
						value.generateCodeInit(aData, source, embeddedName);
					}
				}
			}
		}

		return source;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression) {
		if (convertedValue != null) {
			convertedValue.generateCodeExpression(aData, expression);
			return;
		}

		if (canGenerateSingleExpression()) {
			expression.expression.append(generateSingleExpression(aData));
			return;
		}

		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null) {
			governor = myLastSetGovernor;
		}

		String tempId = aData.getTemporaryVariableName();
		String genName = governor.getGenNameValue(aData, expression.expression, myScope);
		expression.preamble.append(MessageFormat.format("{0} {1} = new {0}();\n", genName, tempId));
		setGenNamePrefix(tempId);
		generateCodeInit(aData, expression.preamble, tempId);
		expression.expression.append(tempId);
	}
}
