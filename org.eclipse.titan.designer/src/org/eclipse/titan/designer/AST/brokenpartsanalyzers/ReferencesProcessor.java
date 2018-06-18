/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.brokenpartsanalyzers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Peter Olah
 */
public abstract class ReferencesProcessor extends AssignmentHandler {

	public ReferencesProcessor(final Assignment assignment) {
		super(assignment);
	}

	public Set<String> computeReferences(final Set<Reference> references) {
		final Set<String> result = new HashSet<String>();
		for (final Reference reference : references) {
			final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false,
					null);

			if (assignment == null || assignment.getIdentifier() == null) {
				// if semantic error occurs, assignment will be null
				// have to get reference id
				if (reference.getId() != null) {
					result.add(reference.getId().getDisplayName());
				}
			} else {
				// the name of the assignment might differ from the reference in
				// incremental parsing mode
				result.add(assignment.getIdentifier().getDisplayName());
			}
		}
		return result;
	}

	public boolean containsErroneousReference(final Set<Reference> references) {
		for (Reference reference : references) {
			if(reference.getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
				return true;
			}

			final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false, null);
			if(assignment == null) {
				return true;
			}
		}

		return false;
	}
}
