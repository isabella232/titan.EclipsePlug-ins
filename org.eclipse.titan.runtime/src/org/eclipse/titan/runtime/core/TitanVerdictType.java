/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

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

	public TitanVerdictType() {
		verdict_value = VerdictTypeEnum.UNBOUND;
	}

	public TitanVerdictType(final VerdictTypeEnum other_value) {
		if (!isValid(other_value)) {
			throw new TtcnError("Initializing a verdict variable with an invalid value (" + other_value + ").");
		}

		verdict_value = other_value;
	}

	public TitanVerdictType(final TitanVerdictType other_value) {
		other_value.mustBound("Copying an unbound verdict value.");

		verdict_value = other_value.verdict_value;
	}

	public void cleanUp() {
		verdict_value = VerdictTypeEnum.UNBOUND;
	}

	//originally #define IS_VALID
	public static boolean isValid(final VerdictTypeEnum aVerdictValue) {
		return aVerdictValue != VerdictTypeEnum.UNBOUND;
	}

	@Override
	public boolean isPresent() {
		return isBound();
	}

	@Override
	public boolean isBound() {
		return verdict_value != VerdictTypeEnum.UNBOUND;
	}

	public void mustBound(final String aErrorMessage) {
		if (verdict_value == VerdictTypeEnum.UNBOUND) {
			throw new TtcnError(aErrorMessage);
		}
	}

	//originally operator==
	public boolean operatorEquals(final TitanVerdictType aOtherValue) {
		mustBound("The left operand of comparison is an unbound verdict value.");
		aOtherValue.mustBound("The right operand of comparison is an unbound verdict value.");

		return verdict_value.equals(aOtherValue.verdict_value);
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanVerdictType) {
			return operatorEquals((TitanVerdictType)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type", otherValue));
	}

	// originally boolean VERDICTTYPE::operator==(verdicttype other_value) const
	public boolean operatorEquals(final VerdictTypeEnum aOtherValue) {
		mustBound("The left operand of comparison is an unbound verdict value.");

		if (!isValid(aOtherValue)) {
			throw new TtcnError("The right operand of comparison is an invalid verdict value (" + aOtherValue + ").");
		}

		return verdict_value == aOtherValue;
	}

	//originally operator=
	public TitanVerdictType assign(final TitanVerdictType aOtherValue) {
		aOtherValue.mustBound("Assignment of an unbound verdict value.");

		if (aOtherValue != this) {
			verdict_value = aOtherValue.verdict_value;
		}

		return this;
	}

	@Override
	public TitanVerdictType assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanVerdictType) {
			return assign((TitanVerdictType)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type", otherValue));
	}

	//originally operator= (verdicttype other_value)
	public TitanVerdictType assign(final VerdictTypeEnum other_value) {
		if (!isValid(other_value)) {
			throw new TtcnError("Assignment of an invalid verdict value (" + other_value + ").");
		}

		verdict_value = other_value;
		return this;
	}

	public VerdictTypeEnum getValue() {
		return verdict_value;
	}

	public void log() {
		if (isValid(verdict_value)) {
			TtcnLogger.log_event_str(verdict_name[verdict_value.ordinal()]);
		} else if (verdict_value == VerdictTypeEnum.UNBOUND) {
			TtcnLogger.log_event_unbound();
		} else {
			TtcnLogger.log_event(MessageFormat.format("<invalid verdict value: {0}>", verdict_value));
		}
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		mustBound("Text encoder: Encoding an unbound verdict value.");

		text_buf.push_int(verdict_value.getValue());
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		final int received_value = text_buf.pull_int().getInt();
		if (received_value < 0 || received_value > 5) {
			throw new TtcnError(MessageFormat.format("Text decoder: Invalid verdict value ({0}) was received.", received_value));
		}
		verdict_value = VerdictTypeEnum.values()[received_value];
	}

	//TODO: implement VERDICTTYPE::set_param()
	//TODO: implement VERDICTTYPE::get_param()
	//TODO: implement VERDICTTYPE::encode()
	//TODO: implement VERDICTTYPE::decode()
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
