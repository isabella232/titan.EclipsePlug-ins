/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views.msc.ui.core;

import org.eclipse.swt.graphics.Color;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.views.msc.util.MSCConstants;

/**
 * Representation of a component termination in the sequence diagram
 */
public class ComponentTermination extends ComponentEventNode {

	private String verdict;

	/**
	 * Constructor
	 *
	 * @param eventOccurrence the occurrence of the component termination
	 * @param verdict         the final verdict of the component
	 */
	public ComponentTermination(final int eventOccurrence, final Lifeline lifeline, final String verdict) {
		super(eventOccurrence, lifeline);
		this.verdict = verdict;
	}

	@Override
	protected Color getBackgroundColor() {
		return (Color) Activator.getDefault().getCachedResource(MSCConstants.getVerdictColor(this.verdict));
	}

	@Override
	public Type getType() {
		return Type.COMPONENT_TERMINATION;
	}

}
