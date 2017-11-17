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
 * Restricted_Length_Template in titan.core
 * 
 * @author Arpad Lovassy
 */
public abstract class Restricted_Length_Template extends Base_Template {

	public enum length_restriction_type_t {
		NO_LENGTH_RESTRICTION,
		SINGLE_LENGTH_RESTRICTION,
		RANGE_LENGTH_RESTRICTION
	}

	public length_restriction_type_t length_restriction_type;

	// originally in union length_restriction
	protected int single_length;

	protected int range_length_min_length;
	protected int range_length_max_length;
	protected boolean range_length_max_length_set;

	public Restricted_Length_Template() {
		length_restriction_type = length_restriction_type_t.NO_LENGTH_RESTRICTION;
	}

	public Restricted_Length_Template(final template_sel other_value) {
		super(other_value);
		length_restriction_type = length_restriction_type_t.NO_LENGTH_RESTRICTION;
	}

	@Override
	protected void setSelection(final template_sel other_value) {
		templateSelection = other_value;
		is_ifPresent = false;
		length_restriction_type = length_restriction_type_t.NO_LENGTH_RESTRICTION;
	}

	protected void setSelection(final Restricted_Length_Template other_value) {
		templateSelection = other_value.templateSelection;
		is_ifPresent = other_value.is_ifPresent;
		length_restriction_type = other_value.length_restriction_type;

		// orginally length_restriction = other_value.length_restriction;
		single_length = other_value.single_length;
		range_length_min_length = other_value.range_length_min_length;
		range_length_max_length = other_value.range_length_max_length;
		range_length_max_length_set = other_value.range_length_max_length_set;
	}

	public boolean match_length(final int value_length) {
		switch (length_restriction_type) {
		case NO_LENGTH_RESTRICTION:
			return true;
		case SINGLE_LENGTH_RESTRICTION:
			return value_length == single_length;
		case RANGE_LENGTH_RESTRICTION:
			return value_length >= range_length_min_length && (!range_length_max_length_set || value_length <= range_length_max_length);
		default:
			throw new TtcnError("Internal error: Matching with a template that has invalid length restriction type.");
		}
	}

	//TODO: implement according to:
	//      Template.hh: class Restricted_Length_Template : public Base_Template

	protected int check_section_is_single(final int min_size, final boolean has_any_or_none, final String operation_name, final String type_name_prefix,
			final String type_name) {
		if (has_any_or_none) {
			// upper limit is infinity
			switch (length_restriction_type) {
			case NO_LENGTH_RESTRICTION:
				throw new TtcnError( MessageFormat.format( "Performing {0}of() operation on {1} {2} with no exact {3}.",
						operation_name, type_name_prefix, type_name, operation_name ) );
			case SINGLE_LENGTH_RESTRICTION:
				if (single_length >= min_size) {
					return single_length;
				}
				throw new TtcnError( MessageFormat.format( "Performing {0}of() operation on an invalid {1}. The minimum {2} ({3}) contradicts the length restriction ({4}).",
						operation_name, type_name, operation_name, min_size, single_length ) );
			case RANGE_LENGTH_RESTRICTION: {
				boolean has_invalid_restriction;
				if (match_length(min_size)) {
					if (range_length_max_length_set && (min_size == range_length_max_length)) {
						return min_size;
					}
					has_invalid_restriction = false;
				} else {
					has_invalid_restriction = min_size > range_length_min_length;
				}

				if (has_invalid_restriction) {
					if (range_length_max_length_set) {
						throw new TtcnError( MessageFormat.format( "Performing {0}of() operation on an invalid {1}. The minimum {2} ({3}) contradicts the length restriction ({4}..{5}).",
								operation_name, type_name, operation_name, min_size, range_length_min_length, range_length_max_length ) );
					} else {
						throw new TtcnError( MessageFormat.format( "Performing {0}of() operation on an invalid {1}. The minimum {2} ({3}) contradicts the length restriction ({4}..infinity).",
								operation_name, type_name, operation_name, min_size, range_length_min_length ) );
					}
				} else {
					throw new TtcnError( MessageFormat.format( "Performing {0}of() operation on {1} {2} with no exact {4}.",
							operation_name, type_name_prefix, type_name, operation_name ) );
				}
			}

			default:
				throw new TtcnError("Internal error: Template has invalid length restriction type.");
			}
		} else {
			// exact size is in min_size, check for invalid restriction
			switch (length_restriction_type) {
			case NO_LENGTH_RESTRICTION:
				return min_size;
			case SINGLE_LENGTH_RESTRICTION:
				if (single_length == min_size) {
					return min_size;
				}
				throw new TtcnError( MessageFormat.format( "Performing {0}of() operation on an invalid {1}. The {2} ({3}) contradicts the length restriction ({4}).",
						operation_name, type_name, operation_name, min_size, single_length ) );
			case RANGE_LENGTH_RESTRICTION:
				if (!match_length(min_size)) {
					if (range_length_max_length_set) {
						throw new TtcnError( MessageFormat.format( "Performing {0}of() operation on an invalid {1}. The {2} ({3}) contradicts the length restriction ({4}..{5}).",
								operation_name, type_name, operation_name, min_size, range_length_min_length, range_length_max_length ) );
					} else {
						throw new TtcnError( MessageFormat.format( "Performing {0}of() operation on an invalid {1}. The {2} ({3}) contradicts the length restriction ({4}..infinity).",
								operation_name, type_name, operation_name, min_size, range_length_min_length ) );
					}
				} else {
					return min_size;
				}
			default:
				throw new TtcnError("Internal error: Template has invalid length restriction type.");
			}
		}
	}

	protected void log_restricted() {
		switch (length_restriction_type) {
		case SINGLE_LENGTH_RESTRICTION:
			TtcnLogger.log_event(MessageFormat.format(" length ({0})", single_length));
			break;
		case NO_LENGTH_RESTRICTION:
			break;
		case RANGE_LENGTH_RESTRICTION:
			TtcnLogger.log_event(MessageFormat.format(" length ({0} .. ", range_length_min_length));
			if (range_length_max_length_set) {
				TtcnLogger.log_event(MessageFormat.format("{0})", range_length_max_length));
			} else {
				TtcnLogger.log_event_str("infinity)");
			}
			break;
		default:
			TtcnLogger.log_event_str("<unknown length restriction>");
			break;
		}
	}

	protected void log_match_length(final int value_length) {
		if (length_restriction_type != length_restriction_type_t.NO_LENGTH_RESTRICTION) {
			if (TtcnLogger.matching_verbosity_t.VERBOSITY_COMPACT == TtcnLogger.get_matching_verbosity()) {
				if (!match_length(value_length)) {
					TtcnLogger.print_logmatch_buffer();
					log_restricted();
					TtcnLogger.log_event(MessageFormat.format(" with {0} ", value_length));
				}
			} else {
				log_restricted();
				TtcnLogger.log_event(MessageFormat.format(" with {0} ", value_length));
				if (match_length(value_length)) {
					TtcnLogger.log_event_str("matched");
				} else {
					TtcnLogger.log_event_str("unmatched");
				}
			}
		}
	}


	void encode_text_restricted(final Text_Buf text_buf) {
		encode_text_base(text_buf);
		text_buf.push_int( length_restriction_type.ordinal() );
		switch (length_restriction_type) {
		case SINGLE_LENGTH_RESTRICTION:
			text_buf.push_int(single_length);
			break;
		case NO_LENGTH_RESTRICTION:
			break;
		case RANGE_LENGTH_RESTRICTION:
			text_buf.push_int(range_length_min_length);
			text_buf.push_int( range_length_max_length_set ? 1 : 0 );
			if (range_length_max_length_set) {
				text_buf.push_int(range_length_max_length);
			}
			break;
		default:
			throw new TtcnError("Text encoder: encoding an unknown/unsupported length restriction type in a template.");
		}
	}

	void decode_text_restricted(final Text_Buf text_buf) {
		decode_text_base(text_buf);
		length_restriction_type = length_restriction_type_t.values()[ text_buf.pull_int().getInt() ];
		switch (length_restriction_type) {
		case SINGLE_LENGTH_RESTRICTION:
			single_length = text_buf.pull_int().getInt();
			break;
		case NO_LENGTH_RESTRICTION:
			break;
		case RANGE_LENGTH_RESTRICTION:
			range_length_min_length = text_buf.pull_int().getInt();
			range_length_max_length_set = text_buf.pull_int().getInt() != 0;
			if (range_length_max_length_set) {
				range_length_max_length = text_buf.pull_int().getInt();
			}
			break;
		default:
			throw new TtcnError("Text decoder: an unknown/unsupported length restriction type was received for a template.");
		}
	}

//TODO: implement
/*
	void set_length_range(final Module_Param param)
	{
		Module_Param_Length_Restriction length_range = param.get_length_restriction();
		if (length_range==null) {
			length_restriction_type = length_restriction_type_t.NO_LENGTH_RESTRICTION;
			return;
		}
		if (length_range.is_single()) {
			length_restriction_type = length_restriction_type_t.SINGLE_LENGTH_RESTRICTION;
			single_length = (int)(length_range.get_min());
		} else {
			length_restriction_type = length_restriction_type_t.RANGE_LENGTH_RESTRICTION;
			range_length_min_length = (int)(length_range.get_min());
			range_length_max_length_set = length_range.get_has_max();
			if (range_length_max_length_set) {
				range_length_max_length = (int)(length_range.get_max());
			}
		}
	}
*/

	protected Module_Param_Length_Restriction get_length_range() {
		if (length_restriction_type == length_restriction_type_t.NO_LENGTH_RESTRICTION) {
			return null;
		}

		final Module_Param_Length_Restriction mp_res = new Module_Param_Length_Restriction();
		if (length_restriction_type == length_restriction_type_t.SINGLE_LENGTH_RESTRICTION) {
			mp_res.set_single(single_length);
		} else {
			mp_res.set_min(range_length_min_length);
			if (range_length_max_length_set) {
				mp_res.set_max(range_length_max_length);
			}
		}

		return mp_res;
	}

	public void set_single_length(final int single_length) {
		length_restriction_type = length_restriction_type_t.SINGLE_LENGTH_RESTRICTION;
		this.single_length = single_length;
	}

	public void set_single_length(final TitanInteger single_length) {
		single_length.mustBound("Using an unbound integer value as length restriction.");

		set_single_length(single_length.getInt());
	}

	public void set_min_length(final int min_length) {
		if (min_length < 0) {
			throw new TtcnError(MessageFormat.format("The lower limit for the length is negative ({0}) in a template with length restriction.", min_length));
		}

		length_restriction_type = length_restriction_type_t.RANGE_LENGTH_RESTRICTION;
		range_length_min_length = min_length;
		range_length_max_length_set = false;
	}

	public void set_min_length(final TitanInteger min_length) {
		min_length.mustBound("Using an unbound integer value as lower length restriction.");

		set_min_length(min_length.getInt());
	}

	public void set_max_length(final int max_length) {
		if (length_restriction_type != length_restriction_type_t.RANGE_LENGTH_RESTRICTION) {
			throw new TtcnError("Internal error: Setting a maximum length for a template the length restriction of which is not a range.");
		}
		if (max_length < 0) {
			throw new TtcnError(MessageFormat.format("The upper limit for the length is negative ({0}) in a template with length restriction.", max_length));
		}
		if (range_length_min_length > max_length) {
			throw new TtcnError( MessageFormat.format( "The upper limit for the length ({0}) is smaller than the lower limit ({1}) in a template with length restriction.",
							max_length, range_length_min_length));
		}

		range_length_max_length = max_length;
		range_length_max_length_set = true;
	}

	public void set_max_length(final TitanInteger max_length) {
		max_length.mustBound("Using an unbound integer value as upper length restriction.");

		set_max_length(max_length.getInt());
	}

	@Override
	public boolean isOmit() {
		return templateSelection == template_sel.OMIT_VALUE && !is_ifPresent
				&& length_restriction_type == length_restriction_type_t.NO_LENGTH_RESTRICTION;
	}

	@Override
	public boolean is_any_or_omit() {
		return templateSelection == template_sel.ANY_OR_OMIT && !is_ifPresent
				&& length_restriction_type == length_restriction_type_t.NO_LENGTH_RESTRICTION;
	}
}
