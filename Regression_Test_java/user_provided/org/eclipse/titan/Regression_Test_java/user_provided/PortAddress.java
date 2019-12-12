package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc2_reply;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc_call;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc_exception;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc_reply;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.PortAddress_BASE;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.TitanAddress;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.s__StopPTC_call;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.s__StopPTC_reply;
import org.eclipse.titan.runtime.core.TitanComponent;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.TtcnError;

public class PortAddress extends PortAddress_BASE {

	public PortAddress(String port_name) {
		super(port_name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void outgoing_call(MyProc_call call_par, TitanAddress destination_address) {
		//based on PortAddress.cc
		if( destination_address == null) { 
			throw new TtcnError("Address is a NULL pointer.");
		}
		TitanAddress a = new TitanAddress(); //sender_omponent
		a.get_field_a1().operator_assign(destination_address.get_field_a2());
		a.get_field_a2().operator_assign(destination_address.get_field_a1());
		if( a.get_field_a1().operator_equals(67)) {
			MyProc_reply tmp = new MyProc_reply();
		    tmp.get_field_Par2().operator_assign("Charstring");
		    tmp.get_field_Par3().operator_assign(1.41);
		    tmp.get_return_value().operator_assign(true);
		    incoming_reply(tmp, TitanComponent.SYSTEM_COMPREF, a);
		} else if( a.get_field_a1().operator_equals(68)) {
			TitanInteger otherValue = a.get_field_a1().add(a.get_field_a2());
			incoming_exception( new MyProc_exception(otherValue),TitanComponent.SYSTEM_COMPREF, a);
		} else {
			throw new TtcnError("Error in address");
		}
	}

	@Override
	public void outgoing_call(s__StopPTC_call call_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outgoing_reply(MyProc2_reply reply_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_reply(MyProc_reply reply_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_reply(s__StopPTC_reply reply_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_raise(MyProc_exception raise_exception, TitanAddress destination_address) {
		// TODO Auto-generated method stub

	}

}
