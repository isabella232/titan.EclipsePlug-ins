/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.TypeOwner_type;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents an in-line template. Consists of: - an optional type - an optional
 * template reference with or without actual parameter list (in case of in-line
 * modified template) - a mandatory template body
 *
 * @author Kristof Szabados
 */
public final class TemplateInstance extends ASTNode implements ILocateableNode, IIncrementallyUpdateable {
	private static final String FULLNAMEPART1 = ".<type>";
	private static final String FULLNAMEPART2 = ".<derivedReference>";

	private static final String INCOMATIBLEEXPLICITETYPE = "Incompatible explicit type specification: `{0}'' was expected instead of {1}";
	private static final String INCOMPATIBLEBASETYPE = "Base template `{0}'' has incompatible type: `{1}'' was expected instead of `{2}''";
	private static final String TEMPLATEREFERENCEXPECTED = "Reference to a template was expected instead of {0}";

	private final Type type;
	private final Reference derivedReference;

	/** The body of the template instance. Can not be null */
	private final TTCN3Template templateBody;

	/** the time when this template was check the last time. */
	private CompilationTimeStamp lastTimeChecked;

	/**
	 * The location of the whole instance. This location encloses the
	 * instance fully, as it is used to report errors to.
	 **/
	private Location location;

	public TemplateInstance(final Type type, final Reference derivedReference, final TTCN3Template templateBody) {
		this.type = type;
		this.derivedReference = derivedReference;
		this.templateBody = templateBody;
		location = NULL_Location.INSTANCE;

		if (type != null && type.getNameParent() == null) {
			type.setOwnertype(TypeOwner_type.OT_TEMPLATE_INST, this);
			type.setFullNameParent(this);
		}
		if (derivedReference != null && derivedReference.getNameParent() == null) {
			derivedReference.setFullNameParent(this);
		}
		if (templateBody.getNameParent() == null) {
			templateBody.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public Location getLocation() {
		return location;
	}

	/** @return the type of this template instance */
	public Type getType() {
		return type;
	}

	/** @return the derived reference */
	public Reference getDerivedReference() {
		return derivedReference;
	}

	/** @return the body of the template */
	public TTCN3Template getTemplateBody() {
		return templateBody;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (type != null) {
			type.setMyScope(scope);
		}
		if (derivedReference != null) {
			derivedReference.setMyScope(scope);
		}

		templateBody.setMyScope(scope);
	}

	/**
	 * originally has_single_expr
	 * */
	public boolean hasSingleExpression() {
		if (derivedReference != null) {
			return false;
		}
		if (type != null) {
			return false;
		}

		return templateBody.hasSingleExpression();
	}

	/**
	 * Sets the code_section attribute for this instance to the provided value.
	 *
	 * @param codeSection the code section where this instance should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (derivedReference != null) {
			derivedReference.setCodeSection(codeSection);
		}

		templateBody.setCodeSection(codeSection);
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(FULLNAMEPART1);
		} else if (derivedReference == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	/**
	 * Creates and returns a string representation if the actual template
	 * instance.
	 *
	 * @return the string representation of the template instance.
	 * */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder();
		if (type != null) {
			builder.append(type.getTypename()).append(" : ");
		}
		if (derivedReference != null) {
			builder.append("modifies ").append(derivedReference.getDisplayName()).append(" := ");
		}
		builder.append(templateBody.createStringRepresentation());

		return builder.toString();
	}

	/**
	 * Calculates the governor of the template instance when used in an
	 * expression.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 *
	 * @return the governor of the template instance if it was used in an
	 *         expression.
	 * */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (type != null) {
			return type;
		}

		if (derivedReference != null) {
			final IType result = checkDerivedReference(timestamp, null);
			if (result != null) {
				return result;
			}
		}

		return templateBody.getExpressionGovernor(timestamp, expectedValue);
	}

	/**
	 * Returns the type of the template instance to be used in expression
	 * evaluation.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param expectedValue
	 *                the kind of the value to be expected.
	 *
	 * @return the type of the template instance
	 * */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		IType tempType = type;

		if(tempType == null) {
			if (derivedReference != null) {
				final IType result = checkDerivedReference(timestamp, null);
				if (result != null) {
					tempType = result;
				}
			}
		}

		if (tempType == null) {
			return templateBody.getExpressionReturntype(timestamp, expectedValue);
		}

		tempType.check(timestamp);

		return tempType.getTypeRefdLast(timestamp).getTypetypeTtcn3();
	}

	/**
	 * Checks the member type (if present) against a governor.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param governor
	 *                the type to check the member against.
	 *
	 * @return the type that shall be considered in further checks.
	 **/
	public IType checkType(final CompilationTimeStamp timestamp, final IType governor) {
		if (type == null) {
			return governor;
		}

		if (governor == null || governor.getIsErroneous(timestamp)) {
			return type;
		}

		if (governor.isCompatible(timestamp, type, null, null, null)) {
			return governor;
		}

		if (!type.getIsErroneous(timestamp)) {
			type.getLocation().reportSemanticError(
					MessageFormat.format(INCOMATIBLEEXPLICITETYPE, governor.getTypename(), type.getTypename()));
		}
		return type;
	}

	public IType checkDerivedReference(final CompilationTimeStamp timestamp, final IType governor) {
		if (derivedReference == null) {
			return governor;
		}

		final Assignment assignment = derivedReference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return governor;
		}

		switch (assignment.getAssignmentType()) {
		case A_TEMPLATE:
			return checkBaseTypeCompatibility(timestamp, governor, assignment, true);
		case A_VAR_TEMPLATE:
			return checkBaseTypeCompatibility(timestamp, governor, assignment, false);
		case A_PAR_TEMP_IN:
			return checkBaseTypeCompatibility(timestamp, governor, assignment, false);
		case A_PAR_TEMP_OUT:
			return checkBaseTypeCompatibility(timestamp, governor, assignment, false);
		case A_PAR_TEMP_INOUT:
			return checkBaseTypeCompatibility(timestamp, governor, assignment, false);
		case A_FUNCTION_RTEMP:
			return checkBaseTypeCompatibility(timestamp, governor, assignment, false);
		case A_EXT_FUNCTION_RTEMP:
			return checkBaseTypeCompatibility(timestamp, governor, assignment, false);
		default:
			derivedReference.getLocation().reportSemanticError(
					MessageFormat.format(TEMPLATEREFERENCEXPECTED, assignment.getAssignmentName()));
			return governor;
		}
	}

	private IType checkBaseTypeCompatibility(final CompilationTimeStamp timestamp, final IType governor, final Assignment derivedAssignment,
			final boolean setBaseTemplate) {
		boolean internalSetBase = setBaseTemplate;
		IType tempGovernor = governor;

		if (tempGovernor == null) {
			tempGovernor = type;
		}

		final IType baseTemplateType = derivedAssignment.getType(timestamp);
		if (tempGovernor != null) {
			if (!tempGovernor.isCompatible(timestamp, baseTemplateType, null, null, null)) {
				derivedReference.getLocation().reportSemanticError(
						MessageFormat.format(INCOMPATIBLEBASETYPE, derivedAssignment.getFullName(),
								tempGovernor.getTypename(), baseTemplateType.getTypename()));

				if (type == null) {
					tempGovernor = baseTemplateType;
				}

				internalSetBase = false;
			}
		} else {
			tempGovernor = baseTemplateType;
		}

		if (internalSetBase && derivedAssignment instanceof Def_Template) {
			templateBody.setBaseTemplate(((Def_Template) derivedAssignment).getTemplate(timestamp));
		}

		return tempGovernor;
	}

	/**
	 * Checks the entire template instance against a governor.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param governor
	 *                the type to check against.
	 * */
	public void check(final CompilationTimeStamp timestamp, final IType governor) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (type != null) {
			type.setIsErroneous(false);
		}
		templateBody.setIsErroneous(false);

		if (governor == null) {
			lastTimeChecked = timestamp;
			return;
		}

		IType localGovernor = checkType(timestamp, governor);
		localGovernor = checkDerivedReference(timestamp, localGovernor);
		templateBody.setMyGovernor(localGovernor);
		final ITTCN3Template temporalBody = localGovernor.checkThisTemplateRef(timestamp, templateBody);
		temporalBody.checkThisTemplateGeneric(timestamp, localGovernor, derivedReference != null, true, true, true, false, null);//TODO: too much automatic true (baat)

		lastTimeChecked = timestamp;
	}

	/**
	 * Checks the template restriction applied to a template.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param definition
	 *                the definition to check.
	 *
	 * @return true if the restriction needs runtime check, false otherwise.
	 * */
	public boolean checkRestriction(final CompilationTimeStamp timestamp, final Definition definition) {
		boolean needsRuntimeCheck = false;

		if (derivedReference != null) {
			final Assignment ass = derivedReference.getRefdAssignment(timestamp, true);
			switch (ass.getAssignmentType()) {
			case A_TEMPLATE:
			case A_VAR_TEMPLATE:
				needsRuntimeCheck = TemplateRestriction.check(timestamp, definition, templateBody, null);
				break;
				//case A_VAR_TEMPLATE:
			case A_EXT_FUNCTION_RTEMP:
			case A_FUNCTION_RTEMP:
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_OUT:
				// create a temporary Referenced_Template to be
				// added as the
				// base template than check and remove after
				// checked
				final Referenced_Template temp = new Referenced_Template(derivedReference);
				temp.setLocation(derivedReference.getLocation());
				templateBody.setBaseTemplate(temp);
				needsRuntimeCheck = TemplateRestriction.check(timestamp, definition, templateBody, null);
				templateBody.setBaseTemplate(null);
				break;
			default:
				break;
			}
		} else {
			needsRuntimeCheck = TemplateRestriction.check(timestamp, definition, templateBody, null);
		}
		return needsRuntimeCheck;
	}

	/**
	 * Checks whether this template instance is defining itself in a
	 * recursive way. This can happen for example if a constant is using
	 * itself to determine its initial value.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references.
	 * */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		templateBody.checkRecursions(timestamp, referenceChain);
	}

	/**
	 * Handles the incremental parsing of this template instance.
	 *
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (type != null) {
			type.updateSyntax(reparser, false);
			reparser.updateLocation(type.getLocation());
		}

		if (derivedReference != null) {
			derivedReference.updateSyntax(reparser, false);
			reparser.updateLocation(derivedReference.getLocation());
		}

		templateBody.updateSyntax(reparser, false);
		reparser.updateLocation(templateBody.getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
		if (derivedReference != null) {
			derivedReference.findReferences(referenceFinder, foundIdentifiers);
		}
		if (templateBody != null) {
			templateBody.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (type != null && !type.accept(v)) {
			return false;
		}
		if (derivedReference != null && !derivedReference.accept(v)) {
			return false;
		}
		if (templateBody != null && !templateBody.accept(v)) {
			return false;
		}
		return true;
	}

	/**
	 * Add generated java code on this level.
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression code generated
	 * @param templateRestriction the template restriction to check in runtime
	 */
	public void generateCode( final JavaGenData aData, final ExpressionStruct expression , final TemplateRestriction.Restriction_type templateRestriction) {
		if (derivedReference != null) {
			final ExpressionStruct derivedExpression = new ExpressionStruct();
			derivedReference.generateCode(aData, derivedExpression);
			final String tempId = aData.getTemporaryVariableName();

			expression.preamble.append(derivedExpression.preamble);
			expression.preamble.append(MessageFormat.format("{0} {1} = new {0}({2});\n", templateBody.getMyGovernor().getGenNameTemplate(aData, expression.expression, myScope), tempId, derivedExpression.expression));

			// perform the modifications on the temporary variable
			templateBody.generateCodeInit(aData, expression.preamble, tempId);
			if (templateRestriction != Restriction_type.TR_NONE) {
				TemplateRestriction.generateRestrictionCheckCode(aData, expression.preamble, location, tempId, templateRestriction);
			}

			expression.expression.append(tempId);
		} else {
			//TODO handle decoded redirect
			templateBody.generateCodeExpression( aData, expression, templateRestriction );
			//TODO handle decoded redirect
		}
	}

	/**
	 * Walks through the templateinstance recursively and appends the java
	 * initialization sequence of all (directly or indirectly) referenced
	 * non-parameterized templates and the default values of all
	 * parameterized templates to source and returns the resulting string.
	 * Only objects belonging to module usageModule are initialized.
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source for code generated
	 * @param usageModule the module where the template is to be used.
	 * */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		if (derivedReference != null) {
			final List<ISubReference> subreferences = derivedReference.getSubreferences();
			if (subreferences != null && !subreferences.isEmpty() && subreferences.get(0) instanceof ParameterisedSubReference) {
				final ParameterisedSubReference subreference = (ParameterisedSubReference) subreferences.get(0);
				final ActualParameterList actualParameterList = subreference.getActualParameters();
				if (actualParameterList != null) {
					actualParameterList.reArrangeInitCode(aData, source, usageModule);
				}
			}

			final Assignment assignment = derivedReference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
			if (assignment == null) {
				return;
			}

			if (assignment.getAssignmentType() == Assignment_type.A_TEMPLATE) {
				final ITTCN3Template template = ((Def_Template)assignment).getTemplate(CompilationTimeStamp.getBaseTimestamp());
				final FormalParameterList formalParameterList = ((Def_Template)assignment).getFormalParameterList();
				if (formalParameterList != null) {
					// the referred template is parameterized
					// the embedded referenced templates shall be visited
					template.reArrangeInitCode(aData, source, usageModule);

					// the constants used for default values have to be initialized now
					if (assignment.getMyScope().getModuleScope() == usageModule) {
						formalParameterList.generateCodeDefaultValues(aData, source);
					}
				} else {
					// the referred template is not parameterized
					// its entire body has to be initialized now
					if (assignment.getMyScope().getModuleScope() == usageModule) {
						template.generateCodeInit(aData, source, template.get_lhs_name());
					}
				}
			}
		}

		if (templateBody != null) {
			templateBody.reArrangeInitCode(aData, source, usageModule);
		}
	}
}
