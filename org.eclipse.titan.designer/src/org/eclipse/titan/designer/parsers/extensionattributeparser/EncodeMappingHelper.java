/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.parsers.extensionattributeparser;

import org.eclipse.titan.designer.AST.TTCN3.attributes.ErrorBehaviorAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;

/**
 * Just a helper class to store information
 *
 * @author Kristof Szabados
 * */
public class EncodeMappingHelper {

	public ExtensionAttribute encodeAttribute;
	public ErrorBehaviorAttribute errorBehaviorAttribute;

	public EncodeMappingHelper(final ExtensionAttribute encodeAttribute, final ErrorBehaviorAttribute errorBehaviorAttribute){
		this.encodeAttribute = encodeAttribute;
		this.errorBehaviorAttribute = errorBehaviorAttribute;
	}
}
