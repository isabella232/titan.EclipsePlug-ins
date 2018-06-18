/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an actual parameter that has a Template as its actual value.
 *
 * @author Kristof Szabados
 * */
public final class Template_ActualParameter extends ActualParameter {

	private final TemplateInstance template;
	private TemplateRestriction.Restriction_type genRestrictionCheck = Restriction_type.TR_NONE;

	public Template_ActualParameter(final TemplateInstance template) {
		this.template = template;
	}

	public TemplateInstance getTemplateInstance() {
		return template;
	}

	//FIXME needs to be called from the right places for the code generation to work
	public void setGenRestrictionCheck(final TemplateRestriction.Restriction_type tr) {
		genRestrictionCheck = tr;
	}

	public TemplateRestriction.Restriction_type getGenRestrictionCheck() {
		return genRestrictionCheck;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (template != null) {
			template.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		if(genRestrictionCheck != Restriction_type.TR_NONE) {
			// TODO needs t check post restriction check generation
			return true;
		}
		if (template != null) {
			return template.hasSingleExpression();
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (template != null) {
			template.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (template == null) {
			return;
		}

		final Reference derivedReference = template.getDerivedReference();
		if (derivedReference != null) {
			final ISubReference subReference = derivedReference.getSubreferences().get(0);
			if (subReference instanceof ParameterisedSubReference) {
				final ActualParameterList parameterList = ((ParameterisedSubReference) subReference).getActualParameters();
				if (parameterList != null) {
					parameterList.checkRecursions(timestamp, referenceChain);
				}
			}
		}

		referenceChain.markState();
		template.getTemplateBody().checkRecursions(timestamp, referenceChain);
		referenceChain.previousState();
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (template != null) {
			template.updateSyntax(reparser, false);
			reparser.updateLocation(template.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (template != null) {
			if (!template.accept(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeDefaultValue(final JavaGenData aData, final StringBuilder source) {
		if (template == null) {
			return;
		}

		final TTCN3Template temp = template.getTemplateBody();
		final Reference baseReference = template.getDerivedReference();
		if (baseReference != null) {
			final ExpressionStruct expression = new ExpressionStruct();
			expression.expression.append(MessageFormat.format("{0}.assign(", temp.get_lhs_name()));
			baseReference.generateCode(aData, expression);
			expression.expression.append(')');
		}
		//FIXME handle the needs conversion case
//		temp.generateCodeInit(aData, source, temp.get_lhs_name());
		//FIXME generate restriction check code if needed
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression) {
		//TODO not complete implementation pl. copye_needed missing
		if (template != null ) {
			final StringBuilder expressionExpression = new StringBuilder();
			final ExpressionStruct tempExpression = new ExpressionStruct();
			template.generateCode(aData, tempExpression, genRestrictionCheck);
			if(tempExpression.preamble.length() > 0) {
				expression.preamble.append(tempExpression.preamble);
			}
			if(tempExpression.postamble.length() == 0) {
				expressionExpression.append(tempExpression.expression);
			} else {
				// make sure the postambles of the parameters are executed before the
				// function call itself (needed if the template contains function calls
				// with lazy or fuzzy parameters)
				final String tempId = aData.getTemporaryVariableName();
				template.getTemplateBody().getMyGovernor().getGenNameTemplate(aData, expression.preamble, myScope);
				expression.preamble.append(" ");
				expression.preamble.append(tempId);
				expression.preamble.append("(");
				expression.preamble.append(tempExpression.expression);
				expression.preamble.append(")");

				expression.preamble.append(tempExpression.postamble);
				expressionExpression.append(tempId);
			}

			//TODO copy might be needed here
			expression.expression.append(expressionExpression);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (template != null) {
			template.reArrangeInitCode(aData, source, usageModule);
		}
	}
}
