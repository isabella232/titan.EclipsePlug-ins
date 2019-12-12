package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.Regression_Test_java.generated.ProcPort.CompileOnlyPortAddress_BASE;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc2_reply;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc3_call;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc3_exception;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc3_reply;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc4_call;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc5_exception;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc5_reply;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc_call;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.TitanAddress;

public class CompileOnlyPortAddress extends CompileOnlyPortAddress_BASE {

	public CompileOnlyPortAddress(String port_name) {
		super(port_name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void outgoing_call(MyProc_call call_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outgoing_call(MyProc4_call call_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outgoing_call(MyProc3_call call_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outgoing_reply(MyProc2_reply reply_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outgoing_reply(MyProc5_reply reply_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outgoing_reply(MyProc3_reply reply_par, TitanAddress destination_address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outgoing_raise(MyProc5_exception raise_exception, TitanAddress destination_address) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outgoing_raise(MyProc3_exception raise_exception, TitanAddress destination_address) {
		// TODO Auto-generated method stub
		
	}

}
