/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * class to represent an actual parameter.
 *
 * @author Kristof Szabados
 * */
public abstract class ActualParameter extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private boolean isErroneous = false;

	/**
	 * The location of the whole actual parameter. This location encloses
	 * the parameter fully, as it is used to report errors to.
	 **/
	private Location location = NULL_Location.INSTANCE;

	public final void setIsErroneous() {
		isErroneous = true;
	}

	public final boolean getIsErroneous() {
		return isErroneous;
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

	/**
	 * originally has_single_expr
	 * */
	public abstract boolean hasSingleExpression();

	/**
	 * Sets the code_section attribute of this actual parameter to the provided value.
	 *
	 * @param codeSection the code section where this actual parameter (it's default value) should be generated.
	 * */
	public abstract void setCodeSection(final CodeSectionType codeSection);

	/**
	 * Checks for circular references within the actual parameter.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references,
	 *                must not be null.
	 **/
	public abstract void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain);

	/**
	 * Handles the incremental parsing of this actual parameter.
	 *
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public abstract void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException;

	/**
	 * Generates the value assignments of the default value of the parameter.
	 * */
	public void generateCodeDefaultValue(final JavaGenData aData, final StringBuilder source) {
		//Do nothing by default
	}

	/**
	 * Add generated java code on this level.
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression used for code generation
	 */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression) {
		//default implementation
		expression.expression.append( "\t\t" );
		expression.expression.append( "//TODO: " );
		expression.expression.append( getClass().getSimpleName() );
		expression.expression.append( ".generateCode() is not implemented!\n" );
	}

	/**
	 * Appends the initialization sequence of all (directly or indirectly)
	 * referred non-parameterized templates and the default values of all
	 * parameterized templates to source and returns the resulting string.
	 * Only objects belonging to module usageModule are initialized.
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source where the could should be added
	 * @param usageModule where the parameter is to be used
	 * */
	public abstract void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule);
}
