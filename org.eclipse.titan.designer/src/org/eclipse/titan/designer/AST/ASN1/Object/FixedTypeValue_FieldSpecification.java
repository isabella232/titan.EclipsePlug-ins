/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISetting;
import org.eclipse.titan.designer.AST.IType.TypeOwner_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ASN1.ASN1Type;
import org.eclipse.titan.designer.AST.ASN1.IASN1Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent a FixedTypeValueFieldSpec.
 *
 * @author Kristof Szabados
 */
public final class FixedTypeValue_FieldSpecification extends FieldSpecification {

	/** Fixed type. */
	private final ASN1Type fixedType;
	private final boolean isUnique;
	// FIXME only temporal solution, should be corrected when values become
	// fully supported for this usage
	private final boolean hasDefault;
	private final IValue defaultValue;

	public FixedTypeValue_FieldSpecification(final Identifier identifier, final ASN1Type fixedType, final boolean isUnique,
			final boolean isOptional, final boolean hasDefault, final IValue defaultValue) {
		super(identifier, isOptional);
		this.fixedType = fixedType;
		this.isUnique = isUnique;
		this.hasDefault = hasDefault;
		this.defaultValue = defaultValue;

		if (null != fixedType) {
			fixedType.setOwnertype(TypeOwner_type.OT_FT_V_FLD, this);
			fixedType.setFullNameParent(this);
		}
		if (null != defaultValue) {
			defaultValue.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Fieldspecification_types getFieldSpecificationType() {
		return Fieldspecification_types.FS_V_FT;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyObjectClass(final ObjectClass_Definition objectClass) {
		super.setMyObjectClass(objectClass);
		if (null != fixedType) {
			fixedType.setMyScope(myObjectClass.getMyScope());
		}
		if (null != defaultValue) {
			defaultValue.setMyScope(myObjectClass.getMyScope());
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasDefault() {
		return hasDefault;
	}

	@Override
	public ISetting getDefault() {
		return defaultValue;
	}

	public IASN1Type getType() {
		return fixedType;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (null != fixedType) {
			fixedType.setGenName(myObjectClass.getGenNameOwn(), identifier.getName());
			fixedType.check(timestamp);

			if (null != defaultValue) {
				if (isOptional) {
					getLocation().reportSemanticError("OPTIONAL and DEFAULT are mutual exclusive");
					isOptional = false;
				} else if (isUnique) {
					getLocation().reportSemanticError("UNIQUE and DEFAULT are mutual exclusive");
				}

				defaultValue.setMyGovernor(fixedType);
				final IValue tempValue = fixedType.checkThisValueRef(timestamp, defaultValue);
				fixedType.checkThisValue(timestamp, tempValue, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT,
						false, false, true, false, false));

				defaultValue.setGenNamePrefix("const_");
				defaultValue.setGenNameRecursive(fixedType.getGenNameOwn() + "_defval_");
				defaultValue.setCodeSection(CodeSectionType.CS_PRE_INIT);
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		if (null != fixedType) {
			fixedType.addDeclaration(declarationCollector, i);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		if (null != fixedType) {
			fixedType.addProposal(propCollector, i);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}
		if (fixedType != null && !fixedType.accept(v)) {
			return false;
		}
		if (defaultValue != null && !defaultValue.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData) {
		if (fixedType == null) {
			return;
		}

		final String typeGenName = fixedType.getGenNameOwn();
		final StringBuilder sb = aData.getCodeForType(typeGenName);
		final StringBuilder typeSource = new StringBuilder();
		fixedType.generateCode( aData, typeSource );
		sb.append(typeSource);

		if (defaultValue != null) {
			final StringBuilder valueSource = new StringBuilder();
			final String defValueGenName = defaultValue.getGenNameOwn();
			final String typeGeneratedName = fixedType.getGenNameValue( aData, valueSource );

			if (defaultValue.canGenerateSingleExpression() ) {
				if (defaultValue.returnsNative()) {
					valueSource.append(MessageFormat.format("\tpublic static final {0} {1} = new {0}({2});\n", typeGeneratedName, defValueGenName, defaultValue.generateSingleExpression(aData)));
				} else {
					valueSource.append(MessageFormat.format("\tpublic static final {0} {1} = {2};\n", typeGeneratedName, defValueGenName, defaultValue.generateSingleExpression(aData)));
				}
			} else {
				valueSource.append(MessageFormat.format("\tstatic final {0} {1} = new {0}();\n", typeGeneratedName, defValueGenName));
				getLocation().update_location_object(aData, aData.getPreInit());

				final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				final IValue last = defaultValue.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), referenceChain);
				referenceChain.release();
				last.generateCodeInit( aData, aData.getPreInit(), defValueGenName );
			}

			aData.addGlobalVariable(typeGeneratedName, valueSource.toString());
		}
	}
}
