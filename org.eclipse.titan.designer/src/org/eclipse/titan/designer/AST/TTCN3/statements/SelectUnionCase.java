/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.Anytype_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
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
 * @author Farkas Izabella Ingrid
 */
public final class SelectUnionCase extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {

	//Error/warning messages
	private static final String NEVER_REACH = "Control never reaches this code because of previous effective cases(s)";
	private static final String INVALID_UNION_FIELD = "Union `{0}'' has no field `{1}''";
	private static final String INVALID_ANYTYPE_FIELD = "Anytype `{0}'' has no field `{1}''";
	private static final String CASE_ALREADY_COVERED = "Case `{0}'' is already covered";

	private static final String FULLNAMEPART = ".block";

	/**
	 * Header part of a select select union case, which is a list of union fields (Identifier) or types (Type).
	 * A syntactically and semantically correct select case header contains only Identifier or only Type objects.
	 */
	private final List<Identifier> mItems;

	private final StatementBlock mStatementBlock;

	private Location location = NULL_Location.INSTANCE;

	public SelectUnionCase( final List<Identifier> aItems, final StatementBlock aStatementBlock ) {
		this.mItems = aItems;
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
		return mItems == null;
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
		if ( mItems != null ) {
			check( aUnionType, aFieldNames );
		} else {
			// case else
			unreachable2 = true;
			aFieldNames.clear();
		}

		mStatementBlock.check( aTimestamp );

		return unreachable2;
	}

	/**
	 * Does the semantic checking of this select case's statement block.
	 *
	 * Please note, that this function is only to be used when the select statement expression was found erroneous.
	 * As it bypasses the checking of the case fields, in other cases this could cause problems.
	 */
	void checkStatementBlock( final CompilationTimeStamp aTimestamp) {
		if( mStatementBlock !=null ) {
			mStatementBlock.check( aTimestamp );
		}
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
			final List<String> aTypesCovered ) {
		if ( aUnreachable ) {
			location.reportConfigurableSemanticProblem(
					Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
							PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING, null), NEVER_REACH);
		}

		boolean unreachable2 = aUnreachable;
		if ( mItems != null ) {
			check( aAnytypeType, aTypesCovered );
		} else {
			// case else
			unreachable2 = true;
		}

		mStatementBlock.check( aTimestamp );

		return unreachable2;
	}

	/**
	 * Check header of union type for invalid or duplicate fields
	 * @param aUnionType
	 *                the referenced union type of the select expression, to check the cases against.
	 *                It can not be null.
	 * @param aFieldNames
	 *                union field names, which are not covered yet.
	 *                If a field name is found, it is removed from the list.
	 *                If case else is found, all the filed names are removed from the list, because all the cases are covered.
	 */
	public void check( final TTCN3_Choice_Type aUnionType, final List<String> aFieldNames ) {
		for ( Identifier identifier : mItems ) {
			// name of the union component
			final String name = identifier.getName();
			if ( aUnionType.hasComponentWithName( name ) ) {
				if ( aFieldNames.contains( name ) ) {
					aFieldNames.remove( name );
				} else {
					//this case is already covered
					identifier.getLocation().reportSemanticWarning( MessageFormat.format( CASE_ALREADY_COVERED, identifier.getTtcnName() ) );
				}
			} else {
				identifier.getLocation().reportSemanticError( MessageFormat.format( INVALID_UNION_FIELD, aUnionType.getFullName(), identifier.getTtcnName() ) );
			}
		}
	}

	/**
	 * Check header of union type for invalid or duplicate fields
	 * @param aAnytypeType
	 *                the referenced union type of the select expression, to check the cases against.
	 *                It can not be null.
	 * @param aTypesCovered
	 *                types, which are already covered.
	 *                If a new type is found, it is added to the list.
	 */
	public void check( final Anytype_Type aAnytypeType, final List<String> aTypesCovered ) {
		final int size = mItems.size();
		for ( int i = 0; i < size; i++ ) {
			final Identifier identifier = mItems.get( i );
			final String name = identifier.getName();
			if ( aAnytypeType.hasComponentWithName( name ) ) {
				if ( aTypesCovered.contains( name ) ) {
					aTypesCovered.remove( name );
				} else {
					//this case is already covered
					identifier.getLocation().reportSemanticWarning( MessageFormat.format( CASE_ALREADY_COVERED, identifier.getTtcnName() ) );
				}
			} else {
				identifier.getLocation().reportSemanticError( MessageFormat.format( INVALID_ANYTYPE_FIELD, aAnytypeType.getFullName(), identifier.getTtcnName() ) );
			}
		}
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

		if ( mItems != null ) {
			for ( Identifier item : mItems ) {
				if ( item instanceof Identifier ) {
					final Identifier identifier = item;
					reparser.updateLocation( identifier.getLocation() );
				} else {
					//program error, this should not happen
					ErrorReporter.INTERNAL_ERROR("Invalid type in select union case");
				}
			}
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
		if ( mItems != null) {
			for ( Identifier item : mItems ) {
				if ( !item.accept( v ) ) {
					return false;
				}
			}
		}
		if (mStatementBlock != null && !mStatementBlock.accept(v)) {
			return false;
		}
		return true;
	}

	public void generateCode(final JavaGenData aData, final StringBuilder source, final AtomicBoolean unreach) {
		if (mItems != null) {
			for (int i = 0; i < mItems.size(); i++) {
				final Identifier identifier = mItems.get(i);
				final String name = identifier.getName();
				source.append(MessageFormat.format("case ALT_{0}:\n", FieldSubReference.getJavaGetterName(name)));
			}
		} else {
			unreach.set(true);
			source.append("default:\n");
		}

		mStatementBlock.generateCode(aData, source);
		source.append("break;\n");
	}
}
