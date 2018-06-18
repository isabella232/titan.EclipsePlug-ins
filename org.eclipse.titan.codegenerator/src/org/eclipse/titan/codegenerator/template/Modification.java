/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.template;

import org.eclipse.titan.codegenerator.SourceCode;
import org.eclipse.titan.codegenerator.Writable;

public class Modification implements Writable {

	private final Writable writable;
	private final String path;

	public Modification(String path, Writable writable) {
		this.writable = writable;
		this.path = path;
	}

	public String path() {
		return path;
	}

	@Override
	public SourceCode write(SourceCode code, int indent) {
		return writable.write(code, indent);
	}
}
