/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

/**
 * Part of the representation of the ASN.1 unrestricted string (CHARACTER STRING) type.
 * 
 * @author Kristof Szabados
 */
public class TitanCharacter_String_identification_syntaxes extends Base_Type {
	TitanObjectid abstract_; //ObjectID_Type
	TitanObjectid transfer; //ObjectID_Type

	public TitanCharacter_String_identification_syntaxes() {
		abstract_ = new TitanObjectid();
		transfer = new TitanObjectid();
	}

	public TitanCharacter_String_identification_syntaxes( final TitanObjectid aAbstract_, final TitanObjectid aTransfer ) {
		abstract_ = new TitanObjectid( aAbstract_ );
		transfer = new TitanObjectid( aTransfer );
	}

	public TitanCharacter_String_identification_syntaxes( final TitanCharacter_String_identification_syntaxes aOtherValue ) {
		this();
		assign( aOtherValue );
	}

	public TitanCharacter_String_identification_syntaxes assign( final TitanCharacter_String_identification_syntaxes aOtherValue ) {
		if ( !aOtherValue.isBound().getValue() ) {
			throw new TtcnError( "Assignment of an unbound value of type CHARACTER STRING.identification.syntaxes" );
		}

		if (aOtherValue != this) {
			if ( aOtherValue.getAbstract_().isBound().getValue() ) {
				this.abstract_.assign( aOtherValue.getAbstract_() );
			} else {
				this.abstract_.cleanUp();
			}
			if ( aOtherValue.getTransfer().isBound().getValue() ) {
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

	public TitanBoolean isBound() {
		if ( abstract_.isBound().getValue() ) { return new TitanBoolean(true); }
		if ( transfer.isBound().getValue() ) { return new TitanBoolean(true); }
		return new TitanBoolean(false);
	}

	public TitanBoolean isPresent() {
		return isBound();
	}

	public TitanBoolean isValue() {
		if ( !abstract_.isValue().getValue() ) { return new TitanBoolean(false); }
		if ( !transfer.isValue().getValue() ) { return new TitanBoolean(false); }
		return new TitanBoolean(true);
	}

	public TitanBoolean operatorEquals( final TitanCharacter_String_identification_syntaxes aOtherValue ) {
		if ( !TitanBoolean.getNative( this.abstract_.operatorEquals( aOtherValue.abstract_ )) ) { return new TitanBoolean(false); }
		if ( !TitanBoolean.getNative( this.transfer.operatorEquals( aOtherValue.transfer )) ) { return new TitanBoolean(false); }
		return new TitanBoolean(true);
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharacter_String_identification_syntaxes ) {
			return operatorEquals((TitanCharacter_String_identification_syntaxes) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to CHARACTER STRING.identification.syntaxes", otherValue));		}

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
		int sizeof = 0;
		sizeof += 2;
		return new TitanInteger(sizeof);
	}
	public void log() {
		if (!isBound().getValue()) {
			TtcnLogger.log_event_unbound();
			return;
		}
		TtcnLogger.log_char('{');
		TtcnLogger.log_event_str(" abstract := ");
		abstract_.log();
		TtcnLogger.log_char(',');
		TtcnLogger.log_event_str(" transfer := ");
		transfer.log();
		TtcnLogger.log_event_str(" }");
	}
}