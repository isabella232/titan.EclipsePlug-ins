/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The SetState_Statement class represents a TTCN3 setstate statement.
 *
 * @author Kristof Szabados
 * */
public class SetState_Statement extends Statement {
	private static final String OPERANDERROR1 = "The first operand of the `setstate' operation should be an integer value";

	private static final String FULLNAMEPART1 = ".value";
	private static final String FULLNAMEPART2 = ".template_instance";
	private static final String STATEMENT_NAME = "setstate";

	private final Value value;
	private final TemplateInstance templateInstance;

	public SetState_Statement(final Value value, final TemplateInstance templateInstance) {
		this.value = value;
		this.templateInstance = templateInstance;

		if (value != null) {
			value.setFullNameParent(this);
		}
		if (templateInstance != null) {
			templateInstance.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_SETSTATE;
	}

	@Override
	/** {@inheritDoc} */
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (value == child) {
			return builder.append(FULLNAMEPART1);
		} else if (templateInstance == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (value != null) {
			value.setMyScope(scope);
		}
		if (templateInstance != null) {
			templateInstance.setMyScope(scope);
		}
	}

	@Override
	public void check(CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (value != null) {
			value.setLoweridToReference(timestamp);
			final Type_type temporalType = value.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

			switch (temporalType) {
			case TYPE_INTEGER:
				final IValue last = value.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
				if (!value.isUnfoldable(timestamp) && Value_type.INTEGER_VALUE.equals(last.getValuetype())) {
					boolean error = false;
					final Integer_Value intValue = (Integer_Value) last;
					if (intValue.isNative()) {
						if (intValue.getValue() < 0 || intValue.getValue() > 4) {
							error = true;
						}
					} else {
						error = true;
					}

					if (error) {
						value.getLocation().reportSemanticError("The value of the first parameter must be 0, 1, 2, 3 or 4.");
					}
				}
				break;
			case TYPE_UNDEFINED:
				setIsErroneous();
				break;
			default:
				if (!isErroneous) {
					location.reportSemanticError(OPERANDERROR1);
				}
				break;
			}
		}

		if (templateInstance != null) {
			IType governor = templateInstance.getExpressionGovernor(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
			if (governor == null) {
				templateInstance.getLocation().reportSemanticError("Cannot determine the type of the parameter.");
			} else {
				templateInstance.check(timestamp, governor);
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (value != null) {
			value.updateSyntax(reparser, false);
			reparser.updateLocation(value.getLocation());
		}

		if (templateInstance != null) {
			templateInstance.updateSyntax(reparser, false);
			reparser.updateLocation(templateInstance.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
		}
		if (templateInstance != null) {
			templateInstance.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(ASTVisitor v) {
		if (value != null && !value.accept(v)) {
			return false;
		}
		if (templateInstance != null && !templateInstance.accept(v)) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		//FIXME implement
		source.append( "\t\t" );
		source.append( "//TODO: " );
		source.append( getClass().getSimpleName() );
		source.append( ".generateCode() is not implemented!\n" );
	}
}
