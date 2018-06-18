/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.function;

import java.util.List;

/**
 * Classes implementing this interface should be able to provide a model for a
 * table view. <code>T</code> is a class representing a line of the table.
 *
 * @author Viktor Varga
 */
interface IModelProvider<T> {

	/** returns the list of items */
	List<T> getItems();

}
