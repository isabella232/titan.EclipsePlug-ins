/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.asn1parser;


import org.eclipse.titan.common.parsers.TitanListener;

public class ASN1Listener extends TitanListener {

	public ASN1Listener() {
		super();
	}

	public ASN1Listener(final Asn1Parser parser) {
		super.errorsStored = parser.getErrorStorage();
	}

}
