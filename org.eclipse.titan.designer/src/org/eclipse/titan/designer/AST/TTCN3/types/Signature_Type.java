/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.NamedTemplate;
import org.eclipse.titan.designer.AST.TTCN3.templates.Named_Template_List;
import org.eclipse.titan.designer.AST.TTCN3.templates.SpecificValue_Template;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Signature_Type extends Type {
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for signature `{1}''";
	private static final String SIGNATUREEXPECTED = "sequence value was expected for type `{0}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed in a template for signature `{0}''";
	private static final String DUPLICATEPARAMETERAGAIN = "Duplicate parameter `{0}'' in template for signature `{1}''";
	private static final String DUPLICATEPARAMETERFIRST = "Parameter `{0}'' is already given here";
	private static final String UNEXPECTEDPARAMETER = "Unexpected parameter `{0}'' in signature template";
	private static final String NONEXISTENTPARAMETER = "Reference to non-existent parameter `{0}'' in template for signature `{1}''";
	private static final String INCOMPLETE1 = "Signature template is incomplete, because the inout parameter `{0}'' is missing";
	private static final String INCOMPLETE2 =
			"Signature template is incomplete, because the in parameter `{0}'' and the out parameter `{1}'' is missing";

	private static final String FULLNAMEPART1 = ".<return_type>";
	private static final String FULLNAMEPART2 = ".<exception_list>";

	private final SignatureFormalParameterList formalParList;
	private final Type returnType;
	private final boolean noBlock;
	private final SignatureExceptions exceptions;

	private boolean componentInternal;

	public Signature_Type(final SignatureFormalParameterList formalParList, final Type returnType,
			final boolean noBlock, final SignatureExceptions exceptions) {
		this.formalParList = formalParList;
		this.returnType = returnType;
		this.noBlock = noBlock;
		this.exceptions = exceptions;
		componentInternal = false;

		if (formalParList != null) {
			formalParList.setFullNameParent(this);
		}
		if (returnType != null) {
			returnType.setFullNameParent(this);
		}
		if (exceptions != null) {
			exceptions.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_SIGNATURE;
	}

	public boolean isNonblocking() {
		return noBlock;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (returnType == child) {
			return builder.append(FULLNAMEPART1);
		} else if (exceptions == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (formalParList != null) {
			formalParList.setMyScope(scope);
		}

		if (returnType != null) {
			returnType.setMyScope(scope);
		}
		if (exceptions != null) {
			exceptions.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		final IType last = otherType.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || last.getIsErroneous(timestamp)) {
			return true;
		}

		return this == last;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isIdentical(final CompilationTimeStamp timestamp, final IType type) {
		check(timestamp);
		type.check(timestamp);
		final IType temp = type.getTypeRefdLast(timestamp);
		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return this == temp;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetypeTtcn3() {
		if (isErroneous) {
			return Type_type.TYPE_UNDEFINED;
		}

		return getTypetype();
	}

	@Override
	/** {@inheritDoc} */
	public String getTypename() {
		return getFullName();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "signature.gif";
	}

	/** @return the number of components */
	public int getNofParameters() {
		if (formalParList == null) {
			return 0;
		}

		return formalParList.getNofParameters();
	}

	/**
	 * Returns whether a parameter with the specified name exists or not.
	 *
	 * @param name the name to check for
	 * @return true if a parameter with that name exist, false otherwise
	 */
	public boolean hasParameterWithName(final String name) {
		return formalParList.hasParameterWithName(name);
	}

	/**
	 * Returns the parameter with the specified name.
	 *
	 * @param name the name of the parameter to return
	 * @return the element with the specified name in this list
	 */
	public SignatureFormalParameter getParameterByName(final String name) {
		return formalParList.getParameterByName(name);
	}

	/**
	 * Returns the identifier of the element at the specified position.
	 *
	 * @param index index of the element to return
	 * @return the identifier of the element at the specified position in this
	 *         list
	 */
	public Identifier getParameterIdentifierByIndex(final int index) {
		return formalParList.getParameterByIndex(index).getIdentifier();
	}

	/** @return the formal parameter list */
	public SignatureFormalParameterList getParameterList() {
		return formalParList;
	}

	/** @return the exceptions of the signature type, or null if none */
	public SignatureExceptions getSignatureExceptions() {
		return exceptions;
	}

	/** @return the return type of the signature type, or null if none */
	public Type getSignatureReturnType() {
		return returnType;
	}

	@Override
	/** {@inheritDoc} */
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return componentInternal;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		componentInternal = false;

		parseAttributes(timestamp);

		if (formalParList != null) {
			formalParList.check(timestamp, this);
			for (int i = 0, size = formalParList.getNofParameters(); i < size && !componentInternal; i++) {
				final IType type = formalParList.getParameterByIndex(i).getType();
				if (type != null && type.isComponentInternal(timestamp)) {
					componentInternal = true;
				}
			}
		}

		if (returnType != null) {
			returnType.setParentType(this);
			returnType.check(timestamp);
			returnType.checkEmbedded(timestamp, returnType.getLocation(), true, "the return type of a signature");
			if (!componentInternal && returnType.isComponentInternal(timestamp)) {
				componentInternal = true;
			}
		}

		if (exceptions != null) {
			exceptions.check(timestamp, this);
			for (int i = 0, size = exceptions.getNofExceptions(); i < size && !componentInternal; i++) {
				final IType type = exceptions.getExceptionByIndex(i);
				if (type != null && type.isComponentInternal(timestamp)) {
					componentInternal = true;
				}
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		if (typeSet.contains(this)) {
			return;
		}

		typeSet.add(this);
		if (formalParList != null) {
			for (int i = 0, size = formalParList.getNofParameters(); i < size; i++) {
				final IType type = formalParList.getParameterByIndex(i).getType();
				if (type != null && type.isComponentInternal(timestamp)) {
					type.checkComponentInternal(timestamp, typeSet, operation);
				}
			}
		}

		if (returnType != null && returnType.isComponentInternal(timestamp)) {
			returnType.checkComponentInternal(timestamp, typeSet, operation);
		}

		if (exceptions != null) {
			for (int i = 0, size = exceptions.getNofExceptions(); i < size; i++) {
				final IType type = exceptions.getExceptionByIndex(i);
				if (type != null && type.isComponentInternal(timestamp)) {
					type.checkComponentInternal(timestamp, typeSet, operation);
				}
			}
		}
		typeSet.remove(this);
	}

	@Override
	public void checkEmbedded(final CompilationTimeStamp timestamp, final Location errorLocation, final boolean defaultAllowed,
			final String errorMessage) {
		errorLocation.reportSemanticError(MessageFormat.format("Signature type `{0}'' cannot be {1}", getTypename(), errorMessage));
	}

	@Override
	/** {@inheritDoc} */
	public void checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final ValueCheckingOptions valueCheckingOptions) {
		if (getIsErroneous(timestamp)) {
			return;
		}

		super.checkThisValue(timestamp, value, valueCheckingOptions);

		IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (last == null || last.getIsErroneous(timestamp)) {
			return;
		}

		// already handled ones
		switch (value.getValuetype()) {
		case OMIT_VALUE:
		case REFERENCED_VALUE:
			return;
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			if (Value_type.REFERENCED_VALUE.equals(last.getValuetype())) {
				return;
			}
			break;
		default:
			break;
		}

		switch (last.getValuetype()) {
		case SEQUENCE_VALUE:
			checkThisValueSequence(
					timestamp, (Sequence_Value) last, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case SEQUENCEOF_VALUE:
			last = last.setValuetype(timestamp, Value_type.SEQUENCE_VALUE);
			checkThisValueSequence(
					timestamp, (Sequence_Value) last, valueCheckingOptions.expected_value,
					valueCheckingOptions.incomplete_allowed, valueCheckingOptions.implicit_omit, valueCheckingOptions.str_elem);
			break;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			break;
		default:
			value.getLocation().reportSemanticError(MessageFormat.format(SIGNATUREEXPECTED, getTypename()));
			value.setIsErroneous(true);
		}

		value.setLastTimeChecked(timestamp);
	}

	/**
	 * Checks the Sequence_Value kind value against this type.
	 * <p>
	 * Please note, that this function can only be called once we know for sure
	 * that the value is of sequence type.
	 *
	 * @param timestamp the timestamp of the actual semantic check cycle.
	 * @param value the value to be checked
	 * @param expectedValue the kind of value expected here.
	 * @param incompleteAllowed wheather incomplete value is allowed or not.
	 * @param implicitOmit true if the implicit omit optional attribute was set
	 *            for the value, false otherwise
	 * */
	private void checkThisValueSequence(final CompilationTimeStamp timestamp, final Sequence_Value value, final Expected_Value_type expectedValue,
			final boolean incompleteAllowed, final boolean implicitOmit, final boolean strElem) {
		final Map<String, NamedValue> componentMap = new HashMap<String, NamedValue>();
		boolean inSnyc = true;
		final int nofTypeComponents = getNofParameters();
		final int nofvalueComponents = value.getNofComponents();
		int nextIndex = 0;
		SignatureFormalParameter lastParameter = null;
		for (int i = 0; i < nofvalueComponents; i++) {
			final NamedValue namedValue = value.getSeqValueByIndex(i);
			final Identifier valueId = namedValue.getName();
			if (!formalParList.hasParameterWithName(valueId.getName())) {
				namedValue.getLocation().reportSemanticError(MessageFormat.format(
						NONEXISTENTPARAMETER, valueId.getDisplayName(), getTypename()));
				inSnyc = false;
				continue;
			} else if (componentMap.containsKey(valueId.getName())) {
				namedValue.getLocation().reportSemanticError(MessageFormat.format(
						DUPLICATEPARAMETERAGAIN, valueId.getDisplayName(), getTypename()));
				componentMap.get(valueId.getName()).getLocation().reportSemanticError(MessageFormat.format(
						"Parameter `{0}'' is already given here", valueId.getDisplayName()));
				inSnyc = false;
			} else {
				componentMap.put(valueId.getName(), namedValue);
			}

			final SignatureFormalParameter formalParameter = formalParList.getParameterByName(valueId.getName());
			if (inSnyc) {
				if (incompleteAllowed) {
					boolean found = false;

					for (int j = nextIndex; j < nofTypeComponents && !found; j++) {
						final SignatureFormalParameter formalParameter2 = formalParList.getParameterByIndex(j);
						if (valueId.getName().equals(formalParameter2.getIdentifier().getName())) {
							lastParameter = formalParameter2;
							nextIndex = j + 1;
							found = true;
						}
					}

					if (lastParameter != null && !found) {
						namedValue.getLocation().reportSemanticError(MessageFormat.format(
								"Field `{0}'' cannot appear after parameter `{1}'' in signature value",
								valueId.getDisplayName(), lastParameter.getIdentifier().getDisplayName()));
						inSnyc = false;
					}
				} else {
					final SignatureFormalParameter formalParameter2 = formalParList.getParameterByIndex(i);
					if (formalParameter != formalParameter2) {
						namedValue.getLocation().reportSemanticError(MessageFormat.format(
								"Unexpected field `{0}'' in signature value, expecting `{1}''",
								valueId.getDisplayName(), formalParameter2.getIdentifier().getDisplayName()));
						inSnyc = false;
					}
				}
			}

			final Type type = formalParameter.getType();
			final IValue componentValue = namedValue.getValue();
			if (componentValue != null) {
				componentValue.setMyGovernor(type);
				final IValue tempValue = type.checkThisValueRef(timestamp, componentValue);
				type.checkThisValue(timestamp, tempValue, new ValueCheckingOptions(expectedValue, false, false, true, implicitOmit, strElem));
			}
		}

		if (!incompleteAllowed) {
			for (int i = 0; i < formalParList.getNofInParameters(); i++) {
				final SignatureFormalParameter formalParameter = formalParList.getInParameterByIndex(i);
				final Identifier identifier = formalParameter.getIdentifier();
				if (!componentMap.containsKey(identifier.getName()) && SignatureFormalParameter.PARAM_OUT != formalParameter.getDirection()) {
					value.getLocation().reportSemanticError(MessageFormat.format(
							"Field `{0}'' is missing from signature value", identifier.getDisplayName()));
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit) {
		registerUsage(template);
		template.setMyGovernor(this);

		switch (template.getTemplatetype()) {
		case TEMPLATE_LIST:
			final ITTCN3Template transformed = template.setTemplatetype(timestamp, Template_type.NAMED_TEMPLATE_LIST);
			checkThisNamedTemplateList(timestamp, (Named_Template_List) transformed, isModified);
			break;
		case NAMED_TEMPLATE_LIST:
			checkThisNamedTemplateList(timestamp, (Named_Template_List) template, isModified);
			break;
		case SPECIFIC_VALUE:
			((SpecificValue_Template) template).checkSpecificValue(timestamp,false);
			break;
		default:
			template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));
			break;
		}

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(MessageFormat.format(LENGTHRESTRICTIONNOTALLOWED, getTypename()));
		}
	}

	//see void Type::chk_this_template_Signature(Template *t, namedbool incomplete_allowed) in Type_chk.cc
	private void checkThisNamedTemplateList(final CompilationTimeStamp timestamp, final Named_Template_List template, final boolean isModified) {
		final Map<String, NamedTemplate> componentMap = new HashMap<String, NamedTemplate>();
		boolean inSynch = true;
		final int nofTypeParameters =  getNofParameters();  //TODO:  alternatives:formalParList.getNofInParameters(); formalParList.getNofOutParameters()
		final int nofTemplateComponents = template.getNofTemplates();

		int tI = 0;

		for (int vI = 0; vI < nofTemplateComponents; vI++) {
			final NamedTemplate namedTemplate = template.getTemplateByIndex(vI);
			final Identifier identifier = namedTemplate.getName();
			final String name = identifier.getName();

			if (hasParameterWithName(name)) {
				if (componentMap.containsKey(name)) {
					namedTemplate.getLocation().reportSemanticError(
							MessageFormat.format(DUPLICATEPARAMETERAGAIN, identifier.getDisplayName(), getTypename()));
					componentMap.get(name).getLocation().reportSingularSemanticError(
							MessageFormat.format(DUPLICATEPARAMETERFIRST, identifier.getDisplayName()));
					inSynch = false;
				} else {
					componentMap.put(name, namedTemplate);
				}

				final SignatureFormalParameter parameter = formalParList.getParameterByName(name);
				if (inSynch) {
					SignatureFormalParameter parameter2 = null;
					boolean found = false;
					for (; tI < nofTypeParameters && !found; tI++) {
						parameter2 = formalParList.getParameterByIndex(tI);
						if (parameter == parameter2) {
							found = true;
							break;
						}
					}
					if (!found) {
						namedTemplate.getLocation().reportSemanticError(MessageFormat.format(UNEXPECTEDPARAMETER, identifier.getDisplayName()));
						inSynch = false;
					}
				}

				final Type parameterType = parameter.getType();
				ITTCN3Template componentTemplate = namedTemplate.getTemplate();
				componentTemplate.setMyGovernor(parameterType); //FIXME: will be overwritten?
				componentTemplate = parameterType.checkThisTemplateRef(timestamp, componentTemplate);
				componentTemplate.checkThisTemplateGeneric(timestamp, parameterType, isModified, false, false, true, false);
			} else {
				namedTemplate.getLocation().reportSemanticError(
						MessageFormat.format(NONEXISTENTPARAMETER, identifier.getDisplayName(), getTypename()));
				inSynch = false;
			}
		}

		if (isModified) {
			SignatureFormalParameter firstUndefIn = null;
			SignatureFormalParameter firstUndefOut = null;

			for (int i = 0; i < nofTypeParameters; i++) {
				final SignatureFormalParameter parameter = formalParList.getParameterByIndex(i);
				final Identifier identifier = parameter.getIdentifier();

				if (!componentMap.containsKey(identifier.getName())
						|| Template_type.TEMPLATE_NOTUSED.equals(componentMap.get(identifier.getName()).getTemplate().getTemplatetype())) {
					switch (parameter.getDirection()) {
					case SignatureFormalParameter.PARAM_IN:
						if (firstUndefIn == null) {
							firstUndefIn = parameter;
						}
						break;
					case SignatureFormalParameter.PARAM_OUT:
						if (firstUndefOut == null) {
							firstUndefOut = parameter;
						}
						break;
					default:
						template.getLocation().reportSemanticError(MessageFormat.format(INCOMPLETE1, identifier.getDisplayName()));
						break;
					}
				}
			}

			if (firstUndefIn != null && firstUndefOut != null) {
				template.getLocation().reportSemanticError(
						MessageFormat.format(INCOMPLETE2, firstUndefIn.getIdentifier().getDisplayName(), firstUndefOut.getIdentifier()
								.getDisplayName()));
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getProposalDescription(final StringBuilder builder) {
		return builder.append("signature");
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (formalParList != null) {
			formalParList.updateSyntax(reparser, false);
			reparser.updateLocation(formalParList.getLocation());
		}

		if (returnType != null) {
			returnType.updateSyntax(reparser, false);
			reparser.updateLocation(returnType.getLocation());
		}

		if (exceptions != null) {
			exceptions.updateSyntax(reparser, false);
			reparser.updateLocation(exceptions.getLocation());
		}

		if (subType != null) {
			subType.updateSyntax(reparser, false);
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (formalParList != null) {
			formalParList.findReferences(referenceFinder, foundIdentifiers);
		}
		if (returnType != null) {
			returnType.findReferences(referenceFinder, foundIdentifiers);
		}
		if (exceptions != null) {
			exceptions.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (formalParList!=null && !formalParList.accept(v)) {
			return false;
		}
		if (returnType!=null && !returnType.accept(v)) {
			return false;
		}
		if (exceptions!=null && !exceptions.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source, final Scope scope) {
		return getGenNameOwn(scope);
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		final String genName = getGenNameOwn();
		final String displayName = getFullName();
		
		aData.addBuiltinTypeImport("TitanBoolean");
		aData.addBuiltinTypeImport("TitanBoolean_template");
		for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
			SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);
			Type type = formalPar.getType();

			type.getGenNameValue(aData, source, myScope);
			type.getGenNameTemplate(aData, source, myScope);
		}
		if (returnType != null) {
			returnType.getGenNameValue(aData, source, myScope);
			returnType.getGenNameTemplate(aData, source, myScope);
		}
		if (exceptions != null) {
			for ( int i = 0; i < exceptions.getNofExceptions(); i++) {
				Type exceptionType = exceptions.getExceptionByIndex(i);
				exceptionType.getGenNameValue(aData, source, myScope);
				exceptionType.getGenNameTemplate(aData, source, myScope);
			}
		}

		source.append(MessageFormat.format("public static class {0}_call '{'\n", genName));
		source.append("// in and inout parameters\n");
		for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
			SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);

			if(formalPar.getDirection() != SignatureFormalParameter.PARAM_OUT) {
				String fieldName = formalPar.getIdentifier().getName();
				String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);
				source.append(MessageFormat.format("private {0} param_{1};\n", typeName, fieldName));
			}
		}

		source.append(MessageFormat.format("public {0}_call() '{'\n", genName));
		for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
			SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);

			if(formalPar.getDirection() != SignatureFormalParameter.PARAM_OUT) {
				String fieldName = formalPar.getIdentifier().getName();
				String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);
				source.append(MessageFormat.format("param_{0} = new {1}();\n", fieldName, typeName));
			}
		}
		source.append("}\n");

		for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
			SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);

			if(formalPar.getDirection() != SignatureFormalParameter.PARAM_OUT) {
				String fieldName = formalPar.getIdentifier().getName();
				String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);
				source.append(MessageFormat.format("public {0} get{1}() '{'\n", typeName, fieldName));
				source.append(MessageFormat.format("return param_{0};\n", fieldName));
				source.append("}\n");

				source.append(MessageFormat.format("public {0} constGet{1}() '{'\n", typeName, fieldName));
				source.append(MessageFormat.format("return param_{0};\n", fieldName));
				source.append("}\n");
			}
		}


		source.append("// FIXME implement encode_text\n");
		source.append("// FIXME implement decode_text\n");
		source.append("// FIXME implement log\n");
		source.append("}\n");

		// FIXME implement MyProc_redirect

		if(!noBlock) {
			source.append(MessageFormat.format("public static class {0}_reply '{'\n", genName));
			source.append("// out parameters\n");
			for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
				SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);
	
				if(formalPar.getDirection() != SignatureFormalParameter.PARAM_IN) {
					String fieldName = formalPar.getIdentifier().getName();
					String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);
					source.append(MessageFormat.format("private {0} param_{1};\n", typeName, fieldName));
				}
			}
			if (returnType != null) {
				source.append("// the reply value of the signature\n");
				source.append("private TitanBoolean reply_value;\n");
			}
	
			source.append(MessageFormat.format("public {0}_reply() '{'\n", genName));
			for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
				SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);
	
				if(formalPar.getDirection() != SignatureFormalParameter.PARAM_IN) {
					String fieldName = formalPar.getIdentifier().getName();
					String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);
					source.append(MessageFormat.format("param_{0} = new {1}();\n", fieldName, typeName));
				}
			}
	
			source.append("reply_value = new TitanBoolean();\n");
			source.append("}\n");
	
			for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
				SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);
	
				if(formalPar.getDirection() != SignatureFormalParameter.PARAM_IN) {
					String fieldName = formalPar.getIdentifier().getName();
					String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);
					source.append(MessageFormat.format("public {0} get{1}() '{'\n", typeName, fieldName));
					source.append(MessageFormat.format("return param_{0};\n", fieldName));
					source.append("}\n");
	
					source.append(MessageFormat.format("public {0} constGet{1}() '{'\n", typeName, fieldName));
					source.append(MessageFormat.format("return param_{0};\n", fieldName));
					source.append("}\n");
				}
			}
	
			if (returnType != null) {
				source.append("public TitanBoolean getreturn_value() {\n");
				source.append("return reply_value;\n");
				source.append("}\n");
	
				source.append("public TitanBoolean constGetreturn_value() {\n");
				source.append("return reply_value;\n");
				source.append("}\n");
			}
	
			source.append("// FIXME implement encode_text\n");
			source.append("// FIXME implement decode_text\n");
			source.append("// FIXME implement log\n");
			source.append("}\n");
		}
		// FIXME implement MyProc_reply_redirect

		if (exceptions != null) {
			source.append(MessageFormat.format("public static class {0}_exception '{'\n", genName));
			source.append("public enum exception_selection_type {");
			for ( int i = 0; i < exceptions.getNofExceptions(); i++) {
				Type exceptionType = exceptions.getExceptionByIndex(i);
				String typeName = exceptionType.getGenNameValue(aData, source, myScope);
				source.append(MessageFormat.format(" ALT_{0},", typeName));
			}
			source.append(" UNBOUND_VALUE };\n");
				
			source.append("private exception_selection_type exception_selection;\n");
			source.append("//originally a union which can not be mapped to Java\n");
			source.append("private Base_Type field;\n");
				
			source.append("//originally clean_up\n");
			source.append("public void cleanUp() {\n");
			source.append("field = null;\n");
			source.append("exception_selection = exception_selection_type.UNBOUND_VALUE;\n");
			source.append("}\n");
	
			source.append(MessageFormat.format("private void copy_value(final {0}_exception otherValue) '{'\n", genName));
			source.append("switch(otherValue.exception_selection){\n");
			for ( int i = 0; i < exceptions.getNofExceptions(); i++) {
				Type exceptionType = exceptions.getExceptionByIndex(i);
				String typeName = exceptionType.getGenNameValue(aData, source, myScope);
				source.append(MessageFormat.format("case ALT_{0}:\n", typeName));
				source.append(MessageFormat.format("field = new {0}(({0})otherValue.field);\n", typeName));
				source.append("break;\n");
			}
				source.append("default:\n");
				source.append(MessageFormat.format("throw new TtcnError(\"Copying an uninitialized exception of signature {0}.\");\n", getFullName()));
				source.append("}\n");
				source.append("exception_selection = otherValue.exception_selection;\n");
			source.append("}\n");
	
			source.append(MessageFormat.format("public {0}_exception() '{'\n", genName));
			source.append("exception_selection = exception_selection_type.UNBOUND_VALUE;\n");
			source.append("}\n");
	
			source.append(MessageFormat.format("public {0}_exception(final {0}_exception otherValue)  '{'\n", genName));
			source.append("copy_value(otherValue);\n");
			source.append("}\n");
	
			for ( int i = 0; i < exceptions.getNofExceptions(); i++) {
				Type exceptionType = exceptions.getExceptionByIndex(i);
				String typeName = exceptionType.getGenNameValue(aData, source, myScope);
				source.append(MessageFormat.format("public {0}_exception( final {1} otherValue) '{'\n", genName, typeName));
				source.append(MessageFormat.format("field = new {0}(otherValue);\n", typeName));
				source.append(MessageFormat.format("exception_selection = exception_selection_type.ALT_{0};\n", typeName));
				source.append("}\n");
	
				source.append(MessageFormat.format("public {0}_exception( final {1}_template otherValue) '{'\n", genName, typeName));
				source.append(MessageFormat.format("field = new {0}(otherValue.valueOf());\n", typeName));
				source.append(MessageFormat.format("exception_selection = exception_selection_type.ALT_{0};\n", typeName));
				source.append("}\n");
			}
	
			source.append("//originally operator=\n");
			source.append(MessageFormat.format("public {0}_exception assign( final {0}_exception otherValue ) '{'\n", genName));
			source.append("if(this != otherValue) {\n");
			source.append("cleanUp();\n");
			source.append("copy_value(otherValue);\n");
			source.append("}\n");
			source.append("return this;\n");
			source.append("}\n");
	
			for ( int i = 0; i < exceptions.getNofExceptions(); i++) {
				Type exceptionType = exceptions.getExceptionByIndex(i);
				String typeName = exceptionType.getGenNameValue(aData, source, myScope);
	
				source.append("//originally {0}_field\n");
				source.append(MessageFormat.format("public {0} get{0}() '{'\n", typeName));
				source.append(MessageFormat.format("if (exception_selection != exception_selection_type.ALT_{0}) '{'\n", typeName));
				source.append("cleanUp();\n");
				source.append(MessageFormat.format("field = new {0}();\n", typeName));
				source.append(MessageFormat.format("exception_selection = exception_selection_type.ALT_{0};\n", typeName));
				source.append("}\n");
				source.append(MessageFormat.format("return ({0})field;\n", typeName));
				source.append("}\n");
	
				source.append("//originally const {0}_field\n");
				source.append(MessageFormat.format("public {0} constGet{0}() '{'\n", typeName));
				source.append(MessageFormat.format("if (exception_selection != exception_selection_type.ALT_{0}) '{'\n", typeName));
	
				source.append(MessageFormat.format("throw new TtcnError(\"Referencing to non-selected type integer in an exception of signature {0}.\");\n", getFullName()));
				source.append("}\n");
				source.append(MessageFormat.format("return ({0})field;\n", typeName));
				source.append("}\n");
			}
	
			source.append("public exception_selection_type get_selection() {\n");
			source.append("return exception_selection;\n");
			source.append("}\n");
	
			source.append("// FIXME implement encode_text\n");
			source.append("// FIXME implement decode_text\n");
			source.append("// FIXME implement log\n");
			source.append("}\n");
	
			source.append(MessageFormat.format("public static class {0}_exception_template '{'\n", genName));
			source.append(MessageFormat.format("private {0}_exception.exception_selection_type exception_selection;\n", genName));
			source.append("//originally a union which can not be mapped to Java\n");
			source.append("private Base_Template field;\n");
	
			source.append("//FIXME add support for redirection\n");
			for ( int i = 0; i < exceptions.getNofExceptions(); i++) {
				Type exceptionType = exceptions.getExceptionByIndex(i);
				String typeName = exceptionType.getGenNameValue(aData, source, myScope);
	
				source.append(MessageFormat.format("public {0}_exception_template(final {1}_template init_template) '{'\n", genName, typeName));
				source.append(MessageFormat.format("exception_selection = {0}_exception.exception_selection_type.ALT_{1};\n", genName, typeName));
				source.append(MessageFormat.format("field = new {0}_template(init_template);\n", typeName));
				source.append("}\n");
			}
	
			source.append("// FIXME implement match\n");
			source.append("// FIXME implement log_match\n");
			source.append("// FIXME implement set_value\n");
			source.append("// FIXME implement is_any_or_omit\n");
			source.append("}\n");
		}

		source.append(MessageFormat.format("public static class {0}_template '{'\n", genName));
		source.append("// all the parameters\n");
		for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
			SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);
			String fieldName = formalPar.getIdentifier().getName();
			String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);

			source.append(MessageFormat.format("private {0}_template param_{1};\n", typeName, fieldName));
		}
		if (returnType != null) {
			source.append("private TitanBoolean_template reply_value;\n");
		}

		source.append(MessageFormat.format("public {0}_template() '{'\n", genName));
		for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
			SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);
			String fieldName = formalPar.getIdentifier().getName();
			String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);

			source.append(MessageFormat.format("param_{0} = new {1}_template(template_sel.ANY_VALUE);\n", fieldName, typeName));
		}
		source.append("}\n");

		for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
			SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);
			String fieldName = formalPar.getIdentifier().getName();
			String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);

			source.append(MessageFormat.format("public {0}_template get{1}() '{'\n", typeName, fieldName ));
			source.append(MessageFormat.format("return param_{0};\n", fieldName ));
			source.append("}\n");

			source.append(MessageFormat.format("public {0}_template constGet{1}() '{'\n", typeName, fieldName ));
			source.append(MessageFormat.format("return param_{0};\n", fieldName ));
			source.append("}\n");
		}

		if (returnType != null) {
			source.append("public TitanBoolean_template return_value() {\n");
			source.append("return reply_value;\n");
			source.append("}\n");
		}

		source.append(MessageFormat.format("public {0}_call create_call() '{'\n", genName));
		source.append(MessageFormat.format("{0}_call return_value = new {0}_call();\n", genName));
		for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
			SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);
			
			if(formalPar.getDirection() != SignatureFormalParameter.PARAM_OUT) {
				String fieldName = formalPar.getIdentifier().getName();
				String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);
	
				source.append(MessageFormat.format("return_value.get{0}().assign(param_{0}.valueOf());\n", fieldName ));
			}
		}
		source.append("return return_value;\n");
		source.append("}\n");

		if(!noBlock) {
			source.append(MessageFormat.format("public {0}_reply create_reply() '{'\n", genName));
			source.append(MessageFormat.format("{0}_reply return_value = new {0}_reply();\n", genName));
			for (int i = 0 ; i < formalParList.getNofParameters(); i++) {
				SignatureFormalParameter formalPar = formalParList.getParameterByIndex(i);
	
				if(formalPar.getDirection() != SignatureFormalParameter.PARAM_IN) {
					String fieldName = formalPar.getIdentifier().getName();
					String typeName = formalPar.getType().getGenNameValue(aData, source, myScope);
	
					source.append(MessageFormat.format("return_value.get{0}().assign(param_{0}.valueOf());\n", fieldName ));
				}
			}
			source.append("return return_value;\n");
			source.append("}\n");
		}

		source.append("//FIXME implement match_call\n");
		source.append("//FIXME implement match_reply\n");
		source.append(MessageFormat.format("public {0}_template set_value_template(final TitanBoolean_template new_template) '{'\n", genName));
		source.append("reply_value = new_template;\n");
		source.append("return this;\n");
		source.append("}\n");

		source.append("// FIXME implement encode_text\n");
		source.append("// FIXME implement decode_text\n");
		source.append("// FIXME implement log\n");
		source.append("// FIXME implement log_match_call\n");
		source.append("// FIXME implement log_match_reply\n");
		source.append("}\n");
		//TODO: implement
		source.append( "\t\t//TODO: Signature_Type.generateCode() is not fully implemented!\n" );
	}
}
