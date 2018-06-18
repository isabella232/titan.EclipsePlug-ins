/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeFactory;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsValueExpression;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The base of what a Value is.
 *
 * @author Kristof Szabados
 * */
// TODO The ASN.1 values can not be incrementally updated.
public abstract class Value extends GovernedSimple implements IReferenceChainElement, IValue, IIncrementallyUpdateable {

	/** The type of the value, which also happens to be its governor. */
	protected IType myGovernor;

	/**
	 * This is the governor last set for this value.
	 * Should only be handle in code generation, and even there with care.
	 *
	 * During the semantic analysis this should not be used
	 * as it only serves a purpose to keep the temporary value of myGovernor around for longer.
	 * */
	protected IType myLastSetGovernor;

	@Override
	/** {@inheritDoc} */
	public Setting_type getSettingtype() {
		return Setting_type.S_V;
	}

	/**
	 * Copies the general value -ish properties of the value in parameter to the actual one.
	 * <p>
	 * This function is used to help writing conversion function without using a generic copy-constructor mechanism.
	 *
	 * @param original the original value, whose properties will be copied
	 * */
	@Override
	public final void copyGeneralProperties(final IValue original) {
		location = original.getLocation();
		super.setFullNameParent(original.getNameParent());
		myGovernor = original.getMyGovernor();
		myLastSetGovernor = original.getMyGovernor();
		setMyScope(original.getMyScope());
	}

	@Override
	/** {@inheritDoc} */
	public abstract Value_type getValuetype();

	/**
	 * Gets the governor type.
	 *
	 * @return the type governing this value.
	 * */
	@Override
	/** {@inheritDoc} */
	public final IType getMyGovernor() {
		return myGovernor;
	}

	/**
	 * Sets the governor type.
	 *
	 * @param governor the governor to be set.
	 * */
	@Override
	/** {@inheritDoc} */
	public final void setMyGovernor(final IType governor) {
		myGovernor = governor;
		if (governor != null) {
			myLastSetGovernor = governor;
		}
	}

	/**
	 * Returns the compilation timestamp of the last time this value was checked.
	 * <p>
	 * In case of values their check is not self contained, but rather done by a type.
	 * As such the timestamp of checking must also be read and set externally.
	 *
	 * @return the timestamp of the last time this value was checked.
	 * */
	@Override
	public final CompilationTimeStamp getLastTimeChecked() {
		return lastTimeChecked;
	}

	/**
	 * Sets the compilation timestamp of the last time this value was checked.
	 * <p>
	 * In case of values their check is not self contained, but rather done by a type.
	 * As such the timestamp of checking must also be read and set externally.
	 *
	 * @param lastTimeChecked the timestamp when this value was last checked.
	 * */
	@Override
	public final void setLastTimeChecked(final CompilationTimeStamp lastTimeChecked) {
		this.lastTimeChecked = lastTimeChecked;
	}

	@Override
	/** {@inheritDoc} */
	public String chainedDescription() {
		return getFullName();
	}

	@Override
	/** {@inheritDoc} */
	public Location getChainLocation() {
		return location;
	}

	/**
	 * Calculates the governor of the value when used in an expression.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 *
	 * @return the governor of the value if it was used in an expression.
	 * */
	@Override
	/** {@inheritDoc} */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		IType type = getMyGovernor();

		if (type == null) {
			type = TypeFactory.createType(getExpressionReturntype(timestamp, expectedValue));
		}
		return type;
	}

	/**
	 * Returns true if the value is unknown at compile-time.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 *
	 * @return true if the value is unfoldable, false if it is foldable
	 * */
	@Override
	public final boolean isUnfoldable(final CompilationTimeStamp timestamp) {
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final boolean result = isUnfoldable(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, referenceChain);
		referenceChain.release();

		return result;
	}

	/**
	 * Returns true if the value is unknown at compile-time.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param referenceChain the reference chain to detect circular references.
	 *
	 * @return true if the value is unfoldable, false if it is foldable
	 * */
	@Override
	public final boolean isUnfoldable(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		return isUnfoldable(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, referenceChain);
	}

	/**
	 * Returns true if the value is unknown at compile-time.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected.
	 * @param referenceChain the reference chain to detect circular references.
	 *
	 * @return true if the value is unfoldable, false if it is foldable
	 * */
	@Override
	public abstract boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain);

	/**
	 * Returns the referenced field value for structured values, or itself in any other case.
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param reference the reference used to select the field.
	 * @param actualSubReference the index used to tell, which element of the reference to use as the field selector.
	 * @param refChain a chain of references used to detect circular references.
	 *
	 * @return the value of the field, self, or null.
	 * */
	@Override
	public abstract IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, int actualSubReference,
			final IReferenceChain refChain);

	/**
	 * Checks the current value for the role of string encoding in decode match templates.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param lhs an assignment to check against self referencing.
	 *
	 * @return true if the value contains a reference to lhs
	 * */
	public boolean checkStringEncoding(final CompilationTimeStamp timestamp, final Assignment lhs) {
		setLoweridToReference(timestamp);

		boolean selfReference = new CharString_Type().checkThisValue(timestamp, this, lhs, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false, false, false, false));
		if (!isUnfoldable(timestamp)) {
			final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			IValue last = getValueRefdLast(timestamp, referenceChain);
			referenceChain.release();

			if (last.getValuetype() == Value_type.CHARSTRING_VALUE) {
				final String encodingName = ((Charstring_Value)last).getValue();
				if (!encodingName.equals("UTF-8")
					&& !encodingName.equals("UTF-16")
					&& !encodingName.equals("UTF-32")
					&& !encodingName.equals("UTF-32LE")
					&& !encodingName.equals("UTF-32BE")) {
					getLocation().reportSemanticError(MessageFormat.format("`{0}'' is not a valid encoding format", encodingName));
				}
			} else {
				getLocation().reportSemanticError("Cannot use this value in charstring value context");
			}
		}

		return selfReference;
	}

	/**
	 * Creates a value of the provided type from the actual value if that is possible.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param newType the new value_type the new value should belong to.
	 *
	 * @return the new value of the provided kind if the conversion is possible, or this value otherwise.
	 * */
	@Override
	/** {@inheritDoc} */
	public Value setValuetype(final CompilationTimeStamp timestamp, final Value_type newType) {
		setIsErroneous(true);
		return this;
	}

	/**
	 * Checks whether this value is defining itself in a recursive way.
	 * This can happen for example if a constant is using itself to determine its initial value.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param referenceChain the ReferenceChain used to detect circular references.
	 * */
	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		final IReferenceChain tempReferencChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue temp = getValueRefdLast(timestamp, tempReferencChain);
		tempReferencChain.release();

		if (!temp.getIsErroneous(timestamp) && this != temp && referenceChain.add(this)) {
			temp.checkRecursions(timestamp, referenceChain);
		}
	}

	@Override
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment assignment) {
		//simple values can not self-reference
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReferenceValue(final CompilationTimeStamp timestamp, final Assignment lhs) {
		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
		}
		if (governor == null) {
			return false;
		}

		return governor.checkThisValue(timestamp, this, lhs, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, true, false, false, false));
	}

	/**
	 * Creates and returns a string representation if the actual value.
	 *
	 * @return the string representation of the value.
	 * */
	@Override
	public abstract String createStringRepresentation();

	/**
	 * Returns the type of the value to be used in expression evaluation.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 *
	 *  @return the type of the value
	 * */
	@Override
	public abstract IType.Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

	/**
	 * Returns the value referred last in case of a referred value, or itself in any other case.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param referenceChain the ReferenceChain used to detect circular references
	 *
	 * @return the actual or the last referred value
	 * */
	@Override
	public final IValue getValueRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		return getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, referenceChain);
	}

	/**
	 * Returns the value referred last in case of a referred value, or itself in any other case.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 * @param referenceChain the ReferenceChain used to detect circular references
	 *
	 * @return the actual or the last referred value
	 * */
	@Override
	/** {@inheritDoc} */
	public IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return this;
		}

		setIsErroneous(false);
		lastTimeChecked = timestamp;
		return this;
	}
	/**
	 * Creates value references from a value that is but a single word.
	 * This can happen if it was not possible to categorize it while parsing.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 *
	 * @return the reference that this lower identifier was converted to, or this value.
	 * */
	@Override
	public IValue setLoweridToReference(final CompilationTimeStamp timestamp) {
		return this;
	}

	/**
	 * Checks if the referenced value is equivalent with omit or not.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param expectedValue the kind of the value to be expected
	 * */
	@Override
	public void checkExpressionOmitComparison(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return;
		}

		getLocation().reportSemanticError("Only a referenced value can be compared with `omit'");
		setIsErroneous(true);
	}

	/**
	 * Check whether the actual value equals the provided one.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 * @param other the value to check against.
	 *
	 * @return true if the two values equal, false otherwise.
	 * */
	@Override
	public abstract boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other);

	/**
	 * Evaluates if a value is a valid value argument of the isvalue expression.
	 *
	 * @see IsValueExpression#evaluateValue(CompilationTimeStamp, Expected_Value_type, ReferenceChain)
	 *
	 * @param fromSequence true if called from a sequence.
	 *
	 * @return true if the value can be used within the isvalue expression directly.
	 * */
	@Override
	/** {@inheritDoc} */
	public boolean evaluateIsvalue(final boolean fromSequence) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean evaluateIsbound(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean evaluateIspresent(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference) {
		return true;
	}

	/**
	 *  Handles the incremental parsing of this value.
	 *
	 *  @param reparser the parser doing the incremental parsing.
	 *  @param isDamaged true if the location contains the damaged area,
	 *    false if only its' location needs to be updated.
	 * */
	@Override
	public abstract void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException;

	@Override
	/** {@inheritDoc} */
	public final Definition getDefiningAssignment() {
		INamedNode parent = getNameParent();
		while (parent != null && !(parent instanceof Definition)) {
			parent = parent.getNameParent();
		}

		return (Definition) parent;

	}

	/**
	 * sets the name to be used when generating code recursively
	 * */
	public void setGenNameRecursive(final String parameterGenName) {
		if(parameterGenName.endsWith("().get()")) {
			setGenName(parameterGenName.substring(0, parameterGenName.length() - 8));
		} else if(parameterGenName.endsWith("().constGet()")) {
			setGenName(parameterGenName.substring(0, parameterGenName.length() - 13));
		} else {
			setGenName(parameterGenName);
		}
	}

	/**
	 * Returns whether the evaluation of this value has side-effects that shall
	 * be eliminated in case of short-circuit evaluation of logical "and" and
	 * "or" operations. This function is applied on the second (right) operand
	 * of the expression.
	 *
	 * needs_short_circuit in the compiler
	 * */
	public boolean needsShortCircuit () {
		//fatal error
		return true;
	}

	/**
	 * Returns whether the value can be represented by an in-line Java
	 *  expression.
	 *
	 *  has_single_expr in the compiler
	 * */
	public boolean canGenerateSingleExpression() {
		//TODO this might be a good location to check for the need of conversion
		return false;
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
		//TODO this might be a good location to check for the need of conversion
		ErrorReporter.INTERNAL_ERROR(getClass().getSimpleName() + ".generateSingleExpression() is not implemented!");
		source.append( "\t//TODO: " );
		source.append( getClass().getSimpleName() );
		source.append( ".generateSingleExpression() is not implemented!\n" );

		return source;
	}

	/**
	 * Returns whether the generated Java expression will return a native value or a Titan object.
	 *
	 * @return true if the expression returns a native value when generated.
	 * */
	public boolean returnsNative() {
		return false;
	}

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
	public abstract StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name);

	/**
	 * Generates a Java code sequence, which initializes the Java
	 *  object named  name with the contents of the value. The code
	 *  sequence is appended to argument source and the resulting
	 *  string is returned.
	 *
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source to be updated
	 * @param name the name to be used for initialization
	 * */
	public StringBuilder generateCodeInitMandatory(final JavaGenData aData, final StringBuilder source, final String name) {
		return generateCodeInit(aData, source, name);
	}

	/**
	 * Appends the initialization sequence of all referred non-parameterized
	 * templates to source and returns the resulting string. Such templates
	 * may appear in the actual parameter list of parameterized value
	 * references (e.g. function calls) and in operands of valueof or match
	 * operations.
	 *
	 * rearrange_init_code in the compiler
	 *
	 * @param aData
	 *                the structure to put imports into and get temporal
	 *                variable names from.
	 * @param source
	 *                the source code to be updated
	 * @param usageModule
	 *                the module where the value needs to be initialized
	 * */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		//Do nothing by default
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
	 * @param forceObject force the code generator to generate object.
	 * */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final boolean forceObject) {
		if (canGenerateSingleExpression()) {
			if (returnsNative() && forceObject) {
				final IType gov = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);

				expression.expression.append(MessageFormat.format("new {0}(", gov.getGenNameValue(aData, new StringBuilder(), myScope)));
				expression.expression.append(generateSingleExpression(aData));
				expression.expression.append(")");
			} else {
				expression.expression.append(generateSingleExpression(aData));
			}

			return;
		}

		ErrorReporter.INTERNAL_ERROR(getClass().getSimpleName() + ".generateCodeExpression() is not implemented!");
	}

	/**
	 * Generates the Java equivalent of this into expression and adds a "get()"
	 * to expression.expression if this is referenced value that points to an optional
	 * field of a record/set value.
	 *
	 * generate_code_expr_mandatory in the compiler
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression to generate source code into
	 * @param forceObject force the code generator to generate object.
	 * */
	public void generateCodeExpressionMandatory(final JavaGenData aData, final ExpressionStruct expression, final boolean forceObject) {
		generateCodeExpression(aData, expression, forceObject);
	}

	/**
	 *  Generates a value for temporary use. Example:
	 *
	 *  str: // empty
	 *  prefix: if(
	 *  blockcount: 0
	 *
	 *  if the value is simple, then returns:
	 *
	 *  // empty
	 *  if(simple
	 *
	 *  if the value is complex, then returns:
	 *
	 *  // empty
	 *  {
	 *    boolean tmp_2;
	 *    {
	 *      preamble... tmp_1...
	 *      tmp_2=func5(tmp_1);
	 *      postamble
	 *    }
	 *    if(tmp_2
	 *
	 *  and also increments the blockcount because you have to close it...
	 *
	 *  generate_code_tmp in the compiler
	 *
	 *  @param aData the structure to put imports into and get temporal variable names from.
	 *  @param source the source to be updated
	 *  @param prefix the string to be used as prefix of the value
	 *  @param blockCount the counter storing the number of open block in the local area
	 */
	public StringBuilder generateCodeTmp(final JavaGenData aData, final StringBuilder source, final String prefix, final AtomicInteger blockCount) {
		final StringBuilder s2 = new StringBuilder();
		final StringBuilder s1 = generateCodeTmp(aData, new StringBuilder(), s2);

		if(s2.length() > 0) {
			if(blockCount.get() == 0) {
				source.append("{\n");
				blockCount.incrementAndGet();
			}
			source.append(s2);
		}

		source.append(prefix);
		source.append(s1);

		return source;
	}

	/**
	 * as above
	 *  @param aData the structure to put imports into and get temporal variable names from.
	 *  @param source the source code to be updated
	 *  @param init is the content to be generated before the current value
	 * */
	public StringBuilder generateCodeTmp(final JavaGenData aData, final StringBuilder source, final StringBuilder init) {
		final ExpressionStruct expression = new ExpressionStruct();

		generateCodeExpressionMandatory(aData, expression, false);

		if(expression.preamble.length() > 0 || expression.postamble.length() > 0) {
			String typeName;
			final String tempId = aData.getTemporaryVariableName();
			final IType lastType = myGovernor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
			if(Type_type.TYPE_BOOL.equals(lastType.getTypetype())) {
				typeName = "boolean";
			} else {
				typeName = myGovernor.getGenNameValue(aData, init, myScope);
			}
			init.append(MessageFormat.format("{0} {1};\n", typeName, tempId));
			init.append("{\n");

			if (expression.preamble.length() > 0) {
				init.append(expression.preamble);
			}

			if(Type_type.TYPE_BOOL.equals(lastType.getTypetype()) && !returnsNative()) {
				init.append(MessageFormat.format("{0} = TitanBoolean.getNative({1});\n", tempId, expression.expression));
			} else {
				init.append(MessageFormat.format("{0} = {1};\n", tempId, expression.expression));
			}

			if(expression.postamble.length() > 0) {
				init.append(expression.postamble);
			}
			init.append("}\n");
			source.append(tempId);
		} else {
			source.append(expression.expression);
		}

		return source;
	}

	/**
	 * Generates the Java statement that puts the value of this into the log.
	 * It is used when the value appears in the argument of a log() statement.
	 *
	 * generate_code_log in the compiler
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression to generate source code into
	 * */
	public void generateCodeLog(final JavaGenData aData, final ExpressionStruct expression) {
		//FIXME add support for explicit cast if needed.
		generateCodeExpression(aData, expression, true);
		expression.expression.append(".log()");
	}

	/**
	 * Adds the character sequence "get()" to expression->expression if reference points to
	 * an optional field of a record/set value.
	 *
	 * generate_code_expr_optional_field_ref in the compiler
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression to generate source code into
	 * @param reference the reference to check
	 * */
	public void generateCodeExpressionOptionalFieldReference(final JavaGenData aData, final ExpressionStruct expression, final Reference reference) {
		final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);

		switch (assignment.getAssignmentType()) {
		case A_CONST:
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_VAR:
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RVAL:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
			//only these are mapped to value objects
			if (assignment.getType(CompilationTimeStamp.getBaseTimestamp()).fieldIsOptional(reference.getSubreferences())) {
				expression.expression.append(".constGet()");
			}
			break;
		default:
			break;
		}
	}
}
