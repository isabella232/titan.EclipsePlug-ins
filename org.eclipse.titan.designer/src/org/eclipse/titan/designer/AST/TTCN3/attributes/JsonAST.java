/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.TTCN3.attributes.RawASTStruct.rawAST_coding_taglist;

/**
 * Represents the JSON encoding related setting extracted from variant attributes.
 *
 * @author Farkas Izabella Ingrid
 * @author Arpad Lovassy
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

	/**
	 * Encoding only.
	 * true  : use the null literal to encode omitted fields in records or sets
	 *         example: { "field1" : value1, "field2" : null, "field3" : value3 }
	 * false : skip both the field name and the value if a field is omitted
	 *         example: { "field1" : value1, "field3" : value3 }
	 * The decoder will always accept both variants.
	 */
	public boolean omit_as_null;

	/**
	 * An alias for the name of the field (in a record, set or union).
	 * Encoding: this alias will appear instead of the name of the field
	 * Decoding: the decoder will look for this alias instead of the field's real name
	 */
	public String alias;

	/**
	 * true if this type is a field of a union with the "as value" coding instruction.
	 * If set, the union will be encoded as a JSON value instead of a JSON object
	 * with one name-value pair.
	 * Since the field name is no longer present, the decoder will determine the
	 * selected field based on the type of the value. The first field (in the order
	 * of declaration) that can successfully decode the value will be the selected one.
	 */
	public boolean as_value;

	/**
	 * Decoding only.
	 * Fields that don't appear in the JSON code will decode this value instead.
	 */
	public String default_value;

	public List<JsonSchemaExtension> schema_extensions;

	/**
	 * If set, encodes unbound fields of records and sets as null and inserts a
	 * meta info field into the JSON object specifying that the field is unbound.
	 * The decoder sets the field to unbound if the meta info field is present and
	 * the field's value in the JSON code is either null or a valid value for that
	 * field.
	 * Example: { "field1" : null, "metainfo field1" : "unbound" }
	 *
	 * Also usable on record of/set of/array types to indicate that an element is
	 * unbound. Unbound elements are encoded as a JSON object containing one
	 * metainfo member. The decoder sets the element to unbound if the object
	 * with the meta information is found.
	 * Example: [ value1, value2, { "metainfo []" : "unbound" }, value3 ]
	 */
	public boolean metainfo_unbound;

	/**
	 * If set, the enumerated value's numeric form will be encoded as a JSON
	 * number, instead of its name form as a JSON string (affects both encoding
	 * and decoding).
	 */
	public boolean as_number;

	/** chosen fields for JSON encoding */
	public List<rawAST_coding_taglist> tag_list;

	/**
	 * If set, encodes the value into a map of key-value pairs (i.e. a fully
	 * customizable JSON object). The encoded type has to be a record of/set of
	 * with a record/set element type, which has 2 fields, the first of which is
	 * a non-optional universal charstring.
	 * Example: { "key1" : value1, "key2" : value2 }
	 */
	public boolean as_map;

	/** List of enumerated values whose texts are changed. */
	public List<JsonEnumText> enum_texts;


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
			tag_list = value.tag_list != null ? new ArrayList<rawAST_coding_taglist>(value.tag_list) : null;
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
		tag_list = null;
		as_map = false;
		enum_texts = new ArrayList<JsonEnumText>();
		schema_extensions = new ArrayList<JsonSchemaExtension>();
	}

	public boolean empty() {
		return omit_as_null == false && alias == null && as_value == false &&
				default_value == null && metainfo_unbound == false && as_number == false &&
				tag_list == null && as_map == false && enum_texts.size() == 0;

	}
}


