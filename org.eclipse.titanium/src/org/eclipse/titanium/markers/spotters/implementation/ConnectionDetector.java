package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.statements.Connect_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Port_Utility;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody.OperationModes;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * 
 * @author jasla
 * This is class is used to detect whether connection is unable or not
 * when connect statement is used.
 */

public class ConnectionDetector extends BaseModuleCodeSmellSpotter {
	private static final String ERROR_MESSAGE = "Neither port type `{0}'' nor port type `{1}'' can send messages";

	public ConnectionDetector() {
		super(CodeSmellType.CONNECTION_DETECTOR);
	}

	@Override
	public void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof Connect_Statement) {
			final Connect_Statement s = (Connect_Statement) node;

			PortTypeBody body1;
			PortTypeBody body2;

			final IType portType1 = Port_Utility.checkConnectionEndpoint(CompilationTimeStamp.getBaseTimestamp(), s, s.getComponentReference1(), s.getPortReference1(), false);
			if (portType1 == null) {
				body1 = null;
			} else {
				body1 = ((Port_Type) portType1).getPortBody();
			}

			final IType portType2 = Port_Utility.checkConnectionEndpoint(CompilationTimeStamp.getBaseTimestamp(), s, s.getComponentReference2(), s.getPortReference2(), false);
			if (portType2 == null) {
				body2 = null;
			} else {
				body2 = ((Port_Type) portType2).getPortBody();
			}

			if ((OperationModes.OP_Message.equals(body1.getOperationMode()) || OperationModes.OP_Mixed.equals(body1.getOperationMode())) && body2.getOutMessage() == null) {
				problems.report(s.getLocation(), MessageFormat.format(ERROR_MESSAGE, portType1.getTypename(), portType2.getTypename()));
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Connect_Statement.class);
		return ret;
	}
}