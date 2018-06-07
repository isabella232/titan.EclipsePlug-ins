/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.AbstractOfType;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class TooComplexOfType extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Too complex of type expression. Please consider to create a separate type.";
	
	private final CompilationTimeStamp timestamp;

	public TooComplexOfType() {
		super(CodeSmellType.TOO_COMPLEX_OF_TYPE);
		timestamp = CompilationTimeStamp.getBaseTimestamp();
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof Def_Type) {
			final Def_Type dt = (Def_Type) node; 
			if(dt.getType(timestamp) instanceof AbstractOfType) {
				final AbstractOfType aot = (AbstractOfType) dt.getType(timestamp);			
				if(aot.getOfType().getSubtype() != null) {
					problems.report(dt.getLocation(), ERROR_MESSAGE);
				}else if(aot.getOfType() instanceof AbstractOfType) {
					problems.report(dt.getLocation(), ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Def_Type.class);
		return ret;
	}
}
