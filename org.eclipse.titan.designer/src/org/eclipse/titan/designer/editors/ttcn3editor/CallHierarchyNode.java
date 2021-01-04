/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.ttcn3editor;

import java.util.HashMap;

import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.editors.ttcn3editor.actions.CallHierarchyAction;

/**
 * <p>
 * <code>CallHierarchyNode</code> is a representation of a CallHierarchy graph node.<br>
 * The graph algorithms are implemented here: {@link CallHierarchy}
 * </p>
 * 
 * @see CallHierarchy
 * @author Sandor Balazs
 */
public class CallHierarchyNode {
	/**
	 * The node's definition's module.
	 */
	private Module nodeModule;

	/**
	 * The Definition what the node Represent.
	 */
	private Definition nodeDefinition;

	/**
	 * The node's children. Indexed by own Definitions.
	 */
	private HashMap<Definition, CallHierarchyNode> nodeChildren;

	/**
	 * The node's references.
	 */
	private HashMap<Reference, Module> nodeReferences;

	/**
	 * Constructor for empty node. The constructor is: {@link #CallHierarchyNode(Module, Definition)}
	 */
	public CallHierarchyNode() {
		this(null, null);
	}

	/**
	 * <p>
	 * Constructor for creating new Nodes. The constructor create a node from a Definition and the definition's module.<br>
	 * The graph algorithms are implemented here: {@link CallHierarchy}
	 * </p>
	 * @param nodeModule
	 * 			The node's definition's module.
	 * @param nodeDefinition
	 * 			The Definition what the node Represent.
	 * @see CallHierarchy
	 */
	public CallHierarchyNode(final Module nodeModule, final Definition nodeDefinition) {
		this.nodeModule 		= nodeModule;
		this.nodeDefinition 	= nodeDefinition;
		this.nodeChildren 		= new HashMap<Definition, CallHierarchyNode>();
		this.nodeReferences 	= new HashMap<Reference, Module>();
	}

	/**
	 * Return theDefinition's name what the node Represent.
	 * @see CallHierarchyLabelProvider
	 * @return
	 * 			The Definition's name what the node Represent.
	 */
	public String getName() {
		return nodeDefinition.getFullName();
	}

	/**
	 * Return the Definition what the node Represent.<br>
	 * @see CallHierarchyLabelProvider
	 * @return
	 * 			The Definition what the node Represent.
	 */
	public Definition getNodeDefinition() {
		return nodeDefinition;
	}

	/**
	 * Return the node's definition's module's name.
	 * @return
	 * 			The node's definition's module's name.
	 */
	public String getModuleName() {
		return nodeModule.getFullName();
	}

	/**
	 * Return the node's definition's module.
	 * @return
	 * 			The node's definition's module.
	 */
	public Module getNodeModule() {
		return nodeModule;
	}

	/**
	 * Return array of the node's References as Object.<br>
	 * Use for the reference list visualization.
	 * @see CallHierarchyView
	 * @return
	 * 			Array of the node's References as Object.
	 */
	public Object[] getReferences() {
		return nodeReferences.keySet().toArray();
	}

	/**
	 * Return references number.<br>
	 * @return
	 * 			Return references number.
	 */
	public int getReferencesNumber() {
		return nodeReferences.size();
	}

	/**
	 * Return array of the node's children as Object.<br>
	 * Use for the call hierarchy tree visualization.
	 *
	 * @see CallHierarchyContentProvider
	 * @return
	 * 			Array of the node's children as Object.
	 */
	public Object[] getChildren() {
		return nodeChildren.values().toArray();
	}

	/**
	 * Check the node children.
	 * @return
	 * 			True when the node has children.
	 */
	public boolean hasChildren() {
		return nodeChildren.size() > 0;
	}

	/**
	 * Add child for the node from a {@link CallHierarchyNode}.
	 *
	 * @see CallHierarchyAction#processing()
	 * @param node
	 * 			The new child as a {@link CallHierarchyNode}.
	 */
	public void addChild(final CallHierarchyNode node) {
		this.nodeChildren.put(node.getNodeDefinition(), node);
	}

	/**
	 * Add child for the node from a <code>Reference</code> and a <code>Module</code>.<br>
	 * When the reference's parent function is an exist child, the method add the reference to the child.<br>
	 * When the parent is new, the method create a new child and add the reference to it.<br>
	 *
	 * @see CallHierarchy#functionCallFinder(org.eclipse.jface.viewers.ISelection)
	 * @see CallHierarchy#functionCallFinder(CallHierarchyNode)
	 * @param referenceModule
	 * 			The Reference's module.
	 * @param reference
	 * 			The new child's Reference.
	 */
	public void addChild(final Module referenceModule, final Reference reference) {
		final Definition parentDefinition = getReferenceParent(reference);
		if(parentDefinition == null) {
			return;
		}

		final CallHierarchyNode node = addNode(referenceModule, parentDefinition);
		node.addReference(referenceModule, reference);
	}

	/**
	 * Add a new reference to the node.
	 *
	 * @param referenceModule
	 * 			The reference contain module.
	 * @param reference
	 * 			The new reference.
	 * @see #addChild(Module, Reference)
	 */
	public void addReference(final Module referenceModule, final Reference reference) {
		nodeReferences.put(reference, referenceModule);
	}

	/**
	 * Create a new {@link CallHierarchyNode} and add to this node as a child.<br>
	 * When the node already exist and registered as a child the method return the exiting child node.
	 *
	 * @param definitionModule
	 * 			The new node's definition's module.
	 * 			
	 * @param definition
	 * 			The Definition what the new node Represent.
	 * @return
	 * 			The created new node. (When the node already exist and registered as a child the method return the exiting child node.)<br>
	 * @see #addChild(Module, Reference)
	 */
	private CallHierarchyNode addNode(final Module definitionModule, final Definition definition) {
		if(nodeChildren.containsKey(definition)) {
			return nodeChildren.get(definition);
		} else {
			final CallHierarchyNode newNode = new CallHierarchyNode(definitionModule, definition);
			nodeChildren.put(definition, newNode);
			return newNode;
		}
	}

	/**
	 * Search and return a reference's parent. The return definition contain the reference.
	 *
	 * @param reference
	 * 			The reference which the method searching the parent.
	 * @return
	 * 			The reference's parent Definition.
	 * 			<b>Possible return NULL!</b>
	 */
	private Definition getReferenceParent(final Reference reference) {
		INamedNode referenceParentNode = reference.getNameParent().getNameParent().getNameParent().getNameParent();
		while( !(referenceParentNode instanceof Definition) && (referenceParentNode != null) ) {
			referenceParentNode = referenceParentNode.getNameParent();
		}
		if(!(referenceParentNode instanceof Definition)) {
			return null;
		}

		return (Definition) referenceParentNode;
	}

	/**
	 * Clear the node's child's and references.
	 */
	public void clearNode() {
		this.nodeChildren.clear();
		this.nodeReferences.clear();
	}
}