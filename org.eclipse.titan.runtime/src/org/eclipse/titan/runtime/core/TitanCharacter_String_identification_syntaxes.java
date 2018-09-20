/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.RAW.RAW_enc_tr_pos;
import org.eclipse.titan.runtime.core.RAW.RAW_enc_tree;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.raw_order_t;

/**
 * Part of the representation of the ASN.1 unrestricted string (CHARACTER STRING) type.
 *
 * @author Kristof Szabados
 */
public class TitanCharacter_String_identification_syntaxes extends Base_Type {
	final TitanObjectid abstract_; //ObjectID_Type
	final TitanObjectid transfer; //ObjectID_Type

	public TitanCharacter_String_identification_syntaxes() {
		abstract_ = new TitanObjectid();
		transfer = new TitanObjectid();
	}

	public TitanCharacter_String_identification_syntaxes( final TitanObjectid aAbstract_, final TitanObjectid aTransfer ) {
		abstract_ = new TitanObjectid( aAbstract_ );
		transfer = new TitanObjectid( aTransfer );
	}

	public TitanCharacter_String_identification_syntaxes( final TitanCharacter_String_identification_syntaxes aOtherValue ) {
		if(!aOtherValue.isBound()) {
			throw new TtcnError("Copying of an unbound value of type CHARACTER STRING.identification.syntaxes.");
		}
		abstract_ = new TitanObjectid();
		transfer = new TitanObjectid();
		assign( aOtherValue );
	}
	public TitanCharacter_String_identification_syntaxes assign(final TitanCharacter_String_identification_syntaxes aOtherValue ) {
		if ( !aOtherValue.isBound() ) {
			throw new TtcnError( "Assignment of an unbound value of type CHARACTER STRING.identification.syntaxes");
		}

		if (aOtherValue != this) {
			if ( aOtherValue.getAbstract_().isBound() ) {
				this.abstract_.assign( aOtherValue.getAbstract_() );
			} else {
				this.abstract_.cleanUp();
			}
			if ( aOtherValue.getTransfer().isBound() ) {
				this.transfer.assign( aOtherValue.getTransfer() );
			} else {
				this.transfer.cleanUp();
			}
		}

		return this;
	}

	@Override
	public TitanCharacter_String_identification_syntaxes assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharacter_String_identification_syntaxes ) {
			return assign((TitanCharacter_String_identification_syntaxes) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to CHARACTER STRING.identification.syntaxes", otherValue));
	}

	public void cleanUp() {
		abstract_.cleanUp();
		transfer.cleanUp();
	}

	@Override
	public boolean isBound() {
		if ( abstract_.isBound() ) { return true; }
		if ( transfer.isBound() ) { return true; }
		return false;
	}

	@Override
	public boolean isPresent() {
		return isBound();
	}

	@Override
	public boolean isValue() {
		if ( !abstract_.isValue() ) { return false; }
		if ( !transfer.isValue() ) { return false; }
		return true;
	}

	public boolean operatorEquals( final TitanCharacter_String_identification_syntaxes aOtherValue ) {
		if ( !this.abstract_.operatorEquals( aOtherValue.abstract_ ) ) { return false; }
		if ( !this.transfer.operatorEquals( aOtherValue.transfer ) ) { return false; }
		return true;
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharacter_String_identification_syntaxes ) {
			return operatorEquals((TitanCharacter_String_identification_syntaxes) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to CHARACTER STRING.identification.syntaxes", otherValue));
	}

	public TitanObjectid getAbstract_() {
		return abstract_;
	}

	public TitanObjectid constGetAbstract_() {
		return abstract_;
	}

	public TitanObjectid getTransfer() {
		return transfer;
	}

	public TitanObjectid constGetTransfer() {
		return transfer;
	}

	public TitanInteger sizeOf() {
		return new TitanInteger(2);
	}

	public void log() {
		if (!isBound()) {
			TTCN_Logger.log_event_unbound();
			return;
		}
		TTCN_Logger.log_char('{');
		TTCN_Logger.log_event_str(" abstract := ");
		abstract_.log();
		TTCN_Logger.log_char(',');
		TTCN_Logger.log_event_str(" transfer := ");
		transfer.log();
		TTCN_Logger.log_event_str(" }");
	}

	@Override
	public void set_implicit_omit() {
		if (abstract_.isBound()) {
			abstract_.set_implicit_omit();
		}
		if (transfer.isBound()) {
			transfer.set_implicit_omit();
		}
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		abstract_.encode_text(text_buf);
		transfer.encode_text(text_buf);
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		abstract_.decode_text(text_buf);
		transfer.decode_text(text_buf);
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