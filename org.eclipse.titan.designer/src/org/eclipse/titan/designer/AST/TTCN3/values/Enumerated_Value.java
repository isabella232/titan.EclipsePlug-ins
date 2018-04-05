/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
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
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IReferencingElement;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an ASN.1 enumerated value.
 * <p>
 * This value can not be parsed, but can only be converted from an undefined identifier
 *
 * @author Kristof Szabados
 * */
public final class Enumerated_Value extends Value implements IReferencingElement {

	private final Identifier value;

	public Enumerated_Value(final Identifier value) {
		this.value = value;
	}

	protected Enumerated_Value(final Undefined_LowerIdentifier_Value original) {
		copyGeneralProperties(original);
		this.value = original.getIdentifier();
	}

	@Override
	/** {@inheritDoc} */
	public Value_type getValuetype() {
		return Value_type.ENUMERATED_VALUE;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		return value.getName();
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_TTCN3_ENUMERATED;
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

	public Identifier getValue() {
		return value;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return Value_type.ENUMERATED_VALUE.equals(last.getValuetype()) && value.equals(((Enumerated_Value) last).getValue());
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(value.getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public boolean evaluateIsvalue(final boolean fromSequence) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value == null || myGovernor == null) {
			return;
		}

		if (referenceFinder.assignment.getAssignmentType() == Assignment_type.A_TYPE
				&& referenceFinder.type == myGovernor && value.equals(referenceFinder.fieldId)) {
			foundIdentifiers.add(new Hit(value));
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (value!=null && !value.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public Declaration getDeclaration() {

		IType type = getMyGovernor();
		if (type == null) {
			return null;
		}
		type = type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());

		if (type instanceof ITypeWithComponents) {
			final Identifier resultId = ((ITypeWithComponents) type).getComponentIdentifierByName(value);
			return Declaration.createInstance(type.getDefiningAssignment(), resultId);
		}

		return null;
	}

	//==== Code generation ====
	/**
	 * Generates a Java code sequence, which initializes the Java
	 *  object named  name with the contents of the value. The code
	 *  sequence is appended to argument source and the resulting
	 *  string is returned.
	 *
	 *  generate_code_init in the compiler
	 *
	 *  @param aData the structure to put imports into and get temporal variable names from.
	 *  @param source the source to be updated
	 *  @param name the name to be used for initialization
	 * */
	@Override
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		source.append(MessageFormat.format("{0}.assign({1}.enum_type.{2});\n",
					name,
					this.getMyGovernor().getGenNameValue(aData, source, myScope),
					this.getValue().getName()
					));
		return source;
	}

	public boolean canGenerateSingleExpression() {
		//TODO this might be a good location to check for the need of conversion
		//TODO implement
		return true;
	}

	/**
	 * Returns the equivalent Java expression.
	 * It can be used only if canGenerateSingleExpression() returns true
	 *
	 * get_single_expr in the compiler
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * */
	public StringBuilder generateSingleExpression(final JavaGenData aData) {
		final StringBuilder source = new StringBuilder();
		//default implementation
		source.append(MessageFormat.format("new {0}({0}.enum_type.{1})", getMyGovernor().getGenNameValue(aData, source, myScope), getValue().getName()));
		//TODO: Implement all cases! This is just the first draft
		return source;
	}

	/**
	 * Generates the equivalent Java code for the value. It is used
	 *  when the value is part of a complex expression (e.g. as
	 *  operand of a built-in operation, actual parameter, array
	 *  index). The generated code fragments are appended to the
	 *  fields of visitor expr.
	 *
	 *  generate_code_expr in the compiler
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression to generate source code into
	 * */
	@Override
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final boolean forceObject) {
		if (canGenerateSingleExpression()) {
			expression.expression.append(generateSingleExpression(aData));
			return;
		}

		expression.expression.append( "\t//TODO: " );
		expression.expression.append( getClass().getSimpleName() );
		expression.expression.append( ".generateCodeExpression() is not implemented!\n" );
	}
}
