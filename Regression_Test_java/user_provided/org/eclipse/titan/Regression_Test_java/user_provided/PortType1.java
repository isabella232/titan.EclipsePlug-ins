package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.Regression_Test_java.generated.AddressPortNegTest.PortType1_BASE;
import org.eclipse.titan.Regression_Test_java.generated.AddressPortNegTest.SIP__address__type;
import org.eclipse.titan.runtime.core.TitanInteger;

public class PortType1 extends PortType1_BASE {

		
	public PortType1(final String portName) {
		// TODO Auto-generated constructor stub
		super(portName);
	}

	@Override
	protected void outgoing_send(TitanInteger send_par, SIP__address__type destination_address) {
		// TODO Auto-generated method stub
		
	}
}
