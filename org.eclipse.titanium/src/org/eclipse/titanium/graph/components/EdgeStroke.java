/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.graph.components;

import java.awt.BasicStroke;
import java.awt.Stroke;

import com.google.common.base.Function;

/**
 * This class can makes edges colored red more emphasized, by doubling their width.
 *
 * @author Kristof Szabados
 */
public 	class EdgeStroke<E> implements Function<E, Stroke> {

	@Override
	public Stroke apply(final E e) {
		if (e instanceof EdgeDescriptor && NodeColours.DARK_RED.equals(((EdgeDescriptor) e).getColor())) {
			return new BasicStroke(2.0f);
		}

		return new BasicStroke(1.0f);
	}

}
