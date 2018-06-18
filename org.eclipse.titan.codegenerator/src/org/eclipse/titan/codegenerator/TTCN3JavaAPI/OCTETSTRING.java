/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * Contributors:
 *   
 *   Keremi, Andras
 *   Eros, Levente
 *   Kovacs, Gabor
 *   Meszaros, Mate Robert
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.TTCN3JavaAPI;

public class OCTETSTRING extends BINARY_STRING implements Indexable<OCTETSTRING> {

    public OCTETSTRING() {
    }

    public OCTETSTRING(String value) {
        super(value);
        Integer.parseInt(value, 16); //throws an exception if not legal
    }
    
    public OCTETSTRING bitwiseNot(){
    	return new OCTETSTRING(Integer.toOctalString(generalBitwiseNot(fromOctetString(value))));
    }    

    public OCTETSTRING bitwiseAnd(OCTETSTRING b){
    	return new OCTETSTRING(Integer.toOctalString(generalBitwiseAnd(fromOctetString(value), fromOctetString(b.value))));
    }
    
    public OCTETSTRING bitwiseOr(OCTETSTRING b){
    	return new OCTETSTRING(Integer.toOctalString(generalBitwiseOr(fromOctetString(value), fromOctetString(b.value))));
    }
    
    public OCTETSTRING bitwiseXor(OCTETSTRING b){
    	return new OCTETSTRING(Integer.toOctalString(generalBitwiseXor(fromOctetString(value), fromOctetString(b.value))));
    }
    
	public String toString() {
		return toString("");
	}
	
	public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
		return "'" + new String(value) + "'O";
	}

	@Override
	public OCTETSTRING get(int index) {
		return new OCTETSTRING(new String(new byte[]{value[2 * index], value[2 * index + 1]}));
	}

	@Override
	public void set(int index, OCTETSTRING octetstring) {
		value[2 * index] = octetstring.value[0];
		value[2 * index + 1] = octetstring.value[1];
	}
}
