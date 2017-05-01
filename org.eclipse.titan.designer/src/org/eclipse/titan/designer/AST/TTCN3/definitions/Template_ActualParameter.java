/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
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

	public Template_ActualParameter(final TemplateInstance template) {
		this.template = template;
	}

	public TemplateInstance getTemplateInstance() {
		return template;
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
	public void generateJava( final JavaGenData aData, final ExpressionStruct expression) {
		//TODO not complete implementation pl. copye_needed missing
		if (template != null ) {
			StringBuilder expressionExpression = new StringBuilder();
			ExpressionStruct tempExpression = new ExpressionStruct();
			template.generateJava(aData, tempExpression);
			if(tempExpression.preamble.length() > 0) {
				expression.preamble.append(tempExpression.preamble);
			}
			if(tempExpression.postamble.length() == 0) {
				expressionExpression.append(tempExpression.expression);
			} else {
				// make sure the postambles of the parameters are executed before the
				// function call itself (needed if the template contains function calls
				// with lazy or fuzzy parameters)
				String tempId = aData.getTemporaryVariableName();
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
}
