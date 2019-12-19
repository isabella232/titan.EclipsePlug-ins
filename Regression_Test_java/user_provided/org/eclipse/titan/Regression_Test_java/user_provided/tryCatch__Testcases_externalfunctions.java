package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TtcnError;

public class tryCatch__Testcases_externalfunctions {

	public static void throw_(TitanCharString e) {
		throw new TtcnError(e.toString());
	}

}
