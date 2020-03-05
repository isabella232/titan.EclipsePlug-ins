/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.parsers.cfg.indices;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Base class of config section handlers, they are responsible for storing section data,
 * which are edited through the corresponding config editor tab,
 * and are written back to the cfg file.
 * @author Arpad Lovassy
 */
public abstract class ConfigSectionHandlerBase {

	/** The root rule of a section */
	private ParserRuleContext mLastSectionRoot = null;

	public ParserRuleContext getLastSectionRoot() {
		return mLastSectionRoot;
	}

	public void setLastSectionRoot( final ParserRuleContext lastSectionRoot ) {
		this.mLastSectionRoot = lastSectionRoot;
	}
}
