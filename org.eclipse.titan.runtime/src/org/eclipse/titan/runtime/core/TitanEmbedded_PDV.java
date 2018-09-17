/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.Base_Template.template_sel;
import org.eclipse.titan.runtime.core.Optional.optional_sel;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * Part of the representation of the ASN.1 EMBEDDED PDV type
 *
 * @author Kristof Szabados
 */
public class TitanEmbedded_PDV extends Base_Type {
	final TitanEmbedded_PDV_identification identification; //ASN1_Choice_Type
	final Optional<TitanUniversalCharString> data__value__descriptor; //ObjectDescriptor_Type
	final TitanOctetString data__value; //OctetString_Type

	public TitanEmbedded_PDV() {
		identification = new TitanEmbedded_PDV_identification();
		data__value__descriptor = new Optional<TitanUniversalCharString>(TitanUniversalCharString.class);
		data__value = new TitanOctetString();
	}

	public TitanEmbedded_PDV( final TitanEmbedded_PDV_identification aIdentification, final Optional<TitanUniversalCharString> aData__value__descriptor, final TitanOctetString aData__value ) {
		identification = new TitanEmbedded_PDV_identification( aIdentification );
		data__value__descriptor = new Optional<TitanUniversalCharString>(TitanUniversalCharString.class);
		this.data__value__descriptor.assign( aData__value__descriptor );
		data__value = new TitanOctetString( aData__value );
	}

	public TitanEmbedded_PDV( final TitanEmbedded_PDV aOtherValue ) {
		if(!aOtherValue.isBound()) {
			throw new TtcnError("Copying of an unbound value of type EMBEDDED PDV.");
		}
		identification = new TitanEmbedded_PDV_identification();
		data__value__descriptor = new Optional<TitanUniversalCharString>(TitanUniversalCharString.class);
		data__value = new TitanOctetString();
		assign( aOtherValue );
	}

	public TitanEmbedded_PDV assign(final TitanEmbedded_PDV aOtherValue ) {
		if ( !aOtherValue.isBound() ) {
			throw new TtcnError( "Assignment of an unbound value of type EMBEDDED PDV");
		}

		if (aOtherValue != this) {
			if ( aOtherValue.getIdentification().isBound() ) {
				this.identification.assign( aOtherValue.getIdentification() );
			} else {
				this.identification.cleanUp();
			}
			if ( aOtherValue.getData__value__descriptor().isBound() ) {
				this.data__value__descriptor.assign( aOtherValue.getData__value__descriptor() );
			} else {
				this.data__value__descriptor.cleanUp();
			}
			if ( aOtherValue.getData__value().isBound() ) {
				this.data__value.assign( aOtherValue.getData__value() );
			} else {
				this.data__value.cleanUp();
			}
		}

		return this;
	}

	@Override
	public TitanEmbedded_PDV assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV ) {
			return assign((TitanEmbedded_PDV) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to EMBEDDED PDV", otherValue));
	}

	public void cleanUp() {
		identification.cleanUp();
		data__value__descriptor.cleanUp();
		data__value.cleanUp();
	}

	@Override
	public boolean isBound() {
		if ( identification.isBound() ) { return true; }
		if ( optional_sel.OPTIONAL_OMIT.equals(data__value__descriptor.get_selection()) || data__value__descriptor.isBound() ) { return true; }
		if ( data__value.isBound() ) { return true; }
		return false;
	}

	@Override
	public boolean isPresent() {
		return isBound();
	}

	@Override
	public boolean isValue() {
		if ( !identification.isValue() ) { return false; }
		if ( !optional_sel.OPTIONAL_OMIT.equals(data__value__descriptor.get_selection()) && !data__value__descriptor.isValue() ) { return false; }
		if ( !data__value.isValue() ) { return false; }
		return true;
	}

	public boolean operatorEquals( final TitanEmbedded_PDV aOtherValue ) {
		if ( !this.identification.operatorEquals( aOtherValue.identification ) ) { return false; }
		if ( !this.data__value__descriptor.operatorEquals( aOtherValue.data__value__descriptor ) ) { return false; }
		if ( !this.data__value.operatorEquals( aOtherValue.data__value ) ) { return false; }
		return true;
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV ) {
			return operatorEquals((TitanEmbedded_PDV) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to EMBEDDED PDV", otherValue));
	}

	public TitanEmbedded_PDV_identification getIdentification() {
		return identification;
	}

	public TitanEmbedded_PDV_identification constGetIdentification() {
		return identification;
	}

	public Optional<TitanUniversalCharString> getData__value__descriptor() {
		return data__value__descriptor;
	}

	public Optional<TitanUniversalCharString> constGetData__value__descriptor() {
		return data__value__descriptor;
	}

	public TitanOctetString getData__value() {
		return data__value;
	}

	public TitanOctetString constGetData__value() {
		return data__value;
	}

	public TitanInteger sizeOf() {
		int sizeof = 0;
		if (data__value__descriptor.isPresent()) {
			sizeof++;
		}
		sizeof += 2;
		return new TitanInteger(sizeof);
	}
	public void log() {
		if (!isBound()) {
			TTCN_Logger.log_event_unbound();
			return;
		}
		TTCN_Logger.log_char('{');
		TTCN_Logger.log_event_str(" identification := ");
		identification.log();
		TTCN_Logger.log_char(',');
		TTCN_Logger.log_event_str(" data-value-descriptor := ");
		data__value__descriptor.log();
		TTCN_Logger.log_char(',');
		TTCN_Logger.log_event_str(" data-value := ");
		data__value.log();
		TTCN_Logger.log_event_str(" }");
	}

	@Override
	public void set_implicit_omit() {
		if (identification.isBound()) {
			identification.set_implicit_omit();
		}
		if (data__value__descriptor.isBound()) {
			data__value__descriptor.set_implicit_omit();
		} else {
			data__value__descriptor.assign(template_sel.OMIT_VALUE);
		}
		if (data__value.isBound()) {
			data__value.set_implicit_omit();
		}
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		identification.encode_text(text_buf);
		data__value__descriptor.encode_text(text_buf);
		data__value.encode_text(text_buf);
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		identification.decode_text(text_buf);
		data__value__descriptor.decode_text(text_buf);
		data__value.decode_text(text_buf);
	}

	@Override
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-encoding type '%s': ", p_td.name);
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			final RAW_enc_tr_pos rp = new RAW_enc_tr_pos(0, null);
			final RAW_enc_tree root = new RAW_enc_tree(false, null, rp, 1, p_td.raw);
			RAW_encode(p_td, root);
			root.put_to_buf(p_buf);
			errorContext.leaveContext();
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type `{0}''", p_td.name));
		}
	}

	@Override
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		switch (p_coding) {
		case CT_RAW: {
			final TTCN_EncDec_ErrorContext errorContext = new TTCN_EncDec_ErrorContext("While RAW-decoding type '%s': ", p_td.name);
			if (p_td.raw == null) {
				TTCN_EncDec_ErrorContext.error_internal("No RAW descriptor available for type '%s'.", p_td.name);
			}
			raw_order_t order;
			switch (p_td.raw.top_bit_order) {
			case TOP_BIT_LEFT:
				order = raw_order_t.ORDER_LSB;
				break;
			case TOP_BIT_RIGHT:
			default:
				order = raw_order_t.ORDER_MSB;
				break;
			}
			final int rawr = RAW_decode(p_td, p_buf, p_buf.get_len() * 8, order);
			if (rawr < 0) {
				final error_type temp = error_type.values()[-rawr];
				switch (temp) {
				case ET_INCOMPL_MSG:
				case ET_LEN_ERR:
					TTCN_EncDec_ErrorContext.error(temp, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
					break;
				case ET_UNBOUND:
				default:
					TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, "Can not decode type '%s', because invalid or incomplete message was received", p_td.name);
					break;
				}
			}
			errorContext.leaveContext();
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
		}
	}

}