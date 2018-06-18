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
import org.eclipse.titan.designer.AST.IType.Coding_Type;
import org.eclipse.titan.designer.AST.IType.MessageEncoding_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.values.ISO2022String_Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.UniversalCharstring_Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a universal charstring value.
 *
 * @author Kristof Szabados
 */
public final class UniversalCharstring_Value extends Value {

	public static final String ISOCONVERTION = "ISO-10646 string value cannot be converted to ISO-2022 string";

	private final UniversalCharstring value;

	public UniversalCharstring_Value(final UniversalCharstring value) {
		this.value = value;
	}

	protected UniversalCharstring_Value(final Charstring_Value original) {
		copyGeneralProperties(original);
		value = new UniversalCharstring(original.getValue());
	}

	@Override
	/** {@inheritDoc} */
	public Value_type getValuetype() {
		return Value_type.UNIVERSALCHARSTRING_VALUE;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_UCHARSTRING;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append('\"').append(value.getStringRepresentation()).append('"');

		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		return new UniversalCharstring_Type();
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
		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference: {
			final Value arrayIndex = ((ArraySubReference) subreference).getValue();
			final IValue valueIndex = arrayIndex.getValueRefdLast(timestamp, refChain);
			if (!valueIndex.isUnfoldable(timestamp)) {
				if (Value_type.INTEGER_VALUE.equals(valueIndex.getValuetype())) {
					final int index = ((Integer_Value) valueIndex).intValue();
					return getStringElement(index, arrayIndex.getLocation());
				}

				arrayIndex.getLocation().reportSemanticError(ArraySubReference.INTEGERINDEXEXPECTED);
				return null;
			}
			return null;
		}
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
		return false;
	}

	public UniversalCharstring getValue() {
		return value;
	}

	public int getValueLength() {
		if (value == null || isErroneous) {
			return 0;
		}

		return value.length();
	}

	public UniversalCharstring_Value getStringElement(final int index, final Location location) {
		if (value == null) {
			return null;
		}

		if (index < 0) {
			location.reportSemanticError(MessageFormat.format(Bitstring_Value.NEGATIVEINDEX, index));
			return null;
		} else if (index >= value.length()) {
			location.reportSemanticError(MessageFormat.format(Bitstring_Value.INDEXOWERFLOW, index, value.length()));
			return null;
		}

		final UniversalCharstring_Value result = new UniversalCharstring_Value(value.substring(index, index + 1));
		result.setMyGovernor(myGovernor);
		result.setMyScope(myScope);
		return result;
	}

	@Override
	/** {@inheritDoc} */
	public Value setValuetype(final CompilationTimeStamp timestamp, final Value_type newType) {
		switch (newType) {
		case CHARSTRING_VALUE:
			return Charstring_Value.convert(timestamp, this);
		case ISO2022STRING_VALUE:
			location.reportSemanticError(ISOCONVERTION);
			setIsErroneous(true);
			return this;
		default:
			return super.setValuetype(timestamp, newType);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		switch (last.getValuetype()) {
		case CHARSTRING_VALUE:
			return value.checkEquality(((Charstring_Value) last).getValue());
		case UNIVERSALCHARSTRING_VALUE:
			return value.checkEquality(((UniversalCharstring_Value) last).getValue());
		case ISO2022STRING_VALUE:
			return value.checkEquality(((ISO2022String_Value) last).getValueISO2022String());
		default:
			return false;
		}
	}

	/**
	 * Checks that the value is a valid dynamic encoding string for the specified type.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param type the type to check against
	 *
	 * @return true if the provided type does not have a coding with this name.
	 * */
	public boolean checkDynamicEncodingString(final CompilationTimeStamp timestamp, final IType type) {
		final IType ct = type.getTypeWithCodingTable(timestamp, false);
		if (ct == null) {
			return false;
		}

		boolean errorFound = false;
		for (int i = 0; i < value.length(); i++) {
			final UniversalChar temp = value.get(i);

			if (temp.group() != 0 || temp.plane() != 0 || temp.row() != 0) {
				errorFound = true;
				break;
			}
		}

		if (!errorFound) {
			final String str = value.getString();
			final MessageEncoding_type coding = Type.getEncodingType(str);
			final boolean builtIn = !coding.equals(MessageEncoding_type.PER) && !coding.equals(MessageEncoding_type.CUSTOM);
			errorFound = true;
			final List<Coding_Type> codingTable = ct.getCodingTable();
			for (int i = 0; i < codingTable.size(); i++) {
				final Coding_Type temp = codingTable.get(i);
				if (builtIn == temp.builtIn && ((builtIn && coding == temp.builtInCoding) ||
						(!builtIn && str.equals(temp.customCoding.name)))) {
					errorFound = false;
					break;
				}
			}
		}

		return errorFound;
	}

	@Override
	/** {@inheritDoc} */
	public boolean evaluateIsvalue(final boolean fromSequence) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		// no members
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateSingleExpression(final JavaGenData aData) {
		aData.addBuiltinTypeImport( "TitanUniversalCharString" );

		final StringBuilder result = new StringBuilder();
		result.append(MessageFormat.format("new TitanUniversalCharString({0})", value.generateSingleExpression(aData)));

		return result;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		aData.addBuiltinTypeImport( "TitanUniversalCharString" );

		source.append(MessageFormat.format("{0}.assign(new TitanUniversalCharString({1}));\n", name, value.generateSingleExpression(aData)));

		return source;
	}
}
