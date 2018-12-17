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
import java.util.List;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.VisibilityModifier;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * This class marks the following code smell:
 * This code smell report a problem, when someone try access to
 * a module private variable from an another module.
 *
 * @author Sandor Balazs
 */
public class PrivateComponentVariableAccess extends BaseModuleCodeSmellSpotter {
	/**
	 * The error message contain the private components name and the module name too.
	 */
	private static final String ERROR_MESSAGE = "The \"{0}\" is a Private component in the \"{1}\" module. Access is not recommended from \"{2}\" module.";
	
	/**
	 * The constructor based on the superclass constructor.
	 * @see org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter
	 */
	public PrivateComponentVariableAccess() {
		super(CodeSmellType.PRIVATE_COMPONENT_VARIABLE_ACCESS);
	}

	 /**
	 * Internal processing the node.
	 * <p>
	 * When the referred object is a definition and the defined variable is private
	 * and the reference's module isn't equals with the definiton's module then the 
	 * code smell report the problem.
	 * Attention! The <code>definition</code> variable can be NULL, because the 
	 * <code>getRefdAssignment</code>'s behavior. In the code it was handled.
	 * </p>
	 *
	 * @see org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter
	 *
	 * @param node
	 *            The node to process.
	 * @param problems
	 *            The handler class where problems should be reported.
	 */
	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		
		if (!(node instanceof Reference)) {
			return;
		}
		final Reference reference = (Reference) node;
		
		if(reference.getIsErroneous((reference.getLastTimeChecked()))){
			return;
		}
		final Assignment referedAssignment = reference.getRefdAssignment(reference.getLastTimeChecked(), false);
		
		if(referedAssignment == null) {
			return;
		}
		if(!(referedAssignment instanceof Definition)) {
			return;
		}
		final Definition definition = (Definition) referedAssignment;
		
		if(definition.getVisibilityModifier().equals(VisibilityModifier.Private)) {
			if(!reference.getMyScope().getModuleScope().equals(definition.getMyScope().getModuleScope())) {
				problems.report(reference.getLocation(), MessageFormat.format(ERROR_MESSAGE, reference.getDisplayName(), 
					definition.getMyScope().getModuleScope().getName(), reference.getMyScope().getModuleScope().getName()));
			}
		}
	}
	
	/**
	 * The spotter was registered for the references in the visitor,
	 * and the process method run on all of references.
	 * The method was inherited from the BaseModuleCodeSmellSpotter.
	 * 
	 * @see org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter
	 * 
	 * @return The type of node on which the spotter will work.
	 */
	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Reference.class);
		return ret;
	}
}