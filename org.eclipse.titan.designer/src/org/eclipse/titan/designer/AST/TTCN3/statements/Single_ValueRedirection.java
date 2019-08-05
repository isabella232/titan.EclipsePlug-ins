/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Class for storing a single element of a value redirect
 * Each of these elements can be:
 * - a lone variable reference (in this case the whole value is redirected to
 *   the referenced variable), or
 * - the assignment of a field or a field's sub-reference to a variable
 *   (in this case one of the value's fields, or a sub-reference of one of the
 *   fields is redirected to the referenced variable; this can only happen if
 *   the value is a record or set).
 *
 * @author Kristof Szabados
 */
public class Single_ValueRedirection extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	/** reference to the variable the value is redirected to */
	final private Reference variableReference;

	/**
	 * indicates which part (record field or array element) of the value is
	 * redirected (optional)
	 */
	final private ArrayList<ISubReference> subreferences;

	/**
	 * indicates whether the redirected field or element should be decoded
	 * (only used if subrefs is not null)
	 */
	final private boolean decoded;

	/**
	 * encoding format for decoded universal charstring value redirects
	 * (only used if subrefs is not null and decoded is true)
	 */
	final private Value encodingString;

	/**
	 * pointer to the type the redirected field or element is decoded into
	 * (only used if subrefs is not null and decoded is true), not owned
	 */
	private IType declarationType;

	private Location location = NULL_Location.INSTANCE;

	public Single_ValueRedirection(final Reference variableReference) {
		this.variableReference = variableReference;
		subreferences = null;
		decoded = false;
		encodingString = null;
		declarationType = null;

		if (variableReference != null) {
			variableReference.setFullNameParent(this);
		}
	}

	public Single_ValueRedirection(final Reference variableReference, final ArrayList<ISubReference> subreferences, final boolean decoded, final Value encodingString) {
		this.variableReference = variableReference;
		this.subreferences = subreferences;
		this.decoded = decoded;
		this.encodingString = encodingString;
		declarationType = null;

		if (variableReference != null) {
			variableReference.setFullNameParent(this);
		}
		if (subreferences != null) {
			for(final ISubReference subreference: subreferences) {
				subreference.setFullNameParent(this);
			}
		}
		if (encodingString != null) {
			encodingString.setFullNameParent(this);
		}
	}

	public Reference getVariableReference() {
		return variableReference;
	}

	public ArrayList<ISubReference> getSubreferences() {
		return subreferences;
	}

	public boolean isDecoded() {
		return decoded;
	}

	public Value getStringEncoding() {
		return encodingString;
	}

	public void setDeclarationType(final IType declarationType) {
		this.declarationType = declarationType;
	}

	public IType getDeclarationType() {
		return declarationType;
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

		if (variableReference == child) {
			return builder.append(".varref");
		} else if (encodingString == child) {
			return builder.append(".<string_encoding>");
		} else if (subreferences != null) {
			for(int i = 0; i < subreferences.size(); i++) {
				final ISubReference subReference = subreferences.get(i);

				if (subReference == child) {
					return builder.append(".fieldrefs.").append(i + 1);
				}
			}
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		variableReference.setMyScope(scope);
		if (subreferences != null) {
			for(final ISubReference subreference: subreferences) {
				subreference.setMyScope(scope);
			}
		}
		if (encodingString != null) {
			encodingString.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (variableReference != null) {
			variableReference.updateSyntax(reparser, false);
			reparser.updateLocation(variableReference.getLocation());
		}
		if (subreferences != null) {
			for(final ISubReference subreference: subreferences) {
				if (subreference != null) {
					subreference.updateSyntax(reparser, false);
					reparser.updateLocation(subreference.getLocation());
				}
			}
		}
		if (encodingString != null) {
			encodingString.updateSyntax(reparser, false);
			reparser.updateLocation(encodingString.getLocation());
		}
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

		if (variableReference != null && !variableReference.accept(v)) {
			return false;
		}
		if (subreferences != null) {
			for(final ISubReference subreference: subreferences) {
				if (subreference != null && !subreference.accept(v)) {
					return false;
				}
			}
		}
		if (encodingString != null && !encodingString.accept(v)) {
			return false;
		}

		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}

		return true;
	}
}
