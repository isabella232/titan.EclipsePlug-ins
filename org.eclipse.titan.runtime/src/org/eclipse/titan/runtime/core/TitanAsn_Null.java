/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

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

	public TitanAsn_Null() {
		boundFlag = false;
	}

	public TitanAsn_Null(final Asn_Null_Type otherValue) {
		boundFlag = true;
	}

	public TitanAsn_Null(final TitanAsn_Null otherValue) {
		if (!otherValue.boundFlag) {
			throw new TtcnError("Copying an unbound ASN.1 NULL value.");
		}

		boundFlag = true;
	}

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

	// originally operator==
	public boolean operatorEquals(final Asn_Null_Type otherValue) {
		if (!boundFlag) {
			throw new TtcnError("The left operand of comparison is an unbound ASN.1 NULL value.");
		}

		return true;
	}

	// originally operator==
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

	// originally operator!=
	public boolean operatorNotEquals(final Asn_Null_Type otherValue) {
		return !operatorEquals(otherValue);
	}

	// originally operator!=
	public boolean operatorNotEquals(final TitanAsn_Null otherValue) {
		return !operatorEquals(otherValue);
	}

	public boolean isBound() {
		return boundFlag;
	}

	public boolean isPresent() {
		return boundFlag;
	}

	public boolean isValue() {
		return boundFlag;
	}

	public void log() {
		if (boundFlag) {
			TtcnLogger.log_event_str("NULL");
		} else {
			TtcnLogger.log_event_unbound();
		}
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
