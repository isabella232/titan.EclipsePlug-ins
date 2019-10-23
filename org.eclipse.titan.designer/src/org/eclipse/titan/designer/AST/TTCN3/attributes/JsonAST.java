/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.util.ArrayList;

/**
 * Represents the JSON encoding related setting extracted from variant attributes.
 *
 * @author Farkas Izabella Ingrid
 * */
public class JsonAST {
	public class JsonSchemaExtension {
		public String key;
		public String value;

		private void init(final String p_key, final String p_value) {
			key = p_key;
			value = p_value;
		}

		public JsonSchemaExtension(final JsonSchemaExtension x) {
			init(x.key, x.value);
		}

		public JsonSchemaExtension(final String p_key, final String p_value) {
			init(p_key, p_value);
		}
	}


	public static class JsonEnumText{
		public String from;
		public String to;
		public int index; // set during semantic analysis
		JsonEnumText(final String p_from, final String p_to){
			from = p_from;
			to = p_to;
		}
	}

	public boolean omit_as_null;
	public String alias;
	public boolean as_value;
	public String default_value;
	public ArrayList<JsonSchemaExtension> schema_extensions;
	public boolean metainfo_unbound;
	public boolean as_number;
	//rawAST_tag_list[] tag_list;
	public boolean as_map;
	public ArrayList<JsonEnumText> enum_texts;


	public JsonAST() {
		init_JsonAST();
	}

	public JsonAST(final JsonAST value) {
		if (value != null) {
			omit_as_null = value.omit_as_null;
			alias = value.alias;
			as_value = value.as_value;
			default_value = value.default_value;
			metainfo_unbound = value.metainfo_unbound;
			as_number = value.as_number;
			//tag_list = NULL;
			as_map = value.as_map;
			enum_texts = new ArrayList<JsonEnumText>(value.enum_texts);
			schema_extensions = new ArrayList<JsonSchemaExtension>(value.schema_extensions);
		} else {
			init_JsonAST();
		}
	}

	private void init_JsonAST() {
		omit_as_null = false;
		alias = null;
		as_value = false;
		default_value = null;
		metainfo_unbound = false;
		as_number = false;
		//tag_list = NULL;
		as_map = false;
		enum_texts = new ArrayList<JsonEnumText>();
		schema_extensions = new ArrayList<JsonSchemaExtension>();
	}

	public boolean empty() {
		return omit_as_null == false && alias == null && as_value == false &&
				default_value == null && metainfo_unbound == false && as_number == false &&
				/*tag_list == null &&*/ as_map == false && enum_texts.size() == 0;

	}
}


