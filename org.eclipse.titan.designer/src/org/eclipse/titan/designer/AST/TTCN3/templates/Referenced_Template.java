/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.List;
import java.util.Stack;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ActualParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Var_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Referenced_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Undefined_LowerIdentifier_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a referenced template.
 *
 * @author Kristof Szabados
 * */
public final class Referenced_Template extends TTCN3Template {
	public static final String CIRCULARTEMPLATEREFERENCE = "circular template reference chain: `{0}''";
	private static final String TYPEMISSMATCH1 = "Type mismatch: a signature template of type `{0}'' was expected instead of `{1}''";
	private static final String TYPEMISSMATCH2 = "Type mismatch: a value or template of type `{0}'' was expected instead of `{1}''";
	private static final String INADEQUATETEMPLATERESTRICTION = "Inadequate restriction on the referenced {0} `{1}'',"
			+ " this may cause a dynamic test case error at runtime";

	private final Reference reference;

	// TODO could be optimized by using a reference to the last referred
	// template
	public Referenced_Template(final Reference reference) {
		this.reference = reference;

		if (reference != null) {
			reference.setFullNameParent(this);
		}
	}

	public Referenced_Template(final CompilationTimeStamp timestamp, final SpecificValue_Template original) {
		copyGeneralProperties(original);
		final IValue value = original.getSpecificValue();
		switch (value.getValuetype()) {
		case REFERENCED_VALUE:
			reference = ((Referenced_Value) value).getReference();
			break;
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			final Identifier identifier = ((Undefined_LowerIdentifier_Value) value).getIdentifier();
			final FieldSubReference subReference = new FieldSubReference(identifier);
			subReference.setLocation(value.getLocation());
			reference = new Reference(null);
			reference.addSubReference(subReference);
			reference.setLocation(value.getLocation());
			reference.setFullNameParent(this);
			reference.setMyScope(value.getMyScope());
			break;
		default:
			reference = null;
			break;
		}
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.TEMPLATE_REFD;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final ITTCN3Template last = getTemplateReferencedLast(CompilationTimeStamp.getBaseTimestamp());
		if (Template_type.TEMPLATE_REFD.equals(last.getTemplatetype())) {
			return reference.getDisplayName();
		}

		final StringBuilder builder = new StringBuilder();
		builder.append(last.createStringRepresentation());

		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}

		return builder.toString();
	}

	@Override
	// Location is optimized not to store an object at it is not needed
	public Location getLocation() {
		return new Location(reference.getLocation());
	}

	@Override
	/** {@inheritDoc} */
	public void setLocation(final Location location) {
		// Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous referenced template";
		}

		return "reference template";
	}

	public Reference getReference() {
		return reference;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (reference != null) {
			reference.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		super.setCodeSection(codeSection);
		if (reference != null) {
			reference.setCodeSection(codeSection);
		}
		if (lengthRestriction != null) {
			lengthRestriction.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return null;
		}

		final IType type = assignment.getType(timestamp).getFieldType(timestamp, reference, 1, expectedValue, false);
		if (type == null) {
			setIsErroneous(true);
		}

		return type;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		final IType type = getExpressionGovernor(timestamp, expectedValue);
		if (type == null) {
			return Type_type.TYPE_UNDEFINED;
		}

		return type.getTypeRefdLast(timestamp).getTypetypeTtcn3();
	}

	/**
	 * Calculates the referenced template, and while doing so checks the
	 * reference too.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the reference chain used to detect cyclic references.
	 *
	 * @return the template referenced
	 * */
	private ITTCN3Template getTemplateReferenced(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (reference == null) {
			setIsErroneous(true);
			return null;
		}

		final Assignment ass = reference.getRefdAssignment(timestamp, true);

		if (ass == null ){
			setIsErroneous(true);
			return this;
		}
		ITTCN3Template template = null;

		switch(ass.getAssignmentType()) {
		case A_TEMPLATE:
			template = ((Def_Template) ass).getTemplate(timestamp);
			break;
		case A_VAR_TEMPLATE:
			((Def_Var_Template) ass).check(timestamp);
			template = ((Def_Var_Template) ass).getInitialValue();
			break;
		case A_MODULEPAR_TEMPLATE:
			template = ((Def_ModulePar_Template) ass).getDefaultTemplate();
			break;
		default:
			setIsErroneous(true);
			return this;
		}

		if ( template != null) {
			template = template.getReferencedSubTemplate(timestamp, reference, referenceChain);
		}

		final List<ISubReference> subreferences = reference.getSubreferences();
		if (template != null) {
			return template;
		} else if (subreferences != null && reference.hasUnfoldableIndexSubReference(timestamp)) {
			// some array indices could not be evaluated
		} else if (reference.getUsedInIsbound()) {
			return this;
		} else {
			setIsErroneous(true);
		}

		return this;
	}

	@Override
	/** {@inheritDoc} */
	public TTCN3Template getTemplateReferencedLast(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (reference == null) {
			setIsErroneous(true);
			return this;
		}

		final boolean newChain = null == referenceChain;
		IReferenceChain tempReferenceChain;
		if (newChain) {
			tempReferenceChain = ReferenceChain.getInstance(CIRCULARTEMPLATEREFERENCE, true);
		} else {
			tempReferenceChain = referenceChain;
		}

		TTCN3Template template = this;
		final Assignment ass = reference.getRefdAssignment(timestamp, true);

		if (ass != null) {
			switch(ass.getAssignmentType()) {
			case A_TEMPLATE:

				tempReferenceChain.markState();

				if (tempReferenceChain.add(this)) {
					final ITTCN3Template refd = getTemplateReferenced(timestamp, tempReferenceChain);
					if (refd != this) {
						template = refd.getTemplateReferencedLast(timestamp, referenceChain);
					}
				} else {
					setIsErroneous(true);
				}

				tempReferenceChain.previousState();
				break;
			case A_VAR_TEMPLATE:
			case A_FUNCTION_RTEMP:
			case A_MODULEPAR_TEMPLATE:
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				return this;
			default:
				setIsErroneous(true);
			}
		} else {
			setIsErroneous(true);
		}

		if (newChain) {
			tempReferenceChain.release();
		}

		return template;
	}

	/**
	 * Returns whether in the chain of referenced templates there is one
	 * which was defined to have the implicit omit attribute set
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * @param referenceChain
	 *                the ReferenceChain used to detect circular references
	 *
	 * @return true if it has, false otherwise.
	 * */
	private boolean hasTemplateImpliciteOmit(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		final boolean newChain = null == referenceChain;
		IReferenceChain tempReferenceChain;
		if (newChain) {
			tempReferenceChain = ReferenceChain.getInstance(CIRCULARTEMPLATEREFERENCE, true);
		} else {
			tempReferenceChain = referenceChain;
		}

		boolean result = false;
		if (reference != null) {
			final Assignment ass = reference.getRefdAssignment(timestamp, true);

			if (ass != null && ass.getAssignmentType() == Assignment_type.A_TEMPLATE) {
				final Def_Template templateDefinition = (Def_Template) ass;
				if (templateDefinition.hasImplicitOmitAttribute(timestamp)) {
					result = true;
				} else {
					tempReferenceChain.markState();

					if (tempReferenceChain.add(this)) {
						final ITTCN3Template refd = getTemplateReferenced(timestamp, tempReferenceChain);
						if (refd != this && refd instanceof Referenced_Template) {
							result = ((Referenced_Template) refd).hasTemplateImpliciteOmit(timestamp, referenceChain);
						}
					} else {
						setIsErroneous(true);
					}

					tempReferenceChain.previousState();
				}
			}
		}

		if (newChain) {
			tempReferenceChain.release();
		}

		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		final TTCN3Template temp = getTemplateReferencedLast(timestamp);
		if (temp != this && !temp.getIsErroneous(timestamp)) {
			temp.checkSpecificValue(timestamp, allowOmit);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		if (referenceChain.add(this) && reference != null) {
			final ISubReference subReference = reference.getSubreferences().get(0);
			if (subReference instanceof ParameterisedSubReference) {
				final ActualParameterList parameterList = ((ParameterisedSubReference) subReference).getActualParameters();
				if (parameterList != null) {
					parameterList.checkRecursions(timestamp, referenceChain);
				}
			}

			final IReferenceChain tempReferenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
			final ITTCN3Template template = getTemplateReferenced(timestamp, tempReferenceChain);
			tempReferenceChain.release();

			if (template != null && !template.getIsErroneous(timestamp) && !this.equals(template)) {
				template.checkRecursions(timestamp, referenceChain);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkExpressionSelfReferenceTemplate(final CompilationTimeStamp timestamp, final Assignment lhs) {
		final Assignment tempAssignment = reference.getRefdAssignment(timestamp, false);

		return tempAssignment == lhs;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified,
			final boolean allowOmit, final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit, final Assignment lhs) {
		if (getIsErroneous(timestamp) || reference == null) {
			return false;
		}

		final Assignment assignment = reference.getRefdAssignment(timestamp, true);
		if (assignment == null) {
			return false;
		}

		final boolean selfReference = lhs == assignment;
		assignment.check(timestamp);

		IType governor = assignment.getType(timestamp);
		if (governor != null) {
			governor = governor.getFieldType(timestamp, reference, 1, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false);
		}
		if (governor == null) {
			setIsErroneous(true);
			return selfReference;
		}

		final TypeCompatibilityInfo info = new TypeCompatibilityInfo(type, governor, true);

		if (!type.isCompatible(timestamp, governor, info, null, null)) {
			final IType last = type.getTypeRefdLast(timestamp);

			switch (last.getTypetype()) {
			case TYPE_PORT:
				// no such thing exists, remain silent
				break;
			case TYPE_SIGNATURE:
				getLocation().reportSemanticError(MessageFormat.format(TYPEMISSMATCH1, type.getTypename(), governor.getTypename()));
				setIsErroneous(true);
				break;
			default:
				getLocation().reportSemanticError(MessageFormat.format(TYPEMISSMATCH2, type.getTypename(), governor.getTypename()));
				setIsErroneous(true);
				break;
			}
		}

		// check for circular references
		final ITTCN3Template temp = getTemplateReferencedLast(timestamp);
		if (temp != this) {
			final IReferenceChain referenceChain = ReferenceChain.getInstance(CIRCULARTEMPLATEREFERENCE, true);
			final boolean referencedHasImplicitOmit = hasTemplateImpliciteOmit(timestamp, referenceChain);
			referenceChain.release();
		}

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	protected void checkTemplateSpecificLengthRestriction(final CompilationTimeStamp timestamp, final Type_type typeType) {
		final TTCN3Template last = getTemplateReferencedLast(timestamp);
		last.checkTemplateSpecificLengthRestriction(timestamp, typeType);
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkValueomitRestriction(final CompilationTimeStamp timestamp, final String definitionName, final boolean omitAllowed,
			final Location usageLocation) {
		if(reference == null) {
			if (omitAllowed) {
				checkRestrictionCommon(timestamp, getTemplateTypeName(), TemplateRestriction.Restriction_type.TR_OMIT, usageLocation);
			} else {
				checkRestrictionCommon(timestamp, getTemplateTypeName(), TemplateRestriction.Restriction_type.TR_VALUE, usageLocation);
			}
		} else {
			//if (reference != null):
			final Assignment ass = reference.getRefdAssignment(timestamp, true);
			if (Assignment_type.A_TEMPLATE == ass.getAssignmentType()) {
				final ITTCN3Template templateLast = getTemplateReferencedLast(timestamp);
				if(! this.equals(templateLast)) {
					templateLast.checkValueomitRestriction(timestamp, getTemplateTypeName(), omitAllowed, usageLocation);
				}
			}
			switch (ass.getAssignmentType()) {
			case A_TEMPLATE:
			case A_VAR_TEMPLATE:
			case A_EXT_FUNCTION_RTEMP:
			case A_FUNCTION_RTEMP:
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				if (ass instanceof Definition) {
					TemplateRestriction.Restriction_type refdTemplateRestriction = ((Definition) ass).getTemplateRestriction();
					refdTemplateRestriction = TemplateRestriction.getSubRestriction(refdTemplateRestriction, timestamp, reference);
					// if restriction is not satisfied issue warning
					if (TemplateRestriction.isLessRestrictive(omitAllowed ? TemplateRestriction.Restriction_type.TR_OMIT
							: TemplateRestriction.Restriction_type.TR_VALUE, refdTemplateRestriction)) {
						getLocation().reportSemanticWarning(
								MessageFormat.format(INADEQUATETEMPLATERESTRICTION, ass.getAssignmentName(), reference.getDisplayName()));
						return true;
					}
				}
				return false;
			default:
				return false;
			}
		}
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkPresentRestriction(final CompilationTimeStamp timestamp, final String definitionName, final Location usageLocation) {
		checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_PRESENT, usageLocation);
		if (reference != null) {
			final Assignment ass = reference.getRefdAssignment(timestamp, true);
			switch (ass.getAssignmentType()) {
			case A_TEMPLATE:
				final ITTCN3Template templateLast = getTemplateReferencedLast(timestamp);
				return templateLast.checkPresentRestriction(timestamp, definitionName, usageLocation);
			case A_VAR_TEMPLATE:
			case A_EXT_FUNCTION_RTEMP:
			case A_FUNCTION_RTEMP:
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_OUT:
			case A_PAR_TEMP_INOUT:
				if (ass instanceof Definition) {
					TemplateRestriction.Restriction_type refdTemplateRestriction = ((Definition) ass).getTemplateRestriction();
					refdTemplateRestriction = TemplateRestriction.getSubRestriction(refdTemplateRestriction, timestamp, reference);
					// if restriction not satisfied issue warning
					if (TemplateRestriction.isLessRestrictive(TemplateRestriction.Restriction_type.TR_PRESENT, refdTemplateRestriction)) {
						getLocation().reportSemanticWarning(
								MessageFormat.format(INADEQUATETEMPLATERESTRICTION, ass.getAssignmentName(), reference.getDisplayName()));
						return true;
					}
				}
				return false;
			default:
				return false;
			}
		}
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (lengthRestriction != null) {
			lengthRestriction.updateSyntax(reparser, false);
			reparser.updateLocation(lengthRestriction.getLocation());
		}

		if (baseTemplate instanceof IIncrementallyUpdateable) {
			((IIncrementallyUpdateable) baseTemplate).updateSyntax(reparser, false);
			reparser.updateLocation(baseTemplate.getLocation());
		} else if (baseTemplate != null) {
			throw new ReParseException();
		}

		if (reference != null) {
			reference.updateSyntax(reparser, false);
			reparser.updateLocation(reference.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (reference == null) {
			return;
		}

		reference.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (reference != null && !reference.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		if (lengthRestriction != null || isIfpresent /* TODO:  || get_needs_conversion()*/) {
			return false;
		}

		final TTCN3Template lastTemplate = getTemplateReferencedLast(CompilationTimeStamp.getBaseTimestamp());
		if (lastTemplate != null && lastTemplate != this && lastTemplate.hasSingleExpression()) {
			for (Scope tempScope = myScope; tempScope != null; tempScope = tempScope.getParentScope()) {
				if (tempScope == lastTemplate.getMyScope()) {
					return true;
				}
			}
		}

		return reference.hasSingleExpression();
	}

	//original:TtcnTemplate.cc Template::isValue()/case TEMPLATE_REFD
	@Override
	/** {@inheritDoc} */
	public boolean isValue(final CompilationTimeStamp timestamp) {
		final Assignment ass = reference.getRefdAssignment(timestamp, true);
		if (ass == null) {
			return true;
		}
		switch( ass.getAssignmentType()){
		case A_EXT_CONST:
		case A_PAR_VAL:
		case A_PAR_VAL_IN:
		case A_PAR_VAL_OUT:
		case A_PAR_VAL_INOUT:
		case A_VAR:
			return true;
		default:
			return false;
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getSingleExpression(final JavaGenData aData, final boolean castIsNeeded) {
		final StringBuilder result = new StringBuilder();

		if (castIsNeeded && (lengthRestriction != null || isIfpresent)) {
			result.append( "\t//TODO: fatal error while generating " );
			result.append( getClass().getSimpleName() );
			result.append( ".getSingleExpression() !\n" );
			// TODO: fatal error
			return result;
		}

		final ExpressionStruct expression = new ExpressionStruct();
		reference.generateCode(aData, expression);
		if (expression.preamble.length() > 0 || expression.postamble.length() > 0) {
			result.append( "\t//TODO: fatal error while generating " );
			result.append( getClass().getSimpleName() );
			result.append( ".getSingleExpression() !\n" );
			// TODO: fatal error
			return result;
		}

		result.append(expression.expression);

		//TODO handle cast needed

		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeExpression(final JavaGenData aData, final ExpressionStruct expression, final TemplateRestriction.Restriction_type templateRestriction) {
		if (lengthRestriction == null && !isIfpresent && templateRestriction == Restriction_type.TR_NONE) {
			//The single expression must be tried first because this rule might cover some referenced templates.
			if (hasSingleExpression()) {
				expression.expression.append(getSingleExpression(aData, true));
				return;
			}
			//TODO handle the needs conversion case
			reference.generateCode(aData, expression);
			return;
		}

		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null) {
			return;
		}

		final String tempId = aData.getTemporaryVariableName();
		expression.preamble.append(MessageFormat.format("{0} {1} = new {0}();\n", governor.getGenNameTemplate(aData, expression.expression, myScope), tempId));

		generateCodeInit(aData, expression.preamble, tempId);

		if (templateRestriction != Restriction_type.TR_NONE) {
			TemplateRestriction.generateRestrictionCheckCode(aData, expression.expression, location, tempId, templateRestriction);
		}

		expression.expression.append(tempId);
	}

	/**
	 * originally use_single_expr_for_init
	 * */
	private boolean useSingleExpressionForInit() {
		final TTCN3Template lastTemplate = getTemplateReferencedLast(CompilationTimeStamp.getBaseTimestamp());
		// return false in case of unfoldable references
		if (lastTemplate.getTemplatetype().equals(Template_type.TEMPLATE_REFD)) {
			return false;
		}

		// return false if lastTemplate is in a different module
		if (lastTemplate.getMyScope().getModuleScope() != myScope.getModuleScope()) {
			return false;
		}

		// return false if lastTemplate cannot be represented by a single expression
		if (!lastTemplate.hasSingleExpression()) {
			return false;
		}

		// return true if t_last is a generic wildcard, string pattern, etc.
		if (!lastTemplate.getTemplatetype().equals(Template_type.SPECIFIC_VALUE)) {
			return true;
		}
		// examine the specific value
		//FIXME implement
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void reArrangeInitCode(final JavaGenData aData, final StringBuilder source, final Module usageModule) {
		final ISubReference tempSubreference = reference.getSubreferences().get(0);
		if (tempSubreference instanceof ParameterisedSubReference) {
			// generate code for the templates that are used in the actual parameter
			// list of the reference
			final ActualParameterList actualParameterList = ((ParameterisedSubReference) tempSubreference).getActualParameters();
			if (actualParameterList != null) {
				actualParameterList.reArrangeInitCode(aData, source, usageModule);
			}
		}

		final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
		if (assignment.getAssignmentType() != Assignment_type.A_TEMPLATE) {
			return;
		}

		ITTCN3Template template = ((Def_Template) assignment).getTemplate(CompilationTimeStamp.getBaseTimestamp());
		final FormalParameterList formalParameterList = ((Def_Template) assignment).getFormalParameterList();
		if (formalParameterList != null) {
			// the reference points to a parameterized template
			// we must perform the rearrangement for all non-parameterized templates
			// that are referred by the parameterized template regardless of the
			// sub-references of reference
			template.reArrangeInitCode(aData, source, usageModule);
			// the parameterized template's default values must also be generated
			// (this only generates their value assignments, their declarations will
			// be generated when the template's definition is reached)
			if (assignment.getMyScope().getModuleScope() == usageModule) {
				formalParameterList.generateCodeDefaultValues(aData, source);
			}
		} else {
			// the reference points to a non-parameterized template
			final List<ISubReference> subReferences = reference.getSubreferences();
			if (subReferences != null && subReferences.size() > 1) {
				// we should follow the sub-references as much as we can
				// and perform the rearrangement for the referred field only
				for (int i = 1; i < subReferences.size(); i++) {
					final ISubReference subReference = subReferences.get(i);
					if (subReference instanceof FieldSubReference) {
						// stop if the body does not have fields
						if (template.getTemplatetype() != Template_type.NAMED_TEMPLATE_LIST) {
							break;
						}
						// the field reference can be followed
						final Identifier fieldId = ((FieldSubReference)subReference).getId();
						template = ((Named_Template_List) template).getNamedTemplate(fieldId).getTemplate();
					} else {
						// stop if the body is not a list
						if (template.getTemplatetype() != Template_type.TEMPLATE_LIST) {
							break;
						}

						IValue arrayIndex = ((ArraySubReference) subReference).getValue();
						final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
						arrayIndex = arrayIndex.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), referenceChain);
						referenceChain.release();
						if(arrayIndex.getValuetype() != Value_type.INTEGER_VALUE) {
							break;
						}

						// the index is available at compilation time
						long index = ((Integer_Value)arrayIndex).getValue();
						// index transformation in case of arrays
						if (template.getMyGovernor().getTypetype() == Type_type.TYPE_ARRAY) {
							index = index - ((Array_Type)template.getMyGovernor()).getDimension().getOffset();
						}
						template = ((Template_List) template).getTemplateByIndex((int)index);
					}
				}
			}
			// otherwise if the reference points to a top-level template
			// we should initialize its entire body
			if (assignment.getMyScope().getModuleScope() == usageModule) {
				template.generateCodeInit(aData, source, template.get_lhs_name());
			}
		}

		if (lengthRestriction != null) {
			lengthRestriction.reArrangeInitCode(aData, source, usageModule);
		}
	}

	private void generateRearrangeInitCodeReferenced(final JavaGenData aData, final StringBuilder source, final ExpressionStruct expression) {
		/**
		 * Initially we can assume that:
		 * - this is a referenced template and a part of a non-parameterized template
		 * - u.ref.ref points to (a field of) a non-parameterized template within the same module as this.
		 * - this ensures that the do-while loop will run at least twice (i.e. the first continue statement will be reached in the first iteration)
		 */
		final Stack<ISubReference> referenceStack = new Stack<ISubReference>();
		ITTCN3Template template = this;
		for ( ; ; ) {
			if (template.getTemplatetype() == Template_type.TEMPLATE_REFD) {
				final Reference reference = ((Referenced_Template) template).getReference();
				final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
				/** Don't follow the reference if:
				 *  - the referenced definition is not a template
				 *  - the referenced template is parameterized or
				 *  - the referenced template is in different module */
				if (assignment.getAssignmentType() == Assignment_type.A_TEMPLATE && ((Def_Template) assignment).getFormalParameterList() == null
						&& assignment.getMyScope().getModuleScope() == myScope.getModuleScope()) {
					// accumulate the sub-references of the referred reference
					final List<ISubReference> subReferences = reference.getSubreferences();
					if (subReferences != null && subReferences.size() > 1) {
						for(int i = subReferences.size(); i > 1; i--) {
							referenceStack.push(subReferences.get(i-1));
						}
					}
					// jump to the referred top-level template
					template = ((Def_Template) assignment).getTemplate(CompilationTimeStamp.getBaseTimestamp());
					// start the iteration from the beginning
					continue;
				} else {
					// the reference cannot be followed
					break;
				}
			}
			// stop if there are no sub-references
			if (referenceStack.isEmpty()) {
				break;
			}
			// take the topmost sub-reference
			final ISubReference subReference = referenceStack.peek();
			if (subReference instanceof FieldSubReference) {
				if (template.getTemplatetype() != Template_type.NAMED_TEMPLATE_LIST) {
					break;
				}
				// the field reference can be followed
				final Identifier fieldId = ((FieldSubReference)subReference).getId();
				template = ((Named_Template_List) template).getNamedTemplate(fieldId).getTemplate();
			} else {
				// trying to follow an array reference
				if (template.getTemplatetype() != Template_type.TEMPLATE_LIST) {
					break;
				}

				IValue arrayIndex = ((ArraySubReference) subReference).getValue();
				final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
				arrayIndex = arrayIndex.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), referenceChain);
				referenceChain.release();
				if(arrayIndex.getValuetype() != Value_type.INTEGER_VALUE) {
					break;
				}
				// the index is available at compilation time
				long index = ((Integer_Value)arrayIndex).getValue();
				// index transformation in case of arrays
				if (template.getMyGovernor().getTypetype() == Type_type.TYPE_ARRAY) {
					index = index - ((Array_Type)template.getMyGovernor()).getDimension().getOffset();
				}
				template = ((Template_List) template).getTemplateByIndex((int)index);
			}
			// the topmost sub-reference was processed
			// it can be erased from the stack
			referenceStack.pop();
		}
		// the smallest dependent template is now in t
		// generate the initializer sequence for t
		template.generateCodeInit(aData, source, template.get_lhs_name());
		// the equivalent Java code of the referenced template is composed of the
		// genname of t and the remained sub-references in refstack
		expression.expression.append(template.getGenNameOwn(myScope));
		while (!referenceStack.isEmpty()) {
			final ISubReference subReference = referenceStack.pop();
			if (subReference instanceof FieldSubReference) {
				expression.expression.append(MessageFormat.format(".get{0}()", FieldSubReference.getJavaGetterName(((FieldSubReference) subReference).getId().getName())));
			} else {
				expression.expression.append(".getAt(");
				((ArraySubReference) subReference).getValue().generateCodeExpression(aData, expression, false);
				expression.expression.append(')');
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		if (lastTimeBuilt != null && !lastTimeBuilt.isLess(aData.getBuildTimstamp())) {
			return;
		}
		lastTimeBuilt = aData.getBuildTimstamp();

		if (useSingleExpressionForInit() && hasSingleExpression()) {
			source.append(MessageFormat.format("{0}.assign({1});\n", name, getSingleExpression(aData, false)));
			return;
		}

		final ExpressionStruct expression = new ExpressionStruct();
		boolean useReferenceForCodegeneration = true;
		if (getCodeSection() == CodeSectionType.CS_POST_INIT) {
			// the referencing template is a part of a non-parameterized template
			final Assignment assignment = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
			if (assignment.getAssignmentType() == Assignment_type.A_TEMPLATE) {
				// the reference points to (a field of) a template
				//FIXME implement formal par check
				if (((Def_Template) assignment).getFormalParameterList() != null) {
					// the referred template is parameterized
					// generate the initialization sequence first for all dependent
					// non-parameterized templates
					reArrangeInitCode(aData, source, myScope.getModuleScope());
				} else if (assignment.getMyScope().getModuleScope() == myScope.getModuleScope()) {
					// the referred template is non-parameterized
					// use a different algorithm for code generation
					generateRearrangeInitCodeReferenced(aData, source, expression);
					useReferenceForCodegeneration = false;
				}
			}
		}
		if (useReferenceForCodegeneration) {
			reference.generateConstRef(aData, expression);
		}
		if (expression.preamble.length() > 0 || expression.postamble.length() > 0) {
			// the expressions within reference need temporary objects
			source.append("{\n");
			source.append(expression.preamble);
			//FIXME handle the needs conversion case
			source.append(MessageFormat.format("{0}.assign({1});\n", name, expression.expression));
			source.append(expression.postamble);
			source.append("}\n");
		} else {
			//FIXME handle needs conversion case
			source.append(MessageFormat.format("{0}.assign({1});\n", name, expression.expression));
		}

		if (lengthRestriction != null) {
			if(getCodeSection() == CodeSectionType.CS_POST_INIT) {
				lengthRestriction.reArrangeInitCode(aData, source, myScope.getModuleScope());
			}
			lengthRestriction.generateCodeInit(aData, source, name);
		}

		if (isIfpresent) {
			source.append(name);
			source.append(".set_ifPresent();\n");
		}
	}
}
