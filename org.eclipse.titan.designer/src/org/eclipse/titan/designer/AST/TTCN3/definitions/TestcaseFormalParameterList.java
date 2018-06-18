/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.util.List;

import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.SkeletonTemplateProposal;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;

/**
 * @author Kristof Szabados
 * */
public final class TestcaseFormalParameterList extends FormalParameterList {

	public TestcaseFormalParameterList(final List<FormalParameter> parameters) {
		super(parameters);
	}

	@Override
	/** {@inheritDoc} */
	public void addSkeletonProposal(final ProposalCollector propCollector) {
		for (final SkeletonTemplateProposal templateProposal : TTCN3CodeSkeletons.FORMAL_VALUE_PARAMETER_PROPOSALS) {
			propCollector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(),
					TTCN3CodeSkeletons.SKELETON_IMAGE);
		}
		for (final SkeletonTemplateProposal templateProposal : TTCN3CodeSkeletons.FORMAL_TEMPLATE_PARAMETER_PROPOSALS) {
			propCollector.addTemplateProposal(templateProposal.getPrefix(), templateProposal.getProposal(),
					TTCN3CodeSkeletons.SKELETON_IMAGE);
		}
	}
}
