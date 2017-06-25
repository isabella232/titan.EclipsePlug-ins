/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents a template that matches everything but the elements in a list.
 *
 * @author Kristof Szabados
 * */
public final class ComplementedList_Template extends CompositeTemplate {
	private static final String ANYOROMITWARNING = "`*'' in complemented list. This template will not match anything.";

	public ComplementedList_Template(final ListOfTemplates templates) {
		super(templates);
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.COMPLEMENTED_LIST;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous complemented list match";
		}

		return "complemented list match";
	}

	@Override
	/** {@inheritDoc} */
	public IType getExpressionGovernor(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (myGovernor != null) {
			return myGovernor;
		}

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final IType type = templates.getTemplateByIndex(i).getExpressionGovernor(timestamp, expectedValue);
			if (type != null) {
				return type;
			}
		}

		return null;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		if (getIsErroneous(timestamp)) {
			return Type_type.TYPE_UNDEFINED;
		}

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final Type_type type = templates.getTemplateByIndex(i).getExpressionReturntype(timestamp, expectedValue);
			if (!Type_type.TYPE_UNDEFINED.equals(type)) {
				return type;
			}
		}

		return Type_type.TYPE_UNDEFINED;
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of a complemented list match");
	}

	@Override
	/** {@inheritDoc} */
	public void checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified,
			final boolean allowOmit, final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit) {

		if(type == null){
			return;
		}

		final boolean allowOmitInValueList = allowOmitInValueList(allowOmit);

		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final ITemplateListItem component = templates.getTemplateByIndex(i);
			component.setMyGovernor(type);
			final ITTCN3Template temporalComponent = type.checkThisTemplateRef(timestamp, component);
			temporalComponent.checkThisTemplateGeneric(timestamp, type, false, allowOmitInValueList, true, subCheck, implicitOmit);

			if (Template_type.ANY_OR_OMIT.equals(temporalComponent.getTemplatetype())) {
				component.getLocation().reportSemanticWarning(ANYOROMITWARNING);
			}
		}

		checkLengthRestriction(timestamp, type);
		if (!allowOmit && isIfpresent) {
			location.reportSemanticError("`ifpresent' is not allowed here");
		}
		if (subCheck) {
			type.checkThisTemplateSubtype(timestamp, this);
		}
	}

	/**
	 * If ALLOW_OMIT_IN_VALUELIST_TEMPLATE_PROPERTY is switched on
	 * and has AnyOrOmit (=AnyOrNone) or omit in the list then accepted, otherwise not
	 */
	@Override
	public boolean checkPresentRestriction(final CompilationTimeStamp timestamp, final String definitionName, final Location usageLocation) {
		checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_PRESENT, usageLocation);

		final boolean allowOmitInValueList = allowOmitInValueList(true);
		if(allowOmitInValueList) {
			boolean hasAnyOrOmit = false;
			for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
				final ITemplateListItem component = templates.getTemplateByIndex(i);

				// === if OMIT_VALUE then hasOmitValue=true and break ====
				// componentType == ITTCN3Template.Template_type.OMIT_VALUE does not work
				// TODO: put this if-block to a higher level
				//TODO: avoid NPE (?)
				if(component instanceof TemplateBody) {
					final ITTCN3Template template = ((TemplateBody) component).getTemplate();
					if(Template_type.SPECIFIC_VALUE.equals(template.getTemplatetype())){
						final IValue value = ((SpecificValue_Template) template).getSpecificValue();
						if( Value_type.OMIT_VALUE.equals(value.getValuetype())){
							hasAnyOrOmit = true;
							break;
						}
					}
				}

				final TTCN3Template.Template_type componentType =  component.getTemplatetype();
				if (ITTCN3Template.Template_type.ANY_OR_OMIT.equals(componentType)) {
					hasAnyOrOmit = true;
					break;
				}

			}
			if (!hasAnyOrOmit) {
				location.reportSemanticError(MessageFormat.format(RESTRICTIONERROR+" without omit or AnyValueOrNone in the list", definitionName, getTemplateTypeName()));
				return false;
			}
		}
		// some basic check was performed, always needs runtime check
		return true;
	}

	@Override
	protected String getNameForStringRep() {
		return "complement";
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getSingleExpression(final JavaGenData aData, final boolean castIsNeeded) {
		StringBuilder result = new StringBuilder();

		result.append( "\t//TODO: fatal error while generating " );
		result.append( getClass().getSimpleName() );
		result.append( ".getSingleExpression() !\n" );
		// TODO: fatal error
		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		aData.addBuiltinTypeImport( "Base_Template.template_sel" );

		int nofTs = templates.getNofTemplates();
		String typeName = myGovernor.getGenNameTemplate(aData, source, myScope);
		//TODO: add support for all_from

		source.append(name);
		source.append(".setType( template_sel.COMPLEMENTED_LIST, ");
		source.append(nofTs);
		source.append( " );\n" );

		for (int i = 0 ; i < nofTs ; i++) {
			ITemplateListItem template = templates.getTemplateByIndex(i);
			// TODO: handle needs template reference
			String embeddedName = name + ".listItem(" + i + ")";
			template.generateCodeInit(aData, source, embeddedName);
		}
		// TODO:  missing parts need to be completed

		if (isIfpresent) {
			source.append(name);
			source.append(".set_ifPresent();\n");
		}
	}
}
