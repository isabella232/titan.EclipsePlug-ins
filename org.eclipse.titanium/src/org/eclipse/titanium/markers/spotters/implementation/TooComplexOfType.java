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

import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titan.designer.AST.TTCN3.types.AbstractOfType;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
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
		if (!(node instanceof Def_Type)) {
			return;
		}
		
		final Def_Type dt = (Def_Type) node; 
		
		
		if(dt.getType(timestamp) instanceof AbstractOfType) {
			final AbstractOfType aot = (AbstractOfType) dt.getType(timestamp);
			final IType oft = aot.getOfType();
			final SubType subt = oft.getSubtype();
			int a = subt.get_length_restriction();
						
						
			if(oft instanceof AbstractOfType) {
				problems.report(dt.getLocation(), ERROR_MESSAGE);
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
