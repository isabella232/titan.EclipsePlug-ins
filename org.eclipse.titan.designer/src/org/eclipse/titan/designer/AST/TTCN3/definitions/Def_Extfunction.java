/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Encoding_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.attributes.AttributeSpecification;
import org.eclipse.titan.designer.AST.TTCN3.attributes.DecodeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.EncodeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErrorBehaviorAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErrorBehaviorList;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PrintingAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PrintingType;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PrototypeAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.Qualifiers;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute.Attribute_Type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function.EncodingPrototype_type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.extensionattributeparser.ExtensionAttributeAnalyzer;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * The Def_ExtFunction class represents TTCN3 external function definitions.
 *
 * @author Kristof Szabados
 * */
public final class Def_Extfunction extends Definition implements IParameterisedAssignment {
	public enum ExternalFunctionEncodingType_type {
		/** manual encoding. */
		MANUAL,
		/** generated encoder function. */
		ENCODE,
		/** generated decoder function. */
		DECODE
	}

	private static final String FULLNAMEPART1 = ".<formal_parameter_list>";
	private static final String FULLNAMEPART2 = ".<type>";
	private static final String FULLNAMEPART3 = ".<errorbehavior_list>";
	public static final String PORTRETURNNOTALLOWED = "External functions can not return ports";

	private static final String KIND = "external function";

	private final Assignment_type assignmentType;
	private final FormalParameterList formalParList;
	private final Type returnType;
	private final boolean returnsTemplate;
	private final TemplateRestriction.Restriction_type templateRestriction;
	private EncodingPrototype_type prototype;
	private Type inputType;
	private Type outputType;
	private ExternalFunctionEncodingType_type functionEncodingType;
	private Encoding_type encodingType;
	private String encodingOptions;
	private ErrorBehaviorList errorBehaviorList;
	private PrintingType printingType;

	public Def_Extfunction(final Identifier identifier, final FormalParameterList formalParameters, final Type returnType,
			final boolean returnsTemplate, final TemplateRestriction.Restriction_type templateRestriction) {
		super(identifier);
		assignmentType = (returnType == null) ? Assignment_type.A_EXT_FUNCTION : (returnsTemplate ? Assignment_type.A_EXT_FUNCTION_RTEMP
				: Assignment_type.A_EXT_FUNCTION_RVAL);
		formalParList = formalParameters;
		formalParList.setMyDefinition(this);
		this.returnType = returnType;
		this.returnsTemplate = returnsTemplate;
		this.templateRestriction = templateRestriction;
		prototype = EncodingPrototype_type.NONE;
		functionEncodingType = ExternalFunctionEncodingType_type.MANUAL;
		encodingType = Encoding_type.UNDEFINED;
		encodingOptions = null;
		errorBehaviorList = null;
		printingType = null;

		formalParList.setFullNameParent(this);
		if (returnType != null) {
			returnType.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Assignment_type getAssignmentType() {
		return assignmentType;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (formalParList == child) {
			return builder.append(FULLNAMEPART1);
		} else if (returnType == child) {
			return builder.append(FULLNAMEPART2);
		} else if (errorBehaviorList == child) {
			return builder.append(FULLNAMEPART3);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public FormalParameterList getFormalParameterList() {
		return formalParList;
	}

	public EncodingPrototype_type getPrototype() {
		return prototype;
	}

	public Type getInputType() {
		return inputType;
	}

	public Type getOutputType() {
		return outputType;
	}

	@Override
	/** {@inheritDoc} */
	public String getProposalKind() {
		return KIND;
	}

	@Override
	/** {@inheritDoc} */
	public String getAssignmentName() {
		return "external function";
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		if (returnType == null) {
			return "function_external.gif";
		}

		return "function_external_return.gif";
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		formalParList.setMyScope(scope);
		if (returnType != null) {
			returnType.setMyScope(scope);
		}
		scope.addSubScope(formalParList.getLocation(), formalParList);
	}

	@Override
	/** {@inheritDoc} */
	public Type getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return returnType;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		check(timestamp, null);
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		isUsed = false;
		prototype = EncodingPrototype_type.NONE;
		functionEncodingType = ExternalFunctionEncodingType_type.MANUAL;
		encodingType = Encoding_type.UNDEFINED;
		encodingOptions = null;
		errorBehaviorList = null;
		printingType = null;
		lastTimeChecked = timestamp;

		NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_EXTERNALFUNCTION, identifier, this);
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (returnType != null) {
			returnType.check(timestamp);
			final IType returnedType = returnType.getTypeRefdLast(timestamp);
			if (returnedType != null && Type_type.TYPE_PORT.equals(returnedType.getTypetype()) && returnType.getLocation() != null) {
				returnType.getLocation().reportSemanticError(PORTRETURNNOTALLOWED);
			}
		}

		formalParList.reset();
		formalParList.check(timestamp, getAssignmentType());

		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
			analyzeExtensionAttributes(timestamp);
			checkPrototype(timestamp);
			checkFunctionType(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void postCheck() {
		super.postCheck();
		postCheckPrivateness();
	}

	/**
	 * Convert and check the encoding attributes applied to this external
	 * function.
	 *
	 * @param timestamp
	 *                the timestamp of the actual build cycle.
	 * */
	public void analyzeExtensionAttributes(final CompilationTimeStamp timestamp) {
		final List<SingleWithAttribute> realAttributes = withAttributesPath.getRealAttributes(timestamp);

		SingleWithAttribute attribute;
		List<AttributeSpecification> specifications = null;
		for (int i = 0, size = realAttributes.size(); i < size; i++) {
			attribute = realAttributes.get(i);
			if (Attribute_Type.Extension_Attribute.equals(attribute.getAttributeType())) {
				final Qualifiers qualifiers = attribute.getQualifiers();
				if (qualifiers == null || qualifiers.getNofQualifiers() == 0) {
					if (specifications == null) {
						specifications = new ArrayList<AttributeSpecification>();
					}
					specifications.add(attribute.getAttributeSpecification());
				}
			}
		}

		if (specifications == null) {
			return;
		}

		final List<ExtensionAttribute> attributes = new ArrayList<ExtensionAttribute>();
		AttributeSpecification specification;
		for (int i = 0, size = specifications.size(); i < size; i++) {
			specification = specifications.get(i);
			final ExtensionAttributeAnalyzer analyzer = new ExtensionAttributeAnalyzer();
			analyzer.parse(specification);
			final List<ExtensionAttribute> temp = analyzer.getAttributes();
			if (temp != null) {
				attributes.addAll(temp);
			}
		}

		ExtensionAttribute extensionAttribute;
		for (int i = 0, size = attributes.size(); i < size; i++) {
			extensionAttribute = attributes.get(i);

			switch (extensionAttribute.getAttributeType()) {
			case PROTOTYPE:
				if (EncodingPrototype_type.NONE.equals(prototype)) {
					prototype = ((PrototypeAttribute) extensionAttribute).getPrototypeType();
				} else {
					location.reportSemanticError("duplicate attribute `prototype'.");
				}
				break;
			case ENCODE:
				switch (functionEncodingType) {
				case MANUAL:
					break;
				case ENCODE:
					location.reportSemanticError("duplicate attribute `encode'.");
					break;
				case DECODE:
					location.reportSemanticError("`decode' and `encode' attributes cannot be used at the same time.");
					break;
				default:
					break;
				}
				encodingType = ((EncodeAttribute) extensionAttribute).getEncodingType();
				encodingOptions = ((EncodeAttribute) extensionAttribute).getOptions();
				functionEncodingType = ExternalFunctionEncodingType_type.ENCODE;
				break;
			case DECODE:
				switch (functionEncodingType) {
				case MANUAL:
					break;
				case ENCODE:
					location.reportSemanticError("`decode' and `encode' attributes cannot be used at the same time.");
					break;
				case DECODE:
					location.reportSemanticError("duplicate attribute `decode'.");
					break;
				default:
					break;
				}
				encodingType = ((DecodeAttribute) extensionAttribute).getEncodingType();
				encodingOptions = ((DecodeAttribute) extensionAttribute).getOptions();
				functionEncodingType = ExternalFunctionEncodingType_type.DECODE;
				break;
			case ERRORBEHAVIOR:
				if (errorBehaviorList == null) {
					errorBehaviorList = ((ErrorBehaviorAttribute) extensionAttribute).getErrrorBehaviorList();
				} else {
					errorBehaviorList.addAllBehaviors(((ErrorBehaviorAttribute) extensionAttribute).getErrrorBehaviorList());
				}
				break;
			case PRINTING:
				if (printingType == null) {
					printingType = ((PrintingAttribute) extensionAttribute).getPrintingType();
				} else {
					location.reportSemanticError("duplicate attribute `printing'.");
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Checks the prototype attribute set for this external function
	 * definition.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	private void checkPrototype(final CompilationTimeStamp timestamp) {
		if (EncodingPrototype_type.NONE.equals(prototype)) {
			return;
		}

		// checking formal parameter list
		if (EncodingPrototype_type.CONVERT.equals(prototype)) {
			if (formalParList.getNofParameters() == 1) {
				final FormalParameter parameter = formalParList.getParameterByIndex(0);
				switch (parameter.getRealAssignmentType()) {
				case A_PAR_VAL:
				case A_PAR_VAL_IN:
					inputType = parameter.getType(timestamp);
					break;
				default: {
					final String message = MessageFormat
							.format("The parameter must be an `in'' value parameter for attribute `prototype({0})'' instead of {1}",
									prototype.getName(), parameter.getAssignmentName());
					parameter.getLocation().reportSemanticError(message);
					break;
				}
				}
			} else {
				final String message = MessageFormat.format(
						"The external function must have one parameter instead of {0} for attribute `prototype({1})''",
						formalParList.getNofParameters(), prototype.getName());
				formalParList.getLocation().reportSemanticError(message);
			}
		} else if (formalParList.getNofParameters() == 2) {
			final FormalParameter firstParameter = formalParList.getParameterByIndex(0);
			if (EncodingPrototype_type.SLIDING.equals(prototype)) {
				if (Assignment_type.A_PAR_VAL_INOUT.semanticallyEquals(firstParameter.getRealAssignmentType())) {
					final Type firstParameterType = firstParameter.getType(timestamp);
					final IType last = firstParameterType.getTypeRefdLast(timestamp);
					if (last.getIsErroneous(timestamp)) {
						inputType = firstParameterType;
					} else {
						switch (last.getTypetypeTtcn3()) {
						case TYPE_OCTETSTRING:
						case TYPE_CHARSTRING:
						case TYPE_BITSTRING:
							inputType = firstParameterType;
							break;
						default: {
							final String message = MessageFormat
									.format("The type of the first parameter must be `octetstring'' or `charstring'' for attribute `prototype({0})''",
											prototype.getName());
							firstParameter.getLocation().reportSemanticError(message);

							break;
						}
						}
					}
				} else {
					firstParameter.getLocation()
					.reportSemanticError(
							MessageFormat.format(
									"The first parameter must be an `inout'' value parameter for attribute `prototype({0})'' instead of {1}",
									prototype.getName(), firstParameter.getAssignmentName()));
				}
			} else {
				if (Assignment_type.A_PAR_VAL_IN.semanticallyEquals(firstParameter.getRealAssignmentType())) {
					inputType = firstParameter.getType(timestamp);
				} else {
					firstParameter.getLocation()
					.reportSemanticError(
							MessageFormat.format(
									"The first parameter must be an `in'' value parameter for attribute `prototype({0})'' instead of {1}",
									prototype.getName(), firstParameter.getAssignmentName()));
				}
			}

			final FormalParameter secondParameter = formalParList.getParameterByIndex(1);
			if (Assignment_type.A_PAR_VAL_OUT.semanticallyEquals(secondParameter.getRealAssignmentType())) {
				outputType = secondParameter.getType(timestamp);
			} else {
				secondParameter.getLocation()
				.reportSemanticError(
						MessageFormat.format(
								"The second parameter must be an `out'' value parameter for attribute `prototype({0})'' instead of {1}",
								prototype.getName(), secondParameter.getAssignmentName()));
			}
		} else {
			formalParList.getLocation().reportSemanticError(
					MessageFormat.format("The function must have two parameters for attribute `prototype({0})'' instead of {1}",
							prototype.getName(), formalParList.getNofParameters()));
		}

		// checking the return type
		if (EncodingPrototype_type.FAST.equals(prototype)) {
			if (returnType != null) {
				returnType.getLocation().reportSemanticError(
						MessageFormat.format("The external function cannot have return type fo attribute `prototype({0})''",
								prototype.getName()));
			}
		} else {
			if (returnType == null) {
				location.reportSemanticError(MessageFormat.format(
						"The external function must have a return type for attribute `prototype({0})''", prototype.getName()));
			} else {
				if (Assignment_type.A_FUNCTION_RTEMP.semanticallyEquals(assignmentType)) {
					returnType.getLocation()
					.reportSemanticError(
							MessageFormat.format(
									"The external function must return a value instead of a template for attribute `prototype({0})''",
									prototype.getName()));
				}

				if (EncodingPrototype_type.CONVERT.equals(prototype)) {
					outputType = returnType;
				} else {
					final IType last = returnType.getTypeRefdLast(timestamp);

					if (!last.getIsErroneous(timestamp) && !Type_type.TYPE_INTEGER.equals(last.getTypetypeTtcn3())) {
						returnType.getLocation()
						.reportSemanticError(
								MessageFormat.format(
										"The return type of the function must be `integer'' instead of `{0}'' for attribute `prototype({1})''",
										returnType.getTypename(), prototype.getName()));
					}
				}
			}
		}
	}

	/**
	 * Checks that the encoding/decoding attributes set for this external
	 * function definition are valid according to the encoding/decoding type
	 * of the function.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void checkFunctionType(final CompilationTimeStamp timestamp) {
		switch (functionEncodingType) {
		case MANUAL:
			if (errorBehaviorList != null) {
				errorBehaviorList.getLocation().reportSemanticError(
						"Attribute `errorbehavior' can only be used together with `encode' or `decode'");
				errorBehaviorList.check(timestamp);
			}
			break;
		case ENCODE:
			switch (prototype) {
			case NONE:
				location.reportSemanticError("Attribute `encode' cannot be used without `prototype'");
				break;
			case BACKTRACK:
			case SLIDING:
				location.reportSemanticError(MessageFormat.format("Attribute `encode'' cannot be used without `prototype({0})''",
						prototype.getName()));
				break;
			default:
				break;
			}
			// FIXME implement once we know what encoding is set for
			// a type
			if (errorBehaviorList != null) {
				errorBehaviorList.check(timestamp);
			}
			if (printingType != null) {
				printingType.check(timestamp);
			}
			break;
		case DECODE:
			if (EncodingPrototype_type.NONE.equals(prototype)) {
				location.reportSemanticError("Attribute `decode' cannot be used without `prototype'");
			}
			// FIXME implement once we know what encoding is set for
			// a type
			if (errorBehaviorList != null) {
				errorBehaviorList.check(timestamp);
			}
			break;
		default:
			// no other option possible
			break;
		}

		if (printingType != null && (functionEncodingType != ExternalFunctionEncodingType_type.ENCODE ||
				encodingType != Encoding_type.JSON)) {
			location.reportSemanticError("Attribute `printing' is only allowed for JSON encoding functions");
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getProposalDescription() {
		final StringBuilder nameBuilder = new StringBuilder(identifier.getDisplayName());
		nameBuilder.append('(');
		formalParList.getAsProposalDesriptionPart(nameBuilder);
		nameBuilder.append(')');
		return nameBuilder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (subrefs.size() == i + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			final StringBuilder patternBuilder = new StringBuilder(identifier.getDisplayName());
			patternBuilder.append('(');
			formalParList.getAsProposalPart(patternBuilder);
			patternBuilder.append(')');
			propCollector.addTemplateProposal(identifier.getDisplayName(),
					new Template(getProposalDescription(), "", propCollector.getContextIdentifier(), patternBuilder.toString(),
							false), TTCN3CodeSkeletons.SKELETON_IMAGE);
			super.addProposal(propCollector, i);
		} else if (subrefs.size() > i + 1 && returnType != null
				&& Subreference_type.parameterisedSubReference.equals(subrefs.get(i).getReferenceType())
				&& identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			returnType.addProposal(propCollector, i + 1);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (identifier.getName().equals(subrefs.get(0).getId().getName())) {
			if (subrefs.size() > i + 1 && returnType != null) {
				returnType.addDeclaration(declarationCollector, i + 1);
			} else {
				declarationCollector.addDeclaration(this);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineText() {
		if (lastTimeChecked == null) {
			check(CompilationTimeStamp.getBaseTimestamp());
		}

		final StringBuilder text = new StringBuilder(identifier.getDisplayName());
		if (formalParList == null) {
			return text.toString();
		}

		text.append('(');
		for (int i = 0; i < formalParList.getNofParameters(); i++) {
			if (i != 0) {
				text.append(", ");
			}
			final FormalParameter parameter = formalParList.getParameterByIndex(i);
			if (Assignment_type.A_PAR_TIMER.semanticallyEquals(parameter.getRealAssignmentType())) {
				text.append("timer");
			} else {
				final IType type = parameter.getType(lastTimeChecked);
				if (type == null) {
					text.append("Unknown type");
				} else {
					text.append(type.getTypename());
				}
			}
		}
		text.append(')');
		return text.toString();
	}

	@Override
	/** {@inheritDoc} */
	public TemplateRestriction.Restriction_type getTemplateRestriction() {
		return templateRestriction;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			boolean enveloped = false;

			final Location temporalIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(temporalIdentifier) || reparser.isExtending(temporalIdentifier)) {
				reparser.extendDamagedRegion(temporalIdentifier);
				final IIdentifierReparser r = new IdentifierReparser(reparser);
				final int result = r.parseAndSetNameChanged();
				identifier = r.getIdentifier();
				// damage handled
				if (result == 0 && identifier != null) {
					enveloped = true;
				} else {
					throw new ReParseException(result);
				}
			}

			if (formalParList != null) {
				if (enveloped) {
					formalParList.updateSyntax(reparser, false);
					reparser.updateLocation(formalParList.getLocation());
				} else if (reparser.envelopsDamage(formalParList.getLocation())) {
					formalParList.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(formalParList.getLocation());
				}
			}

			if (returnType != null) {
				if (enveloped) {
					returnType.updateSyntax(reparser, false);
					reparser.updateLocation(returnType.getLocation());
				} else if (reparser.envelopsDamage(returnType.getLocation())) {
					returnType.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(returnType.getLocation());
				}
			}

			if (withAttributesPath != null) {
				if (enveloped) {
					withAttributesPath.updateSyntax(reparser, false);
					reparser.updateLocation(withAttributesPath.getLocation());
				} else if (reparser.envelopsDamage(withAttributesPath.getLocation())) {
					withAttributesPath.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(withAttributesPath.getLocation());
				}
			}

			if (!enveloped) {
				throw new ReParseException();
			}

			return;
		}

		reparser.updateLocation(identifier.getLocation());

		if (formalParList != null) {
			formalParList.updateSyntax(reparser, false);
			reparser.updateLocation(formalParList.getLocation());
		}

		if (returnType != null) {
			returnType.updateSyntax(reparser, false);
			reparser.updateLocation(returnType.getLocation());
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
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (formalParList != null && !formalParList.accept(v)) {
			return false;
		}
		if (returnType != null && !returnType.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenName() {
		final StringBuilder returnValue = new StringBuilder();

		if (functionEncodingType == ExternalFunctionEncodingType_type.MANUAL) {
			returnValue.append(myScope.getModuleScope().getIdentifier().getName());
			returnValue.append("_externalfunctions.");
		}
		returnValue.append(identifier.getName());

		return returnValue.toString();
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameFromScope(final JavaGenData aData, final StringBuilder source, final Scope scope, final String prefix) {
		if (functionEncodingType == ExternalFunctionEncodingType_type.MANUAL) {
			final StringBuilder returnValue = new StringBuilder();

			returnValue.append(myScope.getModuleScope().getIdentifier().getName());
			returnValue.append("_externalfunctions");
			returnValue.append('.');
			returnValue.append(identifier.getName());

			return returnValue.toString();
		} else {
			return super.getGenNameFromScope(aData, source, scope, prefix);
		}
		
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final boolean cleanUp) {
		final String genName = getGenName();
		if (formalParList != null) {
			formalParList.setGenName(genName);
		}

		if (functionEncodingType == ExternalFunctionEncodingType_type.MANUAL) {
			aData.addImport("org.eclipse.titan.user_provided." + myScope.getModuleScope().getIdentifier().getName() + "_externalfunctions");
			// external functions are implemented elsewhere
			return;
		}

		final StringBuilder sb = aData.getSrc();
		final StringBuilder source = new StringBuilder();
		if(VisibilityModifier.Private.equals(getVisibilityModifier())) {
			source.append( "private" );
		} else {
			source.append( "public" );
		}
		source.append( " static final " );

		// return value
		switch (assignmentType) {
		case A_EXT_FUNCTION:
			source.append( "void" );
			break;
		case A_EXT_FUNCTION_RVAL:
			source.append( returnType.getGenNameValue( aData, source, getMyScope() ) );
			break;
		case A_EXT_FUNCTION_RTEMP:
			source.append( returnType.getGenNameTemplate( aData, source, getMyScope() ) );
			break;
		default:
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous definition `" + getFullName() + "''");
			return;
		}

		source.append( ' ' );

		// function name
		source.append( genName );

		// arguments
		source.append( '(' );
		if ( formalParList != null ) {
			formalParList.generateCode( aData, source );
		}
		source.append( ") {\n" );
		switch(functionEncodingType) {
		case ENCODE:
			generate_code_encode(aData, source);
			break;
		case DECODE:
			generate_code_decode(aData, source);
			break;
		default:
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous definition `" + getFullName() + "''");
			return;
		}
		source.append( "}\n" );

		sb.append(source);
	}

	/**
	 * Generate Java code for the body of an external function with an encoding prototype.
	 *
	 * generate_code_encode in the compiler
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the stringbuilder to generate the code to.
	 */
	private void generate_code_encode(final JavaGenData aData, final StringBuilder source) {
		aData.addCommonLibraryImport("TTCN_Buffer");
		aData.addCommonLibraryImport("TTCN_EncDec");
		aData.addCommonLibraryImport("TtcnLogger");

		final String firstParName = formalParList.getParameterByIndex(0).getIdentifier().getName();

		source.append( "if (TtcnLogger.log_this_event(Severity.DEBUG_ENCDEC)) {\n" );
		source.append( "TtcnLogger.begin_event(Severity.DEBUG_ENCDEC);\n" );
		source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}(): Encoding {1}: \");\n", identifier.getDisplayName(), inputType.getTypename()));
		source.append( MessageFormat.format( "{0}.log();\n", firstParName) );
		source.append( "TtcnLogger.end_event();\n" );
		source.append( "}\n" );
		//FIXME implement error handling
		source.append( "TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_ALL, TTCN_EncDec.error_behavior_type.EB_DEFAULT);\n" );
		source.append( "TTCN_Buffer ttcn_buffer = new TTCN_Buffer();\n" );
		source.append( MessageFormat.format( "{0}.encode({1}_descr_, ttcn_buffer, TTCN_EncDec.coding_type.CT_{2}, 0);\n", firstParName, inputType.getGenNameTypeDescriptor(aData, source, myScope), encodingType.getEncodingName()) );

		//FIXME implement JSON and XER specific parts
		String resultName;
		switch (prototype) {
		case CONVERT:
			resultName = "ret_val";
			// creating a local variable for the result stream
			source.append(MessageFormat.format("{0} ret_val = new {0}();\n", returnType.getGenNameValue( aData, source, getMyScope() )));
			break;
		case FAST:
			resultName = formalParList.getParameterByIndex(1).getIdentifier().getName();
			break;
		default:
			resultName = "";
			break;
		}

		// taking the result from the buffer and producing debug printout
		if (inputType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetypeTtcn3() == Type_type.TYPE_BITSTRING) {
			source.append( "TitanOctetString tmp_os = new TitanOctetString();\n" );
			source.append( "ttcn_buffer.get_string(tmp_os);\n" );
			source.append( MessageFormat.format("{0} = AdditionalFunctions.oct2bit(tmp_os);\n", resultName));
		} else {
			source.append( MessageFormat.format("ttcn_buffer.get_string({0});\n", resultName));
		}
		source.append( "if (TtcnLogger.log_this_event(Severity.DEBUG_ENCDEC)) {\n" );
		source.append( "TtcnLogger.begin_event(Severity.DEBUG_ENCDEC);\n" );
		source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}(): Stream after encoding: \");\n", identifier.getDisplayName()));
		source.append( "ret_val.log();\n" );
		source.append( "TtcnLogger.end_event();\n" );
		source.append( "}\n" );
		source.append( "return ret_val;\n" );
	}

	/**
	 * Generate Java code for the body of an external function with an decoding prototype.
	 *
	 * generate_code_decode in the compiler
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the stringbuilder to generate the code to.
	 */
	private void generate_code_decode(final JavaGenData aData, final StringBuilder source) {
		aData.addCommonLibraryImport("TTCN_Buffer");
		aData.addCommonLibraryImport("TTCN_EncDec");
		aData.addCommonLibraryImport("TTCN_EncDec.error_type");
		aData.addCommonLibraryImport("TtcnLogger");

		//FIXME implement get_string, error handling and other variants
		final String firstParName = formalParList.getParameterByIndex(0).getIdentifier().getName();

		source.append( "if (TtcnLogger.log_this_event(Severity.DEBUG_ENCDEC)) {\n" );
		source.append( "TtcnLogger.begin_event(Severity.DEBUG_ENCDEC);\n" );
		source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}(): Stream before decoding: \");\n", identifier.getDisplayName()));
		source.append( MessageFormat.format( "{0}.log();\n", firstParName) );
		source.append( "TtcnLogger.end_event();\n" );
		source.append( "}\n" );
		source.append( "TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_ALL, TTCN_EncDec.error_behavior_type.EB_DEFAULT);\n" );
		source.append( "TTCN_EncDec.clear_error();\n" );

		// creating a buffer from the input stream
		if (inputType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetypeTtcn3() == Type_type.TYPE_BITSTRING) {
			aData.addCommonLibraryImport("AdditionalFunctions");

			source.append( MessageFormat.format( "TTCN_Buffer ttcn_buffer = new TTCN_Buffer(AdditionalFunctions.bit2oct({0}));\n", firstParName) );
		} else {
			source.append( MessageFormat.format( "TTCN_Buffer ttcn_buffer = new TTCN_Buffer({0});\n", firstParName) );
		}

		String resultName;
		if (prototype == EncodingPrototype_type.CONVERT) {
			source.append( "TitanInteger ret_val = new TitanInteger();\n" );
			resultName = "ret_val";
		} else {
			resultName = formalParList.getParameterByIndex(1).getIdentifier().getName();
		}
		if (encodingType == Encoding_type.TEXT) {
			source.append( "if (TtcnLogger.log_this_event(Severity.DEBUG_ENCDEC)) {\n" );
			source.append( "TTCN_EncDec.set_error_behavior(TTCN_EncDec.error_type.ET_LOG_MATCHING, TTCN_EncDec.error_behavior_type.EB_WARNING);\n" );
			source.append( "}\n" );
		}
		source.append( MessageFormat.format( "{0}.decode({1}_descr_, ttcn_buffer, TTCN_EncDec.coding_type.CT_{2}, {3});\n", resultName, outputType.getGenNameTypeDescriptor(aData, source, myScope), encodingType.getEncodingName(), encodingOptions == null? "0": encodingOptions) );

		// producing debug printout of the result PDU
		source.append( "if (TtcnLogger.log_this_event(Severity.DEBUG_ENCDEC)) {\n" );
		source.append( "TtcnLogger.begin_event(Severity.DEBUG_ENCDEC);\n" );
		source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}(): Decoded {1}: \");\n", identifier.getDisplayName(), outputType.getTypename()));
		source.append( MessageFormat.format( "{0}.log();\n", resultName) );
		source.append( "TtcnLogger.end_event();\n" );
		source.append( "}\n" );
		if (prototype != EncodingPrototype_type.SLIDING) {
			// checking for remaining data in the buffer if decoding was successful
			source.append( "if (TTCN_EncDec.get_last_error_type() == error_type.ET_NONE) {\n" );
			source.append( "if (ttcn_buffer.get_pos() < ttcn_buffer.get_len() -1 && TtcnLogger.log_this_event(Severity.WARNING_UNQUALIFIED)) {\n" );
			source.append( "ttcn_buffer.cut();\n" );
			source.append( MessageFormat.format( "{0} remaining_stream = new {0}();\n", inputType.getGenNameValue(aData, source, myScope)) );
			if (inputType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetypeTtcn3() == Type_type.TYPE_BITSTRING) {
				source.append( "TitanOctetString tmp_os = new TitanOctetString();\n" );
				source.append( "ttcn_buffer.get_string(tmp_os);\n" );
				source.append( "remaining_stream = AdditionalFunctions.oct2bit(tmp_os);\n" );
			} else {
				source.append( "ttcn_buffer.get_string(remaining_stream);\n" );
			}

			source.append( "TtcnLogger.begin_event(Severity.WARNING_UNQUALIFIED);\n" );
			source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}(): Warning: Data remained at the end of the stream after successful decoding: \");\n", identifier.getDisplayName()));
			source.append( "remaining_stream.log();\n" );
			source.append( "TtcnLogger.end_event();\n" );
			source.append( "}\n" );

			// closing the block and returning the appropriate result or status code
			if (prototype == EncodingPrototype_type.BACKTRACK) {
				source.append( "return new TitanInteger(0);\n" );
				source.append( "} else {\n" );
				source.append( "return new TitanInteger(1);\n" );
				source.append( "}\n" );
			} else {
				source.append( "}\n" );
				if (prototype == EncodingPrototype_type.CONVERT) {
					source.append( "return ret_val;\n" );
				}
			}
		} else {
			// result handling and debug printout for sliding decoders
			source.append( "switch (TTCN_EncDec.get_last_error_type()) {\n" );
			source.append( "case ET_NONE: {\n" );
			source.append( "ttcn_buffer.cut();\n" );
			if (inputType.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp()).getTypetypeTtcn3() == Type_type.TYPE_BITSTRING) {
				source.append( "TitanOctetString tmp_os = new TitanOctetString();\n" );
				source.append( "ttcn_buffer.get_string(tmp_os);\n" );
				source.append(MessageFormat.format( "{0} = AdditionalFunctions.oct2bit(tmp_os);\n", firstParName) );
			} else {
				source.append(MessageFormat.format( "ttcn_buffer.get_string({0});\n", firstParName) );
			}
			source.append( "if (TtcnLogger.log_this_event(Severity.DEBUG_ENCDEC)) {\n" );
			source.append( "TtcnLogger.begin_event(Severity.DEBUG_ENCDEC);\n" );
			source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}(): stream after decoding: \");\n", identifier.getDisplayName()));
			source.append( MessageFormat.format( "{0}.log();\n", firstParName) );
			source.append( "TtcnLogger.end_event();\n" );
			source.append( "}\n" );
			source.append( "return new TitanInteger(0); }\n" );
			source.append( "case ET_INCOMPL_MSG:\n" );
			source.append( "case ET_LEN_ERR:\n" );
			source.append( "return new TitanInteger(2);\n" );
			source.append( "default:\n" );
			source.append( "return new TitanInteger(1);\n" );
			source.append( "}\n" );
		}
	}
}
