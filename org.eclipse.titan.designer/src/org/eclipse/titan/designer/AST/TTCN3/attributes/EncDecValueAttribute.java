/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

/**
 * Represents a single encdecvalue attribute on an external function, used to
 * automatically generate the encoding function, according to the encoding type
 * and options passed as parameters..
 *
 * @author Arpad Lovassy
 */
public final class EncDecValueAttribute extends ExtensionAttribute implements IInOutTypeMappingAttribute {

	/** The in-mappings, can be null */
	private TypeMappings mInMappings;

	/** The out-mappings, can be null */
	private TypeMappings mOutMappings;


	public EncDecValueAttribute() {
	}

	@Override
	/** {@inheritDoc} */
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.ENCDECVALUE;
	}

	public TypeMappings getInMappings() {
		return mInMappings;
	}

	public void setInMappings(final TypeMappings aMappings) {
		if ( mInMappings == null ) {
			mInMappings = aMappings;
			return;
		}

		mInMappings.copyMappings( aMappings );
	}

	public TypeMappings getOutMappings() {
		return mOutMappings;
	}

	public void setOutMappings(final TypeMappings aMappings) {
		if ( mOutMappings == null ) {
			mOutMappings = aMappings;
			return;
		}

		mOutMappings.copyMappings( aMappings );
	}
}
