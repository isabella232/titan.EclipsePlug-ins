/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.ASN1.Object;

import org.eclipse.titan.designer.AST.Identifier;

/**
 * Class to represent a Setting in the OCS.
 *
 * @author Kristof Szabados
 */
public final class ObjectClassSyntax_setting extends ObjectClassSyntax_Node {

	public enum SyntaxSetting_types {
		/** undefined. */
		S_UNDEF,
		/** Type. */
		S_T,
		/** Value. */
		S_V,
		/** ValueSet. */
		S_VS,
		/** Object. */
		S_O,
		/** ObjectSet. */
		S_OS
	}

	private final SyntaxSetting_types settingType;
	private final Identifier identifier;

	public ObjectClassSyntax_setting(final SyntaxSetting_types settingType, final Identifier identifier) {
		this.settingType = settingType;
		this.identifier = identifier;
	}

	@Override
	/** {@inheritDoc} */
	public void accept(final ObjectClassSyntax_Visitor visitor) {
		visitor.visitSetting(this);
	}

	public SyntaxSetting_types getSettingType() {
		return settingType;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	@Override
	/** {@inheritDoc} */
	public String getDisplayName() {
		switch (settingType) {
		case S_T:
			return "<Type>";
		case S_V:
			return"<Value>";
		case S_VS:
			return"<ValueSet>";
		case S_O:
			return"<Object>";
		case S_OS:
			return"<ObjectSet>";
		default:
			return"<unknown setting kind>";
		}
	}
}
