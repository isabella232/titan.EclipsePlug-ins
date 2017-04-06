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
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Float_Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a real or float value.
 * 
 * @author Kristof Szabados
 * */
public final class Real_Value extends Value {

	private double value;

	public Real_Value(final double value) {
		this.value = value;
	}

	public Real_Value(final CompilationTimeStamp timestamp, final Sequence_Value original) {
		copyGeneralProperties(original);
		boolean erroneous = false;
		//mantissa
		final Identifier mantissaID = new Identifier(Identifier_type.ID_ASN, "mantissa");
		int mantissa = 0;
		if (original.hasComponentWithName(mantissaID)) {
			IValue tmpValue = original.getComponentByName(mantissaID).getValue();
			if (tmpValue != null) {
				final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				tmpValue = tmpValue.getValueRefdLast(timestamp, referenceChain);
				referenceChain.release();

				if (Value_type.INTEGER_VALUE.equals(tmpValue.getValuetype())) {
					if (((Integer_Value) tmpValue).isNative()) {
						mantissa = ((Integer_Value) tmpValue).intValue();
					} else {
						tmpValue.getLocation().reportSemanticError(MessageFormat.format(
								"Mantissa `{0}'' should be less than `{1}''", ((Integer_Value) tmpValue).getValueValue(), Integer.MAX_VALUE));
						erroneous = true;
					}
				} else {
					erroneous = true;
				}
			}
		} else {
			erroneous = true;
		}

		//base
		final Identifier baseID = new Identifier(Identifier_type.ID_ASN, "base");
		int base = 0;
		if (original.hasComponentWithName(baseID)) {
			IValue tmpValue = original.getComponentByName(baseID).getValue();
			if (tmpValue != null) {
				final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				tmpValue = tmpValue.getValueRefdLast(timestamp, referenceChain);
				referenceChain.release();

				if (Value_type.INTEGER_VALUE.equals(tmpValue.getValuetype())) {
					final BigInteger temp = ((Integer_Value) tmpValue).getValueValue();
					if (!erroneous && temp.compareTo(BigInteger.TEN) != 0 && temp.compareTo(BigInteger.valueOf(2)) != 0) {
						tmpValue.getLocation().reportSemanticError("Base of the REAL must be 2 or 10");
						erroneous = true;
					} else {
						base = ((Integer_Value) tmpValue).intValue();
					}
				} else {
					erroneous = true;
				}
			}
		} else {
			erroneous = true;
		}

		//exponent
		final Identifier exponentID = new Identifier(Identifier_type.ID_ASN, "exponent");
		int exponent = 0;
		if (original.hasComponentWithName(exponentID)) {
			IValue tmpValue = original.getComponentByName(exponentID).getValue();
			if (tmpValue != null) {
				final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				tmpValue = tmpValue.getValueRefdLast(timestamp, referenceChain);
				referenceChain.release();

				if (Value_type.INTEGER_VALUE.equals(tmpValue.getValuetype())) {
					if (((Integer_Value) tmpValue).isNative()) {
						exponent = ((Integer_Value) tmpValue).intValue();
					} else {
						tmpValue.getLocation().reportSemanticError(MessageFormat.format(
								"Exponent `{0}'' should be less than `{1}''", ((Integer_Value) tmpValue).getValueValue(), Integer.MAX_VALUE));
						erroneous = true;
					}
				} else {
					erroneous = true;
				}
			}
		} else {
			erroneous = true;
		}

		if (erroneous) {
			setIsErroneous(true);
			lastTimeChecked = timestamp;
			value = 0;
		} else {
			value = (float) (mantissa * Math.pow(base, exponent));
		}
	}

	@Override
	/** {@inheritDoc} */
	public Value_type getValuetype() {
		return Value_type.REAL_VALUE;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		return Double.toString(value);
	}

	public double getValue() {
		return value;
	}

	public void changeSign() {
		value *= -1.0;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_REAL;
	}

	@Override
	/** {@inheritDoc} */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		return new Float_Type();
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
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDVALUESUBREFERENCE, type.getTypename()));
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
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return Value_type.REAL_VALUE.equals(last.getValuetype()) && Double.compare(value, ((Real_Value) last).getValue()) == 0;
	}

	@Override
	public String toString() {
		return Double.toString(value);
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

	/**
	 * @return true if the values if the positive infinity, false otherwise.
	 * */
	public boolean isPositiveInfinity() {
		return Double.isInfinite(value) && value > 0;
	}

	/**
	 * @return true if the values if the negative infinity, false otherwise.
	 * */
	public boolean isNegativeInfinity() {
		return Double.isInfinite(value) && value < 0;
	}

	public boolean isSpecialFloat() {
		return Double.isInfinite(value) || Double.isNaN(value);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		// no members
		return true;
	}
	
	@Override
	/** {@inheritDoc} */
	public StringBuilder generateJavaInit(final JavaGenData aData, final StringBuilder source, final String name) {
		aData.addBuiltinTypeImport( "TitanFloat" );
		source.append(name);
		source.append(".assign( ");
		source.append( "new TitanFloat(" );
		source.append( createStringRepresentation() );
		source.append( " )" );
		source.append( " );\n" );
		return source;
		//TODO: This solution is ok for a valid double value. Special values should be handled!
		//Special values: +/-not_a_number, +infinity,-infinity
	}
}
