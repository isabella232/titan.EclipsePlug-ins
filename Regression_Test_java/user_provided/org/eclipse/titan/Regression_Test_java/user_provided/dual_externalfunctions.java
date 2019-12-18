package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.Regression_Test_java.generated.dual;
import org.eclipse.titan.Regression_Test_java.generated.dual.PDUType1;
import org.eclipse.titan.runtime.core.TTCN_Buffer;
import org.eclipse.titan.runtime.core.TTCN_EncDec;
import org.eclipse.titan.runtime.core.TTCN_EncDec.error_type;
import org.eclipse.titan.runtime.core.TTCN_EncDec_ErrorContext;
import org.eclipse.titan.runtime.core.TTCN_Logger;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.TitanOctetString;

public class dual_externalfunctions {

	public static TitanOctetString enc__PDUType1(PDUType1 par) {

		if (TTCN_Logger.log_this_event(TTCN_Logger.Severity.DEBUG_ENCDEC)) {
			TTCN_Logger.begin_event(TTCN_Logger.Severity.DEBUG_ENCDEC);
			TTCN_Logger.log_event_str("enc_PDUType1_gen(): Encoding @dual.PDUType1: ");
			par.log();
			TTCN_Logger.end_event();
		}
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_ALL, TTCN_EncDec.error_behavior_type.EB_DEFAULT);
		final TTCN_Buffer ttcn_buffer = new TTCN_Buffer();
		par.encode(dual.PDUType1_descr_, ttcn_buffer, TTCN_EncDec.coding_type.CT_RAW, 0);
		final TitanOctetString ret_val = new TitanOctetString();
		ttcn_buffer.get_string(ret_val);
		if (TTCN_Logger.log_this_event(TTCN_Logger.Severity.DEBUG_ENCDEC)) {
			TTCN_Logger.begin_event(TTCN_Logger.Severity.DEBUG_ENCDEC);
			TTCN_Logger.log_event_str("enc_PDUType1_gen(): Stream after encoding: ");
			ret_val.log();
			TTCN_Logger.end_event();
		}
		return ret_val;

	}

	public static TitanInteger dec__PDUType1(TitanOctetString stream, PDUType1 result) {

		if (TTCN_Logger.log_this_event(TTCN_Logger.Severity.DEBUG_ENCDEC)) {
			TTCN_Logger.begin_event(TTCN_Logger.Severity.DEBUG_ENCDEC);
			TTCN_Logger.log_event_str("dec_PDUType1_gen(): Stream before decoding: ");
			stream.log();
			TTCN_Logger.end_event();
		}
		TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_ALL, TTCN_EncDec.error_behavior_type.EB_WARNING);
		TTCN_EncDec.clear_error();
		final TTCN_Buffer ttcn_buffer = new TTCN_Buffer(stream);
		result.decode(dual.PDUType1_descr_, ttcn_buffer, TTCN_EncDec.coding_type.CT_RAW, 0);
		if (TTCN_Logger.log_this_event(TTCN_Logger.Severity.DEBUG_ENCDEC)) {
			TTCN_Logger.begin_event(TTCN_Logger.Severity.DEBUG_ENCDEC);
			TTCN_Logger.log_event_str("dec_PDUType1_gen(): Decoded @dual.PDUType1: ");
			result.log();
			TTCN_Logger.end_event();
		}
		if (TTCN_EncDec.get_last_error_type() == error_type.ET_NONE) {
			if (ttcn_buffer.get_pos() < ttcn_buffer.get_len()) {
				ttcn_buffer.cut();
				final TitanOctetString tmp_os = new TitanOctetString();
				ttcn_buffer.get_string(tmp_os);
				TTCN_Logger.begin_event_log2str();
				tmp_os.log();
				final TitanCharString remaining_stream = TTCN_Logger.end_event_log2str();
				TTCN_EncDec_ErrorContext.error(error_type.ET_EXTRA_DATA, "dec_PDUType1_gen(): Warning: Data remained at the end of the stream after successful decoding: %s", remaining_stream.get_value());
			}
			return new TitanInteger(0);
		} else {
			return new TitanInteger(1);
		}

	}

}
