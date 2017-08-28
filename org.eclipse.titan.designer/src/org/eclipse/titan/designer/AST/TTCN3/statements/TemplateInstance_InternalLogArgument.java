/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
/**
 * @author Kristof Szabados
 * */
public final class TemplateInstance_InternalLogArgument extends InternalLogArgument {
	private final TemplateInstance templateInstance;

	public TemplateInstance_InternalLogArgument(final TemplateInstance templateInstance) {
		super(ArgumentType.TemplateInstance);
		this.templateInstance = templateInstance;
	}

	public TemplateInstance getTemplate() {
		return templateInstance;
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (templateInstance != null) {
			templateInstance.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (templateInstance == null) {
			return;
		}

		templateInstance.checkRecursions(timestamp, referenceChain);
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression ) {
		//FIXME somewhat more complicated
		if (templateInstance != null) {
			templateInstance.generateCode(aData, expression, Restriction_type.TR_NONE);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeLog( final JavaGenData aData, final ExpressionStruct expression ) {
		//FIXME somewhat more complicated
		if (templateInstance != null) {
			templateInstance.generateCode(aData, expression, Restriction_type.TR_NONE);
			//TODO this wil be the final generated code end
			expression.expression.append(".log()");
		}
	}
}
