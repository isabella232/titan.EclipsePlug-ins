/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class ComponentCreateExpression extends Expression_Value {
	private static final String FIRSTOPERANDERROR = "The first operand of operation `create()' should be a charstring value";
	private static final String SECONDOPERANDERROR = "The second operand of operation `create()' should be a charstring value";
	private static final String COMPONENTEXPECTED = "Operation `create'' should refer to a component type instead of {0}";
	private static final String TYPEMISMATCH1 = "Type mismatch: reference to a component type was expected in operation `create'' instead of `{0}''";
	private static final String TYPEMISMATCH2 = "Incompatible component type: operation `create'' should refer to `{0}'' instaed of `{1}''";
	private static final String OPERATIONNAME = "create()";

	private final Reference componentReference;
	private final Value name;
	private final Value location;
	private final boolean isAlive;

	private CompilationTimeStamp checkCreateTimestamp;
	private Component_Type checkCreateCache;

	public ComponentCreateExpression(final Reference componentReference, final Value name, final Value location, final boolean isAlive) {
		this.componentReference = componentReference;
		this.name = name;
		this.location = location;
		this.isAlive = isAlive;

		if (componentReference != null) {
			componentReference.setFullNameParent(this);
		}
		if (name != null) {
			name.setFullNameParent(this);
		}
		if (location != null) {
			location.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.COMPONENT_CREATE_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		//assume no self-ref
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		if (componentReference == null) {
			return "<erroneous value>";
		}

		final StringBuilder builder = new StringBuilder();
		builder.append(componentReference.getDisplayName());
		builder.append(".create");
		if (name != null || location != null) {
			builder.append('(');
			if (name != null) {
				builder.append(name.createStringRepresentation());
			} else {
				builder.append('-');
			}
			if (location != null) {
				builder.append(", ");
				builder.append(location.createStringRepresentation());
			}
			builder.append(')');
		}
		if (isAlive) {
			builder.append(" alive");
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (componentReference != null) {
			componentReference.setMyScope(scope);
		}
		if (name != null) {
			name.setMyScope(scope);
		}
		if (location != null) {
			location.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (componentReference != null) {
			componentReference.setCodeSection(codeSection);
		}
		if (name != null) {
			name.setCodeSection(codeSection);
		}
		if (location != null) {
			location.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (componentReference == child) {
			return builder.append(OPERAND1);
		} else if (name == child) {
			return builder.append(OPERAND2);
		} else if (location == child) {
			return builder.append(OPERAND3);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_COMPONENT;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public Type getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return checkCreate(timestamp);
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
		checkCreate(timestamp);
		if (name != null) {
			final IValue last = name.setLoweridToReference(timestamp);
			final Type_type typeType = last.getExpressionReturntype(timestamp, expectedValue);
			if (!last.getIsErroneous(timestamp)) {
				switch (typeType) {
				case TYPE_CHARSTRING:
					last.getValueRefdLast(timestamp, referenceChain);
					break;
				case TYPE_UNDEFINED:
					break;
				default:
					name.getLocation().reportSemanticError(FIRSTOPERANDERROR);
					setIsErroneous(true);
					break;
				}
			}
		}
		if (location != null) {
			final IValue last = location.setLoweridToReference(timestamp);
			final Type_type typeType = last.getExpressionReturntype(timestamp, expectedValue);
			if (!last.getIsErroneous(timestamp)) {
				switch (typeType) {
				case TYPE_CHARSTRING:
					last.getValueRefdLast(timestamp, referenceChain);
					break;
				case TYPE_UNDEFINED:
					break;
				default:
					name.getLocation().reportSemanticError(SECONDOPERANDERROR);
					setIsErroneous(true);
					break;
				}
			}
		}
		checkExpressionDynamicPart(expectedValue, OPERATIONNAME, false, true, false);
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

		if (componentReference == null) {
			return lastValue;
		}

		checkExpressionOperands(timestamp, expectedValue, referenceChain);

		return lastValue;
	}

	private Component_Type checkCreate(final CompilationTimeStamp timestamp) {
		if (checkCreateTimestamp != null && !checkCreateTimestamp.isLess(timestamp)) {
			return checkCreateCache;
		}

		checkCreateTimestamp = timestamp;
		checkCreateCache = null;

		final Assignment assignment = componentReference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			setIsErroneous(true);
			return null;
		}

		if (!Assignment_type.A_TYPE.semanticallyEquals(assignment.getAssignmentType())) {
			componentReference.getLocation().reportSemanticError(MessageFormat.format(COMPONENTEXPECTED, assignment.getDescription()));
			setIsErroneous(true);
			return null;
		}

		final IType type = ((Def_Type) assignment).getType(timestamp).getFieldType(timestamp, componentReference, 1,
				Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (type == null) {
			setIsErroneous(true);
			return null;
		}

		if (!Type_type.TYPE_COMPONENT.equals(type.getTypetype())) {
			componentReference.getLocation().reportSemanticError(MessageFormat.format(TYPEMISMATCH1, type.getTypename()));
			setIsErroneous(true);
			return null;
		}

		if (myGovernor != null) {
			final IType last = myGovernor.getTypeRefdLast(timestamp);

			if (Type_type.TYPE_COMPONENT.equals(last.getTypetype()) && !last.isCompatible(timestamp, type, null, null, null)) {
				componentReference.getLocation().reportSemanticError(
						MessageFormat.format(TYPEMISMATCH2, last.getTypename(), type.getTypename()));
				setIsErroneous(true);
				return null;
			}
		}

		checkCreateCache = (Component_Type) type;
		return checkCreateCache;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (componentReference != null) {
			componentReference.updateSyntax(reparser, false);
			reparser.updateLocation(componentReference.getLocation());
		}

		if (name != null) {
			name.updateSyntax(reparser, false);
			reparser.updateLocation(name.getLocation());
		}

		if (location != null) {
			location.updateSyntax(reparser, false);
			reparser.updateLocation(location.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReference != null) {
			componentReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (name != null) {
			name.findReferences(referenceFinder, foundIdentifiers);
		}
		if (location != null) {
			location.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (componentReference != null && !componentReference.accept(v)) {
			return false;
		}
		if (name != null && !name.accept(v)) {
			return false;
		}
		if (location != null && !location.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		return (name == null || name.canGenerateSingleExpression()) &&
				(location == null || location.canGenerateSingleExpression());
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		aData.addCommonLibraryImport("TTCN_Runtime");

		expression.expression.append("TTCN_Runtime.createComponent(");

		// the type of the component (module name and identifier)
		final Assignment assignment = componentReference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
		if (assignment == null || !Assignment_type.A_TYPE.equals(assignment.getAssignmentType())) {
			//TODO FATAL error
			return;
		}

		IType componentType = assignment.getType(CompilationTimeStamp.getBaseTimestamp()).getFieldType(CompilationTimeStamp.getBaseTimestamp(), componentReference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		if (componentType == null) {
			//TODO fatal error
			return;
		}
		componentType = componentType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		if (!Type_type.TYPE_COMPONENT.equals(componentType.getTypetype())) {
			//TODO fatal error
			return;
		}
		((Component_Type) componentType).getComponentBody().generateCodeComponentTypeName(expression);
		expression.expression.append(", ");

		//third argument: component name
		if (name != null) {
			final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue last = name.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), referenceChain);
			referenceChain.release();

			if (Value_type.CHARSTRING_VALUE.equals(last.getValuetype())) {
				//TODO check why translate
				expression.expression.append(MessageFormat.format("\"{0}\"", ((Charstring_Value) last).getValue()));
			} else {
				name.generateCodeExpressionMandatory(aData, expression);
			}
		} else {
			expression.expression.append("null");
		}
		expression.expression.append(", ");

		//fourth argument location
		if (location != null) {
			final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final IValue last = location.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), referenceChain);
			referenceChain.release();

			if (Value_type.CHARSTRING_VALUE.equals(last.getValuetype())) {
				//TODO check why translate
				expression.expression.append(MessageFormat.format("\"{0}\"", ((Charstring_Value) last).getValue()));
			} else {
				location.generateCodeExpressionMandatory(aData, expression);
			}
		} else {
			expression.expression.append("null");
		}

		//fifth argument: alive flag
		expression.expression.append(MessageFormat.format(", {0})", isAlive?"true":"false"));
	}
}
