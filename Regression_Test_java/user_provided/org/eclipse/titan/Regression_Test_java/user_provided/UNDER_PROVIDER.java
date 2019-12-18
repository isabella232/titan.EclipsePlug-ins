package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.runtime.core.TitanOctetString;
import org.eclipse.titan.runtime.core.TitanPort;

public abstract class UNDER_PROVIDER extends TitanPort {

	
	public UNDER_PROVIDER(final String portName) {
		super(portName);
	}
	protected void outgoing_send(final TitanOctetString send_par) {
		incoming_message(send_par);
	}

	protected abstract void incoming_message(final TitanOctetString incoming_par);
}
