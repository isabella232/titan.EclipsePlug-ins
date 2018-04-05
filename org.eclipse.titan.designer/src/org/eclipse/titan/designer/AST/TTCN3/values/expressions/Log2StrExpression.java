/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values.expressions;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.InternalLogArgument;
import org.eclipse.titan.designer.AST.TTCN3.statements.LogArguments;
import org.eclipse.titan.designer.AST.TTCN3.statements.Match_InternalLogArgument;
import org.eclipse.titan.designer.AST.TTCN3.statements.Reference_InternalLogArgument;
import org.eclipse.titan.designer.AST.TTCN3.statements.TemplateInstance_InternalLogArgument;
import org.eclipse.titan.designer.AST.TTCN3.statements.Value_InternalLogArgument;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Delic Adam
 * */
public final class Log2StrExpression extends Expression_Value {
	private static final String FULLNAMEPART = ".logarguments";

	private final LogArguments logArguments;

	public Log2StrExpression(final LogArguments logArguments) {
		this.logArguments = logArguments;
		if (logArguments != null) {
			logArguments.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Operation_type getOperationType() {
		return Operation_type.LOG2STR_OPERATION;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReference(final CompilationTimeStamp timestamp, final Assignment lhs) {
		for(int i = 0; i < logArguments.getNofLogArguments(); i++) {
			final InternalLogArgument logArgument = logArguments.getLogArgumentByIndex(i).getRealArgument();

			if (logArgument == null) {
				continue;
			}
			switch(logArgument.getArgumentType()) {
			case Macro:
			case String:
				//self reference not possible
				break;
			case Value:
				if (((Value_InternalLogArgument)logArgument).getValue().checkExpressionSelfReferenceValue(timestamp, lhs)) {
					return true;
				}
				break;
			case Match:
				if (((Match_InternalLogArgument)logArgument).getMatchExpression().checkExpressionSelfReferenceValue(timestamp, lhs)) {
					return true;
				}
				break;
			case Reference:{
				final Reference reference = ((Reference_InternalLogArgument) logArgument).getReference();
				if (lhs == reference.getRefdAssignment(timestamp, false)) {
					return true;
				}
				break;
			}
			case TemplateInstance: {
				final TTCN3Template template = ((TemplateInstance_InternalLogArgument) logArgument).getTemplate().getTemplateBody();
				if (template.checkExpressionSelfReferenceTemplate(timestamp, lhs)) {
					return true;
				}
				break;
			}
			}
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		return "log2str(...)";
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		if (logArguments != null) {
			logArguments.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);

		if (logArguments != null) {
			logArguments.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);
		if (logArguments == child) {
			return builder.append(FULLNAMEPART);
		}
		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_CHARSTRING;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		// for the common use cases this cannot be determined since the
		// logging functions
		// are located in TITAN's code generator and runtime
		// TODO handle the cases when it can be done. like all
		// parameters being constant strings.
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public IValue evaluateValue(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return this;
		}
		if (logArguments != null) {
			logArguments.check(timestamp);
		}
		lastTimeChecked = timestamp;
		return this;
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			if (logArguments != null) {
				referenceChain.markState();
				logArguments.checkRecursions(timestamp, referenceChain);
				referenceChain.previousState();
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (logArguments != null) {
			logArguments.updateSyntax(reparser, false);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (logArguments == null) {
			return;
		}

		logArguments.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (logArguments != null && !logArguments.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpressionExpression(final JavaGenData aData, final ExpressionStruct expression) {
		if ( logArguments == null ) {
			return;
		}

		aData.addBuiltinTypeImport("TitanCharString");

		expression.expression.append("new TitanCharString(");
		logArguments.generateCodeExpression(aData, expression);
		expression.expression.append(')');
	}


}
