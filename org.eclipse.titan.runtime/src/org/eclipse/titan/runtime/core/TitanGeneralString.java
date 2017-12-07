/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.util.ArrayList;
import java.util.List;

/**
 * ASN.1 general string
 *
 * @author Kristof Szabados
 *
 * TODO this and template version might need to overwrite some functions
 */
public class TitanGeneralString extends TitanUniversalCharString {
	public static TitanGeneralString TTCN_ISO2022_2_GeneralString(final TitanOctetString p_os) {
		final char osstr[] = p_os.getValue();
		final int len = osstr.length;
		final ArrayList<TitanUniversalChar> ucstr = new ArrayList<TitanUniversalChar>(len);

		for (int i = 0; i < len; i++) {
			ucstr.add(new TitanUniversalChar((char)0, (char)0 , (char)0, osstr[i]));
		}

		return new TitanGeneralString(ucstr);
	}

	public TitanGeneralString(final List<TitanUniversalChar> aOtherValue) {
		super(aOtherValue);
	}
}
