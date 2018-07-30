package org.eclipse.titan.designer.AST;

import java.text.MessageFormat;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Extfunction;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Special reference type used by map, unmap, connect, disconnect statements.
 *
 * This reference is never part of the scope hierarchy,
 * but only references a port within a component type.
 *
 * */
public class PortReference extends Reference {
	private static final String NOPORTWITHNAME = "Component type `{0}'' does not have a port with name `{1}''";

	private Component_Type componentType;

	public PortReference(final Reference reference) {
		super(null, reference.getSubreferences());
	}

	public void setComponent(final Component_Type componentType) {
		this.componentType = componentType;
	}

	/** @return a new instance of this reference */
	public Reference newInstance() {
		ErrorReporter.INTERNAL_ERROR("Port referencies should not be cloned");
		return null;
	}

	@Override
	public Assignment getRefdAssignment(final CompilationTimeStamp timestamp, final boolean checkParameterList) {
		if(myScope == null || componentType == null) {
			return null;
		}

		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return referredAssignment;
		}

		super.getRefdAssignment(timestamp, checkParameterList);//just for error compatibility with...
		final Identifier portIdentifier = getId();
		final ComponentTypeBody componentBody = componentType.getComponentBody();
		if (!componentBody.hasLocalAssignmentWithId(portIdentifier)) {
			getLocation().reportSemanticError(
					MessageFormat.format(NOPORTWITHNAME, componentType.getTypename(), portIdentifier.getDisplayName()));
			setIsErroneous(true);
			referredAssignment = null;
			lastTimeChecked = timestamp;
			return null;
		}

		referredAssignment = componentBody.getLocalAssignmentById(portIdentifier);

		if (referredAssignment != null) {
			referredAssignment.check(timestamp);
			referredAssignment.setUsed();

			if (referredAssignment instanceof Definition) {
				final String referingModuleName = getMyScope().getModuleScope().getName();
				if (!((Definition) referredAssignment).referingHere.contains(referingModuleName)) {
					((Definition) referredAssignment).referingHere.add(referingModuleName);
				}
			}
		}

		lastTimeChecked = timestamp;

		return referredAssignment;
	}

	/**
	 * Add generated java code on this level.
	 * @param aData only used to update imports if needed
	 * @param expression the expression for code generated
	 * @param forcedScope the scope to be used as the local scope.
	 */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression, final Scope forcedScope ) {
		if (referredAssignment == null) {
			return;
		}

		boolean isTemplate;
		IType referedGovernor;
		switch (referredAssignment.getAssignmentType()) {
		case A_CONST:
		case A_EXT_CONST:
		case A_MODULEPAR:
		case A_VAR:
		case A_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RVAL:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
			isTemplate = false;
			referedGovernor = referredAssignment.getType(CompilationTimeStamp.getBaseTimestamp());
			break;
		case A_MODULEPAR_TEMPLATE:
		case A_TEMPLATE:
		case A_VAR_TEMPLATE:
		case A_PAR_TEMP_IN:
		case A_PAR_TEMP_OUT:
		case A_PAR_TEMP_INOUT:
			isTemplate = true;
			referedGovernor = referredAssignment.getType(CompilationTimeStamp.getBaseTimestamp());
			break;
		default:
			isTemplate = false;
			referedGovernor = null;
			break;
		}

		FormalParameterList formalParameterList;
		switch (referredAssignment.getAssignmentType()) {
		case A_FUNCTION:
		case A_FUNCTION_RVAL:
		case A_FUNCTION_RTEMP:
			formalParameterList = ((Def_Function) referredAssignment).getFormalParameterList();
			break;
		case A_EXT_FUNCTION:
		case A_EXT_FUNCTION_RVAL:
		case A_EXT_FUNCTION_RTEMP:
			formalParameterList = ((Def_Extfunction) referredAssignment).getFormalParameterList();
			break;
		case A_TEMPLATE:
			formalParameterList = ((Def_Template) referredAssignment).getFormalParameterList();
			break;
		default:
			formalParameterList = null;
			break;
		}

		if (subReferences.get(0) instanceof ParameterisedSubReference) {
			expression.expression.append(referredAssignment.getGenNameFromScope(aData, expression.expression, forcedScope, null));
			expression.expression.append("( ");
			final ParameterisedSubReference temp = ((ParameterisedSubReference)subReferences.get(0));
			temp.getActualParameters().generateCodeAlias(aData, expression);
			expression.expression.append(" )");
		} else if (formalParameterList != null) {
			//the reference does not have an actual parameter list, but the assignment has
			expression.expression.append(referredAssignment.getGenNameFromScope(aData, expression.expression, forcedScope, null));
			expression.expression.append("( ");
			//FieldSubReference temp = ((FieldSubReference)subReferences.get(0));
			for (int i = 0; i < formalParameterList.getNofParameters(); i++) {
				if (i > 0){
					expression.expression.append(", ");
				}
				formalParameterList.getParameterByIndex(i).getDefaultValue().generateCode(aData, expression);
			}

			//temp.getActualParameters().generateCodeAlias(aData, expression);
			expression.expression.append(" )");
		} else {
			//TODO add fuzzy handling
			expression.expression.append(referredAssignment.getGenNameFromScope(aData, expression.expression, forcedScope, null));
		}

		if (referredAssignment.getMyScope() instanceof ComponentTypeBody) {
			switch (referredAssignment.getAssignmentType()) {
			case A_VAR:
			case A_VAR_TEMPLATE:
			case A_PORT:
			case A_TIMER:
				expression.expression.append(".get()");
				break;
			default:
				break;
			}
		}

		generateCode(aData, expression, isTemplate, false, referedGovernor);
	}
}
