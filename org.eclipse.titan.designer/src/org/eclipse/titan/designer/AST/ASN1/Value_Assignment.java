/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.TypeOwner_type;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.graphics.ImageCache;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * a value assignment.
 *
 * @author Kristof Szabados
 * */
public final class Value_Assignment extends ASN1Assignment {
	private static final String UNKNOWNASSIGNMENT = "unknown value assignment";

	/** left. */
	private final IASN1Type type;
	/** right. */
	private final Value value;

	public Value_Assignment(final Identifier id, final Ass_pard assPard, final IASN1Type type, final Value value) {
		super(id, assPard);
		this.type = type;
		this.value = value;

		if (null != type) {
			type.setOwnertype(TypeOwner_type.OT_VAR_ASS, this);
			type.setFullNameParent(this);
		}
		if (null != value) {
			value.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_CONST;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(".<type>");
		} else if (value == child) {
			return builder;
		}

		return builder;
	}

	@Override
	protected ASN1Assignment internalNewInstance(final Identifier identifier) {
		// FIXME ASN.1 values can be cloned
		return new Value_Assignment(identifier, null, type.newInstance(), value);
	}

	@Override
	/** {@inheritDoc} */
	public void setRightScope(final Scope rightScope) {
		if (null != value) {
			value.setMyScope(rightScope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (null != type) {
			type.setMyScope(scope);
		}
		if (null != value) {
			value.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public IType getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return type;
	}

	public IValue getValue() {
		if (null != assPard) {
			location.reportSemanticError(MessageFormat.format("`{0}'' is a parameterized value assignment", getFullName()));
			isErroneous = true;
			return null;
		}

		if (null == lastTimeChecked) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		return value;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		check(timestamp, null);
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
			return;
		}

		checkTTCNIdentifier();
		if (null == type) {
			return;
		}

		type.setGenName("_T_", getGenName());
		type.check(timestamp);

		if (null == value) {
			return;
		}

		value.setMyGovernor(type);
		final IValue tempValue = type.checkThisValueRef(timestamp, value);
		type.checkThisValue(timestamp, tempValue, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_CONSTANT, false, false, true, true,
				false));

		final IReferenceChain chain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		tempValue.checkRecursions(timestamp, chain);
		chain.release();

		value.setGenNameRecursive(getGenName());
		value.setCodeSection(CodeSectionType.CS_PRE_INIT);
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int index) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() >= index + 1 && identifier.getName().equals(subrefs.get(index).getId().getName())) {
			if (subrefs.size() > index + 1 && null != type) {
				type.addDeclaration(declarationCollector, index + 1);
			} else if (subrefs.size() == index + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(index).getReferenceType())) {
				declarationCollector.addDeclaration(identifier.getDisplayName(), identifier.getLocation(), this);
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

		if (subrefs.size() == index + 1 && identifier.getName().toLowerCase(Locale.ENGLISH).startsWith(subrefs.get(index).getId().getName().toLowerCase(Locale.ENGLISH))) {
			final String proposalKind = UNKNOWNASSIGNMENT;
			propCollector.addProposal(identifier, " - " + proposalKind, ImageCache.getImage(getOutlineIcon()), proposalKind);
		} else if (subrefs.size() > index + 1 && null != type && identifier.getName().equals(subrefs.get(index).getId().getName())) {
			// perfect match
			type.addProposal(propCollector, index + 1);
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getAssignmentName() {
		return "value";
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "constant.gif";
	}

	// TODO: remove when location is fixed
	@Override
	/** {@inheritDoc} */
	public Location getLikelyLocation() {
		if (value != null) {
			return Location.interval(super.getLikelyLocation(), value.getLocation());
		}

		return super.getLikelyLocation();
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
		if (value != null) {
			value.findReferences(referenceFinder, foundIdentifiers);
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
		if (value != null && !value.accept(v)) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final boolean cleanUp ) {
		if (null != assPard || dontGenerate) {
			// don't generate code for assignments that still have a parameter at this point.
			return;
		}

		if (value == null) {
			return;
		}

		final String genName = getGenName();

		//value.setGenNamePrefix("const_");//currently does not need the prefix
		value.setGenNameRecursive(genName);

		final StringBuilder sb = aData.getSrc();
		final StringBuilder source = new StringBuilder();
		final String typeGeneratedName = type.getGenNameValue( aData, source );
		if (value.canGenerateSingleExpression() ) {
			if (value.returnsNative()) {
				source.append(MessageFormat.format("\tpublic static final {0} {1} = new {0}({2});\n", typeGeneratedName, genName, value.generateSingleExpression(aData)));
			} else {
				source.append(MessageFormat.format("\tpublic static final {0} {1} = {2};\n", typeGeneratedName, genName, value.generateSingleExpression(aData)));
			}
		} else {
			source.append(MessageFormat.format("\tpublic static final {0} {1} = new {0}();\n", typeGeneratedName, genName));
			value.generateCodeInit( aData, aData.getPreInit(), genName );
		}
		sb.append(source);
	}
}
