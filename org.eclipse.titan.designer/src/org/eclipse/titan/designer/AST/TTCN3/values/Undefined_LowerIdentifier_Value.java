/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.ASN1.values.Named_Integer_Value;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a value who's kind could not be identified yet. The semantic
 * checking must convert it to some other kind.
 *
 * @author Kristof Szabados
 * */
public final class Undefined_LowerIdentifier_Value extends Value {

	private final Identifier identifier;

	private Value realValue;
	private Reference asReference;

	public Undefined_LowerIdentifier_Value(final Identifier identifier) {
		this.identifier = identifier;
	}

	@Override
	/** {@inheritDoc} */
	public Value_type getValuetype() {
		return Value_type.UNDEFINED_LOWERIDENTIFIER_VALUE;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		return identifier.getName();
	}

	@Override
	// Location is optimized not to store an object at it is not needed
	public Location getLocation() {
		return new Location(identifier.getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public void setLocation(final Location location) {
		//Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		if (realValue == null || realValue.getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		return realValue.getExpressionReturntype(timestamp, expectedValue);
	}

	@Override
	/** {@inheritDoc} */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			return null;
		}

		if (realValue == null || realValue.getIsErroneous(timestamp)) {
			return null;
		}

		return realValue.getExpressionGovernor(timestamp, expectedValue);
	}

	@Override
	/** {@inheritDoc} */
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference,
			final int actualSubReference, final IReferenceChain refChain) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		IValue result = getValueRefdLast(timestamp, refChain);
		if (result != null && result != this) {
			result = result.getReferencedSubValue(timestamp, reference, actualSubReference, refChain);
			if (result != null && result.getIsErroneous(timestamp)) {
				setIsErroneous(true);
			}
			return result;
		}

		final IType type = myGovernor.getTypeRefdLast(timestamp);
		if (type.getIsErroneous(timestamp)) {
			return null;
		}

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
		if (lastTimeChecked == null || lastTimeChecked.isLess(timestamp)) {
			return true;
		}

		if (realValue == null || realValue.getIsErroneous(timestamp)) {
			return true;
		}

		return realValue.isUnfoldable(timestamp, expectedValue, referenceChain);
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		if (realValue == null || realValue.getIsErroneous(timestamp)) {
			return false;
		}

		return realValue.checkEquality(timestamp, other);
	}

	@Override
	/** {@inheritDoc} */
	public IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (myGovernor != null && myGovernor.getIsErroneous(timestamp)) {
			setIsErroneous(true);
			return this;
		}

		setLoweridToReference(timestamp);

		return realValue.getValueRefdLast(timestamp, expectedValue, referenceChain);
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the reference form of the lower identifier value.
	 * <p>
	 * Almost the same as steel_ttcn_ref_base.
	 *
	 * @return the reference created from the identifier.
	 * */
	public Reference getAsReference() {
		if (asReference != null) {
			return asReference;
		}
		asReference = new Reference(null);
		asReference.addSubReference(new FieldSubReference(identifier));
		asReference.setLocation(getLocation());
		asReference.setFullNameParent(this);
		asReference.setMyScope(myScope);
		return asReference;
	}

	@Override
	/** {@inheritDoc} */
	public Value setValuetype(final CompilationTimeStamp timestamp, final Value_type newType) {
		lastTimeChecked = timestamp;

		switch (newType) {
		case ENUMERATED_VALUE:
			realValue = new Enumerated_Value(this);
			realValue.copyGeneralProperties(this);
			break;
		case REFERENCED_VALUE:
			realValue = new Referenced_Value(this);
			// FIXME: this seems redundant; the constructor already called it -no, e.g. location is set here
			realValue.copyGeneralProperties(this);
			break;
		case NAMED_INTEGER_VALUE:
			realValue = new Named_Integer_Value(this);
			realValue.copyGeneralProperties(this);
			break;
		default:
			realValue = super.setValuetype(timestamp, newType);
			break;
		}

		return realValue;
	}

	@Override
	public IValue setLoweridToReference(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return realValue;
		}

		lastTimeChecked = timestamp;
		realValue = setValuetype(timestamp, Value_type.REFERENCED_VALUE);

		return realValue;
	}

	@Override
	/** {@inheritDoc} */
	public boolean evaluateIsvalue(final boolean fromSequence) {
		if (realValue == null) {
			return false;
		}

		return realValue.evaluateIsvalue(fromSequence);
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (realValue != null) {
			realValue.findReferences(referenceFinder, foundIdentifiers);
		} else if (asReference != null) {
			asReference.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (realValue != null) {
			if (!realValue.accept(v)) {
				return false;
			}
		} else if (asReference != null) {
			if (!asReference.accept(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void setGenNameRecursive(final String parameterGenName) {
		if (realValue != null) {
			realValue.setGenNameRecursive(parameterGenName);
		}

		super.setGenNameRecursive(parameterGenName);
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		if (realValue != null) {
			return realValue.canGenerateSingleExpression();
		}

		// error
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateSingleExpression(final JavaGenData aData) {
		if (realValue != null) {
			return realValue.generateSingleExpression(aData);
		}

		return new StringBuilder("/* fatal error undefined lower identifier encountered */");
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		if (realValue != null) {
			return realValue.generateCodeInit(aData, source, name);
		}

		return new StringBuilder("/* fatal error undefined lower identifier encountered */");
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression) {
		if (realValue != null) {
			realValue.generateCodeExpression(aData, expression);
			return;
		}

		expression.expression.append("/* fatal error undefined lower identifier encountered */");
	}


}
