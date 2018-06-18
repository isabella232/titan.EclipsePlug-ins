/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties;

import org.eclipse.core.resources.IResource;

/**
 * This interface is implemented by objects that listen to changes of TITAN related properties.
 * 
 * @author Kristof Szabados
 * */
public interface IPropertyChangeListener {

	/**
	 * Notifies that the resource has changed its properties.
	 * 
	 * @param resouce the resource.
	 * */
	public void propertyChanged(final IResource resouce);
}
