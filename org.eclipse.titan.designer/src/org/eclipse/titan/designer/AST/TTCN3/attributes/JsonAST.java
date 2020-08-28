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

import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
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
		public JsonEnumText(final String p_from, final String p_to){
			from = p_from;
			to = p_to;
		}
	}

	public enum json_type_indicator {
		JSON_NO_TYPE,
		JSON_NUMBER,
		JSON_INTEGER,
		JSON_STRING,
		JSON_ARRAY,
		JSON_OBJECT,
		JSON_OBJECT_MEMBER,
		JSON_LITERAL
	}

	public enum json_string_escaping {
		ESCAPING_UNSET, // no escaping attribute was set (equivalent with ESCAPE_AS_SHORT at runtime)
		ESCAPE_AS_SHORT, // attribute "escape as short" was set explicitly
		ESCAPE_AS_USI,
		ESCAPE_AS_TRANSPARENT
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
	 * Stores the text parsed from the default attribute without any further processing.
	 */
	public String parsed_default_value;

	/**
	 * The location of the default value in the source code.
	 */
	public Location defaultLocation;

	/**
	 * Decoding only.
	 * The value parsed from the parsed_default_value.
	 */
	public IValue default_value;

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

	public boolean use_null;

	/** List of enumerated values whose texts are changed. */
	public List<JsonEnumText> enum_texts;

	public json_type_indicator type_indicator;
	public json_string_escaping string_escaping;

	public JsonAST() {
		init_JsonAST();
	}

	public JsonAST(final JsonAST value) {
		if (value != null) {
			omit_as_null = value.omit_as_null;
			alias = value.alias;
			as_value = value.as_value;
			parsed_default_value = value.parsed_default_value;
			metainfo_unbound = value.metainfo_unbound;
			as_number = value.as_number;
			tag_list = value.tag_list != null ? new ArrayList<rawAST_coding_taglist>(value.tag_list) : null;
			as_map = value.as_map;
			enum_texts = new ArrayList<JsonEnumText>(value.enum_texts);
			schema_extensions = new ArrayList<JsonSchemaExtension>(value.schema_extensions);
			use_null = value.use_null;
			type_indicator = value.type_indicator;
			string_escaping = value.string_escaping;
		} else {
			init_JsonAST();
		}
	}

	private void init_JsonAST() {
		omit_as_null = false;
		alias = null;
		as_value = false;
		parsed_default_value = null;
		metainfo_unbound = false;
		as_number = false;
		tag_list = null;
		as_map = false;
		enum_texts = new ArrayList<JsonEnumText>();
		schema_extensions = new ArrayList<JsonSchemaExtension>();
		use_null = false;
		type_indicator = json_type_indicator.JSON_NO_TYPE;
		string_escaping = json_string_escaping.ESCAPING_UNSET;
	}

	public boolean empty() {
		return omit_as_null == false && alias == null && as_value == false &&
				parsed_default_value == null && metainfo_unbound == false && as_number == false &&
				tag_list == null && as_map == false && enum_texts.size() == 0 &&
				use_null == false && type_indicator != json_type_indicator.JSON_OBJECT &&
				type_indicator != json_type_indicator.JSON_OBJECT_MEMBER && type_indicator != json_type_indicator.JSON_LITERAL &&
				(string_escaping == json_string_escaping.ESCAPING_UNSET || string_escaping == json_string_escaping.ESCAPE_AS_SHORT);

	}

	public String get_type_str() {
		switch (type_indicator) {
		case JSON_NO_TYPE:
			return "<none>";
		case JSON_NUMBER:
			return "JSON:number";
		case JSON_INTEGER:
			return "JSON:integer";
		case JSON_STRING:
			return "JSON:string";
		case JSON_ARRAY:
			return "JSON:array";
		case JSON_OBJECT:
			return "JSON:object";
		case JSON_OBJECT_MEMBER:
			return "JSON:objectMember";
		case JSON_LITERAL:
			return "JSON:literal";
		default:
			//TODO
			//FATAL_ERROR("JsonAST.get_type_str");
			return null;
		}
	}

	public String get_escaping_str() {
		switch (string_escaping) {
		case ESCAPE_AS_SHORT:
			return "escape as short";
		case ESCAPE_AS_USI:
			return "escape as usi";
		case ESCAPE_AS_TRANSPARENT:
			return "escape as transparent";
		default:
			//TODO
			//FATAL_ERROR("JsonAST.get_escaping_str");
			return null;
		}
	}

	public String get_escaping_gen_str() {
		switch (string_escaping) {
		case ESCAPING_UNSET:
		case ESCAPE_AS_SHORT:
			return "ESCAPE_AS_SHORT";
		case ESCAPE_AS_USI:
			return "ESCAPE_AS_USI";
		case ESCAPE_AS_TRANSPARENT:
			return "ESCAPE_AS_TRANSPARENT";
		default:
			//TODO
			//FATAL_ERROR("JsonAST.get_escaping_gen_str");
			return null;
		}
	}

	public void print_JsonAST() {
		System.out.printf("\n\rOmit encoding: ");
		if (omit_as_null) {
			System.out.printf("as null value\n\r");
		} else {
			System.out.printf("skip field\n\r");
		}
		if (alias != null) {
			System.out.printf("Name as %s\n\r", alias);
		}
		if (as_value) {
			System.out.printf("Encoding unions as JSON value\n\r");
		}
		if (parsed_default_value != null) {
			System.out.printf("Parsed default value: %s\n\r", parsed_default_value);
		}
		if (as_number) {
			System.out.printf("Encoding enumerated values as numbers\n\r");
		}
		if (0 != schema_extensions.size()) {
			System.out.printf("Extensions:");
			for (int i = 0; i < schema_extensions.size(); ++i) {
				System.out.printf(" \"%s\" : \"%s\"", schema_extensions.get(i).key, schema_extensions.get(i).value);
			}
		}
		if (metainfo_unbound) {
			System.out.printf("Metainfo for unbound field(s)\n\r");
		}
		if (tag_list != null) {
			System.out.printf("Chosen union fields:\n\r");
			System.out.printf("  Number of rules: %d\n\r", tag_list.size());
			for (int i = 0; i < tag_list.size(); ++i) {
				System.out.printf("  Rule #%d:\n\r", i);
				System.out.printf("    Chosen field: %s\n\r", tag_list.get(i).fieldname != null ?
						tag_list.get(i).fieldname : "omit");
				System.out.printf("    Number of conditions: %d\n\r", tag_list.get(i).fields.size());
				for (int j = 0; j < tag_list.get(i).fields.size(); ++j) {
					System.out.printf("    Condition #%d:\n\r", j);
					System.out.printf("      Value: %s\n\r", tag_list.get(i).fields.get(j).value);
					System.out.printf("      Field: ");
					for (int k = 0; k < tag_list.get(i).fields.get(j).fields.size(); ++k) {
						if (k != 0) {
							System.out.printf(".");
						}
						System.out.printf("%s", tag_list.get(i).fields.get(j).fields.get(k).nthfieldname);
					}
					System.out.printf("\n\r");
				}
			}
			System.out.printf("Type: %s\n\r", get_type_str());
			if (string_escaping != json_string_escaping.ESCAPING_UNSET) {
				System.out.printf("%s\n\r", get_escaping_str());
			}
		}
		if (as_map) {
			System.out.printf("Encoding elements into a map of key-value pairs.\n\r");
		}
		if (0 != enum_texts.size()) {
			System.out.printf("Enum texts:");
			for (int i = 0; i < enum_texts.size(); ++i) {
				System.out.printf(" '%s' -> '%s'", enum_texts.get(i).from, enum_texts.get(i).to);
			}
			System.out.printf("\n\r");
		}
	}
}


