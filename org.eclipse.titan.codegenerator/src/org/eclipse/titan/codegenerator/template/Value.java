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

public class Value implements Writable {

	private static Value field(String type, String field) {
		return new Value(type, (code, indent) -> code.append(type, ".", field));
	}

	public static Value ANY(String type) {
		return field(type, "ANY");
	}
	public static Value OMIT(String type) {
		return field(type, "OMIT");
	}
	public static Value ANY_OR_OMIT(String type) {
		return field(type, "ANY_OR_OMIT");
	}

	protected final String type;
	private final Writable writable;

	public Value(String type, Writable writable) {
		this.type = type;
		this.writable = writable;
	}

	protected Value(String type) {
		this(type, NULL);
	}

	@Override
	public SourceCode write(SourceCode code, int indent) {
		return writable.write(code, indent);
	}

	public String getType() {
		return type;
	}
}
