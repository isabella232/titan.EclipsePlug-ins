/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.Assignment.Assignment_type;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.RunsOnScope;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.types.FunctionReferenceGenerator.FunctionReferenceDefinition;
import org.eclipse.titan.designer.AST.TTCN3.types.FunctionReferenceGenerator.fatType;
import org.eclipse.titan.designer.AST.TTCN3.types.subtypes.SubType;
import org.eclipse.titan.designer.AST.TTCN3.values.Altstep_Reference_Value;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * altstep type (TTCN-3).
 *
 * @author Kristof Szabados
 * */
public final class Altstep_Type extends Type {
	private static final String ALTSTEPREFERENCEVALUEEXPECTED = "Reference to an altstep was expected";
	private static final String RUNSONLESSEXPECTED = "Type `{0}'' does not have a `runs on'' clause, but {1} runs on `{2}''.";
	private static final String INCOMPATIBLERUNSONTYPESERROR =
			"Runs on clause mismatch: type `{0}'' expects component type `{1}'', but {2} runs on `{3}''";
	private static final String TEMPLATENOTALLOWED = "{0} cannot be used for type `{1}''";
	private static final String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for type `{0}''";

	private static final String FULLNAMEPART1 = ".<formal_parameter_list>";
	private static final String FULLNAMEPART2 = ".<runsOnType>";

	private final FormalParameterList formalParList;
	private final Reference runsOnRef;
	private Component_Type runsOnType;
	private final boolean runsOnSelf;

	public Altstep_Type(final FormalParameterList formalParList, final Reference runsOnRef, final boolean runsOnSelf) {
		this.formalParList = formalParList;
		this.runsOnRef = runsOnRef;
		this.runsOnSelf = runsOnSelf;

		formalParList.setFullNameParent(this);
		if (runsOnRef != null) {
			runsOnRef.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getTypetype() {
		return Type_type.TYPE_ALTSTEP;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (formalParList == child) {
			return builder.append(FULLNAMEPART1);
		} else if (runsOnRef == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		formalParList.setMyScope(scope);
		if (runsOnRef != null) {
			runsOnRef.setMyScope(scope);
		}
		scope.addSubScope(formalParList.getLocation(), formalParList);
	}

	@Override
	/** {@inheritDoc} */
	public boolean isCompatible(final CompilationTimeStamp timestamp, final IType otherType, final TypeCompatibilityInfo info,
			final TypeCompatibilityInfo.Chain leftChain, final TypeCompatibilityInfo.Chain rightChain) {
		check(timestamp);
		otherType.check(timestamp);
		final IType temp = otherType.getTypeRefdLast(timestamp);

		if (getIsErroneous(timestamp) || temp.getIsErroneous(timestamp)) {
			return true;
		}

		return Type_type.TYPE_ALTSTEP.equals(temp.getTypetype());
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

	/** @return the formal parameterlist of the type */
	public FormalParameterList getFormalParameters() {
		return formalParList;
	}

	/**
	 * Returns the runs on component type of the actual altstep type.
	 *
	 * @param timestamp the time stamp of the actual semantic check cycle.
	 *
	 * @return the runs on component type or null if none.
	 * */
	public Component_Type getRunsOnType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return runsOnType;
	}

	/** @return true if the type has the runs on self clause, false otherwise */
	public boolean isRunsOnSelf() {
		return runsOnSelf;
	}

	@Override
	/** {@inheritDoc} */
	public String getTypename() {
		return getFullName();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "altstep.gif";
	}

	@Override
	/** {@inheritDoc} */
	public boolean isComponentInternal(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return runsOnSelf;
	}

	@Override
	/** {@inheritDoc} */
	public SubType.SubType_type getSubtypeType() {
		return SubType.SubType_type.ST_ALTSTEP;
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		runsOnType = null;
		lastTimeChecked = timestamp;
		isErroneous = false;

		initAttributes(timestamp);

		if (runsOnRef != null) {
			runsOnType = runsOnRef.chkComponentypeReference(timestamp);
			if (runsOnType != null) {
				final Scope formalParlistPreviosScope = formalParList.getParentScope();
				if (formalParlistPreviosScope instanceof RunsOnScope && ((RunsOnScope) formalParlistPreviosScope).getParentScope() == myScope) {
					((RunsOnScope) formalParlistPreviosScope).setComponentType(runsOnType);
				} else {
					final Scope tempScope = new RunsOnScope(runsOnType, myScope);
					formalParList.setMyScope(tempScope);
				}
			}
		}

		formalParList.reset();
		formalParList.check(timestamp, Assignment_type.A_ALTSTEP);

		formalParList.checkNoLazyParams();

		checkSubtypeRestrictions(timestamp);

		if (myScope != null) {
			checkEncode(timestamp);
			checkVariants(timestamp);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkComponentInternal(final CompilationTimeStamp timestamp, final Set<IType> typeSet, final String operation) {
		if (runsOnSelf) {
			location.reportSemanticError(
					MessageFormat.format("Altstep type `{0}'' with `runs on self'' clause cannot be {1}", getTypename(), operation));
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisValue(final CompilationTimeStamp timestamp, final IValue value, final Assignment lhs, final ValueCheckingOptions valueCheckingOptions) {
		final boolean selfReference = super.checkThisValue(timestamp, value, lhs, valueCheckingOptions);

		final IValue last = value.getValueRefdLast(timestamp, valueCheckingOptions.expected_value, null);
		if (last == null || last.getIsErroneous(timestamp)) {
			return selfReference;
		}

		last.setMyGovernor(this);
		// already handled ones
		switch (value.getValuetype()) {
		case OMIT_VALUE:
		case REFERENCED_VALUE:
			return selfReference;
		case UNDEFINED_LOWERIDENTIFIER_VALUE:
			if (Value_type.REFERENCED_VALUE.equals(last.getValuetype())) {
				return selfReference;
			}
			break;
		default:
			break;
		}

		Def_Altstep altstep = null;
		switch (last.getValuetype()) {
		case ALTSTEP_REFERENCE_VALUE:
			altstep = ((Altstep_Reference_Value) last).getReferredAltstep();
			if (altstep == null) {
				setIsErroneous(true);
				return selfReference;
			}
			altstep.check(timestamp);
			break;
		case TTCN3_NULL_VALUE:
			value.setValuetype(timestamp, Value_type.FAT_NULL_VALUE);
			return selfReference;
		case EXPRESSION_VALUE:
		case MACRO_VALUE:
			// already checked
			return selfReference;
		default:
			value.getLocation().reportSemanticError(ALTSTEPREFERENCEVALUEEXPECTED);
			value.setIsErroneous(true);
			return selfReference;
		}

		formalParList.checkCompatibility(timestamp, altstep.getFormalParameterList(), value.getLocation());

		final IType temporalRunsOnType = altstep.getRunsOnType(timestamp);
		if (temporalRunsOnType != null) {
			if (runsOnSelf) {
				//check against the runs on component type of the scope of the value
				final Scope valueScope = value.getMyScope();
				if (valueScope == null) {
					value.setIsErroneous(true);
					return selfReference;
				}

				final RunsOnScope runsOnScope =  valueScope.getScopeRunsOn();
				if (runsOnScope != null) {
					final Component_Type componentType = runsOnScope.getComponentType();
					if (!runsOnType.isCompatible(timestamp, componentType, null, null, null)) {
						value.getLocation().reportSemanticError(MessageFormat.format(
								"Runs on clause mismatch: type `{0}'' has a `runs on self'' clause and the current scope "
										+ "expects component type `{1}'', but {2} runs on `{3}''",
										getTypename(), componentType.getTypename(), altstep.getDescription(), temporalRunsOnType.getTypename()));
					}
				} else {
					// does not have 'runs on' clause
					// if the value's scope is a component body then check the runs on
					// compatibility using this component type as the scope
					if (valueScope instanceof ComponentTypeBody) {
						final ComponentTypeBody body = (ComponentTypeBody) valueScope;
						if (!runsOnType.isCompatible(timestamp, body.getMyType(), null, null, null)) {
							value.getLocation().reportSemanticError(MessageFormat.format(
									"Runs on clause mismatch: type `{0}'' has a `runs on self'' "
											+ "clause and the current component definition is of type `{1}'', but {2} runs on `{3}''",
											getTypename(), body.getMyType().getTypename(), altstep.getDescription(), temporalRunsOnType.getTypename()));
						}
					} else {
						value.getLocation().reportSemanticError(MessageFormat.format(
								"Type `{0}'' has a `runs on self'' clause and the current scope does not have a `runs on'' clause,"
										+ " but {1} runs on `{2}''", getTypename(), altstep.getDescription(), temporalRunsOnType.getTypename()));
					}
				}
			} else {
				if (runsOnRef == null) {
					value.getLocation().reportSemanticError(
							MessageFormat.format(RUNSONLESSEXPECTED, getTypename(), altstep.getAssignmentName(), temporalRunsOnType.getTypename()));
					value.setIsErroneous(true);
				} else {
					if (runsOnType != null && !temporalRunsOnType.isCompatible(timestamp, runsOnType, null, null, null)) {
						value.getLocation().reportSemanticError(
								MessageFormat.format(INCOMPATIBLERUNSONTYPESERROR, getTypename(), runsOnType.getTypename(), altstep
										.getAssignmentName(), temporalRunsOnType.getTypename()));
						value.setIsErroneous(true);
					}
				}
			}
		}

		if (valueCheckingOptions.sub_check) {
			//there is no parent type to check
			if (subType != null) {
				subType.checkThisValue(timestamp, value);
			}
		}

		value.setLastTimeChecked(timestamp);

		return selfReference;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
			final boolean isModified, final boolean implicitOmit, final Assignment lhs) {
		registerUsage(template);
		template.setMyGovernor(this);

		template.getLocation().reportSemanticError(MessageFormat.format(TEMPLATENOTALLOWED, template.getTemplateTypeName(), getTypename()));

		if (template.getLengthRestriction() != null) {
			template.getLocation().reportSemanticError(MessageFormat.format(LENGTHRESTRICTIONNOTALLOWED, getTypename()));
		}

		return false;
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
		return builder.append("altstep type");
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() != i + 1 || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		propCollector.addTemplateProposal("apply", new Template("apply( parameters )", "", propCollector.getContextIdentifier(),
				"apply( ${parameters} )", false), TTCN3CodeSkeletons.SKELETON_IMAGE);
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

		if (runsOnRef != null) {
			runsOnRef.updateSyntax(reparser, false);
			reparser.updateLocation(runsOnRef.getLocation());
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
		if (runsOnRef != null && runsOnType != null) {
			if (runsOnType == referenceFinder.type) {
				foundIdentifiers.add(new Hit(runsOnRef.getId(), runsOnRef));
			}
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
		if (runsOnRef!=null && !runsOnRef.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameValue(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData);
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTemplate(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData).concat("_template");
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameTypeDescriptor(final JavaGenData aData, final StringBuilder source) {
		final String baseName = getGenNameTypeName(aData, source);
		return baseName + "." + getGenNameOwn();
	}

	@Override
	/** {@inheritDoc} */
	public String getGenNameRawDescriptor(final JavaGenData aData, final StringBuilder source) {
		return getGenNameOwn(aData) + "." + getGenNameOwn() + "_raw_";
	}

	@Override
	/** {@inheritDoc} */
	public boolean generatesOwnClass(final JavaGenData aData, final StringBuilder source) {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}

		lastTimeGenerated = aData.getBuildTimstamp();

		final String genName = getGenNameOwn();
		final String displayName = getFullName();

		final StringBuilder localTypeDescriptor = new StringBuilder();
		generateCodeTypedescriptor(aData, source, localTypeDescriptor, null);
		generateCodeDefaultCoding(aData, source, localTypeDescriptor);
		final StringBuilder localCodingHandler = new StringBuilder();
		generateCodeForCodingHandlers(aData, source, localCodingHandler);

		final FunctionReferenceDefinition def = new FunctionReferenceDefinition(genName, displayName);
		def.returnType = null;
		def.type = fatType.ALTSTEP;
		def.runsOnSelf = runsOnSelf;
		def.isStartable = false;
		def.formalParList = formalParList.generateCode(aData).toString();
		def.actualParList = formalParList.generateCodeActualParlist("").toString();
		def.parameterTypeNames = new ArrayList<String>(formalParList.getNofParameters());
		def.parameterNames = new ArrayList<String>(formalParList.getNofParameters());
		for ( int i = 0; i < formalParList.getNofParameters(); i++) {
			final FormalParameter formalParameter = formalParList.getParameterByIndex(i);
			switch (formalParameter.getAssignmentType()) {
			case A_PAR_VAL:
			case A_PAR_VAL_IN:
			case A_PAR_VAL_INOUT:
			case A_PAR_VAL_OUT:
				def.parameterTypeNames.add( formalParameter.getType(CompilationTimeStamp.getBaseTimestamp()).getGenNameValue( aData, source ) );
				break;
			case A_PAR_TEMP_IN:
			case A_PAR_TEMP_INOUT:
			case A_PAR_TEMP_OUT:
				def.parameterTypeNames.add( formalParameter.getType(CompilationTimeStamp.getBaseTimestamp()).getGenNameTemplate( aData, source ) );
				break;
			default:
				break;
			}
			def.parameterNames.add(formalParameter.getIdentifier().getName());
		}

		FunctionReferenceGenerator.generateValueClass(aData, source, def, localTypeDescriptor, localCodingHandler);
		FunctionReferenceGenerator.generateTemplateClass(aData, source, def);

		if (hasDoneAttribute()) {
			generateCodeDone(aData, source);
		}
		if (subType != null) {
			subType.generateCode(aData, source);
		}
	}
}
