/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.types.Anytype_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Header part of a select union case, which is a list of
 * <ul>
 * <li>identifier, if the select union expression is a union
 * <li>type, if the select union expression is anytype
 * </ul>
 * 
 * @author Arpad Lovassy
 */
public class SelectUnionCaseHeader implements IIncrementallyUpdateable, IVisitableNode, ILocateableNode {

	//Error/warning messages
	private static final String INVALID_UNION_FIELD = "Union `{0}'' has no field `{1}''";
	private static final String NOT_A_UNION_FIELD = "There is no union field `{1}'' in union `{0}''";
	private static final String NOT_A_UNION_FIELD_2 = "Not a union field in union `{0}''";
	private static final String NOT_TYPE = "Not a type in union `{0}''";
	private static final String CASE_ALREADY_COVERED = "Case `{0}'' is already covered";

	/**
	 * union fields (Identifier) or types (Type).
	 * A syntactically and semantically correct select case header contains only Identifier or only Type objects.
	 */
	private List<IVisitableNode> mItems;

	/**
	 * location of the header in the source code
	 */
	private Location mLocation;

	public SelectUnionCaseHeader() {
		mItems = new ArrayList<IVisitableNode>();
	}

	/**
	 * Adds a union field
	 * @param aUnionField
	 */
	public void add( final Identifier aUnionField ) {
		mItems.add( aUnionField );
	}

	/**
	 * Adds a type
	 * @param aType a type
	 */
	public void add( final Type aType ) {
		mItems.add( aType );
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax( final TTCN3ReparseUpdater reparser, final boolean isDamaged )  throws ReParseException {
		for ( IVisitableNode item : mItems ) {
			if ( item instanceof Identifier ) {
				//nothing to do
			} else if ( item instanceof Type ) {
				final Type type = (Type)item;
				type.updateSyntax(reparser, isDamaged);
			} else {
				//program error, this should not happen
				ErrorReporter.INTERNAL_ERROR("Invalid select union case header type");
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean accept( final ASTVisitor v ) {
		for ( IVisitableNode item : mItems ) {
			if ( !item.accept( v ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public Location getLocation() {
		return mLocation;
	}

	@Override
	/** {@inheritDoc} */
	public void setLocation( final Location aLocation ) {
		mLocation = aLocation;
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
		for ( IVisitableNode item : mItems ) {
			if ( item instanceof Identifier ) {
				final Identifier identifier = (Identifier)item;
				// name of the union component
				final String name = identifier.getName();
				if ( aUnionType.hasComponentWithName( name ) ) {
					if ( aFieldNames.contains( name ) ) {
						aFieldNames.remove( name );
					} else {
						//this case is already covered
						mLocation.reportSemanticWarning( MessageFormat.format( CASE_ALREADY_COVERED, name ) );
					}
				} else {
					mLocation.reportSemanticError( MessageFormat.format( INVALID_UNION_FIELD, aUnionType.getFullName(), name ) );
				}
			} else 	if ( item instanceof Type ) {
				mLocation.reportSemanticError( MessageFormat.format(NOT_A_UNION_FIELD, aUnionType.getFullName(), ((Type)item ).getTypename() ) );
			} else {
				mLocation.reportSemanticError( MessageFormat.format( NOT_A_UNION_FIELD_2, aUnionType.getFullName() ) );
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
	public void check( final Anytype_Type aAnytypeType, final List<Type> aTypesCovered ) {
		final int size = mItems.size();
		for ( int i = 0; i < size; i++ ) {
			final IVisitableNode item = mItems.get( i );
			if ( item instanceof Type ) {
				final Type type = (Type)item;
				checkType( type, aTypesCovered );
			} else 	if ( item instanceof Identifier ) {
				//This case means, that a type is parsed as an identifier, because we don't know at parse time, if a
				//name in a select union case header is type or identifier. In this case we just transform the identifier to a type
				final Identifier identifier = (Identifier)item;

				//creating new Type from Identifier
				Reference reference = new Reference( null );
				FieldSubReference subReference = new FieldSubReference( identifier );
				subReference.setLocation( identifier.getLocation() );
				reference.addSubReference(subReference);
				final Type type = new Referenced_Type(reference);

				//replace old Identifier to the new Type in the list
				mItems.set( i, type );

				checkType( type, aTypesCovered );
			} else {
				mLocation.reportSemanticError( MessageFormat.format( NOT_TYPE, aAnytypeType.getFullName() ) );
			}
		}
	}

	/**
	 * Checks just one type from the select case header
	 * @param aType the type to check
	 * @param aTypesCovered
	 *                types, which are already covered.
	 *                If a new type is found, it is added to the list.
	 */
	private void checkType( final Type aType, final List<Type> aTypesCovered ) {
		if ( aTypesCovered.contains( aType ) ) {
			//this case is already covered
			mLocation.reportSemanticWarning( MessageFormat.format( CASE_ALREADY_COVERED, aType ) );
		} else {
			aTypesCovered.add( aType );
		}
	}
}
