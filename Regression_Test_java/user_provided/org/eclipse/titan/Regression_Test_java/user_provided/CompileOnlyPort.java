package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.Regression_Test_java.generated.ProcPort.CompileOnlyPort_BASE;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc2_call;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc2_reply;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc3_call;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc5_call;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc5_exception;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc5_reply;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc_exception;
import org.eclipse.titan.Regression_Test_java.generated.ProcPort.MyProc_reply;

public class CompileOnlyPort extends CompileOnlyPort_BASE {

	public CompileOnlyPort(String port_name) {
		super(port_name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void outgoing_call(MyProc3_call call_par) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_call(MyProc2_call call_par) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_call(MyProc5_call call_par) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_reply(MyProc_reply reply_par) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_reply(MyProc2_reply reply_par) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_reply(MyProc5_reply reply_par) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_raise(MyProc_exception raise_exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outgoing_raise(MyProc5_exception raise_exception) {
		// TODO Auto-generated method stub

	}

}
