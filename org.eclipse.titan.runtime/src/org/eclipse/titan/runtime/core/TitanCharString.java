/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * TTCN-3 charstring
 * @author Arpad Lovassy
 * @author Andrea Palfi
 */
public class TitanCharString extends Base_Type {

	/**
	 * charstring value.
	 */
	private StringBuilder val_ptr;

	public TitanCharString() {
		super();
	}

	public TitanCharString( final StringBuilder aOtherValue ) {
		val_ptr = new StringBuilder( aOtherValue );
	}

	public TitanCharString( final String aOtherValue ) {
		copyValue( aOtherValue );
	}

	public TitanCharString( final TitanCharString aOtherValue ) {
		aOtherValue.mustBound( "Copying an unbound charstring value." );

		val_ptr = new StringBuilder(aOtherValue.val_ptr);
	}

	//originally char*()
	public StringBuilder getValue() {
		mustBound("Getting an unbound charstring value as string.");

		return val_ptr;
	}

	private void copyValue( final String aOtherValue ) {
		val_ptr = new StringBuilder( aOtherValue );
	}

	private void copyValue( final StringBuilder aOtherValue ) {
		val_ptr = new StringBuilder( aOtherValue );
	}

	// originally operator=
	// assign for String
	public TitanCharString assign(final String aOtherValue) {
		cleanUp();

		if (aOtherValue == null) {
			val_ptr = new StringBuilder();
		} else {
			val_ptr = new StringBuilder(aOtherValue);
		}

		return this;
	}

	//originally operator=
	public TitanCharString assign( final TitanCharString aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound charstring value." );

		if (aOtherValue != this) {
			copyValue( aOtherValue.val_ptr );
		}

		return this;
	}

	@Override
	public TitanCharString assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharString) {
			return assign((TitanCharString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	public boolean isBound() {
		return val_ptr != null;
	}

	public boolean isPresent() {
		return isBound();
	};

	public boolean isValue() {
		return val_ptr != null;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( val_ptr == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally operator=
	// assign for TitanCharString_Element
	public TitanCharString assign(final TitanCharString_Element aOtherValue) {
		aOtherValue.mustBound("Assignment of an unbound charstring element to a charstring.");

		cleanUp();
		val_ptr = new StringBuilder(1);
		val_ptr.append(aOtherValue.get_char());

		return this;
	}

	// assign for UniversalCharstring
	public TitanCharString assign(final TitanUniversalCharString aOtherValue) {
		aOtherValue.mustBound("Assignment of an unbound universal charstring to a charstring.");

		if (aOtherValue.charstring) {
			return assign(aOtherValue.cstr.toString());
		} else {
			cleanUp();
			for (int i = 0; i < aOtherValue.val_ptr.size(); ++i) {
				final TitanUniversalChar uc = aOtherValue.val_ptr.get(i);
				if (uc.getUc_group() != 0 || uc.getUc_plane() != 0 || uc.getUc_row() != 0) {
					throw new TtcnError(MessageFormat.format("Multiple-byte characters cannot be assigned to a charstring, invalid character char({0},{1}, {2}, {3}) at index {4}.", aOtherValue));
				} else {
					val_ptr.append(uc.getUc_cell());
				}
			}
		}

		return this;
	}

	// originally lengthOf
	public TitanInteger lengthOf() {
		mustBound("Performing lengthof operation on an unbound charstring value.");

		return new TitanInteger(val_ptr.length());
	}

	private static enum States {
		INIT, PCHAR, NPCHAR;
	}

	public void log() {
		if (val_ptr != null) {
			States state = States.INIT;
			StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < val_ptr.length(); i++) {
				char c = val_ptr.charAt(i);
				if (TtcnLogger.isPrintable(c)) {
					switch (state) {
					case NPCHAR:
						buffer.append(" & ");
					case INIT:
						buffer.append("\"");
					case PCHAR:
						TtcnLogger.logCharEscaped(c, buffer);
						break;
					}
					state = States.PCHAR;
				} else {
					switch (state) {
					case PCHAR:
						buffer.append("\"");
					case NPCHAR:
						buffer.append(" & ");
					case INIT:
						buffer.append(MessageFormat.format("char(0, 0, 0, {0})", (int) c));
						break;
					}
					state = States.NPCHAR;
				}
			}
			switch (state) {
			case INIT:
				buffer.append("\"\"");
				break;
			case PCHAR:
				buffer.append("\"");
				break;
			default:
				break;
			}
			TtcnLogger.log_event_str(buffer.toString());

		} else {
			TtcnLogger.log_event_unbound();
		}
	}

	/**
	 * this + aOtherValue
	 * originally operator&
	 */
	public TitanCharString concatenate(final TitanCharString aOtherValue) {
		mustBound("Unbound left operand of charstring addition.");
		aOtherValue.mustBound("Unbound right operand of charstring addition.");

		final TitanCharString result = new TitanCharString(val_ptr);
		result.val_ptr.append(aOtherValue.val_ptr);

		return result;
	}

	// originally operator+
	// concatenate for String
	public TitanCharString concatenate(final String aOtherValue) {
		mustBound("Unbound operand of charstring concatenation.");

		final TitanCharString ret_val = new TitanCharString(val_ptr);
		if (aOtherValue != null && aOtherValue.length() > 0) {
			ret_val.val_ptr.append(aOtherValue);
		}

		return ret_val;
	}

	//originally operator+=
	//append for String
	public TitanCharString append(final String aOtherValue) {
		mustBound(" Appending a string literal to an unbound charstring value.");

		if (aOtherValue != null && aOtherValue.length() > 0) {
			val_ptr.append(aOtherValue);
		}

		return this;
	}

	//originally operator+= 
	// append for charstring_element
	public TitanCharString append(final TitanCharString_Element aOtherValue) {
		mustBound("Appending a charstring value to an unbound charstring value.");
		aOtherValue.mustBound("Appending an unbound charstring value to another charstring value.");

		val_ptr.append(aOtherValue.get_char());

		return this;
	}

	// originally operator+
	// concatenate for charstring_element
	public TitanCharString concatenate(final TitanCharString_Element aOtherValue) {
		mustBound("Unbound operand of charstring concatenation.");
		aOtherValue.mustBound("Unbound operand of charstring element concatenation.");

		final TitanCharString ret_val = new TitanCharString(this);
		ret_val.val_ptr.append(aOtherValue.get_char());

		return ret_val;
	}

	// originally operator+
	public TitanUniversalCharString concatenate(final TitanUniversalCharString aOtherValue) {
		mustBound("The left operand of concatenation is an unbound charstring value.");
		aOtherValue.mustBound("The right operand of concatenation is an unbound universal charstring value.");

		if (val_ptr.length() == 0) {
			return new TitanUniversalCharString(aOtherValue);
		}
		if (aOtherValue.charstring) {
			final StringBuilder ret_val = new StringBuilder(getValue());
			ret_val.append(aOtherValue.cstr.toString());

			return new TitanUniversalCharString(ret_val);
		} else {
			final List<TitanUniversalChar> ret_val = new ArrayList<TitanUniversalChar>();
			for (int i = 0; i < val_ptr.length(); i++) {
				ret_val.add(new TitanUniversalChar((char) 0, (char) 0, (char) 0, val_ptr.charAt(i)));
			}
			ret_val.addAll(aOtherValue.getValue());

			return new TitanUniversalCharString(ret_val);
		}

	}

	/*public TitanUniversalCharString concatenate(final TitanUniversalCharString_Element aOtherValue)
	{
		mustBound("The left operand of concatenation is an unbound charstring value.");
		aOtherValue.mustBound("The right operand of concatenation is an unbound universal charstring element.");
		
	}*/

	// originally operator==
	public boolean operatorEquals(final TitanCharString aOtherValue) {
		mustBound("Unbound left operand of charstring comparison.");
		aOtherValue.mustBound("Unbound right operand of charstring comparison.");

		return val_ptr.toString().equals(aOtherValue.val_ptr.toString());
	}

	// originally operator==
	public boolean operatorEquals(final TitanUniversalCharString otherValue) {
		mustBound("The left operand of comparison is an unbound charstring value.");
		otherValue.mustBound("The right operand of comparison is an unbound universal charstring value.");

		if (otherValue.charstring) {
			return val_ptr.toString().equals(otherValue.cstr.toString());
		}
		if (val_ptr.length() != otherValue.val_ptr.size()) {
			return false;
		}
		for (int i = 0; i < val_ptr.length(); i++) {
			final char tempLeft = val_ptr.charAt(i);
			final TitanUniversalChar tempRight = otherValue.val_ptr.get(i);
			if (tempRight.getUc_group() != 0 || tempRight.getUc_plane() != 0 || tempRight.getUc_row() != 0
					|| tempRight.getUc_cell() != tempLeft) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanCharString) {
			return operatorEquals((TitanCharString)otherValue);
		} else if (otherValue instanceof TitanUniversalCharString) {
			return operatorEquals((TitanUniversalCharString)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to charstring", otherValue));
	}

	// originally operator ==
	// operatorEquals for String
	public boolean operatorEquals(final String aOtherValue) {
		mustBound("Unbound operand of charstring comparison.");

		if (aOtherValue == null) {
			return val_ptr.length() == 0;
		}
		return this.val_ptr.toString().equals(aOtherValue);
	}

	// originally operator ==
	// operatorEquals for charstring_element
	public boolean operatorEquals(final TitanCharString_Element aOtherValue) {
		aOtherValue.mustBound("Unbound operand of charstring element comparison.");
		mustBound("Unbound operand of charstring comparison.");

		if (val_ptr.length() != 1) {
			return false;
		}

		return val_ptr.charAt(0) == aOtherValue.get_char();

	}

	// originally operator ==
	// operatorEquals for Universal_charstring
	public boolean operatorEquals(final TitanUniversalCharString_Element aOtherValue) {
		mustBound("The left operand of comparison is an unbound charstring value.");
		aOtherValue.mustBound("The right operand of comparison is an unbound universal charstring value");

		if (val_ptr.length() != 1) {
			return false;
		}

		final TitanUniversalChar uc = aOtherValue.get_char();
		if (uc.getUc_group() == 0 && uc.getUc_plane() == 0 && uc.getUc_row() == 0 && uc.getUc_cell() == val_ptr.charAt(0)) {
			return true;
		}
		return false;
	}

	//originally operator!=
	public boolean operatorNotEquals( final TitanCharString aOtherValue ) {
		return !operatorEquals( aOtherValue );
	}

	public void cleanUp() {
		val_ptr = null;
	}

	// originally operator!=
	// operatorNotEquals for charstring_element
	public boolean operatorNotEquals(final TitanCharString_Element aOtherValue) {
		aOtherValue.mustBound("Unbound operand of charstring element comparison.");
		mustBound("Unbound operand of charstring comparison.");

		if (val_ptr.length() == 1) {
			return false;
		}
		return val_ptr.charAt(0) != aOtherValue.get_char();
	}

	// originally operator!=
	// operatorNotEquals for String
	public boolean operatorNotEquals(final String aOtherValue) {
		mustBound("Unbound operand of charstring comparison.");

		if (aOtherValue != null) {
			return val_ptr.length() == 0;
		}

		return this.val_ptr.toString().equals(aOtherValue);
	}

	// originally operator<<=
	// rotateLeft for String
	public TitanCharString rotateLeft(int rotatecount) {
		mustBound("Unbound charstring operand of rotate left operator.");

		if (val_ptr.length() == 0) {
			return this;
		}
		if (rotatecount >= 0) {
			rotatecount %= val_ptr.length();
			if (rotatecount == 0) {
				return this;
			}
			final StringBuilder rValue = new StringBuilder(val_ptr.length());
			for (int i = 0; i < val_ptr.length(); i++) {
				rValue.append(val_ptr.charAt((i + rotatecount) % val_ptr.length()));
			}
			return new TitanCharString(rValue);
		}

		return rotateRight(-rotatecount);
	}

	// originally operator<<=
	// rotateLeft for TitanInteger
	public TitanCharString rotateLeft(final TitanInteger rotatecount) {
		rotatecount.mustBound("Unbound integer operand of rotate left operator.");

		return rotateLeft(rotatecount.getInt());
	}

	// originally operator>>=
	// rotateRight for String
	public TitanCharString rotateRight(int rotatecount) {
		mustBound("Unbound charstring operand of rotate right operator.");

		if (val_ptr.length() == 0) {
			return this;
		}
		if (rotatecount >= 0) {
			rotatecount %= val_ptr.length();
			if (rotatecount == 0) {
				return this;
			}
			final StringBuilder rValue = new StringBuilder(val_ptr.length());

			for (int i = 0; i < rotatecount; i++) {
				rValue.append(val_ptr.charAt(i + val_ptr.length() - rotatecount));
			}
			for (int i = rotatecount; i < val_ptr.length(); i++) {
				rValue.append(val_ptr.charAt(i - rotatecount));
			}
			return new TitanCharString(rValue);
		}
		return rotateLeft(-rotatecount);
	}

	// originally operator>>=
	// rotateRight for TitanInteger
	public TitanCharString rotateRight(final TitanInteger rotatecount) {
		rotatecount.mustBound("Unbound integer operand of rotate right operator.");

		return rotateRight(rotatecount.getInt());
	}

	//originally operator[](int)
	public TitanCharString_Element getAt(final int index_value) {
		if (val_ptr == null && index_value == 0) {
			val_ptr = new StringBuilder();
			return new TitanCharString_Element(false, this, 0);
		} else {
			mustBound("Accessing an element of an unbound charstring value.");

			if (index_value < 0) {
				throw new TtcnError("Accessing an charstring element using a negative index (" + index_value + ").");
			}

			final int n_nibbles = val_ptr.length();
			if (index_value > n_nibbles) {
				throw new TtcnError("Index overflow when accessing a charstring element: The index is " + index_value +
						", but the string has only " + n_nibbles + " characters.");
			}
			if (index_value == n_nibbles) {
				val_ptr.setLength(index_value + 1);
				return new TitanCharString_Element( false, this, index_value );
			} else {
				return new TitanCharString_Element( true, this, index_value );
			}
		}
	}

	//originally operator[](const INTEGER&)
	public TitanCharString_Element getAt(final TitanInteger index_value) {
		index_value.mustBound("Indexing a charstring value with an unbound integer value.");

		return getAt( index_value.getInt() );
	}

	//originally operator[](int) const
	public TitanCharString_Element constGetAt( final int index_value ) {
		mustBound("Accessing an element of an unbound charstring value.");

		if (index_value < 0) {
			throw new TtcnError("Accessing an charstring element using a negative index (" + index_value + ").");
		}

		final int n_nibbles = val_ptr.length();
		if (index_value >= n_nibbles) {
			throw new TtcnError("Index overflow when accessing a charstring element: The index is " + index_value +
					", but the string has only " + n_nibbles + " characters.");
		}

		return new TitanCharString_Element(true, this, index_value);
	}

	//originally operator[](const INTEGER&) const
	public TitanCharString_Element constGetAt( final TitanInteger index_value ) {
		index_value.mustBound("Indexing a charstring value with an unbound integer value.");

		return constGetAt( index_value.getInt() );
	}

	@Override
	public String toString() {
		if ( val_ptr == null ) {
			return "<unbound>";
		}

		return val_ptr.toString();
	}

	// static operatorEquals
	public static boolean operatorEquals(final String StringValue, final TitanCharString aOtherValue) {
		aOtherValue.mustBound("Unbound operand of charstring comparison.");

		if (StringValue == null) {
			return aOtherValue.val_ptr.length() == 0;
		}

		return aOtherValue.val_ptr.toString().equals(StringValue);
	}

	public static boolean operatorEquals(final String StringValue, final TitanCharString_Element aOtherValue) {
		aOtherValue.mustBound("Unbound operand of charstring element comparison.");

		if (StringValue.length() != 1) {
			return false;
		}

		return StringValue.charAt(0) == aOtherValue.get_char();
	}

	// static operatorNotEquals
	public static boolean operatorNotEquals(final String StringValue, final TitanCharString aOtherValue) {
		return !operatorEquals(StringValue, aOtherValue);
	}

	public static boolean operatorNotEquals(final String StringValue, final TitanCharString_Element aOtherValue) {
		return !operatorEquals(StringValue, aOtherValue);
	}

	// static concatenate
	public static TitanCharString concatenate(final String StringValue, final TitanCharString aOtherValue) {
		aOtherValue.mustBound("Unbound operand of charstring concatenation.");

		final TitanCharString ret_val = new TitanCharString(StringValue);
		return ret_val.concatenate(aOtherValue);
	}

	public static TitanCharString concatenate(final String StringValue, final TitanCharString_Element aOtherValue) {
		aOtherValue.mustBound("Unbound operand of charstring element concatenation.");

		final TitanCharString ret_val = new TitanCharString(StringValue);
		return ret_val.concatenate(aOtherValue);
	}

}
