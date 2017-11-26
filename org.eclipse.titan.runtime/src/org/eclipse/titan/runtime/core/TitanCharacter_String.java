/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.Optional.optional_sel;

/**
 * Part of the representation of the ASN.1 unrestricted string (CHARACTER STRING) type.
 *
 * @author Kristof Szabados
 */
public class TitanCharacter_String extends Base_Type {
	TitanCharacter_String_identification identification; //ASN1_Choice_Type
	Optional<TitanUniversalCharString> data__value__descriptor; //ObjectDescriptor_Type
	TitanOctetString string__value; //OctetString_Type

	public TitanCharacter_String() {
		identification = new TitanCharacter_String_identification();
		data__value__descriptor = new Optional<TitanUniversalCharString>(TitanUniversalCharString.class);
		string__value = new TitanOctetString();
	}

	public TitanCharacter_String( final TitanCharacter_String_identification aIdentification, final Optional<TitanUniversalCharString> aData__value__descriptor, final TitanOctetString aString__value ) {
		identification = new TitanCharacter_String_identification( aIdentification );
		data__value__descriptor = new Optional<TitanUniversalCharString>(TitanUniversalCharString.class);
		this.data__value__descriptor.assign( aData__value__descriptor );
		string__value = new TitanOctetString( aString__value );
	}

	public TitanCharacter_String( final TitanCharacter_String aOtherValue ) {
		this();
		assign( aOtherValue );
	}

	public TitanCharacter_String assign( final TitanCharacter_String aOtherValue ) {
		if ( !aOtherValue.isBound() ) {
			throw new TtcnError( "Assignment of an unbound value of type CHARACTER STRING" );
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
			if ( aOtherValue.getString__value().isBound() ) {
				this.string__value.assign( aOtherValue.getString__value() );
			} else {
				this.string__value.cleanUp();
			}
		}


		return this;
	}

	@Override
	public TitanCharacter_String assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharacter_String ) {
			return assign((TitanCharacter_String) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to CHARACTER STRING", otherValue));
	}

	public void cleanUp() {
		identification.cleanUp();
		data__value__descriptor.cleanUp();
		string__value.cleanUp();
	}

	public boolean isBound() {
		if ( identification.isBound() ) { return true; }
		if ( optional_sel.OPTIONAL_OMIT.equals(data__value__descriptor.getSelection()) || data__value__descriptor.isBound() ) { return true; }
		if ( string__value.isBound() ) { return true; }
		return false;
	}

	public boolean isPresent() {
		return isBound();
	}

	public boolean isValue() {
		if ( !identification.isValue() ) { return false; }
		if ( !optional_sel.OPTIONAL_OMIT.equals(data__value__descriptor.getSelection()) && !data__value__descriptor.isValue() ) { return false; }
		if ( !string__value.isValue() ) { return false; }
		return true;
	}

	public boolean operatorEquals( final TitanCharacter_String aOtherValue ) {
		if ( !this.identification.operatorEquals( aOtherValue.identification ) ) { return false; }
		if ( !this.data__value__descriptor.operatorEquals( aOtherValue.data__value__descriptor ) ) { return false; }
		if ( !this.string__value.operatorEquals( aOtherValue.string__value ) ) { return false; }
		return true;
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharacter_String ) {
			return operatorEquals((TitanCharacter_String) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to CHARACTER STRING", otherValue));
	}

	public TitanCharacter_String_identification getIdentification() {
		return identification;
	}

	public TitanCharacter_String_identification constGetIdentification() {
		return identification;
	}

	public Optional<TitanUniversalCharString> getData__value__descriptor() {
		return data__value__descriptor;
	}

	public Optional<TitanUniversalCharString> constGetData__value__descriptor() {
		return data__value__descriptor;
	}

	public TitanOctetString getString__value() {
		return string__value;
	}

	public TitanOctetString constGetString__value() {
		return string__value;
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
			TtcnLogger.log_event_unbound();
			return;
		}
		TtcnLogger.log_char('{');
		TtcnLogger.log_event_str(" identification := ");
		identification.log();
		TtcnLogger.log_char(',');
		TtcnLogger.log_event_str(" data-value-descriptor := ");
		data__value__descriptor.log();
		TtcnLogger.log_char(',');
		TtcnLogger.log_event_str(" string-value := ");
		string__value.log();
		TtcnLogger.log_event_str(" }");
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		identification.encode_text(text_buf);
		data__value__descriptor.encode_text(text_buf);
		string__value.encode_text(text_buf);
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		identification.decode_text(text_buf);
		data__value__descriptor.decode_text(text_buf);
		string__value.decode_text(text_buf);
	}
}