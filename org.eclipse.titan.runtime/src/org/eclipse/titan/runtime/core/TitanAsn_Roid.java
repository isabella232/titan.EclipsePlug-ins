/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;

/**
 * Runtime class for ASN.1 relative object identifiers (objid)
 *
 * These are synonyms of object ids.
 *
 * @author Gergo Ujhelyi
 * */
public class TitanAsn_Roid extends TitanObjectid {
	private static final ASN_Tag TitanASN_Roid_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 13)};
	public static final ASN_BERdescriptor TitanASN_Roid_Ber_ = new ASN_BERdescriptor(1, TitanASN_Roid_tag_);
	public static final TTCN_JSONdescriptor TitanAsn_Roid_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);
	public static final TTCN_Typedescriptor TitanAsn_Roid_descr_ = new TTCN_Typedescriptor("RELATIVE-OID", TitanASN_Roid_Ber_, null, TitanAsn_Roid_json_, null);

}
