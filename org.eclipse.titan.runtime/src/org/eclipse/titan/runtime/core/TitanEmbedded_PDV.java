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
 * Part of the representation of the ASN.1 EMBEDDED PDV type
 * 
 * @author Kristof Szabados
 */
public class TitanEmbedded_PDV extends Base_Type {
	TitanEmbedded_PDV_identification identification; //ASN1_Choice_Type
	Optional<TitanUniversalCharString> data__value__descriptor; //ObjectDescriptor_Type
	TitanOctetString data__value; //OctetString_Type

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
		this();
		assign( aOtherValue );
	}

	public TitanEmbedded_PDV assign( final TitanEmbedded_PDV aOtherValue ) {
		if ( !aOtherValue.isBound().getValue() ) {
			throw new TtcnError( "Assignment of an unbound value of type EMBEDDED PDV" );
		}

		if (aOtherValue != this) {
			if ( aOtherValue.getIdentification().isBound().getValue() ) {
				this.identification.assign( aOtherValue.getIdentification() );
			} else {
				this.identification.cleanUp();
			}
			if ( aOtherValue.getData__value__descriptor().isBound().getValue() ) {
				this.data__value__descriptor.assign( aOtherValue.getData__value__descriptor() );
			} else {
				this.data__value__descriptor.cleanUp();
			}
			if ( aOtherValue.getData__value().isBound().getValue() ) {
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

	public TitanBoolean isBound() {
		if ( identification.isBound().getValue() ) { return new TitanBoolean(true); }
		if ( optional_sel.OPTIONAL_OMIT.equals(data__value__descriptor.getSelection()) || data__value__descriptor.isBound().getValue() ) { return new TitanBoolean(true); }
		if ( data__value.isBound().getValue() ) { return new TitanBoolean(true); }
		return new TitanBoolean(false);
	}

	public TitanBoolean isPresent() {
		return isBound();
	}

	public TitanBoolean isValue() {
		if ( !identification.isValue().getValue() ) { return new TitanBoolean(false); }
		if ( !optional_sel.OPTIONAL_OMIT.equals(data__value__descriptor.getSelection()) && !data__value__descriptor.isValue().getValue() ) { return new TitanBoolean(false); }
		if ( !data__value.isValue().getValue() ) { return new TitanBoolean(false); }
		return new TitanBoolean(true);
	}

	public TitanBoolean operatorEquals( final TitanEmbedded_PDV aOtherValue ) {
		if ( !TitanBoolean.getNative( this.identification.operatorEquals( aOtherValue.identification )) ) { return new TitanBoolean(false); }
		if ( !TitanBoolean.getNative( this.data__value__descriptor.operatorEquals( aOtherValue.data__value__descriptor )) ) { return new TitanBoolean(false); }
		if ( !TitanBoolean.getNative( this.data__value.operatorEquals( aOtherValue.data__value )) ) { return new TitanBoolean(false); }
		return new TitanBoolean(true);
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV ) {
			return operatorEquals((TitanEmbedded_PDV) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to EMBEDDED PDV", otherValue));		}

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
		if (data__value__descriptor.isPresent().getValue()) {
			sizeof++;
		}
		sizeof += 2;
		return new TitanInteger(sizeof);
	}
	public void log() {
		if (!isBound().getValue()) {
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
		TtcnLogger.log_event_str(" data-value := ");
		data__value.log();
		TtcnLogger.log_event_str(" }");
	}
}