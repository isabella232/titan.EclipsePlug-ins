/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function.EncodingPrototype_type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator.FunctionPrototype_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator.MessageTypeMappingTarget;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a function based type mapping target.
 *
 * @author Kristof Szabados
 * */
public final class FunctionTypeMappingTarget extends TypeMappingTarget {
	private static final String FULLNAMEPART1 = ".<target_type>";
	private static final String FULLNAMEPART2 = ".<function_ref>";

	private final Type targetType;
	private final Reference functionReference;
	private Def_Function functionReferenced;
	private Def_Extfunction extfunctionReferenced;

	public FunctionTypeMappingTarget(final Type targetType, final Reference functionReference) {
		this.targetType = targetType;
		this.functionReference = functionReference;
	}

	@Override
	/** {@inheritDoc} */
	public TypeMapping_type getTypeMappingType() {
		return TypeMapping_type.FUNCTION;
	}

	@Override
	/** {@inheritDoc} */
	public String getMappingName() {
		return "function";
	}

	@Override
	/** {@inheritDoc} */
	public Type getTargetType() {
		return targetType;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (targetType == child) {
			return builder.append(FULLNAMEPART1);
		} else if (functionReference == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (targetType != null) {
			targetType.setMyScope(scope);
		}
		if (functionReference != null) {
			functionReference.setMyScope(scope);
		}
	}

	public Def_Function getFunction() {
		return functionReferenced;
	}

	public Def_Extfunction getExternalFunction() {
		return extfunctionReferenced;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final Type sourceType, final Port_Type portType, final boolean legacy, final boolean incoming) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;
		functionReferenced = null;
		extfunctionReferenced = null;

		if (functionReference == null) {
			return;
		}

		final Assignment assignment = functionReference.getRefdAssignment(timestamp, false);
		if (assignment == null) {
			return;
		}

		assignment.check(timestamp);

		EncodingPrototype_type referencedPrototype;
		Type inputType;
		Type outputType;
		switch (assignment.getAssignmentType()) {
		case A_FUNCTION:
		case A_FUNCTION_RVAL:
		case A_FUNCTION_RTEMP:
			functionReferenced = (Def_Function) assignment;
			referencedPrototype = functionReferenced.getPrototype();
			inputType = functionReferenced.getInputType();
			outputType = functionReferenced.getOutputType();
			break;
		case A_EXT_FUNCTION:
		case A_EXT_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RTEMP:
			// External functions are not allowed when the standard like behavior is used
			if (legacy) {
				extfunctionReferenced = (Def_Extfunction) assignment;
				referencedPrototype = extfunctionReferenced.getPrototype();
				inputType = extfunctionReferenced.getInputType();
				outputType = extfunctionReferenced.getOutputType();
				break;
			}
		default:
			functionReference.getLocation().reportSemanticError(
					MessageFormat.format("Reference to a function or external function was expected instead of {0}",
							assignment.getDescription()));
			return;
		}

		if (legacy && EncodingPrototype_type.NONE.equals(referencedPrototype)) {
			functionReference.getLocation().reportSemanticError(
					MessageFormat.format("The referenced {0} does not have `prototype'' attribute", assignment.getDescription()));
			return;
		}

		if (!legacy && !EncodingPrototype_type.FAST.equals(referencedPrototype)) {
			functionReference.getLocation().reportSemanticError(
					MessageFormat.format("The referenced {0} does not have `prototype'' fast attribute", assignment.getDescription()));
			return;
		}

		if (legacy && inputType != null && sourceType != null && !sourceType.isIdentical(timestamp, inputType)) {
			final String message = MessageFormat
					.format("The input type of {0} must be the same as the source type of the mapping: `{1}'' was expected instead of `{2}''",
							assignment.getDescription(), sourceType.getTypename(), inputType.getTypename());
			sourceType.getLocation().reportSemanticError(message);
		}
		if (legacy && outputType != null && !targetType.isIdentical(timestamp, outputType)) {
			final String message = MessageFormat
					.format("The output type of {0} must be the same as the target type of the mapping: `{1}'' was expected instead of `{2}''",
							assignment.getDescription(), targetType.getTypename(), outputType.getTypename());
			targetType.getLocation().reportSemanticError(message);
		}

		//  The standard like behavior has different function param checking
		if (!legacy) {
			// In the error message the source type is the target_type
			// and the target type is the source_type for a reason.
			// Reason: In the new standard like behavior the conversion functions 
			// has the correct param order for in and out parameters
			// (which is more logical than the old behavior)
			// For example:
			// in octetstring from integer with int_to_oct()
			//         |              |             |
			//    target_type     source_type   conv. func.
			if (incoming) {
				if (inputType != null && !targetType.isIdentical(timestamp, inputType)) {
					final String message = MessageFormat
							.format("The input type of {0} must be the same as the source type of the mapping: `{1}'' was expected instead of `{2}''",
									assignment.getDescription(), targetType.getTypename(), inputType.getTypename());
					targetType.getLocation().reportSemanticError(message);
				}
				if (outputType != null && !sourceType.isIdentical(timestamp, outputType)) {
					final String message = MessageFormat
							.format("The output type of {0} must be the same as the target type of the mapping: `{1}'' was expected instead of `{2}''",
									assignment.getDescription(), sourceType.getTypename(), outputType.getTypename());
					targetType.getLocation().reportSemanticError(message);
				}
			} else {
				// For example:
				// out octetstring to integer with oct_to_int()
				//         |              |             |
				//    source_type     target_type   conv. func.
				if (inputType != null && !sourceType.isIdentical(timestamp, inputType)) {
					final String message = MessageFormat
							.format("The input type of {0} must be the same as the source type of the mapping: `{1}'' was expected instead of `{2}''",
									assignment.getDescription(), sourceType.getTypename(), inputType.getTypename());
					targetType.getLocation().reportSemanticError(message);
				}
				if (outputType != null && !targetType.isIdentical(timestamp, outputType)) {
					final String message = MessageFormat
							.format("The output type of {0} must be the same as the target type of the mapping: `{1}'' was expected instead of `{2}''",
									assignment.getDescription(), targetType.getTypename(), outputType.getTypename());
					targetType.getLocation().reportSemanticError(message);
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (targetType != null) {
			targetType.findReferences(referenceFinder, foundIdentifiers);
		}
		if (functionReference != null) {
			functionReference.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (targetType != null && !targetType.accept(v)) {
			return false;
		}
		if (functionReference != null && !functionReference.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public MessageTypeMappingTarget fillTypeMappingTarget(final JavaGenData aData, final StringBuilder source, final IType outType, final Scope scope, final AtomicBoolean hasSliding) {
		String targetTypeName = null;
		String targetTemplateName = null;
		String displayName = null;

		hasSliding.set(false);
		if (targetType != null) {
			targetTypeName = outType.getGenNameValue(aData, source, scope);
			targetTemplateName = outType.getGenNameTemplate(aData, source, scope);
			displayName = outType.getTypename();
		}

		String functionName = functionReferenced.getGenNameFromScope(aData, source, scope, "");
		String functionDisplayName = functionReferenced.getFullName();
		FunctionPrototype_Type prototype = null;
		switch (functionReferenced.getPrototype()) {
		case CONVERT:
			prototype = FunctionPrototype_Type.CONVERT;
			break;
		case FAST:
			prototype = FunctionPrototype_Type.FAST;
			break;
		case BACKTRACK:
			prototype = FunctionPrototype_Type.BACKTRACK;
			break;
		case SLIDING:
			prototype = FunctionPrototype_Type.SLIDING;
			hasSliding.set(true);
			break;
		default:
			break;
		}

		return new PortGenerator.MessageTypeMappingTarget(targetTypeName, targetTemplateName, displayName, functionName, functionDisplayName, prototype);
	}
}
