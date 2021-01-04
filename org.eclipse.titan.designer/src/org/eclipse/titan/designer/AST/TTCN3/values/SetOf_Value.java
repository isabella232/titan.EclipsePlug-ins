/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class SetOf_Value extends Value {
	private static final String NONNEGATIVEINDEXEXPECTED =
			"A non-negative integer value was expected instead of {0} for indexing a value of `set of'' type `{1}''";
	private static final String INDEXOVERFLOW =
			"Index overflow in a value of `set of'' type `{0}'': the index is {1}, but the value has only {2} elements";
	private static final String NOINDEX = "There is no value assigned to index {0} in the value `{1}''";

	private final Values values;

	public SetOf_Value(final Values values) {
		this.values = values;

		if (values != null) {
			values.setFullNameParent(this);
		}
	}

	protected SetOf_Value(final SequenceOf_Value original) {
		copyGeneralProperties(original);
		values = original.getValues();
	}

	@Override
	/** {@inheritDoc} */
	public Value_type getValuetype() {
		return Value_type.SETOF_VALUE;
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

					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
				} else if (index.compareTo(BigInteger.valueOf(values.getNofValues())) >= 0) {
					arrayIndex.getLocation().reportSemanticError(
							MessageFormat.format(INDEXOVERFLOW, type.getTypename(), index, values.getNofValues()));
				} else {
					return values.getValueByIndex(index.intValue()).getReferencedSubValue(timestamp, reference,
							actualSubReference + 1, refChain);
				}

				return null;
			}

			arrayIndex.getLocation().reportSemanticError(ArraySubReference.INTEGERINDEXEXPECTED);
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE,
							((FieldSubReference) subreference).getId().getDisplayName(), type.getTypename()));
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

		if (!Value_type.SETOF_VALUE.equals(last.getValuetype())) {
			return false;
		}

		final SetOf_Value otherSetof = (SetOf_Value) last;

		if (isIndexed()) {
			if (otherSetof.isIndexed()) {
				if (values.getNofIndexedValues() != otherSetof.values.getNofIndexedValues()) {
					return false;
				}

				final List<Integer> indicesuncovered = new ArrayList<Integer>();
				for (int i = 0; i < values.getNofIndexedValues(); i++) {
					indicesuncovered.add(i);
				}
				for (int i = values.getNofIndexedValues() - 1; i >= 0; i--) {
					final IndexedValue localTemp = values.getIndexedValueByIndex(i);
					boolean found = false;
					for (int j = indicesuncovered.size() - 1; j >= 0 && !found; j--) {
						final IValue otherTemp = otherSetof.values.getIndexedValueByRealIndex(indicesuncovered.get(j));

						if (localTemp.getValue().checkEquality(timestamp, otherTemp)) {
							found = true;
							indicesuncovered.remove(j);
						}
					}

					if (!found) {
						return false;
					}
				}
			} else {
				if (values.getNofIndexedValues() != otherSetof.values.getNofValues()) {
					return false;
				}

				final List<Integer> indicesuncovered = new ArrayList<Integer>();
				for (int i = 0; i < values.getNofIndexedValues(); i++) {
					indicesuncovered.add(i);
				}
				for (int i = values.getNofIndexedValues() - 1; i >= 0; i--) {
					final IndexedValue localTemp = values.getIndexedValueByIndex(i);
					boolean found = false;
					for (int j = indicesuncovered.size() - 1; j >= 0 && !found; j--) {
						final IValue otherTemp = otherSetof.values.getValueByIndex(indicesuncovered.get(j));

						if (localTemp.getValue().checkEquality(timestamp, otherTemp)) {
							found = true;
							indicesuncovered.remove(j);
						}
					}

					if (!found) {
						return false;
					}
				}
			}
		} else {
			if (otherSetof.isIndexed()) {
				if (values.getNofValues() != otherSetof.values.getNofIndexedValues()) {
					return false;
				}

				final List<Integer> indicesuncovered = new ArrayList<Integer>();
				for (int i = 0; i < values.getNofValues(); i++) {
					indicesuncovered.add(i);
				}
				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					final IValue localTemp = values.getValueByIndex(i);
					boolean found = false;
					for (int j = indicesuncovered.size() - 1; j >= 0 && !found; j--) {
						final IndexedValue otherTemp = otherSetof.values.getIndexedValueByIndex(indicesuncovered.get(j));

						if (localTemp.checkEquality(timestamp, otherTemp.getValue())) {
							found = true;
							indicesuncovered.remove(j);
						}
					}

					if (!found) {
						return false;
					}
				}
			} else {
				if (values.getNofValues() != otherSetof.values.getNofValues()) {
					return false;
				}

				final List<Integer> indicesuncovered = new ArrayList<Integer>();
				for (int i = 0; i < values.getNofValues(); i++) {
					indicesuncovered.add(i);
				}
				for (int i = 0, size = values.getNofValues(); i < size; i++) {
					final IValue localTemp = values.getValueByIndex(i);
					boolean found = false;
					for (int j = indicesuncovered.size() - 1; j >= 0 && !found; j--) {
						final IValue otherTemp = otherSetof.values.getValueByIndex(indicesuncovered.get(j));

						if (localTemp.checkEquality(timestamp, otherTemp)) {
							found = true;
							indicesuncovered.remove(j);
						}
					}

					if (!found) {
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

					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
				} else if (index >= values.getNofValues()) {
					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(INDEXOVERFLOW, type.getTypename(), index, values.getNofValues()));
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

					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(NOINDEX, index, values.getFullName()));
				} else if (index >= values.getNofValues()) {
					arrayIndex.getLocation().reportSemanticError(MessageFormat.format(INDEXOVERFLOW, type.getTypename(), index, values.getNofValues()));
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
		if (values != null && !values.accept(v)) {
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

		if (isIndexed()) {
			for (int i = 0; i < values.getNofIndexedValues(); i++) {
				final StringBuilder embeddedName = new StringBuilder(parameterGenName);
				embeddedName.append(".get_at(").append(i).append(')');
				values.getIndexedValueByIndex(i).getValue().setGenNameRecursive(embeddedName.toString());
			}
		} else {
			for (int i = 0; i < values.getNofValues(); i++) {
				final StringBuilder embeddedName = new StringBuilder(parameterGenName);
				embeddedName.append(".get_at(").append(i).append(')');
				values.getValueByIndex(i).setGenNameRecursive(embeddedName.toString());
			}
		}
	}

	@Override
	public boolean needsTemporaryReference() {
		if (isIndexed()) {
			for (int i = 0; i < values.getNofIndexedValues(); i++) {
				final IValue tempValue = values.getIndexedValueByIndex(i).getValue();
				if (tempValue.getValuetype() != Value_type.NOTUSED_VALUE && tempValue.needsTemporaryReference()) {
					return true;
				}
			}
		} else {
			for (int i = 0; i < values.getNofValues(); i++) {
				final IValue tempValue = values.getValueByIndex(i);
				if (tempValue.getValuetype() != Value_type.NOTUSED_VALUE && tempValue.needsTemporaryReference()) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
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
		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null) {
			governor = myLastSetGovernor;
		}

		aData.addBuiltinTypeImport("TitanNull_Type");

		if (governor == null) {
			return new StringBuilder("TitanNull_Type.NULL_VALUE");
		}

		final StringBuilder result = new StringBuilder();
		final String genName = governor.getGenNameValue(aData, result);
		result.append(MessageFormat.format("new {0}(TitanNull_Type.NULL_VALUE)", genName));
		return result;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		if (isIndexed()) {
			final int nofIndexedValues = values.getNofIndexedValues();
			if (nofIndexedValues == 0) {
				aData.addBuiltinTypeImport("TitanNull_Type");

				source.append(MessageFormat.format("{0}.operator_assign(TitanNull_Type.NULL_VALUE);\n", name));
			} else {
				final IType ofType = values.getIndexedValueByIndex(0).getValue().getMyGovernor();
				final String ofTypeName = ofType.getGenNameValue(aData, source);
				for (int i = 0; i < nofIndexedValues; i++) {
					final IndexedValue indexedValue = values.getIndexedValueByIndex(i);
					final String tempId1 = aData.getTemporaryVariableName();
					source.append("{\n");
					final Value index = indexedValue.getIndex().getValue();
					if (index.getValuetype().equals(Value_type.INTEGER_VALUE)) {
						source.append(MessageFormat.format("final {0} {1} = {2}.get_at({3});\n", ofTypeName, tempId1, name, ((Integer_Value) index).getValue()));
					} else {
						final String tempId2 = aData.getTemporaryVariableName();
						source.append(MessageFormat.format("final TitanInteger {0} = new TitanInteger();\n", tempId2));
						index.generateCodeInit(aData, source, tempId2);
						source.append(MessageFormat.format("final {0} {1} = {2}.get_at({3});\n", ofTypeName, tempId1, name, tempId2));
					}
					indexedValue.getValue().generateCodeInit(aData, source, tempId1);
					source.append("}\n");
				}
			}
		} else {
			final int nofValues = values.getNofValues();
			if (nofValues == 0) {
				aData.addBuiltinTypeImport("TitanNull_Type");

				source.append(MessageFormat.format("{0}.operator_assign(TitanNull_Type.NULL_VALUE);\n", name));
			} else {
				String tempId;
				if (name.contains(".")) {
					tempId = aData.getTemporaryVariableName();
					source.append(MessageFormat.format("final {0} {1} = {2};\n", getMyGovernor().getGenNameValue(aData, source), tempId, name));
				} else {
					tempId = name;
				}

				source.append(MessageFormat.format("{0}.set_size({1});\n", tempId, nofValues));
				final IType ofType = values.getValueByIndex(0).getMyGovernor();
				String embeddedTypeName = null;

				for (int i = 0; i < nofValues; i++) {
					final IValue value = values.getValueByIndex(i);
					if (value.getValuetype().equals(Value_type.NOTUSED_VALUE)) {
						continue;
					} else if (value.needsTemporaryReference()) {
						if (embeddedTypeName == null) {
							embeddedTypeName = ofType.getGenNameValue(aData, source);
						}

						final String tempId2 = aData.getTemporaryVariableName();
						source.append("{\n");
						source.append(MessageFormat.format("{0} {1} = {2}.get_at({3});\n", embeddedTypeName, tempId2, tempId, i));
						value.generateCodeInitMandatory(aData, source, tempId2);
						source.append("}\n");
					} else {
						final String embeddedName = MessageFormat.format("{0}.get_at({1})", tempId, i);
						value.generateCodeInitMandatory(aData, source, embeddedName);
					}
				}
			}
		}

		lastTimeGenerated = aData.getBuildTimstamp();

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

		final String tempId = aData.getTemporaryVariableName();
		final String genName = myLastSetGovernor.getGenNameValue(aData, expression.expression);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", genName, tempId));
		setGenNamePrefix(tempId);
		generateCodeInit(aData, expression.preamble, tempId);
		expression.expression.append(tempId);
	}
}
