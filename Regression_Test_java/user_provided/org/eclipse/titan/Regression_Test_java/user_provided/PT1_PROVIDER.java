package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.Regression_Test_java.generated.dual;
import org.eclipse.titan.Regression_Test_java.generated.dual.ControlRequest;
import org.eclipse.titan.Regression_Test_java.generated.dual.ControlResponse;
import org.eclipse.titan.Regression_Test_java.generated.dual.ErrorSignal;
import org.eclipse.titan.Regression_Test_java.generated.dual.PDUType1;
import org.eclipse.titan.Regression_Test_java.generated.dual.PDUType2;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanOctetString;
import org.eclipse.titan.runtime.core.TitanPort;
import org.eclipse.titan.runtime.core.TitanUniversalCharString;

public abstract class PT1_PROVIDER extends TitanPort {
	
	public PT1_PROVIDER(final String portName) {
		super(portName);
	}
	
	protected void outgoing_send(final TitanOctetString send_par) {
		incoming_message(send_par);
	}
	
	protected void outgoing_send(final ControlRequest send_par) {
		if( send_par.get_field_text().get_at(0).operator_equals("C")) {
			ControlResponse r = new ControlResponse(new TitanCharString("CResp from PT1_PROVIDER"));
			incoming_message(r);
		} else {
			incoming_message(new dual.ErrorSignal(new TitanCharString("Not C")));
		}
	}

	protected void outgoing_send(final PDUType1 send_par) {
		incoming_message(dual.enc__PDUType1__gen(send_par));
	}
	
	protected void outgoing_send(final PDUType2 send_par) {
		TitanOctetString os = new TitanOctetString();
		dual.PDUType2.PDUType2_encoder(send_par, os, new TitanUniversalCharString("RAW"));
		incoming_message(os);
	}
	
	protected abstract void incoming_message(final ControlResponse incoming_par);
//	protected abstract void incoming_message(final PDUType1 incoming_par);
	protected abstract void incoming_message(final TitanOctetString incoming_par);
	protected abstract void incoming_message(final ErrorSignal errorSignal);
//	protected abstract void incoming_message(final PreGenRecordOf.PREGEN__RECORD__OF__CHARSTRING incoming_par);
//	protected abstract void incoming_message(final PreGenRecordOf.PREGEN__RECORD__OF__OCTETSTRING incoming_par);
//	protected abstract void incoming_message(final PDUType2 incoming_par);
}
