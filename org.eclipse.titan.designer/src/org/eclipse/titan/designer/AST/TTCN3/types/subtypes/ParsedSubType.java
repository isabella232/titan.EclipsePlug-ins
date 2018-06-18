/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types.subtypes;

import org.eclipse.titan.designer.AST.IIdentifierContainer;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;

/**
 * Represents a sub-type restriction as it was parsed.
 *
 * @author Adam Delic
 * */
public abstract class ParsedSubType implements IIncrementallyUpdateable, IIdentifierContainer, IVisitableNode {
	public enum ParsedSubType_type {
		SINGLE_PARSEDSUBTYPE, RANGE_PARSEDSUBTYPE, LENGTH_PARSEDSUBTYPE, PATTERN_PARSEDSUBTYPE
	}

	public abstract ParsedSubType_type getSubTypetype();

	public abstract Location getLocation();
}
