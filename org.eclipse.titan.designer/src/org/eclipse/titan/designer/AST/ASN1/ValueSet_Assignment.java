/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.TypeOwner_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class ValueSet_Assignment extends ASN1Assignment {
	private static final String UNKNOWNASSIGNMENT = "unknown value assignment";

	/** left. */
	private final IASN1Type type;
	/** right */
	private final Block mBlock;

	public ValueSet_Assignment(final Identifier id, final Ass_pard assPard, final IASN1Type type, final Block aBlock) {
		super(id, assPard);
		this.type = type;
		this.mBlock = aBlock;

		if (null != type) {
			type.setOwnertype(TypeOwner_type.OT_VSET_ASS, this);
			type.setFullNameParent(this);
		}

		if (null != aBlock) {
			aBlock.setFullNameParent(this);
		}
	}

	@Override
	protected ASN1Assignment internalNewInstance(final Identifier identifier) {
		return new ValueSet_Assignment(identifier, null, type.newInstance(), mBlock);
	}

	@Override
	/** {@inheritDoc} */
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_VS;
	}

	@Override
	/** {@inheritDoc} */
	public void setRightScope(final Scope rightScope) {
		//Do nothing

	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != type) {
			type.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (null != lastTimeChecked && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (null != assPard) {
			assPard.check(timestamp);
			// lastTimeChecked = timestamp;
			return;
		}

		//FIXME implement parsing of the constraint.
		type.setGenName(getGenName());
		type.check(timestamp);

		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		type.checkRecursions(timestamp, chain);
		chain.release();
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int index) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > index && identifier.getName().equals(subrefs.get(index).getId().getName())) {
			if (subrefs.size() > index + 1 && null != type) {
				type.addDeclaration(declarationCollector, index + 1);
			} else if (subrefs.size() == index + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(index).getReferenceType())) {
				declarationCollector.addDeclaration(this);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int index) {
		final List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= index) {
			return;
		}

		if (subrefs.size() == index + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(index).getId().getName().toLowerCase())) {
			propCollector.addProposal(identifier, " - " + UNKNOWNASSIGNMENT, ImageCache.getImage(getOutlineIcon()), UNKNOWNASSIGNMENT);
		} else if (subrefs.size() > index + 1 && null != type && identifier.getName().equals(subrefs.get(index).getId().getName())) {
			// perfect match
			type.addProposal(propCollector, index + 1);
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getAssignmentName() {
		return "value set";
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "titan.gif";
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}

		if (assPard != null) {
			// if parameterised the rest was not checked.
			return true;
		}

		if (type != null && !type.accept(v)) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final boolean cleanUp) {
		if (null != assPard || dontGenerate) {
			// don't generate code for assignments that still have a parameter at this point.
			return;
		}

		if (type == null) {
			return;
		}

		final String genName = getGenName();
		final StringBuilder sb = aData.getCodeForType(genName);//aData.getSrc();
		final StringBuilder source = new StringBuilder();

		type.generateCode(aData, source);
		sb.append(source);
	}
}
