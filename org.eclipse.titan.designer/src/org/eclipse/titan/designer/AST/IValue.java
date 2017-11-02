/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsValueExpression;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public interface IValue extends IGovernedSimple, IIdentifierContainer, IVisitableNode {

	public enum Value_type {
		// common values (they reside among the TTCN-3 values package)
		/** NULL (ASN.1). */
		ASN1_NULL_VALUE,
		/** boolean. */
		BOOLEAN_VALUE,
		/** integer. */
		INTEGER_VALUE,
		/** real / float. */
		REAL_VALUE,
		/** charstring. */
		CHARSTRING_VALUE,
		/** universal charstrin. */
		UNIVERSALCHARSTRING_VALUE,
		/** omit value. */
		OMIT_VALUE,
		/** object identifier. */
		OBJECTID_VALUE,
		/** sequence of. */
		SEQUENCEOF_VALUE,
		/** set of. */
		SETOF_VALUE,
		/** sequence. */
		SEQUENCE_VALUE,
		/** set. */
		SET_VALUE,
		/** referenced. */
		REFERENCED_VALUE,
		/** enumerated. */
		ENUMERATED_VALUE,
		/** undefined loweridentifier. */
		UNDEFINED_LOWERIDENTIFIER_VALUE,
		/** choice (union) */
		CHOICE_VALUE,
		// choice

		// TTCN-3 values
		/** general NULL (TTCN-3). */
		TTCN3_NULL_VALUE,
		/** default null (TTCN-3). */
		DEFAULT_NULL_VALUE,
		/** function reference null (TTCN-3). */
		FAT_NULL_VALUE,
		/** bitstring (TTCN-3). */
		BITSTRING_VALUE,
		/** hexstring (TTCN-3). */
		HEXSTRING_VALUE,
		/** octetstring (TTCN-3). */
		OCTETSTRING_VALUE,
		/** expressions (TTCN-3). */
		EXPRESSION_VALUE,
		/** verdict. */
		VERDICT_VALUE,
		/** macro. */
		MACRO_VALUE,
		/** not used symbol ('-'). */
		NOTUSED_VALUE,
		/** array. */
		ARRAY_VALUE,
		/** function reference. */
		FUNCTION_REFERENCE_VALUE,
		/** altstep reference. */
		ALTSTEP_REFERENCE_VALUE,
		/** testcase reference. */
		TESTCASE_REFERENCE_VALUE,
		/** anytype. */
		ANYTYPE_VALUE,

		// ASN.1 values
		/** undefined block. */
		UNDEFINED_BLOCK,
		/** named integer. */
		NAMED_INTEGER_VALUE,
		/** named bits. */
		NAMED_BITS,
		/** parsed ASN.1 string notation. */
		CHARSYMBOLS_VALUE,
		/** ISO-2022 string. */
		ISO2022STRING_VALUE,
		/** relative object identifier */
		RELATIVEOBJECTIDENTIFIER_VALUE
		// iso2022str
		// opentype
	}

	/**
	 * Copies the general value -ish properties of the value in parameter to
	 * the actual one.
	 * <p>
	 * This function is used to help writing conversion function without
	 * using a generic copy-constructor mechanism.
	 *
	 * @param original
	 *                the original value, whose properties will be copied
	 * */
	void copyGeneralProperties(final IValue original);

	/** @return the kind of the value represented by the Value instance */
	Value_type getValuetype();

	@Override
	IType getMyGovernor();

	/**
	 * Sets the governor type.
	 *
	 * @param governor
	 *                the governor to be set.
	 * */
	void setMyGovernor(final IType governor);

	/**
	 * Returns the compilation timestamp of the last time this value was
	 * checked.
	 * <p>
	 * In case of values their check is not self contained, but rather done
	 * by a type. As such the timestamp of checking must also be read and
	 * set externally.
	 *
	 * @return the timestamp of the last time this value was checked.
	 * */
	CompilationTimeStamp getLastTimeChecked();

	/**
	 * Sets the compilation timestamp of the last time this value was
	 * checked.
	 * <p>
	 * In case of values their check is not self contained, but rather done
	 * by a type. As such the timestamp of checking must also be read and
	 * set externally.
	 *
	 * @param lastTimeChecked
	 *                the timestamp when this value was last checked.
	 * */
	void setLastTimeChecked(final CompilationTimeStamp lastTimeChecked);

	/**
	 * Calculates the governor of the value when used in an expression.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected
	 *
	 * @return the governor of the value if it was used in an expression.
	 * */
	IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

	/**
	 * Returns true if the value is unknown at compile-time.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 *
	 * @return true if the value is unfoldable, false if it is foldable
	 * */
	boolean isUnfoldable(final CompilationTimeStamp timestamp);

	/**
	 * Returns true if the value is unknown at compile-time.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the reference chain to detect circular references.
	 *
	 * @return true if the value is unfoldable, false if it is foldable
	 * */
	boolean isUnfoldable(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Returns true if the value is unknown at compile-time.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 * @param referenceChain
	 *                the reference chain to detect circular references.
	 *
	 * @return true if the value is unfoldable, false if it is foldable
	 * */
	boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue, final IReferenceChain referenceChain);

	/**
	 * Returns the referenced field value for structured values, or itself
	 * in any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference used to select the field.
	 * @param actualSubReference
	 *                the index used to tell, which element of the reference
	 *                to use as the field selector.
	 * @param refChain
	 *                a chain of references used to detect circular
	 *                references.
	 *
	 * @return the value of the field, self, or null.
	 * */
	IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference, int actualSubReference,
			final IReferenceChain refChain);

	/**
	 * Creates a value of the provided type from the actual value if that is
	 * possible.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param newType
	 *                the new value_type the new value should belong to.
	 *
	 * @return the new value of the provided kind if the conversion is
	 *         possible, or this value otherwise.
	 * */
	IValue setValuetype(final CompilationTimeStamp timestamp, final Value_type newType);

	/**
	 * Checks whether this value is defining itself in a recursive way. This
	 * can happen for example if a constant is using itself to determine its
	 * initial value.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references.
	 * */
	void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Check that the value (a V_EXPR) - being used as the RHS - refers to
	 * the LHS of the assignment.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param lhs
	 *                the assignment to check against
	 * @return true if self-assignment
	 * */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs);

	/**
	 * Check that the value (a V_EXPR) - being used as the RHS - refers to the LHS of the assignment.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param lhs
	 *                the assignment to check against
	 * @return true if self-assignment
	 * */
	public boolean checkExpressionSelfReferenceValue(final CompilationTimeStamp timestamp, final Assignment lhs);

	/**
	 * Creates and returns a string representation if the actual value.
	 *
	 * @return the string representation of the value.
	 * */
	String createStringRepresentation();

	/**
	 * Returns the type of the value to be used in expression evaluation.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected
	 *
	 * @return the type of the value
	 * */
	IType.Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

	/**
	 * Returns the value referred last in case of a referred value, or
	 * itself in any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references
	 *
	 * @return the actual or the last referred value
	 * */
	IValue getValueRefdLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Returns the value referred last in case of a referred value, or
	 * itself in any other case.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references
	 *
	 * @return the actual or the last referred value
	 * */
	IValue getValueRefdLast(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue, final IReferenceChain referenceChain);

	/**
	 * Creates value references from a value that is but a single word. This
	 * can happen if it was not possible to categorize it while parsing.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 *
	 * @return the reference that this lower identifier was converted to, or
	 *         this value.
	 * */
	IValue setLoweridToReference(final CompilationTimeStamp timestamp);

	/**
	 * Checks if the referenced value is equivalent with omit or not.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected
	 * */
	void checkExpressionOmitComparison(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue);

	/**
	 * Check whether the actual value equals the provided one.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param other
	 *                the value to check against.
	 *
	 * @return true if the two values equal, false otherwise.
	 * */
	boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other);

	/**
	 * Evaluates if a value is a valid value argument of the isvalue
	 * expression.
	 *
	 * @see IsValueExpression#evaluateValue(CompilationTimeStamp,
	 *      Expected_Value_type, ReferenceChain)
	 *
	 * @param fromSequence
	 *                true if called from a sequence.
	 *
	 * @return true if the value can be used within the isvalue expression
	 *         directly.
	 * */
	boolean evaluateIsvalue(final boolean fromSequence);

	/**
	 * Evaluates whether the value is bound or not.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference to resolve at this object.
	 * @param actualSubReference
	 *                the index of the sub reference we are resolving at
	 *                this time.
	 *
	 * @return true if the value is bound, false otherwise.
	 * */
	boolean evaluateIsbound(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference);

	/**
	 * Evaluates whether the value is present or not.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param reference
	 *                the reference to resolve at this object.
	 * @param actualSubReference
	 *                the index of the sub reference we are resolving at
	 *                this time.
	 *
	 * @return true if the value is present, false otherwise.
	 * */
	boolean evaluateIspresent(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference);

	/**
	 * Try to find the definition in which this value was defined.
	 *
	 * @return the definition defining this value, or null
	 * */
	Definition getDefiningAssignment();

	/**
	 * sets the name to be used when generating code recursively
	 * */
	public void setGenNameRecursive(final String parameterGenName);

	/**
	 * Returns whether the evaluation of this value has side-effects that shall
	 * be eliminated in case of short-circuit evaluation of logical "and" and
	 * "or" operations. This function is applied on the second (right) operand
	 * of the expression.
	 *
	 * needs_short_circuit in the compiler
	 * */
	public boolean needsShortCircuit ();

	/**
	 * Returns whether the value can be represented by an in-line Java
	 *  expression.
	 *
	 *  has_single_expr in the compiler
	 * */
	public boolean canGenerateSingleExpression();

	/**
	 * Returns the equivalent Java expression.
	 * It can be used only if canGenerateSingleExpression() returns true
	 *
	 * get_single_expr in the compiler
	 *
	 * @param aData the generated java code
	 * */
	public StringBuilder generateSingleExpression(final JavaGenData aData);

	/**
	 * Returns whether the generated Java expression will return a native value or a Titan object.
	 *
	 * @return true if the expression returns a native value when generated.
	 * */
	public boolean returnsNative();

	/**
	 * Generates a Java code sequence, which initializes the Java
	 *  object named  name with the contents of the value. The code
	 *  sequence is appended to argument source and the resulting
	 *  string is returned.
	 *
	 *  generate_code_init in the compiler
	 *
	 *  @param aData the structure to put imports into and get temporal variable names from.
	 *  @param source the source code to be updated
	 *  @param name the name which should be used to initialize
	 * */
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name);

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
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule);

	/**
	 * Generates the equivalent Java code for the value. It is used
	 *  when the value is part of a complex expression (e.g. as
	 *  operand of a built-in operation, actual parameter, array
	 *  index). The generated code fragments are appended to the
	 *  fields of visitor expr.
	 *
	 * @param aData only used to update imports if needed
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression struct to be used to generate source code
	 * @param forceObject force the code generator to generate object.
	 * */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final boolean forceObject);

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
	public void generateCodeExpressionMandatory(final JavaGenData aData, final ExpressionStruct expression, final boolean forceObject);

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
	 *  @param source the source code to be updated
	 *  @param prefix the prefix to be generated before the actual value
	 *  @param blockCount the block counter storing how many open blocks there are in the local area.
	 */
	StringBuilder generateCodeTmp(final JavaGenData aData, final StringBuilder source, final String prefix, final AtomicInteger blockCount);

	/**
	 * as above
	 *  @param aData the structure to put imports into and get temporal variable names from.
	 *  @param source the source code to be updated
	 *  @param init is the content to be generated before the current value
	 * */
	StringBuilder generateCodeTmp(final JavaGenData aData, final StringBuilder source, final StringBuilder init);

	/**
	 * Generates the Java statement that puts the value of this into the log.
	 * It is used when the value appears in the argument of a log() statement.
	 *
	 * generate_code_log in the compiler
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression to generate source code into
	 * */
	public void generateCodeLog(final JavaGenData aData, final ExpressionStruct expression);

	/**
	 * Returns a Java reference that points to this setting from the local module.
	 *
	 * @return The name of the Java setting in the generated code.
	 */
	String getGenNameOwn();
}
