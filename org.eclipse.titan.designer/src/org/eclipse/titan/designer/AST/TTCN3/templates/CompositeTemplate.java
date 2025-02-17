/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public abstract class CompositeTemplate extends TTCN3Template {
	private static final String FULLNAMEPART = ".list_item(";

	protected final ListOfTemplates templates;

	public CompositeTemplate(final ListOfTemplates templates) {
		this.templates = templates;

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			templates.getTemplateByIndex(i).setFullNameParent(this);
		}
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 *
	 * @return the template on the indexed position.
	 * */
	public TTCN3Template getTemplateByIndex(final int index) {
		return templates.getTemplateByIndex(index);
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		templates.setMyScope(scope);
	}


	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			templates.getTemplateByIndex(i).setCodeSection(codeSection);
		}
		if (lengthRestriction != null) {
			lengthRestriction.setCodeSection(codeSection);
		}
	}

	/** @return the number of templates in the list */
	public int getNofTemplates() {
		return templates.getNofTemplates();
	}

	/**
	 * Calculates the number of list members which are not the any or none
	 * symbol.
	 *
	 * @return the number calculated.
	 * */
	public int getNofTemplatesNotAnyornone(final CompilationTimeStamp timestamp) {
		int result = 0;
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final ITTCN3Template template = templates.getTemplateByIndex(i);

			switch (template.getTemplatetype()) {
			case ANY_OR_OMIT:
				break;
			case PERMUTATION_MATCH:
				result += ((PermutationMatch_Template) template).getNofTemplatesNotAnyornone(timestamp);
				break;
			case ALL_FROM:
				result += ((All_From_Template) template).getNofTemplatesNotAnyornone(timestamp);
				break;
			default:
				result++;
				break;
			}
		}

		return result;
	}

	/**
	 * Checks if the list of templates has at least one any or none symbol.
	 *
	 * @return true if an any or none symbol was found, false otherwise.
	 * */
	public boolean templateContainsAnyornone() {
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final ITTCN3Template template = templates.getTemplateByIndex(i);
			switch (template.getTemplatetype()) {
			case ANY_OR_OMIT:
				return true;
			case PERMUTATION_MATCH:
				if (((PermutationMatch_Template) template).templateContainsAnyornone()) {
					return true;
				}
				break;
			default:
				break;
			}
		}

		return false;
	}

	/**
	 * Checks if the list of templates has at least one any or none or permutation symbol
	 * <p> It is prohibited after "all from"
	 *
	 * @return true if an any or none symbol was found, false otherwise.
	 * */
	public boolean containsAnyornoneOrPermutation(final CompilationTimeStamp timestamp) {
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final ITTCN3Template template = templates.getTemplateByIndex(i);
			switch (template.getTemplatetype()) {
			case ANY_OR_OMIT:
			case PERMUTATION_MATCH:
				return true;
			case ALL_FROM:
				return ((All_From_Template)template).containsAnyornoneOrPermutation(timestamp);
			default:
				break;
			}
		}

		return false;
	}


	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (templates.getTemplateByIndex(i) == child) {
				return builder.append(FULLNAMEPART).append(String.valueOf(i)).append(INamedNode.RIGHTPARENTHESES);
			}
		}

		return builder;
	}

	protected abstract String getNameForStringRep();

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getNameForStringRep()).append("( ");
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			if (i > 0) {
				builder.append(", ");
			}

			final ITTCN3Template template = templates.getTemplateByIndex(i);
			builder.append(template.createStringRepresentation());
		}
		builder.append(" )");

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this)) {
			for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
				final ITTCN3Template template = templates.getTemplateByIndex(i);
				if (template != null) {
					referenceChain.markState();
					template.checkRecursions(timestamp, referenceChain);
					referenceChain.previousState();
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (lengthRestriction != null) {
			lengthRestriction.updateSyntax(reparser, false);
			reparser.updateLocation(lengthRestriction.getLocation());
		}

		if (baseTemplate instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) baseTemplate).updateSyntax(reparser, false);
			reparser.updateLocation(baseTemplate.getLocation());
		} else if (baseTemplate != null) {
			throw new ReParseException();
		}

		templates.updateSyntax(reparser, false);
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (templates == null) {
			return;
		}

		templates.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (templates != null && !templates.accept(v)) {
			return false;
		}
		return true;
	}
}
