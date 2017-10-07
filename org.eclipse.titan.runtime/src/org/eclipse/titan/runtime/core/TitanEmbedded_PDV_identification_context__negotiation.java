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
 * Part of the representation of the ASN.1 EMBEDDED PDV type
 * 
 * @author Kristof Szabados
 */
public class TitanEmbedded_PDV_identification_context__negotiation extends Base_Type {
	TitanInteger presentation__context__id; //ASN1_Integer_Type
	TitanObjectid transfer__syntax; //ObjectID_Type

	public TitanEmbedded_PDV_identification_context__negotiation() {
		presentation__context__id = new TitanInteger();
		transfer__syntax = new TitanObjectid();
	}

	public TitanEmbedded_PDV_identification_context__negotiation( final TitanInteger aPresentation__context__id, final TitanObjectid aTransfer__syntax ) {
		presentation__context__id = new TitanInteger( aPresentation__context__id );
		transfer__syntax = new TitanObjectid( aTransfer__syntax );
	}

	public TitanEmbedded_PDV_identification_context__negotiation( final TitanEmbedded_PDV_identification_context__negotiation aOtherValue ) {
		this();
		assign( aOtherValue );
	}

	public TitanEmbedded_PDV_identification_context__negotiation assign( final TitanEmbedded_PDV_identification_context__negotiation aOtherValue ) {
		if ( !aOtherValue.isBound().getValue() ) {
			throw new TtcnError( "Assignment of an unbound value of type EMBEDDED PDV.identification.context-negotiation" );
		}

		if (aOtherValue != this) {
			if ( aOtherValue.getPresentation__context__id().isBound().getValue() ) {
				this.presentation__context__id.assign( aOtherValue.getPresentation__context__id() );
			} else {
				this.presentation__context__id.cleanUp();
			}
			if ( aOtherValue.getTransfer__syntax().isBound().getValue() ) {
				this.transfer__syntax.assign( aOtherValue.getTransfer__syntax() );
			} else {
				this.transfer__syntax.cleanUp();
			}
		}


		return this;
	}

	@Override
	public TitanEmbedded_PDV_identification_context__negotiation assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_context__negotiation ) {
			return assign((TitanEmbedded_PDV_identification_context__negotiation) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to EMBEDDED PDV.identification.context-negotiation", otherValue));
	}

	public void cleanUp() {
		presentation__context__id.cleanUp();
		transfer__syntax.cleanUp();
	}

	public TitanBoolean isBound() {
		if ( presentation__context__id.isBound().getValue() ) return new TitanBoolean(true);
		if ( transfer__syntax.isBound().getValue() ) return new TitanBoolean(true);
		return new TitanBoolean(false);
	}

	public TitanBoolean isPresent() {
		return isBound();
	}

	public TitanBoolean isValue() {
		if ( !presentation__context__id.isValue().getValue() ) return new TitanBoolean(false);
		if ( !transfer__syntax.isValue().getValue() ) return new TitanBoolean(false);
		return new TitanBoolean(true);
	}

	public TitanBoolean operatorEquals( final TitanEmbedded_PDV_identification_context__negotiation aOtherValue ) {
		if ( !TitanBoolean.getNative( this.presentation__context__id.operatorEquals( aOtherValue.presentation__context__id )) ) return new TitanBoolean(false);
		if ( !TitanBoolean.getNative( this.transfer__syntax.operatorEquals( aOtherValue.transfer__syntax )) ) return new TitanBoolean(false);
		return new TitanBoolean(true);
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_context__negotiation ) {
			return operatorEquals((TitanEmbedded_PDV_identification_context__negotiation) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to EMBEDDED PDV.identification.context-negotiation", otherValue));		}

	public TitanInteger getPresentation__context__id() {
		return presentation__context__id;
	}

	public TitanInteger constGetPresentation__context__id() {
		return presentation__context__id;
	}

	public TitanObjectid getTransfer__syntax() {
		return transfer__syntax;
	}

	public TitanObjectid constGetTransfer__syntax() {
		return transfer__syntax;
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
		TtcnLogger.log_event_str(" presentation__context__id := ");
		presentation__context__id.log();
		TtcnLogger.log_char(',');
		TtcnLogger.log_event_str(" transfer__syntax := ");
		transfer__syntax.log();
		TtcnLogger.log_event_str(" }");
	}
}