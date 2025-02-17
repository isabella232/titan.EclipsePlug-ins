/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.TypeOwner_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction.Restriction_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template.Template_type;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString.PatternType;
import org.eclipse.titan.designer.AST.TTCN3.templates.TTCN3Template;
import org.eclipse.titan.designer.AST.TTCN3.templates.UnivCharString_Pattern_Template;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * Represents a TTCN3 template variable.
 *
 * @author Kristof Szabados
 * @author Arpad Lovassy
 * @author Gergo Ujhelyi
 */
public final class Def_Var_Template extends Definition {
	private static final String FULLNAMEPART1 = ".<type>";
	private static final String FULLNAMEPART2 = ".<initial_value>";
	public static final String PORTNOTALLOWED = "Template variable can not be defined for port type `{0}''";
	private static final String PARAMETRIZED_LOCAL_TEMPLATE_VAR = "Code generation for parameterized local template variable `{0}'' is not yet supported";

	private static final String KIND = " template variable definition";

	private final Type type;

	/**
	 * Formal parameters.
	 * NOTE: It is not yet supported, so semantic error must be marked if not null
	 */
	private final FormalParameterList mFormalParList;
	private final TTCN3Template initialValue;
	private final TemplateRestriction.Restriction_type templateRestriction;
	private boolean generateRestrictionCheck = false;

	private boolean wasAssigned;

	public Def_Var_Template( final TemplateRestriction.Restriction_type templateRestriction,
			final Identifier identifier,
			final Type type,
			final FormalParameterList aFormalParList,
			final TTCN3Template initialValue) {
		super(identifier);
		this.templateRestriction = templateRestriction;
		this.type = type;
		mFormalParList = aFormalParList;
		this.initialValue = initialValue;

		if (type != null) {
			type.setOwnertype(TypeOwner_type.OT_VARTMPL_DEF, this);
			type.setFullNameParent(this);
		}
		if (initialValue != null) {
			initialValue.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_VAR_TEMPLATE;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(FULLNAMEPART1);
		} else if (initialValue == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public String getAssignmentName() {
		return "template variable";
	}

	@Override
	/** {@inheritDoc} */
	public String getDescription() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getAssignmentName()).append(" `");

		if (isLocal()) {
			builder.append(identifier.getDisplayName());
		} else {
			builder.append(getFullName());
		}

		builder.append('\'');
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "template_dynamic.gif";
	}

	@Override
	/** {@inheritDoc} */
	public int category() {
		int result = super.category();
		if (type != null) {
			result += type.category();
		}
		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (type != null) {
			type.setMyScope(scope);
		}
		if (initialValue != null) {
			initialValue.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Type getType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return type;
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
		lastTimeChecked = timestamp;
		isUsed = false;
		wasAssigned = false;

		NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_VARTEMPLATE, identifier, this);
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (type == null) {
			lastTimeChecked = timestamp;
			return;
		}

		type.setGenName("_T_", getGenName());
		type.check(timestamp);

		if (initialValue == null) {
			return;
		}

		final IType lastType = type.getTypeRefdLast(timestamp);
		switch (lastType.getTypetype()) {
		case TYPE_PORT:
			location.reportSemanticError(MessageFormat.format(PORTNOTALLOWED, lastType.getFullName()));
			break;
		default:
			break;
		}

		TTCN3Template realInitialValue = initialValue;

		initialValue.setMyGovernor(type);

		// Needed in case of universal charstring templates
		if (initialValue.getTemplatetype() == Template_type.CSTR_PATTERN && lastType.getTypetype() == Type.Type_type.TYPE_UCHARSTRING) {
			realInitialValue = initialValue.setTemplatetype(timestamp, Template_type.USTR_PATTERN);
			if (realInitialValue instanceof UnivCharString_Pattern_Template) {
				((UnivCharString_Pattern_Template)realInitialValue).getPatternstring().setPatterntype(PatternType.UNIVCHARSTRING_PATTERN);
			}
		}

		final ITTCN3Template temporalValue = type.checkThisTemplateRef(timestamp, realInitialValue);
		temporalValue.checkThisTemplateGeneric(timestamp, type, true, true, true, true, false, this);
		generateRestrictionCheck = TemplateRestriction.check(timestamp, this, realInitialValue, null);

		realInitialValue.setCodeSection(CodeSectionType.CS_INLINE);

		// Only to follow the pattern, otherwise no such field can exist
		// here
		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
		}

		if ( mFormalParList != null ) {
			mFormalParList.reset();
			mFormalParList.check(timestamp, getAssignmentType());
			// template variable is always local
			location.reportSemanticError(MessageFormat.format(PARAMETRIZED_LOCAL_TEMPLATE_VAR, getIdentifier()));
		}

		initialValue.setGenNameRecursive(getGenName());
		initialValue.setCodeSection(CodeSectionType.CS_INLINE);
	}

	public TTCN3Template getInitialValue() {
		return initialValue;
	}

	/**
	 * Indicates that this variable template was used in a way where its
	 * value can be changed.
	 * */
	public void setWritten() {
		wasAssigned = true;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkIdentical(final CompilationTimeStamp timestamp, final Definition definition) {
		check(timestamp);
		definition.check(timestamp);

		if (!Assignment_type.A_VAR_TEMPLATE.semanticallyEquals(definition.getAssignmentType())) {
			location.reportSemanticError(MessageFormat
					.format("Local definition `{0}'' is a template variable, but the definition inherited from component type `{1}'' is a {2}",
							identifier.getDisplayName(), definition.getMyScope().getFullName(),
							definition.getAssignmentName()));
			return false;
		}

		final Def_Var_Template otherVariable = (Def_Var_Template) definition;
		if (!type.isIdentical(timestamp, otherVariable.type)) {
			final String message = MessageFormat
					.format("Local template variable `{0}'' has type `{1}'', but the template variable inherited from component type `{2}'' has type `{3}''",
							identifier.getDisplayName(), type.getTypename(), otherVariable.getMyScope().getFullName(),
							otherVariable.type.getTypename());
			type.getLocation().reportSemanticError(message);
			return false;
		}

		if (initialValue != null) {
			if (otherVariable.initialValue == null) {
				initialValue.getLocation()
				.reportSemanticWarning(
						MessageFormat.format(
								"Local template variable `{0}'' has initial value, but the template variable inherited from component type `{1}'' does not",
								identifier.getDisplayName(), otherVariable.getMyScope().getFullName()));
			}
		} else if (otherVariable.initialValue != null) {
			location.reportSemanticWarning(MessageFormat
					.format("Local template variable `{0}'' does not have initial value, but the template variable inherited from component type `{1}'' has",
							identifier.getDisplayName(), otherVariable.getMyScope().getFullName()));
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public String getProposalKind() {
		final StringBuilder builder = new StringBuilder();
		if (type != null) {
			type.getProposalDescription(builder);
		}
		builder.append(KIND);
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int index) {
		final List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= index) {
			return;
		}

		if (subrefs.size() == index + 1 && identifier.getName().toLowerCase(Locale.ENGLISH).startsWith(subrefs.get(index).getId().getName().toLowerCase(Locale.ENGLISH))) {
			super.addProposal(propCollector, index);
		} else if (subrefs.size() > index + 1 && type != null && identifier.getName().equals(subrefs.get(index).getId().getName())) {
			// perfect match
			type.addProposal(propCollector, index + 1);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int index) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > index && identifier.getName().equals(subrefs.get(index).getId().getName())) {
			if (subrefs.size() > index + 1 && type != null) {
				type.addDeclaration(declarationCollector, index + 1);
			} else if (subrefs.size() == index + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(index).getReferenceType())) {
				declarationCollector.addDeclaration(this);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public TemplateRestriction.Restriction_type getTemplateRestriction() {
		return templateRestriction;
	}

	@Override
	/** {@inheritDoc} */
	public List<Integer> getPossibleExtensionStarterTokens() {
		final List<Integer> result = super.getPossibleExtensionStarterTokens();

		if (initialValue == null) {
			result.add(Ttcn3Lexer.ASSIGNMENTCHAR);
		}

		return result;
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

			if (type != null) {
				if (enveloped) {
					type.updateSyntax(reparser, false);
					reparser.updateLocation(type.getLocation());
				} else if (reparser.envelopsDamage(type.getLocation())) {
					type.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(type.getLocation());
				}
			}

			if (initialValue != null) {
				if (enveloped) {
					initialValue.updateSyntax(reparser, false);
					reparser.updateLocation(initialValue.getLocation());
				} else if (reparser.envelopsDamage(initialValue.getLocation())) {
					initialValue.updateSyntax(reparser, true);
					enveloped = true;
					reparser.updateLocation(initialValue.getLocation());
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
		if (type != null) {
			type.updateSyntax(reparser, false);
			reparser.updateLocation(type.getLocation());
		}

		if (initialValue != null) {
			initialValue.updateSyntax(reparser, false);
			reparser.updateLocation(initialValue.getLocation());
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
		if (type != null) {
			type.findReferences(referenceFinder, foundIdentifiers);
		}
		if (initialValue != null) {
			initialValue.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (type != null && !type.accept(v)) {
			return false;
		}
		if (initialValue != null && !initialValue.accept(v)) {
			return false;
		}
		return true;
	}

	public boolean getWritten() {
		return wasAssigned;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final boolean cleanUp ) {
		final String genName = getGenName();
		final StringBuilder sb = aData.getSrc();
		final StringBuilder source = new StringBuilder();
		final StringBuilder initComp = aData.getInitComp();
		final String typeGeneratedName = type.getGenNameTemplate( aData, source );

		if (type.getTypetype().equals(Type_type.TYPE_ARRAY)) {
			final Array_Type arrayType =  (Array_Type) type;
			final StringBuilder sbforTemp = aData.getCodeForType(arrayType.getGenNameOwn());
			arrayType.generateCodeValue(aData, sbforTemp);
			arrayType.generateCodeTemplate(aData, sbforTemp);
		}

		if (getMyScope() instanceof ComponentTypeBody) {
			source.append(MessageFormat.format("public static final ThreadLocal<{0}> {1} = new ThreadLocal<{0}>() '{'\n", typeGeneratedName, genName));
			source.append("@Override\n" );
			source.append(MessageFormat.format("protected {0} initialValue() '{'\n", typeGeneratedName));
			source.append(MessageFormat.format("return new {0}();\n", typeGeneratedName));
			source.append("}\n");
			source.append("};\n");
			sb.append(source);
			if ( initialValue != null ) {
				final IType initialGovernor = initialValue.getMyGovernor();
				if (initialGovernor.getTypetype() == Type_type.TYPE_SEQUENCE_OF || initialGovernor.getTypetype() == Type_type.TYPE_ARRAY) {
					initComp.append(MessageFormat.format("{0}.get().remove_all_permutations();\n", genName));
				}

				initialValue.generateCodeInit( aData, initComp, genName + ".get()" );
				if (templateRestriction != Restriction_type.TR_NONE && generateRestrictionCheck) {
					TemplateRestriction.generateRestrictionCheckCode(aData, initComp, location, genName + ".get()", templateRestriction);
				}
			} else if (cleanUp) {
				initComp.append(MessageFormat.format("{0}.get().clean_up();\n", genName));
			}
		} else {
			source.append(MessageFormat.format(" public static final {0} {1} = new {0}();\n", typeGeneratedName, genName));
			sb.append(source);

			if ( initialValue != null ) {
				initialValue.generateCodeInit( aData, initComp, genName );
				if (templateRestriction != Restriction_type.TR_NONE && generateRestrictionCheck) {
					TemplateRestriction.generateRestrictionCheckCode(aData, initComp, location, genName, templateRestriction);
				}
			} else if (cleanUp) {
				initComp.append(MessageFormat.format("{0}.clean_up();\n", genName));
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeString(final JavaGenData aData, final StringBuilder source) {
		final String genName = getGenName();

		// FIXME temporal code until generate_code_object is supported for templates
		final String typeGeneratedName = type.getGenNameTemplate( aData, source );
		if (type.getTypetype().equals(Type_type.TYPE_ARRAY)) {
			final Array_Type arrayType = (Array_Type) type;
			final StringBuilder sb = aData.getCodeForType(arrayType.getGenNameOwn());
			arrayType.generateCodeValue(aData, sb);
			arrayType.generateCodeTemplate(aData, sb);
		}

		if (initialValue != null && initialValue.hasSingleExpression()) {
			source.append(MessageFormat.format("final {0} {1} = new {0}({2});\n", typeGeneratedName, genName, initialValue.getSingleExpression(aData, false)));
		} else {
			source.append(MessageFormat.format("final {0} {1} = new {0}();\n", typeGeneratedName, genName));
			if ( initialValue != null ) {
				initialValue.generateCodeInit( aData, source, genName );
				if (templateRestriction != Restriction_type.TR_NONE && generateRestrictionCheck) {
					TemplateRestriction.generateRestrictionCheckCode(aData, source, location, genName, templateRestriction);
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInitComp(final JavaGenData aData, final StringBuilder initComp, final Definition definition) {
		if (initialValue != null) {
			initialValue.generateCodeInit(aData, initComp, definition.getGenNameFromScope(aData, initComp, ""));
			if (templateRestriction != Restriction_type.TR_NONE && generateRestrictionCheck) {
				TemplateRestriction.generateRestrictionCheckCode(aData, initComp, location, genName, templateRestriction);
			}
		}
	}
}
