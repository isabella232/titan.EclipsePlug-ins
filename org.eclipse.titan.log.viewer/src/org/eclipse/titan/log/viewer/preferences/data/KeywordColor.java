/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.preferences.data;

import org.eclipse.swt.graphics.RGB;

public class KeywordColor {

	private String keyword;
	private RGB color;

	public KeywordColor(String keyword, RGB color) {
		this.keyword = keyword;
		this.color = color;
	}

	public RGB getColor() {
		return this.color;
	}
	public String getKeyword() {
		return this.keyword;
	}
}
