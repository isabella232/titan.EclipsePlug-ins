/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.ILocateableNode;

/**
 * Interface for extension attributes with in and out type mappings
 * @author Arpad Lovassy
 */
public interface IInOutTypeMappingAttribute extends ILocateableNode {

	public void setInMappings( final TypeMappings aMappings );

	public void setOutMappings( final TypeMappings aMappings );

}
