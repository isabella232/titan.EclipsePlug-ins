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

/**
 * ASN.1 NULL type
 *
 * @author Kristof Szabados
 */
public class TitanAsn_Null extends Base_Type {
	public enum Asn_Null_Type {
		ASN_NULL_VALUE
	};

	private boolean boundFlag;


	/**
	 * Initializes to unbound value.
	 * */
	public TitanAsn_Null() {
		boundFlag = false;
	}

	/**
	 * Initializes to a given value.
	 *
	 * @param otherValue
	 *                the value to initialize to.
	 * */
	public TitanAsn_Null(final Asn_Null_Type otherValue) {
		boundFlag = true;
	}

	/**
	 * Copy constructor.
	 *
	 * @param otherValue
	 *                the value to copy.
	 * */
	public TitanAsn_Null(final TitanAsn_Null otherValue) {
		if (!otherValue.boundFlag) {
			throw new TtcnError("Copying an unbound ASN.1 NULL value.");
		}

		boundFlag = true;
	}

	@Override
	public void cleanUp() {
		boundFlag = false;
	}

	// originally operator=
	public TitanAsn_Null assign(final Asn_Null_Type otherValue) {
		boundFlag = true;

		return this;
	}

	// originally operator=
	public TitanAsn_Null assign(final TitanAsn_Null otherValue) {
		if (!otherValue.boundFlag) {
			throw new TtcnError("Assignment of an unbound ASN.1 NULL value.");
		}

		boundFlag = true;

		return this;
	}

	@Override
	public TitanAsn_Null assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanAsn_Null) {
			return assign((TitanAsn_Null)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to ASN.1 NULL", otherValue));
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
	public boolean operatorEquals(final Asn_Null_Type otherValue) {
		if (!boundFlag) {
			throw new TtcnError("The left operand of comparison is an unbound ASN.1 NULL value.");
		}

		return true;
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
	public boolean operatorEquals(final TitanAsn_Null otherValue) {
		if (!boundFlag) {
			throw new TtcnError("The left operand of comparison is an unbound ASN.1 NULL value.");
		}
		if (!otherValue.boundFlag) {
			throw new TtcnError("The right operand of comparison is an unbound ASN.1 NULL value.");
		}

		return true;
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanAsn_Null) {
			return operatorEquals((TitanAsn_Null) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to ASN.1 NULL", otherValue));
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
	public boolean operatorNotEquals(final Asn_Null_Type otherValue) {
		return !operatorEquals(otherValue);
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
	public boolean operatorNotEquals(final TitanAsn_Null otherValue) {
		return !operatorEquals(otherValue);
	}

	@Override
	public boolean isBound() {
		return boundFlag;
	}

	@Override
	public boolean isPresent() {
		return boundFlag;
	}

	@Override
	public boolean isValue() {
		return boundFlag;
	}

	@Override
	public void log() {
		if (boundFlag) {
			TTCN_Logger.log_event_str("NULL");
		} else {
			TTCN_Logger.log_event_unbound();
		}
	}

	@Override
	public void set_param(final Module_Parameter param) {
		param.basic_check(basic_check_bits_t.BC_VALUE.getValue(), "NULL value");
		if (param.get_type() != type_t.MP_Asn_Null) {
			param.type_error("NULL value");
		}
		boundFlag = true;
	}

	@Override
	/** {@inheritDoc} */
	public void encode_text(final Text_Buf text_buf) {
		if (!boundFlag) {
			throw new TtcnError("Text encoder: Encoding an ASN.1 NULL value.");
		}
	}

	@Override
	/** {@inheritDoc} */
	public void decode_text(final Text_Buf text_buf) {
		boundFlag = true;
	}

	// static operator==
	public static boolean operatorEquals(final Asn_Null_Type parValue, final TitanAsn_Null otherValue) {
		if (!otherValue.isBound()) {
			throw new TtcnError("The right operand of comparison is an unbound ASN.1 NULL value.");
		}

		return true;
	}

	// static operator!=
	public static boolean operatorNotEquals(final Asn_Null_Type parValue, final TitanAsn_Null otherValue) {
		return !operatorEquals(parValue, otherValue);
	}
}
