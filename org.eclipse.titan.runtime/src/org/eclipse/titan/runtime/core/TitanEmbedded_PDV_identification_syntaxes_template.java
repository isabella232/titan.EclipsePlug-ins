/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;

/**
 * Part of the representation of the ASN.1 EMBEDDED PDV type
 *
 * @author Kristof Szabados
 */
public class TitanEmbedded_PDV_identification_syntaxes_template extends Base_Template {
	private TitanObjectid_template abstract_; //ObjectID_Type
	private TitanObjectid_template transfer; //ObjectID_Type
	//originally value_list/list_value
	private List<TitanEmbedded_PDV_identification_syntaxes_template> list_value;

	/**
	 * Gives access to the field abstract.
	 * Turning the template into a specific value template if needed.
	 *
	 * @return the field abstract.
	 * */
	public TitanObjectid_template getabstract_() {
		setSpecific();
		return abstract_;
	}

	/**
	 * Gives read-only access to the field abstract.
	 * Being called on a non specific value template causes dynamic test case error.
	 *
	 * @return the field abstract.
	 * */
	public TitanObjectid_template constGetabstract_() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field abstract of a non-specific template of type EMBEDDED PDV.identification.syntaxes.");
		}
		return abstract_;
	}

	/**
	 * Gives access to the field transfer.
	 * Turning the template into a specific value template if needed.
	 *
	 * @return the field transfer.
	 * */
	public TitanObjectid_template gettransfer() {
		setSpecific();
		return transfer;
	}

	/**
	 * Gives read-only access to the field transfer.
	 * Being called on a non specific value template causes dynamic test case error.
	 *
	 * @return the field transfer.
	 * */
	public TitanObjectid_template constGettransfer() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing field transfer of a non-specific template of type EMBEDDED PDV.identification.syntaxes.");
		}
		return transfer;
	}

	private void setSpecific() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			final template_sel old_selection = templateSelection;
			cleanUp();
			set_selection(template_sel.SPECIFIC_VALUE);
			abstract_ = new TitanObjectid_template();
			transfer = new TitanObjectid_template();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {
				abstract_.assign(template_sel.ANY_VALUE);
				transfer.assign(template_sel.ANY_VALUE);
			}
		}
	}

	public TitanEmbedded_PDV_identification_syntaxes_template() {
		// do nothing
	}

	public TitanEmbedded_PDV_identification_syntaxes_template(final template_sel other_value ) {
		super( other_value );
		checkSingleSelection( other_value );
	}

	public TitanEmbedded_PDV_identification_syntaxes_template( final TitanEmbedded_PDV_identification_syntaxes otherValue ) {
		copyValue(otherValue);
	}

	public TitanEmbedded_PDV_identification_syntaxes_template( final TitanEmbedded_PDV_identification_syntaxes_template otherValue ) {
		copyTemplate( otherValue );
	}

	public TitanEmbedded_PDV_identification_syntaxes_template( final Optional<TitanEmbedded_PDV_identification_syntaxes> other_value ) {
		switch (other_value.get_selection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Creating a template of type EMBEDDED PDV.identification.syntaxes from an unbound optional field.");
		}
	}

	//originally operator=
	public TitanEmbedded_PDV_identification_syntaxes_template assign( final template_sel other_value ) {
		checkSingleSelection(other_value);
		cleanUp();
		set_selection(other_value);
		return this;
	}

	//originally operator=
	public TitanEmbedded_PDV_identification_syntaxes_template assign( final TitanEmbedded_PDV_identification_syntaxes other_value ) {
		cleanUp();
		copyValue(other_value);
		return this;
	}

	//originally operator=
	public TitanEmbedded_PDV_identification_syntaxes_template assign( final TitanEmbedded_PDV_identification_syntaxes_template other_value ) {
		if (other_value != this) {
			cleanUp();
			copyTemplate(other_value);
		}
		return this;
	}

	@Override
	public TitanEmbedded_PDV_identification_syntaxes_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_syntaxes) {
			return assign((TitanEmbedded_PDV_identification_syntaxes) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanEmbedded_PDV_identification_syntaxes' can not be cast to {1}", otherValue));
	}

	@Override
	public TitanEmbedded_PDV_identification_syntaxes_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_syntaxes_template) {
			return assign((TitanEmbedded_PDV_identification_syntaxes_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `TitanEmbedded_PDV_identification_syntaxes' can not be cast to {1}_template", otherValue));
	}

	public TitanEmbedded_PDV_identification_syntaxes_template assign( final Optional<TitanEmbedded_PDV_identification_syntaxes> other_value ) {
		cleanUp();
		switch (other_value.get_selection()) {
		case OPTIONAL_PRESENT:
			copyValue(other_value.constGet());
			break;
		case OPTIONAL_OMIT:
			set_selection(template_sel.OMIT_VALUE);
			break;
		default:
			throw new TtcnError("Assignment of an unbound optional field to a template of type EMBEDDED PDV.identification.syntaxes.");
		}
		return this;
	}

	private void copyValue(final TitanEmbedded_PDV_identification_syntaxes other_value) {
		if (other_value.getabstract_().isBound()) {
			getabstract_().assign(other_value.getabstract_());
		} else {
			getabstract_().cleanUp();
		}
		if (other_value.gettransfer().isBound()) {
			gettransfer().assign(other_value.gettransfer());
		} else {
			gettransfer().cleanUp();
		}
		set_selection(template_sel.SPECIFIC_VALUE);
	}

	private void copyTemplate(final TitanEmbedded_PDV_identification_syntaxes_template other_value) {
		switch (other_value.templateSelection) {
		case SPECIFIC_VALUE:
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.getabstract_().get_selection()) {
				getabstract_().cleanUp();
			} else {
				getabstract_().assign(other_value.getabstract_());
			}
			if (template_sel.UNINITIALIZED_TEMPLATE == other_value.gettransfer().get_selection()) {
				gettransfer().cleanUp();
			} else {
				gettransfer().assign(other_value.gettransfer());
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			list_value = new ArrayList<TitanEmbedded_PDV_identification_syntaxes_template>(other_value.list_value.size());
			for(int i = 0; i < other_value.list_value.size(); i++) {
				final TitanEmbedded_PDV_identification_syntaxes_template temp = new TitanEmbedded_PDV_identification_syntaxes_template(other_value.list_value.get(i));
				list_value.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized template of type EMBEDDED PDV.identification.syntaxes.");
		}
		set_selection(other_value);
	}

	public boolean isPresent(final boolean legacy) {
		return isPresent_(legacy);
	}

	private boolean isPresent_(final boolean legacy) {
		if (templateSelection==template_sel.UNINITIALIZED_TEMPLATE) {
			return false;
		}
		return !match_omit_(legacy);
	}

	public boolean match_omit(final boolean legacy) {
		return match_omit_(legacy);
	}

	private boolean match_omit_(final boolean legacy) {
		if (is_ifPresent) {
			return true;
		}
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				for (int l_idx=0; l_idx<list_value.size(); l_idx++) {
					if (list_value.get(l_idx).match_omit_(legacy)) {
						return templateSelection==template_sel.VALUE_LIST;
					}
				}
				return templateSelection==template_sel.COMPLEMENTED_LIST;
			} // else fall through
		default:
			return false;
		}
	}

	public TitanEmbedded_PDV_identification_syntaxes valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific template of type EMBEDDED PDV.identification.syntaxes.");
		}
		final TitanEmbedded_PDV_identification_syntaxes ret_val = new TitanEmbedded_PDV_identification_syntaxes();
		if (abstract_.isBound()) {
			ret_val.getabstract_().assign(abstract_.valueOf());
		}
		if (transfer.isBound()) {
			ret_val.gettransfer().assign(transfer.valueOf());
		}
		return ret_val;
	}

	public TitanEmbedded_PDV_identification_syntaxes_template listItem(final int list_index) {
		if (templateSelection != template_sel.VALUE_LIST && templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list template of type EMBEDDED PDV.identification.syntaxes.");
		}
		if (list_index >= list_value.size()) {
			throw new TtcnError("Index overflow in a value list template of type EMBEDDED PDV.identification.syntaxes.");
		}
		return list_value.get(list_index);
	}

	public void setType(final template_sel template_type, final int list_length) {
		if (template_type != template_sel.VALUE_LIST && template_type != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list for a template of type EMBEDDED PDV.identification.syntaxes.");
		}
		cleanUp();
		set_selection(template_type);
		list_value = new ArrayList<TitanEmbedded_PDV_identification_syntaxes_template>(list_length);
		for(int i = 0 ; i < list_length; i++) {
			list_value.add(new TitanEmbedded_PDV_identification_syntaxes_template());
		}
	}

	@Override
	public boolean isBound() {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE && !is_ifPresent) {
			return false;
		}
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			return true;
		}
		if (abstract_.isBound()) {
			return true;
		}
		if (transfer.isBound()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return false;
		}
		if (!abstract_.isValue()) {
			return false;
		}
		if (!transfer.isValue()) {
			return false;
		}
		return true;
	}

	public boolean match(final TitanEmbedded_PDV_identification_syntaxes other_value) {
		return match(other_value, false);
	}

	public boolean match(final TitanEmbedded_PDV_identification_syntaxes other_value, final boolean legacy) {
		if (!other_value.isBound()) {
			return false;
		}
		switch (templateSelection) {
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return true;
		case OMIT_VALUE:
			return false;
		case SPECIFIC_VALUE:
			if(!other_value.getabstract_().isBound()) {
				return false;
			}
			if(!abstract_.match(other_value.getabstract_(), legacy)) {
				return false;
			}
			if(!other_value.gettransfer().isBound()) {
				return false;
			}
			if(!transfer.match(other_value.gettransfer(), legacy)) {
				return false;
			}
			return true;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int list_count = 0; list_count < list_value.size(); list_count++) {
				if (list_value.get(list_count).match(other_value, legacy)) {
					return templateSelection == template_sel.VALUE_LIST;
				}
			}
			return templateSelection == template_sel.COMPLEMENTED_LIST;
		default:
			throw new TtcnError("Matching an uninitialized/unsupported template of type EMBEDDED PDV.identification.syntaxes.");
		}
	}

	@Override
	public boolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanEmbedded_PDV_identification_syntaxes) {
			return match((TitanEmbedded_PDV_identification_syntaxes)otherValue, legacy);
		}

		throw new TtcnError("Internal Error: The left operand of assignment is not of type TitanEmbedded_PDV_identification_syntaxes.");
	}

	public TitanInteger sizeOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.syntaxes which has an ifpresent attribute.");
		}
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return new TitanInteger(2);
		case VALUE_LIST:
			if (list_value.isEmpty()) {
				throw new TtcnError("Internal error: Performing sizeof() operation on a template of type EMBEDDED PDV.identification.syntaxes containing an empty list.");
			}
			final int item_size = list_value.get(0).sizeOf().getInt();
			for (int l_idx = 1; l_idx < list_value.size(); l_idx++) {
				if (list_value.get(l_idx).sizeOf().getInt() != item_size) {
					throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.syntaxes containing a value list with different sizes.");
				}
			}
			return new TitanInteger(item_size);
		case OMIT_VALUE:
			throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.syntaxes containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.syntaxes containing */? value.");
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing sizeof() operation on a template of type EMBEDDED PDV.identification.syntaxes containing complemented list.");
		default:
			throw new TtcnError("Performing sizeof() operation on an uninitialized/unsupported template of type EMBEDDED PDV.identification.syntaxes.");
		}
	}

	@Override
	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			TTCN_Logger.log_char('{');
			TTCN_Logger.log_event_str(" abstract := ");
			abstract_.log();
			TTCN_Logger.log_char(',');
			TTCN_Logger.log_event_str(" transfer := ");
			transfer.log();
			TTCN_Logger.log_event_str(" }");
			break;
		case COMPLEMENTED_LIST:
			TTCN_Logger.log_event_str("complement");
		case VALUE_LIST:
			TTCN_Logger.log_char('(');
			for (int list_count = 0; list_count < list_value.size(); list_count++) {
				if (list_count > 0) {
					TTCN_Logger.log_event_str(", ");
				}
				list_value.get(list_count).log();
			}
			TTCN_Logger.log_char(')');
			break;
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}

	public void log_match(final TitanEmbedded_PDV_identification_syntaxes match_value) {
		log_match(match_value, false);
	}

	@Override
	public void log_match(final Base_Type match_value, final boolean legacy) {
		if (match_value instanceof TitanEmbedded_PDV_identification_syntaxes) {
			log_match((TitanEmbedded_PDV_identification_syntaxes)match_value, legacy);
			return;
		}

		throw new TtcnError("Internal Error: value can not be cast to EMBEDDED PDV.identification.syntaxes.");
	}

	public void log_match(final TitanEmbedded_PDV_identification_syntaxes match_value, final boolean legacy) {
		if ( TTCN_Logger.matching_verbosity_t.VERBOSITY_COMPACT == TTCN_Logger.get_matching_verbosity() ) {
			if(match(match_value, legacy)) {
				TTCN_Logger.print_logmatch_buffer();
				TTCN_Logger.log_event_str(" matched");
			} else {
				if (templateSelection == template_sel.SPECIFIC_VALUE) {
					final int previous_size = TTCN_Logger.get_logmatch_buffer_len();
					if( !abstract_.match(match_value.constGetabstract_(), legacy) ) {
						TTCN_Logger.log_logmatch_info(".abstract");
						abstract_.log_match(match_value.constGetabstract_(), legacy);
						TTCN_Logger.set_logmatch_buffer_len(previous_size);
					}
					if( !transfer.match(match_value.constGettransfer(), legacy) ) {
						TTCN_Logger.log_logmatch_info(".transfer");
						transfer.log_match(match_value.constGettransfer(), legacy);
						TTCN_Logger.set_logmatch_buffer_len(previous_size);
					}
				} else {
					TTCN_Logger.print_logmatch_buffer();
					match_value.log();
					TTCN_Logger.log_event_str(" with ");
					log();
					TTCN_Logger.log_event_str(" unmatched");
				}
			}
			return;
		}
		if (templateSelection == template_sel.SPECIFIC_VALUE) {
			TTCN_Logger.log_event_str("{ abstract := ");
			abstract_.log_match(match_value.constGetabstract_(), legacy);
			TTCN_Logger.log_event_str("{ transfer := ");
			transfer.log_match(match_value.constGettransfer(), legacy);
			TTCN_Logger.log_event_str(" }");
		} else {
			match_value.log();
			TTCN_Logger.log_event_str(" with ");
			log();
			if ( match(match_value, legacy) ) {
				TTCN_Logger.log_event_str(" matched");
			} else {
				TTCN_Logger.log_event_str(" unmatched");
			}
		}
	}

	@Override
	public void encode_text(final Text_Buf text_buf) {
		encode_text_base(text_buf);
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			abstract_.encode_text(text_buf);
			transfer.encode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			text_buf.push_int(list_value.size());
			for (int i = 0; i < list_value.size(); i++) {
				list_value.get(i).encode_text(text_buf);
			}
			break;
		default:
			throw new TtcnError("Text encoder: Encoding an uninitialized/unsupported template of type EMBEDDED PDV.identification.syntaxes.");
		}
	}

	@Override
	public void decode_text(final Text_Buf text_buf) {
		cleanUp();
		decode_text_base(text_buf);
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case SPECIFIC_VALUE:
			abstract_ = new TitanObjectid_template();
			abstract_.decode_text(text_buf);
			transfer = new TitanObjectid_template();
			transfer.decode_text(text_buf);
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST: {
			final int size = text_buf.pull_int().getInt();
			list_value = new ArrayList<TitanEmbedded_PDV_identification_syntaxes_template>(size);
			for (int i = 0; i < size; i++) {
				final TitanEmbedded_PDV_identification_syntaxes_template temp = new TitanEmbedded_PDV_identification_syntaxes_template();
				temp.decode_text(text_buf);
				list_value.add(temp);
			}
			break;
		}
		default:
			throw new TtcnError("Text decoder: An unknown/unsupported selection was received in a template of type EMBEDDED PDV.identification.syntaxes.");
		}
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(Module_Parameter.basic_check_bits_t.BC_TEMPLATE.getValue(), "record template");
		switch (param.get_type()) {
		case MP_Omit:
			assign(template_sel.OMIT_VALUE);
			break;
		case MP_Any:
			assign(template_sel.ANY_VALUE);
			break;
		case MP_AnyOrNone:
			assign(template_sel.ANY_OR_OMIT);
			break;
		case MP_List_Template:
		case MP_ComplementList_Template: {
			final int size = param.get_size();
			setType(param.get_type() == Module_Parameter.type_t.MP_List_Template ? template_sel.VALUE_LIST : template_sel.COMPLEMENTED_LIST, size);
			for (int i = 0; i < size; i++) {
				listItem(i).set_param(param.get_elem(i));
			}
			break;
		}
		case MP_Value_List:
			if (param.get_size() > 2) {
				param.error(MessageFormat.format("record template of type EMBEDDED PDV.identification.syntaxes has 2 fields but list value has {0} fields.", param.get_size()));
			}
			if (param.get_size() > 0 && param.get_elem(0).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				getabstract_().set_param(param.get_elem(0));
			}
			if (param.get_size() > 1 && param.get_elem(1).get_type() != Module_Parameter.type_t.MP_NotUsed) {
				gettransfer().set_param(param.get_elem(1));
			}
			break;
		case MP_Assignment_List: {
			final boolean value_used[] = new boolean[param.get_size()];
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				final Module_Parameter curr_param = param.get_elem(val_idx);
				if ("abstract".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						getabstract_().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				final Module_Parameter curr_param = param.get_elem(val_idx);
				if ("transfer".equals(curr_param.get_id().get_name())) {
					if (curr_param.get_type() != Module_Parameter.type_t.MP_NotUsed) {
						gettransfer().set_param(curr_param);
					}
					value_used[val_idx] = true;
				}
			}
			for (int val_idx = 0; val_idx < param.get_size(); val_idx++) {
				if (!value_used[val_idx]) {
					final Module_Parameter curr_param = param.get_elem(val_idx);
					curr_param.error(MessageFormat.format("Non existent field name in type EMBEDDED PDV.identification.syntaxes: {0}", curr_param.get_id().get_name()));
					break;
				}
			}
			break;
		}
		default:
			param.type_error("record template", "EMBEDDED PDV.identification.syntaxes");
		}
		is_ifPresent = param.get_ifpresent();
	}

	@Override
	public void check_restriction(final template_res restriction, final String name, final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return;
		}
		switch ((name != null && restriction == template_res.TR_VALUE) ? template_res.TR_OMIT : restriction) {
		case TR_OMIT:
			if (templateSelection == template_sel.OMIT_VALUE) {
				return;
			}
		case TR_VALUE:
			if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
				break;
			}
			this.abstract_.check_restriction(restriction, name == null ? "EMBEDDED PDV.identification.syntaxes" : name, legacy);
			this.transfer.check_restriction(restriction, name == null ? "EMBEDDED PDV.identification.syntaxes" : name, legacy);
			return;
		case TR_PRESENT:
			if (!match_omit(legacy)) {
				return;
			}
			break;
		default:
			return;
		}
		throw new TtcnError(MessageFormat.format("Restriction `{0}'' on template of type {1} violated.", getResName(restriction), name == null ? "EMBEDDED PDV.identification.syntaxes" : name));
	}
}