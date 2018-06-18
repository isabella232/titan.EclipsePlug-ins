/*******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/

package org.eclipse.titan.log.viewer.views.msc.ui.core;

import org.eclipse.titan.log.viewer.views.msc.ui.view.IGC;

/**
 * Created by Szabolcs Beres
 */
public abstract class FunctionNode extends BaseMessage {

	public FunctionNode() {
		super(0);
	}

	@Override
	public void drawSymbol(final IGC context, final int xLeft, final int xRight, final int yTop, final int yBottom, final int direction) {
		drawSymbol(context, xLeft, yBottom, direction);
	}
}
