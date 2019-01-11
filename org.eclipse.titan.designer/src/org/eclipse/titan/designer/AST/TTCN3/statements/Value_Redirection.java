/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.Verdict_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the value redirection of several operations.
 * TODO list of operations.
 *
 * @author Kristof Szabados
 * */
public class Value_Redirection extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	private ArrayList<Single_ValueRedirection> valueRedirections;

	// pointer to the type of the redirected value, not owned here
	private IType valueType = null;

	/**
	 * Indicates whether the value redirect is restricted to only one value of
	 * type 'verdicttype' */
	private boolean verdictOnly = false;

	private Location location = NULL_Location.INSTANCE;

	/** the time when this was checked the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	public Value_Redirection() {
		valueRedirections = new ArrayList<Single_ValueRedirection>();
	}

	public void add(final Single_ValueRedirection single_ValueRedirect){
		if (single_ValueRedirect != null) {
			single_ValueRedirect.setFullNameParent(this);

			valueRedirections.add(single_ValueRedirect);
		}
	}

	/**
	 * @return {@code true} if at least one of the value redirects has the
	 * '@decoded' modifier
	 */
	public boolean has_decoded_modifier() {
		for (Single_ValueRedirection redirect : valueRedirections) {
			if (redirect.isDecoded()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Attempts to identify the type of the redirected value. Only those
	 * single redirects are checked, which redirect the whole value, not
	 * just a field. If multiple whole-value-redirects of separate types are
	 * found, then an error is displayed.
	 *
	 * @return the found type, if any.
	 */
	public IType getType(final CompilationTimeStamp timestamp) {
		IType returnValue = null;
		for (int i = 0; i < valueRedirections.size(); i++) {
			final Single_ValueRedirection redirect = valueRedirections.get(i);

			if (redirect.getSubreferences() == null) {
				IType variableType = redirect.getVariableReference().checkVariableReference(timestamp);
				if (variableType != null) {
					if (returnValue == null) {
						returnValue = variableType;
					} else {
						if (!returnValue.isIdentical(timestamp, variableType)) {
							getLocation().reportSemanticError("The variable references the whole value is redirected to should be of the same type");

							return null;
						}
					}
				}
			}
		}

		return valueType;
	}

	@Override
	/** {@inheritDoc} */
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public final Location getLocation() {
		return location;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < valueRedirections.size(); i++) {
			final Single_ValueRedirection redirect = valueRedirections.get(i);

			if (redirect == child) {
				return builder.append(".redirect_").append(i + 1);
			}
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (Single_ValueRedirection redirect : valueRedirections) {
			redirect.setMyScope(scope);
		}
	}

	/**
	 * Sets the code_section attribute for the statements in this parameter assignment to the provided value.
	 *
	 * @param codeSection the code section where these statements should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		for (Single_ValueRedirection redirect : valueRedirections) {
			redirect.getVariableReference().setCodeSection(codeSection);;
		}
	}

	//FIXME comment
	public void check(final CompilationTimeStamp timestamp, final IType type) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		//FIXME implement

		lastTimeChecked = timestamp;
	}

	//FIXME comment
	public void checkErroneous(final CompilationTimeStamp timestamp) {
		for (int i = 0; i < valueRedirections.size(); i++) {
			Single_ValueRedirection redirect = valueRedirections.get(i);

			redirect.getVariableReference().checkVariableReference(timestamp);
			final Value stringEncoding = redirect.getStringEncoding();
			if (stringEncoding != null) {
				stringEncoding.checkStringEncoding(timestamp, null);
			}
		}
	}

	//FIXME comment
	public void checkVerdictOnly(final CompilationTimeStamp timestamp) {
		verdictOnly = true;
		check(timestamp, new Verdict_Type());
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for(Single_ValueRedirection redirect : valueRedirections) {
			if (redirect != null) {
				redirect.updateSyntax(reparser, false);
				reparser.updateLocation(redirect.getLocation());
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		//FIXME implement
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}

		if (valueRedirections != null) {
			for (final Single_ValueRedirection redirect : valueRedirections) {
				if (!redirect.accept(v)) {
					return false;
				}
			}
		}

		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}

		return true;
	}

	public void generateCode( final JavaGenData aData, final ExpressionStruct expression, final TemplateInstance matchedTi ) {
		if (verdictOnly) {
			//verdict only case
			if (valueRedirections.size() == 1) {
				valueRedirections.get(0).getVariableReference().generateCode(aData, expression);
			}
		} else {
			//TODO maybe the compiler can also benefit from this optimization
			if (valueRedirections.size() == 1 && valueRedirections.get(0).getSubreferences() == null) {
				valueRedirections.get(0).getVariableReference().generateCode(aData, expression);

				return;
			}

			//FIXME implement fully
			expression.expression.append("//FIXME for the time being not yet supported\n");
		}
	}
}
