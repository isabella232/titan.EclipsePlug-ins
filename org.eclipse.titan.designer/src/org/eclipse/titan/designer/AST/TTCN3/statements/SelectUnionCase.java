/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.Anytype_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * Helper class for the SelectUnionCase_Statement class.
 * Represents a select union case branch parsed from the source code.
 * 
 * @see SelectUnionCase_Statement
 * @see SelectUnionCases
 * 
 * @author Arpad Lovassy
 */
public final class SelectUnionCase extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String NEVER_REACH = "Control never reaches this code because of previous effective cases(s)";

	private static final String FULLNAMEPART = ".block";

	/** Header part of a select select union case, which is a list of union fields or types */
	private final SelectUnionCaseHeader mHeader;
	private final StatementBlock mStatementBlock;

	private Location location = NULL_Location.INSTANCE;

	public SelectUnionCase(final SelectUnionCaseHeader aHeader, final StatementBlock aStatementBlock) {
		this.mHeader = aHeader;
		this.mStatementBlock = aStatementBlock;

		if (mStatementBlock != null) {
			mStatementBlock.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (mStatementBlock == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	public StatementBlock getStatementBlock() {
		return mStatementBlock;
	}

	/**
	 * Sets the scope of the select case branch.
	 * 
	 * @param scope
	 *                the scope to be set.
	 */
	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		if (mStatementBlock != null) {
			mStatementBlock.setMyScope(scope);
		}
	}

	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		if (mStatementBlock != null) {
			mStatementBlock.setMyStatementBlock(statementBlock, index);
		}
	}

	public void setMyDefinition(final Definition definition) {
		if (mStatementBlock != null) {
			mStatementBlock.setMyDefinition(definition);
		}
	}

	public void setMyAltguards(final AltGuards altGuards) {
		if (mStatementBlock != null) {
			mStatementBlock.setMyAltguards(altGuards);
		}
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

	/** @return true if the select case is the else case, false otherwise. */
	public boolean hasElse() {
		return mHeader == null;
	}

	/**
	 * Checks whether the select case has a return statement, either
	 * directly or embedded.
	 * 
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * 
	 * @return the return status of the select case.
	 * */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (mStatementBlock != null) {
			return mStatementBlock.hasReturn(timestamp);
		}

		return StatementBlock.ReturnStatus_type.RS_NO;
	}

	/**
	 * Does the semantic checking of this select case of union type.
	 * 
	 * @param aTimestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param aUnionType
	 *                the referenced union type of the select expression, to check the cases against.
	 *                It can not be null.
	 * @param aUnreachable
	 *                tells if this case branch is still reachable or not.
	 * @param aFieldNames
	 *                union field names, which are not covered yet.
	 *                If a field name is found, it is removed from the list.
	 *                If case else is found, all the filed names are removed from the list, because all the cases are covered.
	 * 
	 * @return true if this case branch was found to be unreachable, false
	 *         otherwise.
	 */
	public boolean check( final CompilationTimeStamp aTimestamp, final TTCN3_Choice_Type aUnionType, final boolean aUnreachable,
						  final List<String> aFieldNames ) {
		if ( aUnreachable ) {
			location.reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING, null), NEVER_REACH);
		}

		boolean unreachable2 = aUnreachable;
		if ( mHeader != null ) {
			mHeader.check( aUnionType, aFieldNames );
		} else {
			// case else
			unreachable2 = true;
			aFieldNames.clear();
		}

		mStatementBlock.check( aTimestamp );

		return unreachable2;
	}

	/**
	 * Does the semantic checking of this select case of anytype type.
	 * 
	 * @param aTimestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param aAnytypeType
	 *                the referenced anytype type of the select expression, to check the cases against.
	 *                It can not be null.
	 * @param aUnreachable
	 *                tells if this case branch is still reachable or not.
	 * 
	 * @return true if this case branch was found to be unreachable, false
	 *         otherwise.
	 */
	public boolean check( final CompilationTimeStamp aTimestamp, final Anytype_Type aAnytypeType, final boolean aUnreachable,
						  final List<Type> aTypesCovered ) {
		if ( aUnreachable ) {
			location.reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING, null), NEVER_REACH);
		}

		boolean unreachable2 = aUnreachable;
		if ( mHeader != null ) {
			mHeader.check( aAnytypeType, aTypesCovered );
		} else {
			// case else
			unreachable2 = true;
		}

		mStatementBlock.check( aTimestamp );

		return unreachable2;
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		if (mStatementBlock != null) {
			mStatementBlock.checkAllowedInterleave();
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public void postCheck() {
		if (mStatementBlock != null) {
			mStatementBlock.postCheck();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if ( mHeader != null ) {
			mHeader.updateSyntax( reparser, false );
			reparser.updateLocation( mHeader.getLocation() );
		}

		if (mStatementBlock != null) {
			mStatementBlock.updateSyntax(reparser, false);
			reparser.updateLocation(mStatementBlock.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (mStatementBlock != null) {
			mStatementBlock.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if ( mHeader != null && !mHeader.accept( v ) ) {
			return false;
		}
		if (mStatementBlock != null && !mStatementBlock.accept(v)) {
			return false;
		}
		return true;
	}
}
