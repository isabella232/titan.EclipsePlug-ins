package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;

/**
 * TTCN-3 verdict type
 * originally VERDICTTYPE
 * @author Arpad Lovassy
 */
public class TitanVerdictType extends Base_Type {

	//originally Types.hh/verdicttype
	public enum VerdictTypeEnum {
		NONE(0, "none"), PASS(1, "pass"), INCONC(2, "inconc"), FAIL(3, "fail"), ERROR(4, "error");
		
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
	};

	public static final String verdict_name[] = { "none", "pass", "inconc", "fail", "error" };

	private VerdictTypeEnum verdict_value = null;

	TitanVerdictType() {
		//do nothing
	}

	public TitanVerdictType(final VerdictTypeEnum other_value) {
		if ( !isValid( other_value ) ) {
			throw new TtcnError("Initializing a verdict variable with an invalid value (" + other_value + ").");
		}

		verdict_value = other_value;
	}

	public TitanVerdictType( final TitanVerdictType other_value ) {
		if ( !other_value.isBound() ) {
			throw new TtcnError("Copying an unbound verdict value.");
		}

		verdict_value = other_value.verdict_value;
	}

	public void cleanUp() {
		verdict_value = null;
	}

	//originally #define IS_VALID
	public static boolean isValid( final VerdictTypeEnum aVerdictValue ) {
		return aVerdictValue != null;
	}

	@Override
	public boolean isPresent() {
		return isBound();
	}

	@Override
	public boolean isBound() {
		return verdict_value != null;
	}

	public void mustBound( final String aErrorMessage ) {
		if ( verdict_value == null ) {
			throw new TtcnError( aErrorMessage );
		}
	}

	//originally operator==
	public TitanBoolean operatorEquals( final TitanVerdictType aOtherValue ) {
		mustBound("The left operand of comparison is an unbound verdict value.");
		aOtherValue.mustBound("The right operand of comparison is an unbound verdict value.");

		return new TitanBoolean(verdict_value.equals(aOtherValue.verdict_value));
	}

	@Override
	public TitanBoolean operatorEquals( final Base_Type otherValue ) {
		if (otherValue instanceof TitanVerdictType) {
			return operatorEquals((TitanVerdictType)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to verdict type", otherValue));
	}

	// originally boolean VERDICTTYPE::operator==(verdicttype other_value) const
	public TitanBoolean operatorEquals( final VerdictTypeEnum aOtherValue ) {
		mustBound( "The left operand of comparison is an unbound verdict value." );

		if (!isValid(aOtherValue)) {
			throw new TtcnError("The right operand of comparison is an invalid verdict value (" + aOtherValue + ")." );
		}

		return new TitanBoolean(verdict_value == aOtherValue);
	}

	//originally operator=
	public TitanVerdictType assign( final TitanVerdictType aOtherValue ) {
		aOtherValue.mustBound( "Assignment of an unbound verdict value." );

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
	public TitanVerdictType assign( final VerdictTypeEnum other_value ) {
		if ( !isValid( other_value ) ) {
			throw new TtcnError("Assignment of an invalid verdict value (" + other_value + ").");
		}

		verdict_value = other_value;
		return this;
	}

	public VerdictTypeEnum getValue() {
		return verdict_value;
	}

	public void log() {
		if (isValid( verdict_value )) {
			TtcnLogger.log_event_str( verdict_name[ verdict_value.ordinal() ] );
		}
		else {
			TtcnLogger.log_event("<invalid verdict value: %d>", verdict_value);
		}
	}

	//TODO: implement VERDICTTYPE::set_param()
	//TODO: implement VERDICTTYPE::get_param()
	//TODO: implement VERDICTTYPE::encode_text()
	//TODO: implement VERDICTTYPE::decode_text()
	//TODO: implement VERDICTTYPE::encode()
	//TODO: implement VERDICTTYPE::decode()
	//TODO: implement VERDICTTYPE::XER_encode()

	public VerdictTypeEnum str_to_verdict(final String v, final boolean silent) {
		for (VerdictTypeEnum i : VerdictTypeEnum.values()) {
			if ( verdict_name[i.ordinal()].equals( v ) ) {
				return i;
			}
		}

		if (!silent) {
			//TODO
			//TTCN_EncDec_ErrorContext.error(TTCN_EncDec.ET_INVAL_MSG, "Invalid value for verdicttype: '%s'", v);
		}
		return null;
	}

	//TODO: implement VERDICTTYPE::XER_decode()
	//TODO: implement VERDICTTYPE::XER_decode()
	//TODO: implement VERDICTTYPE::JSON_encode()
	//TODO: implement VERDICTTYPE::JSON_decode()

}
