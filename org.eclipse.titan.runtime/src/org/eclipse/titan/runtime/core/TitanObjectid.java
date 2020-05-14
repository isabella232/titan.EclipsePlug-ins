/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.titan.runtime.core.BER.ASN_BERdescriptor;
import org.eclipse.titan.runtime.core.BER.ASN_Tag;
import org.eclipse.titan.runtime.core.BER.ASN_TagClass;
import org.eclipse.titan.runtime.core.JSON.TTCN_JSONdescriptor;
import org.eclipse.titan.runtime.core.JSON.json_string_escaping;
import org.eclipse.titan.runtime.core.JSON_Tokenizer.json_token_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Objid;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;

/**
 * Runtime class for object identifiers (objid)
 *
 * @author Gergo Ujhelyi
 * @author Arpad Lovassy
 * */
public class TitanObjectid extends Base_Type {
	private static final ASN_Tag TitanObjectId_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 6)};
	public static final ASN_BERdescriptor TitanObjectId_Ber_ = new ASN_BERdescriptor(1, TitanObjectId_tag_);
	public static final TTCN_JSONdescriptor TitanObjectid_json_ = new TTCN_JSONdescriptor(false, null, false, null, false, false, false, 0, null, false, json_string_escaping.ESCAPE_AS_SHORT);

	public static final TTCN_Typedescriptor TitanObjectid_descr_ = new TTCN_Typedescriptor("OBJECT IDENTIFIER", TitanObjectId_Ber_, null, TitanObjectid_json_, null);

	public static final int MIN_COMPONENTS = 2;

	private int n_components; // number of elements in components_ptr (min. 2)
	private int overflow_idx; // index of the first overflow, or -1
	private List<TitanInteger> components_ptr;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanObjectid() {
		super();
	}

	public TitanObjectid(final int init_n_components, final TitanInteger... values) {
		if (init_n_components < 0) {
			throw new TtcnError("Initializing an objid value with a negative number of components.");
		}

		n_components = init_n_components;
		overflow_idx = -1;
		components_ptr = new ArrayList<TitanInteger>(values.length);
		for (int i = 0; i < values.length; i++) {
			components_ptr.add(values[i]);
		}
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanObjectid(final TitanObjectid otherValue) {
		otherValue.must_bound("Copying an unbound objid value.");

		components_ptr = new ArrayList<TitanInteger>();
		components_ptr.addAll(otherValue.components_ptr);
		n_components = otherValue.n_components;
		overflow_idx = otherValue.overflow_idx;
	}

	@Override
	public void clean_up() {
		components_ptr = null;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public TitanObjectid operator_assign(final TitanObjectid otherValue) {
		otherValue.must_bound("Assignment of an unbound objid value.");

		clean_up();
		components_ptr = new ArrayList<TitanInteger>();
		components_ptr.addAll(otherValue.components_ptr);
		n_components = otherValue.n_components;
		overflow_idx = otherValue.overflow_idx;

		return this;
	}

	// originally operator=
	@Override
	public Base_Type operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanObjectid) {
			return operator_assign((TitanObjectid) otherValue);
		} else {
			throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to objectid", otherValue));
		}
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final TitanObjectid otherValue) {
		must_bound("The left operand of comparison is an unbound objid value.");
		otherValue.must_bound("The right operand of comparison is an unbound objid value.");

		if (n_components != otherValue.n_components) {
			return false;
		}
		if (overflow_idx != otherValue.overflow_idx) {
			return false;
		}

		for (int i = 0; i < components_ptr.size(); i++) {
			if (!components_ptr.get(i).operator_equals(otherValue.components_ptr.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanObjectid) {
			return operator_equals((TitanObjectid) otherValue);
		} else {
			return false;
		}
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final TitanObjectid otherValue) {
		return !operator_equals(otherValue);
	}

	@Override
	public boolean is_present() {
		return components_ptr != null;
	}

	@Override
	public boolean is_bound() {
		return components_ptr != null;
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this objectid
	 * */
	public final TitanInteger constGet_at(final int index_value) {
		if (components_ptr == null) {
			if (index_value != 0) {
				throw new TtcnError("Accessing a component of an unbound objid value.");
			}
			n_components = 1;
			overflow_idx = -1;
			components_ptr = new ArrayList<TitanInteger>();
			components_ptr.add(new TitanInteger(0));

			return components_ptr.get(0);
		}
		if (index_value < 0) {
			throw new TtcnError(MessageFormat.format("Accessing an objid component using a negative index {0}.", index_value));
		}
		if (index_value > n_components) {
			throw new TtcnError(MessageFormat.format("Index overflow when accessing an objid component: the index is {0}, but the value has only {1} components.", index_value, n_components));
		} else if (index_value == n_components) {
			n_components++;
			components_ptr.add(new TitanInteger(0));
		}

		return components_ptr.get(index_value);
	}

	/**
	 * Gives read-only access to the given element.
	 *
	 * Index underflow and overflow causes dynamic test case error.
	 *
	 * const operator[] const in the core.
	 *
	 * @param index
	 *            the index of the element to return.
	 * @return the element at the specified position in this objectid
	 * */
	public final TitanInteger constGet_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a objid component with an unbound integer value.");

		return constGet_at(index_value.get_int());
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the objectid.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this objectid
	 * */
	public TitanInteger get_at(final int index_value) {
		if (components_ptr == null) {
			if (index_value != 0) {
				throw new TtcnError("Accessing a component of an unbound objid value.");
			}
			n_components = 1;
			overflow_idx = -1;
			components_ptr = new ArrayList<TitanInteger>();
			components_ptr.add(new TitanInteger(0));

			return components_ptr.get(0);
		}
		if (index_value < 0) {
			throw new TtcnError(MessageFormat.format("Accessing an objid component using a negative index {0}.", index_value));
		}
		if (index_value > n_components) {
			throw new TtcnError(MessageFormat.format("Index overflow when accessing an objid component: the index is {0}, but the value has only {1} components.", index_value, n_components));
		} else if (index_value == n_components) {
			n_components++;
			components_ptr.add(new TitanInteger(0));
		}

		return components_ptr.get(index_value);
	}

	/**
	 * Gives access to the given element. Indexing begins from zero.
	 * Over-indexing by 1 extends the objectid.
	 *
	 * operator[] in the core.
	 *
	 * @param index_value
	 *            the index of the element to return.
	 * @return the element at the specified position in this objectid
	 * */
	public TitanInteger get_at(final TitanInteger index_value) {
		index_value.must_bound("Indexing a objid component with an unbound integer value.");

		return get_at(index_value.get_int());
	}

	/**
	 * Returns the number of elements, that is, the largest used index plus
	 * one and zero for the empty value.
	 *
	 * lengthof in the core
	 *
	 * @return the number of elements.
	 * */
	public TitanInteger lengthof() {
		must_bound("Getting the size of an unbound objid value.");

		return new TitanInteger(n_components);
	}

	/**
	 * Returns the number of elements, that is, the largest used index plus
	 * one and zero for the empty value.
	 *
	 * size_of in the core
	 *
	 * @return the number of elements.
	 * */
	public TitanInteger size_of() {
		must_bound("Getting the size of an unbound objid value.");

		return new TitanInteger(n_components);
	}

	public static TitanInteger from_integer(final TitanInteger p_int) {
		if (p_int.is_less_than(0)) {
			throw new TtcnError("An OBJECT IDENTIFIER component cannot be negative");
		}

		return new TitanInteger(p_int);
	}

	@Override
	public void log() {
		if (components_ptr != null) {
			TTCN_Logger.log_event_str("objid { ");
			for (int i = 0; i < n_components; i++) {
				if (i == overflow_idx) {
					TTCN_Logger.log_event_str("overflow:");
				}

				components_ptr.get(i).log();
				TTCN_Logger.log_char(' ');
			}
			TTCN_Logger.log_char('}');
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "objid value");

		// Originally RT2
		if (param.get_type() == Module_Parameter.type_t.MP_Reference) {
			param = param.get_referenced_param().get();
		}

		if (param.get_type() != type_t.MP_Objid) {
			param.type_error("objid value");
		}
		clean_up();
		n_components = param.get_string_size();
		components_ptr = new ArrayList<TitanInteger>(Arrays.asList((TitanInteger[]) param.get_string_data()));
		overflow_idx = -1;
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name) {
	  if (!is_bound()) {
	    return new Module_Param_Unbound();
	  }
	  final TitanInteger[] intarray = new TitanInteger[components_ptr.size()];
	  components_ptr.toArray(intarray);
	  return new Module_Param_Objid(n_components, intarray);
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound objid value.");

		text_buf.push_int(n_components);
		for (int i = 0; i < n_components; i++) {
			text_buf.push_int(components_ptr.get(i));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();

		n_components = text_buf.pull_int().get_int();
		if (n_components < 0) {
			throw new TtcnError("Text decoder: Negative number of components was received for an objid value.");
		}
		components_ptr = new ArrayList<TitanInteger>(n_components);
		for (int i = 0; i < n_components; i++) {
			components_ptr.add(text_buf.pull_int());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-encoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final JSON_Tokenizer tok = new JSON_Tokenizer(flavour != 0);
				JSON_encode(p_td, tok);
				p_buf.put_s(tok.get_buffer().toString().getBytes());
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_JSON: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While JSON-decoding type '%s': ", p_td.name);
			try {
				if(p_td.json == null) {
					TTCN_EncDec_ErrorContext.error_internal("No JSON descriptor available for type '%s'.", p_td.name);
				}

				final JSON_Tokenizer tok = new JSON_Tokenizer(new String(p_buf.get_data()), p_buf.get_len());
				if(JSON_decode(p_td, tok, false) < 0) {
					TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INCOMPL_MSG,
							"Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
				}
				p_buf.set_pos(tok.get_buf_pos());
			} finally {
				errorContext.leave_context();
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_encode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_parent_is_map) {
		if (!is_bound()) {
			TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_UNBOUND,"Encoding an unbound object identifier value.");
			return -1;
		}

		final StringBuilder objid_str = new StringBuilder();
		objid_str.append('"');
		for (int i = 0; i < n_components; ++i) {
			if ( i > 0 ) {
				objid_str.append('.');
			}
			objid_str.append(components_ptr.get(i));
		}
		objid_str.append('"');
		final int enc_len = p_tok.put_next_token(json_token_t.JSON_TOKEN_STRING, objid_str.toString());
		return enc_len;
	}

	@Override
	/** {@inheritDoc} */
	public int JSON_decode(final TTCN_Typedescriptor p_td, final JSON_Tokenizer p_tok, final boolean p_silent, final boolean p_parent_is_map, final int p_chosen_field) {
		final AtomicReference<json_token_t> token = new AtomicReference<json_token_t>(json_token_t.JSON_TOKEN_NONE);
		final StringBuilder value = new StringBuilder();
		final AtomicInteger value_len = new AtomicInteger(0);
		boolean error = false;
		int dec_len = 0;
		final boolean use_default = p_td.json.getDefault_value() != null && 0 == p_tok.get_buffer_length();
		if (use_default) {
			// No JSON data in the buffer -> use default value
			value.setLength(0);
			value.append( p_td.json.getDefault_value() );
			value_len.set(value.length());
		} else {
			dec_len = p_tok.get_next_token(token, value, value_len);
		}
		if (json_token_t.JSON_TOKEN_ERROR == token.get()) {
			if(!p_silent) {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_BAD_TOKEN_ERROR, "");
			}
			return JSON.JSON_ERROR_FATAL;
		} else if (json_token_t.JSON_TOKEN_STRING == token.get() || use_default) {
			if (use_default || (value_len.get() >= 2 && value.charAt(0) == '\"' && value.charAt(value_len.get() - 1) == '\"')) {
				if (!use_default) {
					// The default value doesn't have quotes around it
					final String valueWithoutQuotes = value.substring(1, value.length() - 1);
					value.setLength(0);
					value.append( valueWithoutQuotes );
					value_len.set(value.length());
				}
				from_string(value.toString());
			}
		} else {
			return JSON.JSON_ERROR_INVALID_TOKEN;
		}

		if (error) {
			if (p_silent) {
				clean_up();
			} else {
				TTCN_EncDec_ErrorContext.error(TTCN_EncDec.error_type.ET_INVAL_MSG, JSON.JSON_DEC_FORMAT_ERROR, "string", "object identifier");
			}
			return JSON.JSON_ERROR_FATAL;
		}

		return dec_len;
	}

	private void from_string(final String p_str) {
		final String[] components = p_str.split("\\.");
		final List<TitanInteger> tmpList = new ArrayList<TitanInteger>();
		for (final String component : components) {
			if ( component.length() > 0 ) {
				tmpList.add(new TitanInteger(Integer.parseInt(component)));
			}
		}
		overflow_idx = -1;
		components_ptr = tmpList;
		n_components = components_ptr.size();
	}
}
