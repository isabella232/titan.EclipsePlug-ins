/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignments;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;
import org.eclipse.titan.designer.AST.TTCN3.definitions.*;

/**
 * This class marks the following code smell:
 * The code contains duplicate/similar names, which is not recommended.
 *
 * @author Gaurav Dhongde
*/
public class DuplicateName extends BaseModuleCodeSmellSpotter {
	public static final String SIMILARDEFINITIONREPEATED = "Multiple definitions with similar names `{0}'', `{1}'' were found";
	protected final CompilationTimeStamp timestamp;

	public DuplicateName() {
		super(CodeSmellType.DUPLICATE_NAME);
		timestamp = CompilationTimeStamp.getBaseTimestamp();
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (!(node instanceof TTCN3Module)) {
			return;
		}

//		final List<String> names = new ArrayList<String>();
		final HashMap<String, Assignment> names = new HashMap<String, Assignment>();
		final Assignments assignments = ((TTCN3Module) node).getAssignments();

		for (int i = 0; i < assignments.getNofAssignments(); i++) {
			final Assignment assignment = assignments.getAssignmentByIndex(i);
			final String sUp = assignment.getIdentifier().getDisplayName().toUpperCase();
			if (names.containsKey(sUp)) {
				problems.report(assignment.getIdentifier().getLocation(), MessageFormat.format(SIMILARDEFINITIONREPEATED, names.get(sUp).getIdentifier().getDisplayName(), assignment.getIdentifier().getDisplayName()));
			} else {
				names.put(sUp, assignment);
			}

			if (i == assignments.getNofAssignments() - 1) {
				names.clear();
			}
		}

	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(TTCN3Module.class);

		return ret;
	}

}