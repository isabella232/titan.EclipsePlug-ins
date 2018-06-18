/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.ttcn3parser;

import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;

/**
 * @author Kristof Szabados
 * */
public class CatchOp_Helper {
	public Reference signatureReference;
	public TemplateInstance parameter;
	public boolean timeout;

	public CatchOp_Helper(final Reference signatureReference, final TemplateInstance parameter,
			final boolean timeout) {
		this.signatureReference = signatureReference;
		this.parameter = parameter;
		this.timeout = timeout;
	}
}
