/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * Ass_Error::get_Setting() returns this.
 *
 * @author Kristof Szabados
 */
public final class Error_Setting extends Setting {

	@Override
	/** {@inheritDoc} */
	public Setting_type getSettingtype() {
		return Setting_type.S_ERROR;
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		// no members
		return true;
	}
}
