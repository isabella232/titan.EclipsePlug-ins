/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Array_Value extends Value {
	private static final String NOINDEX = "There is no value assigned to index {0} in the value `{1}''";

	private final Values values;

	protected Array_Value(final SequenceOf_Value original) {
		copyGeneralProperties(original);
		values = original.getValues();
	}

	@Override
	/** {@inheritDoc} */
	public Value_type getValuetype() {
		return Value_type.ARRAY_VALUE;
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

	public boolean isIndexed() {
		return values.isIndexed();
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

		final IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp) || !Type_type.TYPE_ARRAY.equals(type.getTypetype())) {
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
				final ArrayDimension dimension = ((Array_Type) type).getDimension();
				dimension.checkIndex(timestamp, valueIndex, Expected_Value_type.EXPECTED_CONSTANT);
				if (dimension.getIsErroneous(timestamp)) {
					return null;
				}

				final int index = ((Integer_Value) valueIndex).intValue() - (int) dimension.getOffset();
				if (isIndexed()) {
					for (int i = 0; i < values.getNofIndexedValues(); i++) {
						IValue indexedValue = values.getIndexedValueByIndex(i).getIndex().getValue();
						indexedValue = indexedValue.getValueRefdLast(timestamp, refChain);

						if (Value_type.INTEGER_VALUE.equals(indexedValue.getValuetype())
								&& ((Integer_Value) indexedValue).intValue() == index) {
							return values.getIndexedValueByIndex(i).getValue().getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
						}
					}

					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
				} else if (index < 0 || index >= values.getNofValues()) {
					//the error was already reported
				} else {
					return values.getValueByIndex(index).getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
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
			for (int i = 0, size = values.getNofIndexedValues(); i < size; ++i) {
				if (values.getIndexedValueByIndex(i).isUnfoldable(timestamp, expectedValue, referenceChain)) {
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

	public Value getIndexByIndex(final int index) {
		if (values.isIndexed()) {
			return values.getIndexedValueByIndex(index).getIndex().getValue();
		}

		return null;
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
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (values != null) {
			values.setCodeSection(codeSection);
		}
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

		if (!Value_type.ARRAY_VALUE.equals(last.getValuetype())) {
			return false;
		}

		final Array_Value otherArray = (Array_Value) last;
		if (values.isIndexed()) {
			if (otherArray.isIndexed()) {
				if (values.getNofIndexedValues() != otherArray.values.getNofIndexedValues()) {
					return false;
				}

				for (int i = 0, size = values.getNofIndexedValues(); i < size; i++) {
					final IndexedValue localTemp = values.getIndexedValueByIndex(i);
					final IValue indexValue = localTemp.getIndex().getValue();
					if (Value_type.INTEGER_VALUE.equals(indexValue.getValuetype())) {
						final Integer_Value integerValue = (Integer_Value) indexValue;
						final IValue otherValue = otherArray.values.getIndexedValueByRealIndex(integerValue.intValue());
						if (otherValue == null || !localTemp.getValue().checkEquality(timestamp, otherValue)) {
							return false;
						}
					} else {
						return false;
					}
				}
			} else {
				if (values.getNofIndexedValues() != otherArray.values.getNofValues()) {
					return false;
				}

				for (int i = 0, size = otherArray.values.getNofValues(); i < size; i++) {
					final IValue value = values.getIndexedValueByRealIndex(i);
					if (value == null || !otherArray.values.getValueByIndex(i).checkEquality(timestamp, value)) {
						return false;
					}
				}
			}
		} else {
			if (otherArray.isIndexed()) {
				if (values.getNofValues() != otherArray.values.getNofIndexedValues()) {
					return false;
				}

				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					final IValue otherValue = otherArray.values.getIndexedValueByRealIndex(i);
					if (otherValue == null || !values.getValueByIndex(i).checkEquality(timestamp, otherValue)) {
						return false;
					}
				}
			} else {
				if (values.getNofValues() != otherArray.values.getNofValues()) {
					return false;
				}

				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					if (!values.getValueByIndex(i).checkEquality(timestamp, otherArray.values.getValueByIndex(i))) {
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

		final IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp) || !Type_type.TYPE_ARRAY.equals(type.getTypetype())) {
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

					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
				} else if (index < 0 || index >= values.getNofValues()) {
					//the error was already reported
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

		final IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp) || !Type_type.TYPE_ARRAY.equals(type.getTypetype())) {
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

					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
				} else if (index < 0 || index >= values.getNofValues()) {
					//the error was already reported
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

		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null) {
			governor = myLastSetGovernor;
		}

		if (governor == null) {
			return;
		}

		final IType type = governor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (!Type_type.TYPE_ARRAY.equals(type.getTypetype())) {
			return;
		}

		final long offset = ((Array_Type) type).getDimension().getOffset();
		if (isIndexed()) {
			for (int i = 0; i < values.getNofIndexedValues(); i++) {
				final StringBuilder embeddedName = new StringBuilder(parameterGenName);
				embeddedName.append(".getAt(").append(offset + i).append(')');
				values.getIndexedValueByIndex(i).getValue().setGenNameRecursive(embeddedName.toString());
			}
		} else {
			for (int i = 0; i < values.getNofValues(); i++) {
				final StringBuilder embeddedName = new StringBuilder(parameterGenName);
				embeddedName.append(".getAt(").append(offset + i).append(')');
				values.getValueByIndex(i).setGenNameRecursive(embeddedName.toString());
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null) {
			governor = myLastSetGovernor;
		}

		final IType lastType = governor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (isIndexed()) {
			final int nofIndexedValues = values.getNofIndexedValues();
			if (nofIndexedValues == 0) {
				aData.addBuiltinTypeImport("TitanNull_Type");

				source.append(MessageFormat.format("{0}.assign(TitanNull_Type.NULL_VALUE);\n", name));
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
						source.append(MessageFormat.format("TitanInteger {0} = new TitanInteger();\n", tempId2));
						index.generateCodeInit(aData, source, tempId2);
						source.append(MessageFormat.format("{0} {1} = {2}.getAt({3});\n", ofTypeName, tempId1, name, tempId2));
					}
					indexedValue.getValue().generateCodeInit(aData, source, tempId1);
					source.append("}\n");
				}
			}
		} else {
			final int nofValues = values.getNofValues();
			if (!Type_type.TYPE_ARRAY.equals(lastType.getTypetype())) {
				return source;
			}

			final long indexOffset = ((Array_Type) lastType).getDimension().getOffset();
			lastType.getGenNameValue(aData, source, myScope);

			for (int i = 0; i < nofValues; i++) {
				final IValue value = values.getValueByIndex(i);
				if (value.getValuetype().equals(Value_type.NOTUSED_VALUE)) {
					continue;
				} else // FIXME needs temporary reference branch
					// (needs_temp_ref function missing)
				{
					final String embeddedName = MessageFormat.format("{0}.getAt({1})", name, indexOffset + i);
					value.generateCodeInit(aData, source, embeddedName);
				}
			}
		}

		return source;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final boolean forceObject) {
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

		final IType lastType = governor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		final String tempId = aData.getTemporaryVariableName();
		final String genName = lastType.getGenNameValue(aData, expression.expression, myScope);
		expression.preamble.append(MessageFormat.format("{0} {1} = new {0}();\n", genName, tempId));

		setGenNamePrefix(tempId);
		generateCodeInit(aData, expression.preamble, tempId);
		expression.expression.append(tempId);
	}
}
