package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

/**
 * TTCN-3 default
 *
 * @author Kristof Szabados
 * 
 * FIXME implement rest
 */
public class TitanDefault extends Base_Type {
	// TODO check if we could use null instead of this object
	static final Default_Base UNBOUND_DEFAULT = new Default_Base();

	Default_Base default_ptr;

	public TitanDefault() {
		default_ptr = UNBOUND_DEFAULT;
	}

	public TitanDefault(final Default_Base aOtherValue) {
		default_ptr = aOtherValue;
	}

	public TitanDefault(final TitanDefault aOtherValue) {
		if (aOtherValue.default_ptr == UNBOUND_DEFAULT) {
			throw new TtcnError( "Copying an unbound default reference." );
		}

		default_ptr = aOtherValue.default_ptr;
	}

	@Override
	public boolean isPresent() {
		return default_ptr != UNBOUND_DEFAULT;
	}

	@Override
	public boolean isBound() {
		return default_ptr != UNBOUND_DEFAULT;
	}

	@Override
	public boolean isValue() {
		return default_ptr != UNBOUND_DEFAULT;
	}

	public TitanBoolean operatorEquals(final Default_Base otherValue) {
		if (default_ptr == UNBOUND_DEFAULT) {
			throw new TtcnError( "The left operand of comparison is an unbound default reference." );
		}

		return new TitanBoolean(default_ptr == otherValue);
	}
	
	public TitanBoolean operatorEquals(final TitanDefault otherValue) {
		if (default_ptr == UNBOUND_DEFAULT) {
			throw new TtcnError( "The left operand of comparison is an unbound default reference." );
		}
		if (otherValue.default_ptr == UNBOUND_DEFAULT) {
			throw new TtcnError( "The right operand of comparison is an unbound default reference." );
		}

		return new TitanBoolean(default_ptr == otherValue.default_ptr);
	}

	@Override
	public TitanBoolean operatorEquals(final Base_Type otherValue) {
		if (otherValue instanceof TitanDefault) {
			return operatorEquals((TitanDefault)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to default", otherValue));

	}

	public TitanDefault assign(final Default_Base otherValue) {
		if (otherValue == UNBOUND_DEFAULT) {
			throw new TtcnError( "Assignment of an unbound default reference." );
		}

		default_ptr = otherValue;
		return this;
	}

	public TitanDefault assign(final TitanDefault otherValue) {
		if (otherValue.default_ptr == UNBOUND_DEFAULT) {
			throw new TtcnError( "Assignment of an unbound default reference." );
		}

		default_ptr = otherValue.default_ptr;
		return this;
	}

	@Override
	public Base_Type assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanDefault) {
			return assign((TitanDefault)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to default", otherValue));

	}

}
