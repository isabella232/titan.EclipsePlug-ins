package org.eclipse.titanium.markers.spotters.implementation;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.statements.Port_Utility;
import org.eclipse.titan.designer.AST.TTCN3.statements.Start_Component_Statement;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;
/**
 * @author Basil Kaikoni 27-05-2019
 * detecting the usage of functions with out / inout formal  parameters in startup statement 
 */

public class FunctionsWithInoutParametersWithStartupStatement extends BaseModuleCodeSmellSpotter{
	private static final String PROBLEM = "detecting the usage of functions with out / inout formal  parameters in startup statement ";

	public FunctionsWithInoutParametersWithStartupStatement() {
		super(CodeSmellType.STARTED_FUNCTION_WITH_OUT_INOUT_FORMAL_PARAMETERS);
	}

	@Override
	protected void process(final IVisitableNode node, final Problems problems) {
		if (node instanceof Start_Component_Statement) {
			final CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp();
			final Start_Component_Statement s = (Start_Component_Statement) node;
			final IType compType = Port_Utility.checkComponentReference(timestamp, s, s.getComponent(), false, false, false);

			final Assignment assignment = s.getFunctionInstanceReference().getRefdAssignment(timestamp, false);
			if (assignment == null) {
				return;
			}

			switch (assignment.getAssignmentType()) {
			case A_FUNCTION:
				break;
			case A_FUNCTION_RTEMP:
				break;
			case A_FUNCTION_RVAL:
				break;
			default:
				return;
			}

			final Def_Function function = (Def_Function) assignment;
			final IType runsOnType = function.getRunsOnType(timestamp);

			if (compType == null || runsOnType == null || !function.isStartable()) {
				return;
			}

			final FormalParameterList fpl = function.getFormalParameterList();
			final int formalParametersNum = fpl.getNofParameters();
			int inoutFormalParametersCount = 0;

			for (int i = 0; i < formalParametersNum; i++) {
				final FormalParameter fp = fpl.getParameterByIndex(i);
				switch (fp.getAssignmentType()) {
				case A_PAR_VAL_OUT:
					inoutFormalParametersCount++;
					break;
				case A_PAR_VAL_INOUT:
					inoutFormalParametersCount++;
					break;
				default:
					break;
				}
			}

			if (inoutFormalParametersCount > 0) {
				problems.report(s.getFunctionInstanceReference().getLocation(), PROBLEM);
			}
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(Start_Component_Statement.class);
		return ret;
	}
}
