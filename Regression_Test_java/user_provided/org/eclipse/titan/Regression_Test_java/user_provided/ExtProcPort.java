package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.Regression_Test_java.generated.ProcPort.ExtProcPort_BASE;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc5_call;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc5_exception;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc5_reply;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanFloat;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.AdditionalFunctions;
public class ExtProcPort extends ExtProcPort_BASE {

	public ExtProcPort(String port_name) {
		super(port_name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void outgoing_call(MyProc5_call call_par) {
		TitanInteger a = call_par.constGet_field_A();
		TitanInteger b = call_par.constGet_field_B();
		TitanFloat f = AdditionalFunctions.int2float(a);
		if( TitanInteger.operator_equals(0, b)) {
			incoming_exception(new MyProc5_exception(new TitanCharString("Divide by 0.")));
		} else {
			f = f.div(AdditionalFunctions.int2float(b));
			MyProc5_reply tmp = new MyProc5_reply();
			tmp.get_return_value().operator_assign(f);
			incoming_reply(tmp);
		}
	}

	@Override
	public void outgoing_reply(MyProc5_reply reply_par) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_raise(MyProc5_exception raise_exception) {
		// TODO Auto-generated method stub

	}

}
