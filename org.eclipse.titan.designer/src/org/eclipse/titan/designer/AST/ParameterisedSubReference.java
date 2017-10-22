/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.List;

import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.templates.ParsedActualParameters;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The ParameterisedSubReference class represents a part of a TTCN3 or ASN.1
 * reference, which was given in a parameterized notation ('name(value1,
 * value2)').
 *
 * @author Kristof Szabados
 * */
public final class ParameterisedSubReference extends ASTNode implements ISubReference, ILocateableNode {
	public static final String INVALIDSUBREFERENCE = "The type `{0}'' cannot be parameterised.";
	public static final String INVALIDVALUESUBREFERENCE = "Invalid reference: internal parameterisation is not supported";

	private final Identifier identifier;
	private final ParsedActualParameters parsedParameters;
	private ActualParameterList actualParameters;

	private Location location;

	public ParameterisedSubReference(final Identifier identifier, final ParsedActualParameters parsedParameters) {
		this.identifier = identifier;
		this.parsedParameters = parsedParameters;

		if (parsedParameters != null) {
			parsedParameters.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Subreference_type getReferenceType() {
		return Subreference_type.parameterisedSubReference;
	}

	@Override
	/** {@inheritDoc} */
	public Identifier getId() {
		return identifier;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (parsedParameters != null) {
			parsedParameters.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (parsedParameters != null) {
			parsedParameters.setCodeSection(codeSection);
		}
		if (actualParameters != null) {
			actualParameters.setCodeSection(codeSection);
		}
	}

	public ParsedActualParameters getParsedParameters() {
		return parsedParameters;
	}

	public ActualParameterList getActualParameters() {
		return actualParameters;
	}

	@Override
	/** {@inheritDoc} */
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public Location getLocation() {
		return location;
	}

	public boolean checkParameters(final CompilationTimeStamp timestamp, final FormalParameterList formalParameterList) {
		actualParameters = new ActualParameterList();
		final boolean isErroneous = formalParameterList.checkActualParameterList(timestamp, parsedParameters, actualParameters);
		actualParameters.setFullNameParent(this);
		actualParameters.setMyScope(myScope);

		return isErroneous;
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return "parameterisedSubReference: " + identifier.getDisplayName();
	}

	@Override
	/** {@inheritDoc} */
	public void appendDisplayName(final StringBuilder builder) {
		if (builder.length() > 0) {
			builder.append('.');
		}
		builder.append(identifier.getDisplayName()).append("()");
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
		if (parsedParameters != null) {
			parsedParameters.updateSyntax(reparser, false);
			reparser.updateLocation(parsedParameters.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (parsedParameters != null) {
			parsedParameters.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (identifier != null && !identifier.accept(v)) {
			return false;
		}

		if (parsedParameters != null && !parsedParameters.accept(v)) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		if (actualParameters == null) {
			return true;
		}

		for (int i = 0; i < actualParameters.getNofParameters(); i++) {
			final ActualParameter actualParameter = actualParameters.getParameter(i);
			if(!actualParameter.hasSingleExpression()) {
				return false;
			}
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression, final boolean isFirst) {
		expression.expression.append( identifier.getName() );
		expression.expression.append( "(" );
		actualParameters.generateCodeAlias( aData, expression );
		expression.expression.append( ")" );
	}
}
