/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ImportModule;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

public class UnusedImport extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Possibly unused importation";

	public UnusedImport() {
		super(CodeSmellType.UNUSED_IMPORT);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof ImportModule) {
			final ImportModule s = (ImportModule) node;
			if (!s.getUsedForImportation()) {
				final TTCN3Module module = s.getMyModule();
				if (module.getLastCompilationTimeStamp() != null && !module.getLastCompilationTimeStamp().isLess(module.getLastImportationCheckTimeStamp())) {
					problems.report(s.getIdentifier().getLocation(), ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(ImportModule.class);
		return ret;
	}
}
