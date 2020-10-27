package org.eclipse.titan.Regression_Test_java.user_provided;

import org.eclipse.titan.Regression_Test_java.generated.map__param__common;
import org.eclipse.titan.Regression_Test_java.generated.map__param__common.PT_BASE;
import org.eclipse.titan.runtime.core.TTCN_Logger;
import org.eclipse.titan.runtime.core.TTCN_Runtime;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanOctetString;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;

public class PT extends PT_BASE {

	public PT(String port_name) {
		super(port_name);
	}

	@Override
	protected void outgoing_send(TitanCharString send_par) {
		// TODO Auto-generated method stub

	}
	
	
	@Override
	protected void user_map(final String system_port, final Map_Params params) {
		//based on titan.core/regression_test/map_params/PT.cc
		if (params.get_nof_params() != 0) {
			TitanCharString p1_str = params.get_param(0);
			if (p1_str.lengthof().is_greater_than(0)) {
				TitanOctetString p1 = new TitanOctetString();
				TitanCharString.string_to_ttcn(p1_str, p1);
				//check the value
				if (p1.operator_not_equals(map__param__common.P1__INITIAL)) {
					final String reason = "Initial value of parameter p1 is incorrect: ".concat(p1_str.toString());
					TTCN_Runtime.setverdict(VerdictTypeEnum.FAIL, reason);
				}
			}
		} else {
			TTCN_Runtime.setverdict(VerdictTypeEnum.FAIL, "Parameter p1 is unset");
		}
		if ( params.get_nof_params() > 1 ) {
			TitanCharString p2_str = params.get_param(1);
			if (p2_str.lengthof().operator_equals(0)) {
				//OK
				// now set the output value
				params.set_param(1, TitanCharString.ttcn_to_string(map__param__common.P2__FINAL));
			} else {
				final String reason = "Parameter p2 is set: ".concat(p2_str.toString());
				TTCN_Runtime.setverdict(VerdictTypeEnum.FAIL, reason);
			}
			map__param__common.CT_component_map__param.get().operator_assign( true );
		} else {
			map__param__common.CT_component_map__empty.get().operator_assign( true );
		}
	}
	
	


}
