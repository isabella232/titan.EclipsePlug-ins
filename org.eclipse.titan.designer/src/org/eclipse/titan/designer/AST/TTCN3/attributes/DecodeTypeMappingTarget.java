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
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator.MessageMappingType_type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortGenerator.MessageTypeMappingTarget;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents an decoding type mapping target.
 *
 * @author Kristof Szabados
 * */
public final class DecodeTypeMappingTarget extends TypeMappingTarget {

	private final Type targetType;
	private final DecodeAttribute decodeAttribute;
	private final ErrorBehaviorAttribute errorBehaviorAttribute;

	public DecodeTypeMappingTarget(final Type targetType, final ExtensionAttribute decodeAttribute,
			final ErrorBehaviorAttribute errorBehaviorAttribute) {
		this.targetType = targetType;
		if (decodeAttribute instanceof DecodeAttribute) {
			this.decodeAttribute = (DecodeAttribute) decodeAttribute;
		} else {
			this.decodeAttribute = null;
		}
		this.errorBehaviorAttribute = errorBehaviorAttribute;
	}

	@Override
	/** {@inheritDoc} */
	public TypeMapping_type getTypeMappingType() {
		return TypeMapping_type.DECODE;
	}

	@Override
	/** {@inheritDoc} */
	public String getMappingName() {
		return "decode";
	}

	@Override
	/** {@inheritDoc} */
	public Type getTargetType() {
		return targetType;
	}

	public MessageEncoding_type getCodingType() {
		if (decodeAttribute != null) {
			return decodeAttribute.getEncodingType();
		}

		return MessageEncoding_type.UNDEFINED;
	}

	public boolean hasCodingOptions() {
		if (decodeAttribute != null) {
			return decodeAttribute.getOptions() != null;
		}

		return false;
	}

	public String getCodingOptions() {
		if (decodeAttribute != null) {
			return decodeAttribute.getOptions();
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
			return builder.append(".<target_type>");
		} else if (errorBehaviorAttribute == child) {
			return builder.append(".<errorbehavior>");
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

		final Type streamType = Type.getStreamType(decodeAttribute.getEncodingType(), 1);
		if (streamType != null && !streamType.isIdentical(timestamp, sourceType)) {
			sourceType.getLocation().reportSemanticError(MessageFormat.format("Source type of {0} encoding should be `{1}'' instead of `{2}''", decodeAttribute.getEncodingType().getEncodingName(), streamType.getTypename(), sourceType.getTypename()));
		}

		if (!targetType.hasEncoding(timestamp, decodeAttribute.getEncodingType(), decodeAttribute.getOptions())) {
			targetType.getLocation().reportSemanticError(MessageFormat.format("Target type `{0}'' does not support {1} encoding", targetType.getTypename(), decodeAttribute.getEncodingType().getEncodingName()));
		}

		if (errorBehaviorAttribute != null) {
			errorBehaviorAttribute.check(timestamp);
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
		if (decodeAttribute != null && !decodeAttribute.accept(v)) {
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
		String encodingType = decodeAttribute.getEncodingType().getEncodingName();
		String encodingOptions = null;
		if (decodeAttribute.getOptions() != null) {
			encodingOptions = decodeAttribute.getOptions();
		}
		StringBuilder errorBehaviour = new StringBuilder();
		if (errorBehaviorAttribute != null) {
			errorBehaviorAttribute.getErrrorBehaviorList().generateCode(aData, errorBehaviour);
		} else {
			aData.addCommonLibraryImport("TTCN_EncDec");

			errorBehaviour.append( "TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_ALL, TTCN_EncDec.error_behavior_type.EB_DEFAULT);\n" );
		}

		return new PortGenerator.MessageTypeMappingTarget(targetTypeName, targetTemplateName, displayName, typeDescriptorName, encodingType, encodingOptions, errorBehaviour.toString(), MessageMappingType_type.DECODE);
	}
}
