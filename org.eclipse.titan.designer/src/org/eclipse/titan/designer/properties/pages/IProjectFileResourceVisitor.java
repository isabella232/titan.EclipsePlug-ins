/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.pages;

import org.eclipse.core.resources.IResourceVisitor;
import org.w3c.dom.Document;

/**
 * @author Kristof Szabados
 * */
public interface IProjectFileResourceVisitor extends IResourceVisitor {

	void setDocument(Document document);
}
