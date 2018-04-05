/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ASN1.ASN1Object;
import org.eclipse.titan.designer.AST.ASN1.ObjectClass;
import org.eclipse.titan.designer.AST.ASN1.ObjectSet;
import org.eclipse.titan.designer.AST.ASN1.ObjectSetElement_Visitor;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * ObjectSetElement Visitor, object collector.
 *
 * @author Kristof Szabados
 */
public final class ObjectSetElementVisitor_objectCollector extends ObjectSetElement_Visitor {
	private static final String OBJECTOFCLASSEXPECTED = "Objects of objectclass `{0}'' are expected; `{1}'' is object of class `{2}''";

	private final ObjectClass_Definition governor;
	private final Set<Object> visitedElements = new HashSet<Object>();
	private ASN1Objects objects;
	private final CompilationTimeStamp timestamp;

	public ObjectSetElementVisitor_objectCollector(final ObjectSet parent, final CompilationTimeStamp timestamp) {
		super(parent.getLocation());
		governor = parent.getMyGovernor().getRefdLast(timestamp, null);
		objects = new ASN1Objects();
		this.timestamp = timestamp;
	}

	public ObjectSetElementVisitor_objectCollector(final Location location, final ObjectClass governor, final CompilationTimeStamp timestamp) {
		super(location);
		this.governor = governor.getRefdLast(timestamp, null);
		objects = new ASN1Objects();
		this.timestamp = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void visitObject(final ASN1Object p) {
		final Object_Definition object = p.getRefdLast(timestamp, null);
		if (object.getIsErroneous(timestamp)) {
			return;
		}
		if (visitedElements.contains(object)) {
			return;
		}
		final ObjectClass myClass = governor.getRefdLast(timestamp, null);
		final ObjectClass refdClass = object.getMyGovernor().getRefdLast(timestamp, null);

		if (myClass != refdClass) {
			if( location != NULL_Location.INSTANCE && refdClass!=null) {
				location.reportSemanticError(MessageFormat.format(OBJECTOFCLASSEXPECTED, myClass.getFullName(), p.getFullName(), refdClass.getFullName()));
			}
			return;
		}
		visitedElements.add(object);
		objects.addObject(object);
	}

	@Override
	/** {@inheritDoc} */
	public void visitObjectSetReferenced(final Referenced_ObjectSet p) {
		visitObjectSet(p, false);
	}

	public void visitObjectSet(final ObjectSet p, final boolean force) {

		if(governor == null || p == null) {
			return;
		}

		final ObjectClass myClass = governor.getRefdLast(timestamp, null);
		ObjectClass refdClass = null;
		if (p instanceof Referenced_ObjectSet){
			refdClass = ((Referenced_ObjectSet)p).getRefdObjectClass(timestamp);
		}

		if(myClass != refdClass &&  myClass!=null && refdClass!=null) {
			p.getLocation().reportSemanticError(
					MessageFormat.format(OBJECTOFCLASSEXPECTED, myClass.getFullName(), p.getFullName(), refdClass.getFullName()));
			return;
		}

		final ObjectSet_definition os = p.getRefdLast(timestamp, null);
		if (visitedElements.contains(os)) {
			if (!force) {
				return;
			}
		} else {
			visitedElements.add(os);
		}

		//=== Visit objects =====

		//In case of Parameterised_Reference, the ObjectSet contains the parameters
		// therefore its objects have different type
		// E.g: Ericsson-MAP-ReturnError-v2 ::= ReturnError{{Errors {{Supported-Ericsson-MAP-Operations-v2}}}}
		//                                                   ^Par ref ^^^^^^ObjectSet
		if(((Referenced_ObjectSet)p).isReferencedParameterisedReference()) {
			return;
		}
		//In case of Defined_Reference, the ObjectSet contains...
		//TODO: check this!
		if(((Referenced_ObjectSet)p).isReferencedDefinedReference()){
			return;
		}

		final ASN1Objects otherObjects = os.getObjs();

		otherObjects.trimToSize();
		for (int i = 0; i < otherObjects.getNofObjects(); i++) {
			visitObject(otherObjects.getObjectByIndex(i));
		}
	}

	public ASN1Objects getObjects() {
		return objects;
	}

	public ASN1Objects giveObjects() {
		final ASN1Objects temp = objects;
		objects = null;
		return temp;
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		// TODO
		return true;
	}
}
