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
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Class to represent value FieldSettings.
 *
 * @author Kristof Szabados
 */
public final class FieldSetting_Value extends FieldSetting {

	private final IValue setting;

	public FieldSetting_Value(final Identifier name, final IValue setting ) {
		super(name);
		this.setting = setting;
	}

	@Override
	/** {@inheritDoc} */
	public FieldSetting newInstance() {
		return new FieldSetting_Value(name.newInstance(), setting);
	}

	@Override
	/** {@inheritDoc} */
	public IValue getSetting() {
		return setting;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final FieldSpecification fieldSpecification) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (!(fieldSpecification instanceof FixedTypeValue_FieldSpecification)) {
			getLocation().reportSemanticError("Value setting was expected");
			//FIXME set erroneous
			return;
		}

		final FixedTypeValue_FieldSpecification fs = (FixedTypeValue_FieldSpecification)fieldSpecification;
		final IType type = fs.getType();
		setting.setMyGovernor(type);
		final IValue tempValue = type.checkThisValueRef(timestamp, setting);
		type.checkThisValue(timestamp, tempValue, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, false, true, true,
				false));

		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		setting.checkRecursions(timestamp, chain);
		chain.release();

		setting.setGenNameRecursive(setting.getGenNameOwn());
		setting.setCodeSection(CodeSectionType.CS_PRE_INIT);
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		//Do nothing while values are missing
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		//Do nothing while values are missing
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (name != null && !name.accept(v)) {
			return false;
		}
		if (setting != null && !setting.accept(v)) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData) {
		final String genName = setting.get_lhs_name();
		final IType type = setting.getMyGovernor();

		final StringBuilder sb = aData.getSrc();
		final StringBuilder source = new StringBuilder();
		final String typeGeneratedName = type.getGenNameValue( aData, source );

		source.append(MessageFormat.format("\tpublic static final {0} {1}  = new {0}();\n", typeGeneratedName, genName));
		setting.generateCodeInit( aData, aData.getPreInit(), genName );
		sb.append(source);
	}
}
