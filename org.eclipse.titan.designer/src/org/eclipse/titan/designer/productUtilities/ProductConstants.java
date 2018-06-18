/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.productUtilities;

/**
 * This is a class storing product identification strings.
 * So that all packages depending on this information, does not automatically depend on unnecessary things too.
 * 
 * @author Kristof Szabados
 * */
public final class ProductConstants {
	public static final String TITAN_PREFIX = "org.eclipse.titan";
	public static final String PRODUCT_ID_DESIGNER = TITAN_PREFIX + ".designer";

	private ProductConstants() {
		// Hide constructor
	}
}
