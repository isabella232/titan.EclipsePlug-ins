/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents templates created from a list of values.
 *
 * @author Kristof Szabados
 * */
public final class ValueList_Template extends CompositeTemplate {

	public ValueList_Template(final ListOfTemplates templates) {
		super(templates);
	}

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.VALUE_LIST;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous value list match";
		}

		return "value list match";
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
		}

		checkLengthRestriction(timestamp, type);
		if (!allowOmit && isIfpresent) {
			if( location != null && !(location instanceof NULL_Location)) {
				location.reportSemanticError("`ifpresent' is not allowed here");
			}
		}
		if (subCheck) {
			type.checkThisTemplateSubtype(timestamp, this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value was expected instead of value list match");
	}

	@Override
	public boolean checkPresentRestriction(final CompilationTimeStamp timestamp, final String definitionName, final Location usageLocation) {
		checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_PRESENT, usageLocation);
		boolean needsRuntimeCheck = false;
		for (int i = 0, size = templates.getNofTemplates(); i < size; i++) {
			final ITTCN3Template component = templates.getTemplateByIndex(i);
			if (component.checkPresentRestriction(timestamp, definitionName, usageLocation)) {
				needsRuntimeCheck = true;
			}
		}
		return needsRuntimeCheck;
	}

	@Override
	protected String getNameForStringRep() {
		return "";
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getSingleExpresion(JavaGenData aData, boolean castIsNeeded) {
		StringBuilder result = new StringBuilder();

		result.append( "\t//TODO: fatal error while generating " );
		result.append( getClass().getSimpleName() );
		result.append( ".generateSingleExpression() !\n" );
		// TODO: fatal error
		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void generateJavaInit(JavaGenData aData, StringBuilder source, String name) {
		aData.addBuiltinTypeImport( "Base_Template.template_sel" );

		int nofTs = templates.getNofTemplates();
		String typeName = myGovernor.getGenNameTemplate(aData, source, myScope);
		//TODO: add support for all_from

		source.append(name);
		source.append(".setType( template_sel.VALUE_LIST, ");
		source.append(nofTs);
		source.append( " );\n" );

		for (int i = 0 ; i < nofTs ; i++) {
			ITemplateListItem template = templates.getTemplateByIndex(i);
			// TODO: handle needs template reference
			String embeddedName = name + ".listItem(" + i + ")";
			template.generateJavaInit(aData, source, embeddedName);
		}
		// TODO:  missing parts need to be completed

		if (isIfpresent) {
			source.append(name);
			source.append(".set_ifPresent();\n");
		}
	}
}
