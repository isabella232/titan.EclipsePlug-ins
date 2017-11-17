/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;


/**
 * Part of the representation of the ASN.1 EXTERNAL type
 * 
 * @author Kristof Szabados
 */
public class TitanExternal_identification extends Base_Type {
	public enum union_selection_type { UNBOUND_VALUE,  ALT_Syntaxes,  ALT_Syntax,  ALT_Presentation__context__id,  ALT_Context__negotiation,  ALT_Transfer__syntax,  ALT_Fixed };
	TitanExternal_identification.union_selection_type union_selection;
	//originally a union which can not be mapped to Java
	Base_Type field;
	public TitanExternal_identification() {
		union_selection = union_selection_type.UNBOUND_VALUE;
	};
	public TitanExternal_identification(final TitanExternal_identification otherValue) {
		copy_value(otherValue);
	};

	private void copy_value(final TitanExternal_identification otherValue) {
		switch(otherValue.union_selection){
		case ALT_Syntaxes:
			field = new TitanExternal_identification_syntaxes((TitanExternal_identification_syntaxes)otherValue.field);
			break;
		case ALT_Syntax:
			field = new TitanObjectid((TitanObjectid)otherValue.field);
			break;
		case ALT_Presentation__context__id:
			field = new TitanInteger((TitanInteger)otherValue.field);
			break;
		case ALT_Context__negotiation:
			field = new TitanExternal_identification_context__negotiation((TitanExternal_identification_context__negotiation)otherValue.field);
			break;
		case ALT_Transfer__syntax:
			field = new TitanObjectid((TitanObjectid)otherValue.field);
			break;
		case ALT_Fixed:
			field = new TitanAsn_Null((TitanAsn_Null)otherValue.field);
			break;
		default:
			throw new TtcnError("Assignment of an unbound union value of type EXTERNAL.identification.");
		}
		union_selection = otherValue.union_selection;
	}

	//originally operator=
	public TitanExternal_identification assign( final TitanExternal_identification otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copy_value(otherValue);
		}

		return this;
	}
	@Override
	public TitanExternal_identification assign( final Base_Type otherValue ) {
		if (otherValue instanceof TitanExternal_identification) {
			return assign((TitanExternal_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to EXTERNAL.identification.");
	}

	//originally clean_up
	public void cleanUp() {
		field = null;
		union_selection = union_selection_type.UNBOUND_VALUE;
	}

	public boolean isChosen(final TitanExternal_identification.union_selection_type checked_selection) {
		if(checked_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError("Internal error: Performing ischosen() operation on an invalid field of union type EXTERNAL.identification.");
		}
		if (union_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError("Performing ischosen() operation on an unbound value of union type EXTERNAL.identification.");
		}
		return union_selection == checked_selection;
	}

	public boolean isBound() {
		return union_selection != union_selection_type.UNBOUND_VALUE;
	}

	public boolean isValue() {
		switch(union_selection) {
		case UNBOUND_VALUE: return false;
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

	public boolean isPresent() {
		return isBound();
	}

	//originally operator==
	public boolean operatorEquals( final TitanExternal_identification otherValue ) {
		if (union_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError( "The left operand of comparison is an unbound value of union type EXTERNAL.identification." );
		}
		if (otherValue.union_selection == union_selection_type.UNBOUND_VALUE) {
			throw new TtcnError( "The right operand of comparison is an unbound value of union type EXTERNAL.identification." );
		}
		if (union_selection != otherValue.union_selection) {
			return false;
		}
		switch(union_selection) {
		case ALT_Syntaxes:
			return ((TitanExternal_identification_syntaxes)field).operatorEquals((TitanExternal_identification_syntaxes)otherValue.field);
		case ALT_Syntax:
			return ((TitanObjectid)field).operatorEquals((TitanObjectid)otherValue.field);
		case ALT_Presentation__context__id:
			return ((TitanInteger)field).operatorEquals((TitanInteger)otherValue.field);
		case ALT_Context__negotiation:
			return ((TitanExternal_identification_context__negotiation)field).operatorEquals((TitanExternal_identification_context__negotiation)otherValue.field);
		case ALT_Transfer__syntax:
			return ((TitanObjectid)field).operatorEquals((TitanObjectid)otherValue.field);
		case ALT_Fixed:
			return ((TitanAsn_Null)field).operatorEquals((TitanAsn_Null)otherValue.field);
		default:
			return false;
		}
	}
	@Override
	public boolean operatorEquals( final Base_Type otherValue ) {
		if (otherValue instanceof TitanExternal_identification) {
			return operatorEquals((TitanExternal_identification)otherValue);
		}
		throw new TtcnError("Internal Error: value can not be cast to EXTERNAL.identification.");
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanExternal_identification otherValue ) {
		return !operatorEquals(otherValue);
	}

	public TitanExternal_identification_syntaxes getSyntaxes() {
		if (union_selection != union_selection_type.ALT_Syntaxes) {
			cleanUp();
			field = new TitanExternal_identification_syntaxes();
			union_selection = union_selection_type.ALT_Syntaxes;
		}
		return (TitanExternal_identification_syntaxes)field;
	}

	public TitanExternal_identification_syntaxes constGetSyntaxes() {
		if (union_selection != union_selection_type.ALT_Syntaxes) {
			throw new TtcnError("Using non-selected field syntaxes in a value of union type EXTERNAL.identification.");
		}
		return (TitanExternal_identification_syntaxes)field;
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
			throw new TtcnError("Using non-selected field syntax in a value of union type EXTERNAL.identification.");
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
			throw new TtcnError("Using non-selected field presentation-context-id in a value of union type EXTERNAL.identification.");
		}
		return (TitanInteger)field;
	}

	public TitanExternal_identification_context__negotiation getContext__negotiation() {
		if (union_selection != union_selection_type.ALT_Context__negotiation) {
			cleanUp();
			field = new TitanExternal_identification_context__negotiation();
			union_selection = union_selection_type.ALT_Context__negotiation;
		}
		return (TitanExternal_identification_context__negotiation)field;
	}

	public TitanExternal_identification_context__negotiation constGetContext__negotiation() {
		if (union_selection != union_selection_type.ALT_Context__negotiation) {
			throw new TtcnError("Using non-selected field context-negotiation in a value of union type EXTERNAL.identification.");
		}
		return (TitanExternal_identification_context__negotiation)field;
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
			throw new TtcnError("Using non-selected field transfer-syntax in a value of union type EXTERNAL.identification.");
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
			throw new TtcnError("Using non-selected field fixed in a value of union type EXTERNAL.identification.");
		}
		return (TitanAsn_Null)field;
	}

	public TitanExternal_identification.union_selection_type get_selection() {
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

	@Override
	public void encode_text(final Text_Buf text_buf) {
		switch (union_selection) {
		case ALT_Syntaxes:
			text_buf.push_int(0);
			break;
		case ALT_Syntax:
			text_buf.push_int(1);
			break;
		case ALT_Presentation__context__id:
			text_buf.push_int(2);
			break;
		case ALT_Context__negotiation:
			text_buf.push_int(3);
			break;
		case ALT_Transfer__syntax:
			text_buf.push_int(4);
			break;
		case ALT_Fixed:
			text_buf.push_int(5);
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an unbound value of union type EXTERNAL.identification.");
		}
		field.encode_text(text_buf);
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		final int temp = text_buf.pull_int().getInt();
		switch (temp) {
		case 0:
			getSyntaxes().decode_text(text_buf);
			break;
		case 1:
			getSyntax().decode_text(text_buf);
			break;
		case 2:
			getPresentation__context__id().decode_text(text_buf);
			break;
		case 3:
			getContext__negotiation().decode_text(text_buf);
			break;
		case 4:
			getTransfer__syntax().decode_text(text_buf);
			break;
		case 5:
			getFixed().decode_text(text_buf);
			break;
		default:
			throw new TtcnError("Text decoder: Unrecognized union selector was received for type EXTERNAL.identification.");
		}
	}
}