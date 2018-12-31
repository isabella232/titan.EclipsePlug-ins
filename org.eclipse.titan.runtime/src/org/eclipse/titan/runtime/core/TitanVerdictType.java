/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.basic_check_bits_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.type_t;
import org.eclipse.titan.runtime.core.TTCN_EncDec.coding_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;

/**
 * TTCN-3 verdict type
 * originally VERDICTTYPE
 * @author Arpad Lovassy
 */
public class TitanVerdictType extends Base_Type {

	//originally Types.hh/verdicttype
	public enum VerdictTypeEnum {
		NONE(0, "none"), PASS(1, "pass"), INCONC(2, "inconc"), FAIL(3, "fail"), ERROR(4, "error"), UNBOUND(5, "unbound");

		private int index;
		private String name;
		VerdictTypeEnum(final int index, final String name) {
			this.index = index;
			this.name = name;
		}

		public int getValue() {
			return index;
		}

		public String getName() {
			return name;
		}
	}

	public static final String verdict_name[] = { "none", "pass", "inconc", "fail", "error" };

	private VerdictTypeEnum verdict_value;

	/**
	 * Initializes to unbound value.
	 * */
	public TitanVerdictType() {
		verdict_value = VerdictTypeEnum.UNBOUND;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanVerdictType(final VerdictTypeEnum otherValue) {
		if (!is_valid(otherValue)) {
			throw new TtcnError("Initializing a verdict variable with an invalid value (" + otherValue + ").");
		}

		verdict_value = otherValue;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanVerdictType(final TitanVerdictType otherValue) {
		otherValue.must_bound("Copying an unbound verdict value.");

		verdict_value = otherValue.verdict_value;
	}

	@Override
	public void clean_up() {
		verdict_value = VerdictTypeEnum.UNBOUND;
	}

	//originally #define IS_VALID
	public static boolean is_valid(final VerdictTypeEnum aVerdictValue) {
		return aVerdictValue != VerdictTypeEnum.UNBOUND;
	}

	@Override
	public boolean is_present() {
		return is_bound();
	}

	@Override
	public boolean is_bound() {
		return verdict_value != VerdictTypeEnum.UNBOUND;
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
	public boolean operator_equals(final TitanVerdictType otherValue) {
		must_bound("The left operand of comparison is an unbound verdict value.");
		otherValue.must_bound("The right operand of comparison is an unbound verdict value.");

		return verdict_value.equals(otherValue.verdict_value);
	}

	@Override
	public boolean operator_equals(final Base_Type otherValue) {
		if (otherValue instanceof TitanVerdictType) {
			return operator_equals((TitanVerdictType)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type", otherValue));
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
	public boolean operator_equals(final VerdictTypeEnum otherValue) {
		must_bound("The left operand of comparison is an unbound verdict value.");

		if (!is_valid(otherValue)) {
			throw new TtcnError("The right operand of comparison is an invalid verdict value (" + otherValue + ").");
		}

		return verdict_value == otherValue;
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
	public TitanVerdictType operator_assign(final TitanVerdictType otherValue) {
		otherValue.must_bound("Assignment of an unbound verdict value.");

		if (otherValue != this) {
			verdict_value = otherValue.verdict_value;
		}

		return this;
	}

	@Override
	public TitanVerdictType operator_assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanVerdictType) {
			return operator_assign((TitanVerdictType)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type", otherValue));
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
	public TitanVerdictType operator_assign(final VerdictTypeEnum otherValue) {
		if (!is_valid(otherValue)) {
			throw new TtcnError("Assignment of an invalid verdict value (" + otherValue + ").");
		}

		verdict_value = otherValue;
		return this;
	}

	public VerdictTypeEnum get_value() {
		return verdict_value;
	}

	@Override
	public void log() {
		if (is_valid(verdict_value)) {
			TTCN_Logger.log_event_str(verdict_name[verdict_value.ordinal()]);
		} else if (verdict_value == VerdictTypeEnum.UNBOUND) {
			TTCN_Logger.log_event_unbound();
		} else {
			TTCN_Logger.log_event(MessageFormat.format("<invalid verdict value: {0}>", verdict_value));
		}
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "verdict value");
		if (param.get_type() != type_t.MP_Verdict) {
			param.type_error("verdict value");
		}
		final TitanVerdictType verdict = param.get_verdict();
		if (!is_valid(verdict.verdict_value)) {
			param.error("Internal error: invalid verdict value (%d).", verdict);
		}
		verdict_value = verdict.verdict_value;
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		must_bound("Text encoder: Encoding an unbound verdict value.");

		text_buf.push_int(verdict_value.getValue());
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		final int received_value = text_buf.pull_int().get_int();
		if (received_value < 0 || received_value > 5) {
			throw new TtcnError(MessageFormat.format("Text decoder: Invalid verdict value ({0}) was received.", received_value));
		}
		verdict_value = VerdictTypeEnum.values()[received_value];
	}

	/**
	 * Checks if the first value is equivalent to the second one.
	 *
	 * static operator== in the core
	 *
	 * @param par_value
	 *                the first value.
	 * @param other_value
	 *                the other value to check against.
	 * @return {@code true} if the values are equivalent.
	 */
	public static boolean operator_equals(final VerdictTypeEnum par_value, final TitanVerdictType other_value) {
		if (!TitanVerdictType.is_valid(par_value)) {
			throw new TtcnError("The left operand of comparison is an invalid verdict value (" + par_value + ").");
		}

		other_value.must_bound("The right operand of comparison is an unbound verdict value.");

		return par_value == other_value.get_value();
	}

	//TODO: implement VERDICTTYPE::get_param()

	@Override
	/** {@inheritDoc} */
	public void encode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		//only xer and JSON will be supported
		throw new TtcnError(MessageFormat.format("Unknown coding method requested to encode type `{0}''", p_td.name));
	}

	@Override
	/** {@inheritDoc} */
	public void decode(final TTCN_Typedescriptor p_td, final TTCN_Buffer p_buf, final coding_type p_coding, final int flavour) {
		//only xer and JSON will be supported
		throw new TtcnError(MessageFormat.format("Unknown coding method requested to decode type `{0}''", p_td.name));
	}

	//TODO: implement VERDICTTYPE::XER_encode()

	public VerdictTypeEnum str_to_verdict(final String v, final boolean silent) {
		for (final VerdictTypeEnum i : VerdictTypeEnum.values()) {
			if (verdict_name[i.ordinal()].equals(v)) {
				return i;
			}
		}

		if (!silent) {
			TTCN_EncDec_ErrorContext.error(error_type.ET_INVAL_MSG, "Invalid value for verdicttype: '%s'", v);
		}

		return VerdictTypeEnum.UNBOUND;
	}

	//TODO: implement VERDICTTYPE::XER_decode()
	//TODO: implement VERDICTTYPE::XER_decode()
	//TODO: implement VERDICTTYPE::JSON_encode()
	//TODO: implement VERDICTTYPE::JSON_decode()

}
