/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.Base_Template.template_sel;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Omit;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Unbound;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;

/**
 * TTCN-3 boolean
 * @author Kristof Szabados
 */
public final class Optional<TYPE extends Base_Type> extends Base_Type {
	public enum optional_sel { OPTIONAL_UNBOUND, OPTIONAL_OMIT, OPTIONAL_PRESENT };

	private TYPE optionalValue;

	private optional_sel optionalSelection;

	private final Class<TYPE> clazz;

	public Optional(final Class<TYPE> clazz) {
		optionalValue = null;
		optionalSelection = optional_sel.OPTIONAL_UNBOUND;
		this.clazz = clazz;
	}

	public Optional(final Class<TYPE> clazz, final template_sel otherValue) {
		if (otherValue != template_sel.OMIT_VALUE) {
			throw new TtcnError("Setting an optional field to an invalid value.");
		}
		optionalValue = null;
		optionalSelection = optional_sel.OPTIONAL_OMIT;
		this.clazz = clazz;
	}

	public Optional(final Optional<TYPE> otherValue) {
		//super(otherValue);
		optionalValue = null;
		optionalSelection = otherValue.optionalSelection;
		clazz = otherValue.clazz;
		if (optional_sel.OPTIONAL_PRESENT.equals(otherValue.optionalSelection)) {
			try {
				optionalValue = clazz.newInstance();
			} catch (Exception e) {
				throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
			}

			optionalValue.operator_assign(otherValue.optionalValue);
		}
	}

	@Override
	public void clean_up() {
		if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
			optionalValue = null;
		}
		optionalSelection = optional_sel.OPTIONAL_UNBOUND;
	}

	/**
	 * Sets the current selection to be omit.
	 * Any other parameter causes dynamic testcase error.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public Optional<TYPE> operator_assign(final template_sel otherValue) {
		if (!template_sel.OMIT_VALUE.equals(otherValue)) {
			throw new TtcnError("Internal error: Setting an optional field to an invalid value.");
		}
		set_to_omit();
		return this;
	}

	/**
	 * Assigns the other value to this value.
	 * Overwriting the current content in the process.
	 *<p>
	 * operator= in the core.
	 *
	 * @param otherValue
	 *                the other value to assign.
	 * @return the new value object.
	 */
	public Optional<TYPE> operator_assign(final Optional<TYPE> otherValue) {
		switch (otherValue.optionalSelection) {
		case OPTIONAL_PRESENT:
			if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
				optionalValue.operator_assign(otherValue.optionalValue);
			} else {
				try {
					optionalValue = clazz.newInstance();
				} catch (Exception e) {
					throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
				}
				optionalValue.operator_assign(otherValue.optionalValue);
				optionalSelection = optional_sel.OPTIONAL_PRESENT;
			}
			break;
		case OPTIONAL_OMIT:
			if (otherValue != this) {
				set_to_omit();
			}
			break;
		default:
			clean_up();
			break;
		}

		return this;
	}

	@Override
	public Optional<TYPE> operator_assign(final Base_Type otherValue) {
		if (!(otherValue instanceof Optional<?>)) {
			if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
				optionalValue.operator_assign(otherValue);
			} else {
				try {
					optionalValue = clazz.newInstance();
				} catch (Exception e) {
					throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
				}
				optionalValue.operator_assign(otherValue);
				optionalSelection = optional_sel.OPTIONAL_PRESENT;
			}
			return this;
		}

		final Optional<?> optionalOther = (Optional<?>)otherValue;
		switch (optionalOther.optionalSelection) {
		case OPTIONAL_PRESENT:
			if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
				optionalValue.operator_assign(optionalOther.optionalValue);
			} else {
				try {
					optionalValue = clazz.newInstance();
				} catch (Exception e) {
					throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
				}
				optionalValue.operator_assign(optionalOther.optionalValue);
				optionalSelection = optional_sel.OPTIONAL_PRESENT;
			}
			break;
		case OPTIONAL_OMIT:
			if (optionalOther != this) {
				set_to_omit();
			}
			break;
		default:
			clean_up();
			break;
		}

		return this;
	}

	public void set_to_present() {
		if (!optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
			optionalSelection = optional_sel.OPTIONAL_PRESENT;
			try {
				optionalValue = clazz.newInstance();
			} catch (Exception e) {
				throw new TtcnError(MessageFormat.format("Internal Error: exception `{0}'' thrown while instantiating class of `{1}'' type", e.getMessage(), clazz.getName()));
			}
		}
	}

	public void set_to_omit() {
		if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
			optionalValue = null;
		}
		optionalSelection = optional_sel.OPTIONAL_OMIT;
	}

	public optional_sel get_selection() {
		return optionalSelection;
	}

	@Override
	public void log() {
		switch (optionalSelection) {
		case OPTIONAL_PRESENT:
			optionalValue.log();
			break;
		case OPTIONAL_OMIT:
			TTCN_Logger.log_event_str("omit");
			break;
		case OPTIONAL_UNBOUND:
			TTCN_Logger.log_event_unbound();
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void set_param(final Module_Parameter param) {
		if (param.get_type() == type_t.MP_Omit) {
			if (param.get_ifpresent()) {
				param.error("An optional field of a record value cannot have an 'ifpresent' attribute");
			}
			if (param.get_length_restriction() != null) {
				param.error("An optional field of a record value cannot have a length restriction");
			}
			set_to_omit();
			return;
		}
		set_to_present();
		optionalValue.set_param(param);
		if (!optionalValue.is_bound()) {
			clean_up();
		}
	}

	@Override
	/** {@inheritDoc} */
	public Module_Parameter get_param(final Module_Param_Name param_name) {
		switch (optionalSelection) {
		case OPTIONAL_PRESENT:
			return optionalValue.get_param(param_name);
		case OPTIONAL_OMIT:
			return new Module_Param_Omit();
		case OPTIONAL_UNBOUND:
		default:
			return new Module_Param_Unbound();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		switch (optionalSelection) {
		case OPTIONAL_OMIT:
			text_buf.push_int(0);
			break;
		case OPTIONAL_PRESENT:
			text_buf.push_int(1);
			optionalValue.encode_text(text_buf);
			break;
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Text encoder: Encoding an unbound optional value.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		clean_up();

		final int temp = text_buf.pull_int().get_int();
		if (temp == 1) {
			set_to_present();
			optionalValue.decode_text(text_buf);
		} else {
			set_to_omit();
		}
	}

	@Override
	public boolean is_bound() {
		switch (optionalSelection) {
		case OPTIONAL_PRESENT:
		case OPTIONAL_OMIT:
			return true;
		default:
			if (null != optionalValue) {
				return optionalValue.is_bound();
			}
			return false;
		}
	}

	@Override
	public boolean is_present() {
		return optional_sel.OPTIONAL_PRESENT.equals(optionalSelection);
	}

	/**
	 * Checks if this optional value contains a value. Please note the
	 * optional value itself can be present (checked with is_present), while
	 * its value is set to omit (checked with ispresent).
	 * <p>
	 * Note: this is not the TTCN-3 ispresent(), kept for backward
	 * compatibility with the runtime and existing testports which use this
	 * version where unbound errors are caught before causing more trouble.
	 *
	 * @return {@code true} if the value in this optional value is present
	 *         (optionalSelection == OPTIONAL_PRESENT), {@code false}
	 *         otherwise.
	 * */
	public boolean ispresent() {
		switch (optionalSelection) {
		case OPTIONAL_PRESENT:
			return true;
		case OPTIONAL_OMIT:
			return false;
		default:
			throw new TtcnError("Using an unbound optional field.");
		}
	}

	@Override
	public boolean is_value() {
		return optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)
				&& optionalValue.is_value();
	}

	@Override
	public boolean is_optional() {
		return true;
	}

	//originally operator()
	public TYPE get() {
		set_to_present();
		return optionalValue;
	}

	// originally const operator()
	public TYPE constGet() {
		switch (optionalSelection) {
		case OPTIONAL_UNBOUND:
			throw new TtcnError("Using the value of an unbound optional field ");
		case OPTIONAL_OMIT:
			throw new TtcnError("Using the value of an optional field containing omit.");
		default:
			return optionalValue;
		}
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final template_sel otherValue) {
		if (optional_sel.OPTIONAL_UNBOUND.equals(optionalSelection)) {
			if (template_sel.UNINITIALIZED_TEMPLATE.equals(otherValue)) {
				return true;
			}
			throw new TtcnError("The left operand of comparison is an unbound optional value.");
		}

		if (!template_sel.OMIT_VALUE.equals(otherValue)) {
			throw new TtcnError("Internal error: The right operand of comparison is an invalid value.");
		}

		return optional_sel.OPTIONAL_OMIT.equals(optionalSelection);
	}

	/**
	 * Checks if the current value is equivalent to the provided one.
	 *
	 * operator== in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public boolean operator_equals(final Optional<TYPE> otherValue) {
		if (optional_sel.OPTIONAL_UNBOUND.equals(optionalSelection)) {
			if (optional_sel.OPTIONAL_UNBOUND.equals(otherValue.optionalSelection)) {
				return true;
			} else {
				throw new TtcnError("The left operand of comparison is an unbound optional value.");
			}
		} else {
			if (optional_sel.OPTIONAL_UNBOUND.equals(otherValue.optionalSelection)) {
				throw new TtcnError("The right operand of comparison is an unbound optional value.");
			} else {
				if (optionalSelection != otherValue.optionalSelection) {
					return false;
				} else if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
					return optionalValue.operator_equals(otherValue.optionalValue);
				} else {
					return true;
				}
			}
		}
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (!(otherValue instanceof Optional<?>)) {
			if (optional_sel.OPTIONAL_UNBOUND.equals(optionalSelection)) {
				if (!otherValue.is_bound()) {
					return true;
				} else {
					throw new TtcnError("The left operand of comparison is an unbound optional value.");
				}
			} else {
				if (!otherValue.is_bound()) {
					throw new TtcnError("The right operand of comparison is an unbound optional value.");
				} else {
					if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
						return optionalValue.operator_equals(otherValue);
					} else {
						return false;
					}
				}
			}
		}

		final Optional<?> optionalOther = (Optional<?>) otherValue;
		if (optional_sel.OPTIONAL_UNBOUND.equals(optionalSelection)) {
			if (optional_sel.OPTIONAL_UNBOUND.equals(optionalOther.optionalSelection)) {
				return true;
			} else {
				throw new TtcnError("The left operand of comparison is an unbound optional value.");
			}
		} else {
			if (optional_sel.OPTIONAL_UNBOUND.equals(optionalOther.optionalSelection)) {
				throw new TtcnError("The right operand of comparison is an unbound optional value.");
			} else {
				if (optionalSelection != optionalOther.optionalSelection) {
					return false;
				} else if (optional_sel.OPTIONAL_PRESENT.equals(optionalSelection)) {
					return optionalValue.operator_equals(optionalOther.optionalValue);
				} else {
					return true;
				}
			}
		}
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final template_sel otherValue) {
		return !operator_equals(otherValue);
	}

	/**
	 * Checks if the current value is not equivalent to the provided one.
	 *
	 * operator!= in the core
	 *
	 * @param otherValue
	 *                the other value to check against.
	 * @return {@code true} if the values are not equivalent.
	 */
	public boolean operator_not_equals(final Optional<TYPE> otherValue) {
		return !operator_equals(otherValue);
	}
}
