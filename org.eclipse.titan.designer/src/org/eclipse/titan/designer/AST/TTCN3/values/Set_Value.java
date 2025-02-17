/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.BridgingNamedNode;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Choice_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Set_Value extends Value {
	private static final String NONEXISTENTFIELD = "Reference to non-existent set field `{0}'' in type `{1}''";

	private final NamedValues values;

	public Set_Value(final NamedValues values) {
		this.values = values;

		if (values != null) {
			values.setFullNameParent(this);
		}
	}

	public Set_Value(final Sequence_Value original) {
		copyGeneralProperties(original);
		values = original.getValues();
	}

	@Override
	/** {@inheritDoc} */
	public Value_type getValuetype() {
		return Value_type.SET_VALUE;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("{");
		final boolean isAsn1 = isAsn();
		for (int i = 0; i < values.getSize(); i++) {
			if (i > 0) {
				builder.append(", ");
			}

			final NamedValue namedValue = values.getNamedValueByIndex(i);
			builder.append(namedValue.getName().getDisplayName());
			if (isAsn1) {
				builder.append(' ');
			} else {
				builder.append(" := ");
			}
			builder.append(namedValue.getValue().createStringRepresentation());
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
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final IReferenceChain refChain) {
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
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(ArraySubReference.INVALIDVALUESUBREFERENCE, type.getTypename()));
			return null;
		case fieldSubReference:
			final Identifier fieldId = ((FieldSubReference) subreference).getId();
			switch (type.getTypetype()) {
			case TYPE_TTCN3_SET:
				if (!((TTCN3_Set_Type) type).hasComponentWithName(fieldId.getName())) {
					subreference.getLocation().reportSemanticError(
							MessageFormat.format(NONEXISTENTFIELD, fieldId.getDisplayName(), type.getTypename()));
					return null;
				}
				break;
			case TYPE_ASN1_SET:
				if (!((ASN1_Set_Type) type).hasComponentWithName(fieldId)) {
					subreference.getLocation().reportSemanticError(
							MessageFormat.format(NONEXISTENTFIELD, fieldId.getDisplayName(), type.getTypename()));
					return null;
				}
				break;
			default:
				return null;
			}

			if (values.hasNamedValueWithName(fieldId)) {
				return values.getNamedValueByName(fieldId).getValue().getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
			}

			if (Type_type.TYPE_TTCN3_SET.equals(type.getTypetype())) {
				if (!reference.getUsedInIsbound()) {
					subreference.getLocation().reportSemanticError(
							MessageFormat.format("Reference to unbound set field `{0}''", fieldId.getDisplayName()));
				}
				// this is an error, that was already reported
				return null;
			}

			final CompField compField = ((ASN1_Sequence_Type) type).getComponentByName(fieldId);
			if (compField.isOptional()) {
				// create an explicit omit value
				final Value result = new Omit_Value();

				final BridgingNamedNode bridge = new BridgingNamedNode(this, "." + fieldId.getDisplayName());
				result.setFullNameParent(bridge);

				result.setMyScope(getMyScope());

				return result.getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
			} else if (compField.hasDefault()) {
				return compField.getDefault().getReferencedSubValue(timestamp, reference, actualSubReference + 1, refChain);
			}

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
		if (values == null) {
			return true;
		}

		for (int i = 0; i < values.getSize(); i++) {
			if (values.getNamedValueByIndex(i).getValue().isUnfoldable(timestamp, expectedValue, referenceChain)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Adds the provided named value to the list of named values in this set.
	 * <p>
	 * Right now is only used to add implicit omit elements.
	 *
	 * @param value
	 *                the named value to add.
	 * */
	public void addNamedValue(final NamedValue value) {
		if (value != null) {
			values.addNamedValue(value);
			value.setMyScope(myScope);
		}
	}

	/**
	 * Remove all named values that were not parsed, but generated during
	 * previous semantic checks.
	 * */
	public void removeGeneratedValues() {
		if (values != null) {
			values.removeGeneratedValues();
		}
	}

	public int getNofComponents() {
		return values.getSize();
	}

	public NamedValue getSequenceValueByIndex(final int index) {
		if (values == null) {
			return null;
		}

		return values.getNamedValueByIndex(index);
	}

	public boolean hasComponentWithName(final Identifier name) {
		if (values == null) {
			return false;
		}

		return values.hasNamedValueWithName(name);
	}

	public NamedValue getComponentByName(final Identifier name) {
		if (values == null) {
			return null;
		}

		return values.getNamedValueByName(name);
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

	/**
	 * Checks the uniqueness of the set value.
	 *
	 * @param timestamp
	 *                the timestamp of the actual build cycle
	 * */
	public void checkUniqueness(final CompilationTimeStamp timestamp) {
		if (values == null) {
			return;
		}

		values.checkUniqueness(timestamp);
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			for (int i = 0, size = values.getSize(); i < size; i++) {
				final IValue temp = values.getNamedValueByIndex(i).getValue();
				if (temp != null) {
					referenceChain.markState();
					temp.checkRecursions(timestamp, referenceChain);
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

		if (!Value_type.SET_VALUE.equals(last.getValuetype())) {
			return false;
		}

		if (myGovernor == null) {
			return false;
		}

		final Set_Value otherSet = (Set_Value) last;
		if (values.getSize() != otherSet.values.getSize()) {
			return false;
		}

		int nofComps = 0;
		final IType leftGovernor = myGovernor.getTypeRefdLast(timestamp);
		switch (leftGovernor.getTypetype()) {
		case TYPE_TTCN3_SET:
			nofComps = ((TTCN3_Set_Type) leftGovernor).getNofComponents();
			break;
		case TYPE_ASN1_SET:
			nofComps = ((ASN1_Set_Type) leftGovernor).getNofComponents();
			break;
		default:
			return false;
		}

		CompField compField = null;
		for (int i = 0; i < nofComps; i++) {
			switch (leftGovernor.getTypetype()) {
			case TYPE_TTCN3_SET:
				compField = ((TTCN3_Set_Type) leftGovernor).getComponentByIndex(i);
				break;
			case TYPE_ASN1_SET:
				compField = ((ASN1_Set_Type) leftGovernor).getComponentByIndex(i);
				break;
			default:
				return false;
			}
			final Identifier fieldName = compField.getIdentifier();

			if (hasComponentWithName(fieldName)) {
				final IValue leftValue = getComponentByName(fieldName).getValue();
				if (otherSet.hasComponentWithName(fieldName)) {
					final IValue otherValue = otherSet.getComponentByName(fieldName).getValue();
					if ((Value_type.OMIT_VALUE.equals(leftValue.getValuetype()) && !Value_type.OMIT_VALUE.equals(otherValue.getValuetype()))
							|| (!Value_type.OMIT_VALUE.equals(leftValue.getValuetype()) && Value_type.OMIT_VALUE.equals(otherValue.getValuetype()))) {
						return false;
					}

					if (!leftValue.checkEquality(timestamp, otherValue)) {
						return false;
					}
				} else {
					if (compField.hasDefault()) {
						if (!leftValue.checkEquality(timestamp, compField.getDefault())) {
							return false;
						}
					} else {
						if (!Value_type.OMIT_VALUE.equals(leftValue.getValuetype())) {
							return false;
						}
					}
				}
			} else {
				if (otherSet.hasComponentWithName(fieldName)) {
					final IValue otherValue = otherSet.getComponentByName(fieldName).getValue();
					if (compField.hasDefault()) {
						if (Value_type.OMIT_VALUE.equals(otherValue.getValuetype())) {
							return false;
						}

						if (!compField.getDefault().checkEquality(timestamp, otherValue)) {
							return false;
						}
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

		for (int i = 0, size = values.getSize(); i < size; i++) {
			if (!values.getNamedValueByIndex(i).getValue().evaluateIsvalue(true)) {
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
			return false;
		case fieldSubReference:
			final Identifier fieldId = ((FieldSubReference) subreference).getId();
			switch (type.getTypetype()) {
			case TYPE_TTCN3_SET:
				if (!((TTCN3_Set_Type) type).hasComponentWithName(fieldId.getName())) {
					return false;
				}
				break;
			case TYPE_ASN1_SET:
				if (!((ASN1_Set_Type) type).hasComponentWithName(fieldId)) {
					return false;
				}
				break;
			default:
				return false;
			}

			if (values.hasNamedValueWithName(fieldId)) {
				// we can move on with the check
				return values.getNamedValueByName(fieldId).getValue().evaluateIsbound(timestamp, reference, actualSubReference + 1);
			}

			if (Type_type.TYPE_TTCN3_SET.equals(type.getTypetype())) {
				return false;
			}

			final CompField compField = ((ASN1_Set_Type) type).getComponentByName(fieldId);
			if (compField.isOptional()) {
				// create an explicit omit value
				final Value result = new Omit_Value();

				final BridgingNamedNode bridge = new BridgingNamedNode(this, "." + fieldId.getDisplayName());
				result.setFullNameParent(bridge);

				result.setMyScope(getMyScope());

				return result.evaluateIsbound(timestamp, reference, actualSubReference + 1);
			} else if (compField.hasDefault()) {
				return compField.getDefault().evaluateIsbound(timestamp, reference, actualSubReference + 1);
			}

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
			return false;
		case fieldSubReference:
			final Identifier fieldId = ((FieldSubReference) subreference).getId();
			switch (type.getTypetype()) {
			case TYPE_TTCN3_SET:
				if (!((TTCN3_Set_Type) type).hasComponentWithName(fieldId.getName())) {
					return false;
				}
				break;
			case TYPE_ASN1_SET:
				if (!((ASN1_Set_Type) type).hasComponentWithName(fieldId)) {
					return false;
				}
				break;
			default:
				return false;
			}

			if (values.hasNamedValueWithName(fieldId)) {
				// we can move on with the check
				return values.getNamedValueByName(fieldId).getValue().evaluateIspresent(timestamp, reference, actualSubReference + 1);
			}

			if (Type_type.TYPE_TTCN3_SET.equals(type.getTypetype())) {
				return false;
			}

			final CompField compField = ((ASN1_Set_Type) type).getComponentByName(fieldId);
			if (compField.isOptional()) {
				// create an explicit omit value
				final Value result = new Omit_Value();

				final BridgingNamedNode bridge = new BridgingNamedNode(this, "." + fieldId.getDisplayName());
				result.setFullNameParent(bridge);

				result.setMyScope(getMyScope());

				return result.evaluateIspresent(timestamp, reference, actualSubReference + 1);
			} else if (compField.hasDefault()) {
				return compField.getDefault().evaluateIspresent(timestamp, reference, actualSubReference + 1);
			}

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

		if (referenceFinder.assignment.getAssignmentType() == Assignment_type.A_TYPE && referenceFinder.fieldId != null && myGovernor != null) {
			// check if this is the type and field we are searching for
			final IType governorLast = myGovernor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
			if (referenceFinder.type == governorLast) {
				final NamedValue nv = values.getNamedValueByName(referenceFinder.fieldId);
				if (nv != null) {
					foundIdentifiers.add(new Hit(nv.getName()));
				}
			}
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
		for (int i = 0; i < values.getSize(); i++) {
			values.getNamedValueByIndex(i).getValue().setGenNamePrefix(prefix);
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
		if (Type_type.TYPE_TTCN3_SET.equals(type.getTypetype())) {
			for (int i = 0; i < values.getSize(); i++) {
				final NamedValue namedValue = values.getNamedValueByIndex(i);
				final String name = namedValue.getName().getName();
				if (((TTCN3_Set_Type) type).hasComponentWithName(name)) {
					final StringBuilder embeddedName = new StringBuilder();
					embeddedName.append(MessageFormat.format("{0}.get_field_{1}()", parameterGenName, name));
					if (((TTCN3_Set_Type) type).getComponentByName(name).isOptional()) {
						embeddedName.append(".get()");
					}
					final IValue v = namedValue.getValue();
					if (v != null) {
						v.setGenNameRecursive(embeddedName.toString());
					}
				}
			}
		}
	}

	@Override
	public boolean needsTemporaryReference() {
		if (isAsn()) {
			// it depends on the type since fields with omit or default value
			// may not be present
			final IType lastType = myGovernor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

			return ((ASN1_Set_Type)lastType).getNofComponents() > 1;
		} else {
			// incomplete values are allowed in TTCN-3
			// we should check the number of value components that would be generated
			for (int i = 0; i < values.getSize(); i++) {
				final IValue value = values.getNamedValueByIndex(i).getValue();
				if (value.getValuetype() != Value_type.NOTUSED_VALUE && value.needsTemporaryReference()) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		if (values == null) {
			return false;
		}

		return values.getSize() == 0;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateSingleExpression(final JavaGenData aData) {
		// TODO the empty record is so frequent that it is worth to handle in the library
		aData.addBuiltinTypeImport("TitanNull_Type");

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
	/** {@inheritDoc}
	 * generate_code_init_se in the compiler
	 * */
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null) {
			governor = myLastSetGovernor;
		}

		final IType type = governor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		int nofComps = 0;
		switch (type.getTypetype()) {
		case TYPE_TTCN3_SET:
			nofComps = ((TTCN3_Set_Type) type).getNofComponents();
			break;
		case TYPE_ASN1_SET:
			nofComps = ((ASN1_Set_Type) type).getNofComponents();
			break;
		case TYPE_TTCN3_CHOICE:
			nofComps = ((TTCN3_Choice_Type) type).getNofComponents();
			break;
		case TYPE_ASN1_CHOICE:
			nofComps = ((ASN1_Choice_Type) type).getNofComponents();
			break;
		default:
			ErrorReporter.INTERNAL_ERROR("FATAL ERROR while generating code for value `" + getFullName() + "''");
			break;
		}

		if (nofComps == 0) {
			aData.addBuiltinTypeImport("TitanNull_Type");

			source.append(MessageFormat.format("{0}.operator_assign(TitanNull_Type.NULL_VALUE);\n", name));

			lastTimeGenerated = aData.getBuildTimstamp();

			return source;
		}

		CompField compField = null;
		for (int i = 0; i < nofComps; i++) {
			switch (type.getTypetype()) {
			case TYPE_TTCN3_SET:
				compField = ((TTCN3_Set_Type) type).getComponentByIndex(i);
				break;
			case TYPE_ASN1_SET:
				compField = ((ASN1_Set_Type) type).getComponentByIndex(i);
				break;
			case TYPE_TTCN3_CHOICE:
				compField = ((TTCN3_Choice_Type) type).getComponentByIndex(i);
				break;
			case TYPE_ASN1_CHOICE:
				compField = ((ASN1_Choice_Type) type).getComponentByIndex(i);
				break;
			default:
				ErrorReporter.INTERNAL_ERROR("FATAL ERROR while generating code for value `" + getFullName() + "''");
				break;
			}

			final Identifier fieldName = compField.getIdentifier();
			IValue fieldValue;
			if (hasComponentWithName(fieldName)) {
				fieldValue = getComponentByName(fieldName).getValue();
				if (Value_type.NOTUSED_VALUE.equals(fieldValue.getValuetype())) {
					continue;
				} else if (Value_type.OMIT_VALUE.equals(fieldValue.getValuetype())) {
					fieldValue = null;
				}
			} else if (isAsn()) {
				if (compField.hasDefault()) {
					// handle like a referenced value
					final Value defaultValue = compField.getDefault();
					if (needsInitPrecede(aData, defaultValue)) {
						defaultValue.generateCodeInit(aData, source, defaultValue.get_lhs_name());
					}
					source.append(MessageFormat.format("{0}.get_field_{1}().operator_assign({2});\n", name, fieldName, defaultValue.getGenNameOwn(aData)));
					continue;
				} else {
					fieldValue = null;
				}
			} else {
				continue;
			}

			final String javaGetterName = FieldSubReference.getJavaGetterName(fieldName.getName());
			if (fieldValue != null) {
				// the value is not omit
				if (fieldValue.needsTemporaryReference()) {
					final String tempId = aData.getTemporaryVariableName();
					source.append("{\n");
					final String embeddedTypeName = compField.getType().getGenNameValue(aData, source);
					source.append(MessageFormat.format("{0} {1} = {2}.get_field_{3}()", embeddedTypeName, tempId, name, javaGetterName));
					if(compField.isOptional() /*&& fieldValue.isCompound() */) {
						source.append(".get()");
					}
					source.append(";\n");

					fieldValue.generateCodeInit(aData, source, tempId);
					source.append("}\n");
				} else {
					final StringBuilder embeddedName = new StringBuilder();
					embeddedName.append(MessageFormat.format("{0}.get_field_{1}()", name, javaGetterName));
					if(compField.isOptional() /*&& fieldValue.isCompound() */) {
						embeddedName.append(".get()");
					}

					fieldValue.generateCodeInit(aData, source, embeddedName.toString());
				}
			} else {
				aData.addBuiltinTypeImport("Base_Template.template_sel");

				source.append(MessageFormat.format("{0}.get_field_{1}().operator_assign(template_sel.OMIT_VALUE);\n", name, javaGetterName));
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
		final String genName = governor.getGenNameValue(aData, expression.expression);
		expression.preamble.append(MessageFormat.format("final {0} {1} = new {0}();\n", genName, tempId));
		setGenNamePrefix(tempId);
		generateCodeInit(aData, expression.preamble, tempId);
		expression.expression.append(tempId);
	}
}
