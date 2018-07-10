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
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.MessageEncoding_type;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator.MessageMappingType_type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator.MessageTypeMappingTarget;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents an encoding type mapping target.
 *
 * @author Kristof Szabados
 * */
public final class EncodeTypeMappingTarget extends TypeMappingTarget {
	private static final String FULLNAMEPART1 = ".<target_type>";
	private static final String FULLNAMEPART2 = ".<errorbehavior>";

	private final Type targetType;
	private final EncodeAttribute encodeAttribute;
	private final ErrorBehaviorAttribute errorBehaviorAttribute;

	public EncodeTypeMappingTarget(final Type targetType, final ExtensionAttribute encodeAttribute,
			final ErrorBehaviorAttribute errorBehaviorAttribute) {
		this.targetType = targetType;
		if (encodeAttribute instanceof EncodeAttribute) {
			this.encodeAttribute = (EncodeAttribute) encodeAttribute;
		} else {
			this.encodeAttribute = null;
		}
		this.errorBehaviorAttribute = errorBehaviorAttribute;
	}

	@Override
	/** {@inheritDoc} */
	public TypeMapping_type getTypeMappingType() {
		return TypeMapping_type.ENCODE;
	}

	@Override
	/** {@inheritDoc} */
	public String getMappingName() {
		return "encode";
	}

	@Override
	/** {@inheritDoc} */
	public Type getTargetType() {
		return targetType;
	}

	public MessageEncoding_type getCodingType() {
		if (encodeAttribute != null) {
			return encodeAttribute.getEncodingType();
		}

		return MessageEncoding_type.UNDEFINED;
	}

	public boolean hasCodingOptions() {
		if (encodeAttribute != null) {
			return encodeAttribute.getOptions() != null;
		}

		return false;
	}

	public String getCodingOptions() {
		if (encodeAttribute != null) {
			return encodeAttribute.getOptions();
		}

		return "UNDEFINED";
	}

	public ErrorBehaviorList getErrrorBehaviorList() {
		return errorBehaviorAttribute.getErrrorBehaviorList();
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (targetType == child) {
			return builder.append(FULLNAMEPART1);
		} else if (errorBehaviorAttribute == child) {
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
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final Type sourceType, final Port_Type portType, final boolean legacy, final boolean incoming) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (targetType != null) {
			targetType.check(timestamp);
		}

		if (!sourceType.hasEncoding(timestamp, encodeAttribute.getEncodingType(), encodeAttribute.getOptions())) {
			sourceType.getLocation().reportSemanticError(MessageFormat.format("Source type `{0}'' does not support {1} encoding", sourceType.getTypename(), encodeAttribute.getEncodingType().getEncodingName()));
		}

		final Type streamType = Type.getStreamType(encodeAttribute.getEncodingType(), 1);
		if (streamType != null && !streamType.isIdentical(timestamp, targetType)) {
			targetType.getLocation().reportSemanticError(MessageFormat.format("Target type of {0} encoding should be `{1}'' instead of `{2}''", encodeAttribute.getEncodingType().getEncodingName(), streamType.getTypename(), targetType.getTypename()));
		}

		if (errorBehaviorAttribute != null) {
			errorBehaviorAttribute.getErrrorBehaviorList().check(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (targetType != null) {
			targetType.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (targetType != null && !targetType.accept(v)) {
			return false;
		}
		if (encodeAttribute != null && !encodeAttribute.accept(v)) {
			return false;
		}
		if (errorBehaviorAttribute != null && !errorBehaviorAttribute.accept(v)) {
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

		String typeDescriptorName = outType.getGenNameTypeDescriptor(aData, source, scope);
		String encodingType = encodeAttribute.getEncodingType().getEncodingName();
		String encodingOptions = null;
		if (encodeAttribute.getOptions() != null) {
			encodingOptions = encodeAttribute.getOptions();
		}
		StringBuilder errorBehaviour = new StringBuilder();
		if (errorBehaviorAttribute != null) {
			errorBehaviorAttribute.getErrrorBehaviorList().generateCode(aData, errorBehaviour);
		} else {
			aData.addCommonLibraryImport("TTCN_EncDec");

			errorBehaviour.append( "TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_ALL, TTCN_EncDec.error_behavior_type.EB_DEFAULT);\n" );
		}

		return new PortGenerator.MessageTypeMappingTarget(targetTypeName, targetTemplateName, displayName, typeDescriptorName, encodingType, encodingOptions, errorBehaviour.toString(), MessageMappingType_type.ENCODE);
	}
}
