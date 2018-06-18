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

import java.util.List;

public abstract class RecordOfDef<T extends TypeDef> extends StructuredTypeDef {
    public List<T> value;
    
    public BOOLEAN equals(RecordOfDef<T> v) {
        if (this.value.size() != v.value.size()) return BOOLEAN.FALSE;
        for (int i = 0; i < this.value.size(); i++)
            if (!(this.value.get(i).equals(v.value.get(i)))) return BOOLEAN.FALSE;
        return BOOLEAN.TRUE;
    }
    
    public String toString(){
    	return toString("");
    }
    
    public String toString(String tabs){
		if(anyField) return "?";
		if(omitField) return "omit";
		if(anyOrOmitField) return "*";
    	String retv = "[";
    	for(int i=0;i<value.size();i++){
    		retv += value.get(i).toString(tabs);
    		if(i<value.size()-1) retv += ",";
    	}
    	retv += "]";
    	return retv;
    }
}
