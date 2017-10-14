/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;




/**
 * Part of the representation of the ASN.1 EMBEDDED PDV type
 * 
 * @author Kristof Szabados
 */
public class TitanEmbedded_PDV_identification extends Base_Type {
	public enum union_selection_type { UNBOUND_VALUE,  ALT_Syntaxes,  ALT_Syntax,  ALT_Presentation__context__id,  ALT_Context__negotiation,  ALT_Transfer__syntax,  ALT_Fixed };
	TitanEmbedded_PDV_identification.union_selection_type union_selection;
	//originally a union which can not be mapped to Java
	Base_Type field;
	public TitanEmbedded_PDV_identification() {
		union_selection = union_selection_type.UNBOUND_VALUE;
	};
	public TitanEmbedded_PDV_identification(final TitanEmbedded_PDV_identification otherValue) {
		copy_value(otherValue);
	};

	private void copy_value(final TitanEmbedded_PDV_identification otherValue) {
		switch(otherValue.union_selection){
		case ALT_Syntaxes:
			field = new TitanEmbedded_PDV_identification_syntaxes((TitanEmbedded_PDV_identification_syntaxes)otherValue.field);
			break;
		case ALT_Syntax:
			field = new TitanObjectid((TitanObjectid)otherValue.field);
			break;
		case ALT_Presentation__context__id:
			field = new TitanInteger((TitanInteger)otherValue.field);
			break;
		case ALT_Context__negotiation:
			field = new TitanEmbedded_PDV_identification_context__negotiation((TitanEmbedded_PDV_identification_context__negotiation)otherValue.field);
			break;
		case ALT_Transfer__syntax:
			field = new TitanObjectid((TitanObjectid)otherValue.field);
			break;
		case ALT_Fixed:
			field = new TitanAsn_Null((TitanAsn_Null)otherValue.field);
			break;
		default:
			throw new TtcnError("Assignment of an unbound union value of type EMBEDDED PDV.identification.");
		}
		union_selection = otherValue.union_selection;
	}

	//originally operator=
	public TitanEmbedded_PDV_identification assign( final TitanEmbedded_PDV_identification otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copy_value(otherValue);
		}

		return this;
	}
	@Override
	public TitanEmbedded_PDV_identification assign( final Base_Type otherValue ) {
		if (otherValue instanceof TitanEmbedded_PDV_identification) {
			return assign((TitanEmbedded_PDV_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to EMBEDDED PDV.identification.");
	}

	//originally clean_up
	public void cleanUp() {
		field = null;
		union_selection = union_selection_type.UNBOUND_VALUE;
	}

	public boolean isChosen(final TitanEmbedded_PDV_identification.union_selection_type checked_selection) {
		if(checked_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError("Internal error: Performing ischosen() operation on an invalid field of union type EMBEDDED PDV.identification.");
		}
		if (union_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError("Performing ischosen() operation on an unbound value of union type EMBEDDED PDV.identification.");
		}
		return union_selection == checked_selection;
	}

	public TitanBoolean isBound() {
		return new TitanBoolean(union_selection != union_selection_type.UNBOUND_VALUE);
	}

	public TitanBoolean isValue() {
		switch(union_selection) {
		case UNBOUND_VALUE: return new TitanBoolean(false);
		case ALT_Syntaxes:
			return field.isValue();
		case ALT_Syntax:
			return field.isValue();
		case ALT_Presentation__context__id:
			return field.isValue();
		case ALT_Context__negotiation:
			return field.isValue();
		case ALT_Transfer__syntax:
			return field.isValue();
		case ALT_Fixed:
			return field.isValue();
		default:
			throw new TtcnError("Invalid selection in union is_bound");
		}
	}

	public TitanBoolean isPresent() {
		return isBound();
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanEmbedded_PDV_identification otherValue ) {
		if (union_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError( "The left operand of comparison is an unbound value of union type EMBEDDED PDV.identification." );
		}
		if (otherValue.union_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError( "The right operand of comparison is an unbound value of union type EMBEDDED PDV.identification." );
		}
		if (union_selection != otherValue.union_selection) {
			return new TitanBoolean(false);
		}
		switch(union_selection) {
		case ALT_Syntaxes:
			return ((TitanEmbedded_PDV_identification_syntaxes)field).operatorEquals((TitanEmbedded_PDV_identification_syntaxes)otherValue.field);
		case ALT_Syntax:
			return ((TitanObjectid)field).operatorEquals((TitanObjectid)otherValue.field);
		case ALT_Presentation__context__id:
			return ((TitanInteger)field).operatorEquals((TitanInteger)otherValue.field);
		case ALT_Context__negotiation:
			return ((TitanEmbedded_PDV_identification_context__negotiation)field).operatorEquals((TitanEmbedded_PDV_identification_context__negotiation)otherValue.field);
		case ALT_Transfer__syntax:
			return ((TitanObjectid)field).operatorEquals((TitanObjectid)otherValue.field);
		case ALT_Fixed:
			return ((TitanAsn_Null)field).operatorEquals((TitanAsn_Null)otherValue.field);
		default:
			return new TitanBoolean(false);
		}
	}
	@Override
	public TitanBoolean operatorEquals( final Base_Type otherValue ) {
		if (otherValue instanceof TitanEmbedded_PDV_identification) {
			return operatorEquals((TitanEmbedded_PDV_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to EMBEDDED PDV.identification.");
	}

	//originally operator!=
	public TitanBoolean operatorNotEquals( final TitanEmbedded_PDV_identification otherValue ) {
		return operatorEquals(otherValue).not();
	}

	public TitanEmbedded_PDV_identification_syntaxes getSyntaxes() {
		if (union_selection != union_selection_type.ALT_Syntaxes) {
			cleanUp();
			field = new TitanEmbedded_PDV_identification_syntaxes();
			union_selection = union_selection_type.ALT_Syntaxes;
		}
		return (TitanEmbedded_PDV_identification_syntaxes)field;
	}

	public TitanEmbedded_PDV_identification_syntaxes constGetSyntaxes() {
		if (union_selection != union_selection_type.ALT_Syntaxes) {
			throw new TtcnError("Using non-selected field syntaxes in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanEmbedded_PDV_identification_syntaxes)field;
	}

	public TitanObjectid getSyntax() {
		if (union_selection != union_selection_type.ALT_Syntax) {
			cleanUp();
			field = new TitanObjectid();
			union_selection = union_selection_type.ALT_Syntax;
		}
		return (TitanObjectid)field;
	}

	public TitanObjectid constGetSyntax() {
		if (union_selection != union_selection_type.ALT_Syntax) {
			throw new TtcnError("Using non-selected field syntax in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanObjectid)field;
	}

	public TitanInteger getPresentation__context__id() {
		if (union_selection != union_selection_type.ALT_Presentation__context__id) {
			cleanUp();
			field = new TitanInteger();
			union_selection = union_selection_type.ALT_Presentation__context__id;
		}
		return (TitanInteger)field;
	}

	public TitanInteger constGetPresentation__context__id() {
		if (union_selection != union_selection_type.ALT_Presentation__context__id) {
			throw new TtcnError("Using non-selected field presentation-context-id in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanInteger)field;
	}

	public TitanEmbedded_PDV_identification_context__negotiation getContext__negotiation() {
		if (union_selection != union_selection_type.ALT_Context__negotiation) {
			cleanUp();
			field = new TitanEmbedded_PDV_identification_context__negotiation();
			union_selection = union_selection_type.ALT_Context__negotiation;
		}
		return (TitanEmbedded_PDV_identification_context__negotiation)field;
	}

	public TitanEmbedded_PDV_identification_context__negotiation constGetContext__negotiation() {
		if (union_selection != union_selection_type.ALT_Context__negotiation) {
			throw new TtcnError("Using non-selected field context-negotiation in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanEmbedded_PDV_identification_context__negotiation)field;
	}

	public TitanObjectid getTransfer__syntax() {
		if (union_selection != union_selection_type.ALT_Transfer__syntax) {
			cleanUp();
			field = new TitanObjectid();
			union_selection = union_selection_type.ALT_Transfer__syntax;
		}
		return (TitanObjectid)field;
	}

	public TitanObjectid constGetTransfer__syntax() {
		if (union_selection != union_selection_type.ALT_Transfer__syntax) {
			throw new TtcnError("Using non-selected field transfer-syntax in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanObjectid)field;
	}

	public TitanAsn_Null getFixed() {
		if (union_selection != union_selection_type.ALT_Fixed) {
			cleanUp();
			field = new TitanAsn_Null();
			union_selection = union_selection_type.ALT_Fixed;
		}
		return (TitanAsn_Null)field;
	}

	public TitanAsn_Null constGetFixed() {
		if (union_selection != union_selection_type.ALT_Fixed) {
			throw new TtcnError("Using non-selected field fixed in a value of union type EMBEDDED PDV.identification.");
		}
		return (TitanAsn_Null)field;
	}

	public TitanEmbedded_PDV_identification.union_selection_type get_selection() {
		return union_selection;
	}
	public void log() {
		switch (union_selection) {
		case ALT_Syntaxes:
			TtcnLogger.log_event_str("{ Syntaxes := ");
			field.log();
			TtcnLogger.log_event_str(" }");
			break;
		case ALT_Syntax:
			TtcnLogger.log_event_str("{ Syntax := ");
			field.log();
			TtcnLogger.log_event_str(" }");
			break;
		case ALT_Presentation__context__id:
			TtcnLogger.log_event_str("{ Presentation__context__id := ");
			field.log();
			TtcnLogger.log_event_str(" }");
			break;
		case ALT_Context__negotiation:
			TtcnLogger.log_event_str("{ Context__negotiation := ");
			field.log();
			TtcnLogger.log_event_str(" }");
			break;
		case ALT_Transfer__syntax:
			TtcnLogger.log_event_str("{ Transfer__syntax := ");
			field.log();
			TtcnLogger.log_event_str(" }");
			break;
		case ALT_Fixed:
			TtcnLogger.log_event_str("{ Fixed := ");
			field.log();
			TtcnLogger.log_event_str(" }");
			break;
		default:
			TtcnLogger.log_event_unbound();
			break;
		}
	}
}