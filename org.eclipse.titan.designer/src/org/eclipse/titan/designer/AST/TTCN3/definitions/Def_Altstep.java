/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NamedBridgeScope;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuards;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.T3Doc;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.editors.ttcn3editor.TTCN3CodeSkeletons;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * The Def_Altstep class represents TTCN3 altstep definitions.
 *
 * @author Kristof Szabados
 * */
public final class Def_Altstep extends Definition implements IParameterisedAssignment {
	private static final String FULLNAMEPART1 = ".<formal_parameter_list>";
	private static final String FULLNAMEPART2 = ".<runs_on_type>";
	private static final String FULLNAMEPART3 = ".<block>";
	private static final String FULLNAMEPART4 = ".<guards>";

	private static final String DASHALLOWEDONLYFORTEMPLATES = "Using not used symbol (`-') as the default parameter"
			+ " is allowed only for modified templates";

	private static final String KIND = "altstep";

	public static String getKind() {
		return KIND;
	}

	private final FormalParameterList formalParList;
	private final Reference runsOnRef;
	private Component_Type runsOnType = null;
	private final StatementBlock block;
	private final AltGuards altGuards;
	private NamedBridgeScope bridgeScope = null;

	public Def_Altstep(final Identifier identifier, final FormalParameterList formalParameters, final Reference runsOnRef,
			final StatementBlock block, final AltGuards altGuards) {
		super(identifier);
		this.formalParList = formalParameters;
		this.runsOnRef = runsOnRef;
		this.block = block;
		this.altGuards = altGuards;

		if (formalParList != null) {
			formalParList.setMyDefinition(this);
			formalParList.setFullNameParent(this);
		}
		if (runsOnRef != null) {
			runsOnRef.setFullNameParent(this);
		}
		if (block != null) {
			block.setMyDefinition(this);
		}
		if (altGuards != null) {
			altGuards.setMyDefinition(this);
		}
	}

	public AltGuards getAltGuards() {
		return altGuards;
	}

	@Override
	/** {@inheritDoc} */
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_ALTSTEP;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (formalParList == child) {
			return builder.append(FULLNAMEPART1);
		} else if (runsOnRef == child) {
			return builder.append(FULLNAMEPART2);
		} else if (block == child) {
			return builder.append(FULLNAMEPART3);
		} else if (altGuards == child) {
			return builder.append(FULLNAMEPART4);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public FormalParameterList getFormalParameterList() {
		return formalParList;
	}

	@Override
	/** {@inheritDoc} */
	public String getProposalKind() {
		return KIND;
	}

	@Override
	/** {@inheritDoc} */
	public String getAssignmentName() {
		return "altstep";
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "altstep.gif";
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
	public void setMyScope(final Scope scope) {
		if (bridgeScope != null && bridgeScope.getParentScope() == scope) {
			return;
		}

		bridgeScope = new NamedBridgeScope();
		bridgeScope.setParentScope(scope);
		scope.addSubScope(getLocation(), bridgeScope);
		bridgeScope.setScopeMacroName(identifier.getDisplayName());

		super.setMyScope(bridgeScope);
		if (runsOnRef != null) {
			runsOnRef.setMyScope(bridgeScope);
		}
		formalParList.setMyScope(bridgeScope);
		if (block != null) {
			block.setMyScope(formalParList);
			altGuards.setMyScope(block);
			bridgeScope.addSubScope(block.getLocation(), block);
		}

		bridgeScope.addSubScope(formalParList.getLocation(), formalParList);
		if (altGuards != null) {
			for (int i = 0; i < altGuards.getNofAltguards(); i++) {
				final AltGuard ag = altGuards.getAltguardByIndex(i);
				final StatementBlock sb = ag.getStatementBlock();
				if (sb != null) {
					bridgeScope.addSubScope(sb.getLocation(), sb);
				}
			}
		}
	}

	public Component_Type getRunsOnType(final CompilationTimeStamp timestamp) {
		check(timestamp);

		return runsOnType;
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
		runsOnType = null;
		lastTimeChecked = timestamp;

		T3Doc.check(this.getCommentLocation(), KIND);

		if (runsOnRef != null) {
			runsOnType = runsOnRef.chkComponentypeReference(timestamp);
			if (runsOnType != null) {
				final Scope formalParlistPreviosScope = formalParList.getParentScope();
				if (formalParlistPreviosScope instanceof RunsOnScope
						&& ((RunsOnScope) formalParlistPreviosScope).getParentScope() == myScope) {
					((RunsOnScope) formalParlistPreviosScope).setComponentType(runsOnType);
				} else {
					final Scope tempScope = new RunsOnScope(runsOnType, myScope);
					formalParList.setMyScope(tempScope);
				}
			}
		}

		if (formalParList.hasNotusedDefaultValue()) {
			formalParList.getLocation().reportSemanticError(DASHALLOWEDONLYFORTEMPLATES);
		}

		boolean canSkip = false;
		if (myScope != null) {
			final Module module = myScope.getModuleScope();
			if (module != null) {
				if (module.getSkippedFromSemanticChecking()) {
					canSkip = true;
				}
			}
		}

		if(!canSkip) {
			formalParList.reset();
		}
		formalParList.check(timestamp, getAssignmentType());

		if(canSkip) {
			return;
		}


		NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_ALTSTEP, identifier, this);
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (block != null) {
			block.check(timestamp);
			block.setCodeSection(CodeSectionType.CS_INLINE);
		}

		if (altGuards != null) {
			altGuards.setIsAltstep();
			altGuards.setMyAltguards(altGuards);
			altGuards.setMyLaicStmt(altGuards, null);
			altGuards.check(timestamp);
			altGuards.setCodeSection(CodeSectionType.CS_INLINE);
		}

		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
		}

		if (block != null) {
			block.postCheck();
		}
		if (altGuards != null) {
			altGuards.postCheck();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void postCheck() {
		if (myScope != null) {
			final Module module = myScope.getModuleScope();
			if (module != null) {
				if (module.getSkippedFromSemanticChecking()) {
					return;
				}
			}
		}

		super.postCheck();
		postCheckPrivateness();

		formalParList.postCheck();
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (Subreference_type.parameterisedSubReference.equals(subrefs.get(i).getReferenceType())
				&& identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match, but the chain of references ends here,
			// as altsteps can not return with a type
			return;
		} else if (identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			// prefix
			if (subrefs.size() == i + 1) {
				final StringBuilder patternBuilder = new StringBuilder(identifier.getDisplayName());
				patternBuilder.append('(');
				formalParList.getAsProposalPart(patternBuilder);
				patternBuilder.append(')');
				propCollector.addTemplateProposal(identifier.getDisplayName(), new Template(getProposalDescription(), "",
						propCollector.getContextIdentifier(), patternBuilder.toString(), false),
						TTCN3CodeSkeletons.SKELETON_IMAGE);
				super.addProposal(propCollector, i);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || Subreference_type.arraySubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (identifier.getName().equals(subrefs.get(i).getId().getName()) && subrefs.size() == i + 1) {
			declarationCollector.addDeclaration(this);
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
					removeBridge();
					throw new ReParseException(result);
				}
			}

			if (formalParList != null) {
				if (enveloped) {
					formalParList.updateSyntax(reparser, false);
					reparser.updateLocation(formalParList.getLocation());
				} else if (reparser.envelopsDamage(formalParList.getLocation())) {
					try {
						formalParList.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(formalParList.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (runsOnRef != null) {
				if (enveloped) {
					runsOnRef.updateSyntax(reparser, false);
					reparser.updateLocation(runsOnRef.getLocation());
				} else if (reparser.envelopsDamage(runsOnRef.getLocation())) {
					try {
						runsOnRef.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(runsOnRef.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (altGuards != null) {
				if (enveloped) {
					altGuards.updateSyntax(reparser, false);
					reparser.updateLocation(altGuards.getLocation());
				} else if (reparser.envelopsDamage(altGuards.getLocation())) {
					try {
						altGuards.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(altGuards.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (block != null) {
				if (enveloped) {
					block.updateSyntax(reparser, false);
					reparser.updateLocation(block.getLocation());
				} else if (reparser.envelopsDamage(block.getLocation())) {
					try {
						block.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(block.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (withAttributesPath != null) {
				if (enveloped) {
					withAttributesPath.updateSyntax(reparser, false);
					reparser.updateLocation(withAttributesPath.getLocation());
				} else if (reparser.envelopsDamage(withAttributesPath.getLocation())) {
					try {
						withAttributesPath.updateSyntax(reparser, true);
						enveloped = true;
						reparser.updateLocation(withAttributesPath.getLocation());
					} catch (ReParseException e) {
						removeBridge();
						throw e;
					}
				}
			}

			if (!enveloped) {
				removeBridge();
				throw new ReParseException();
			}

			return;
		}

		reparser.updateLocation(identifier.getLocation());

		if (formalParList != null) {
			formalParList.updateSyntax(reparser, false);
			reparser.updateLocation(formalParList.getLocation());
		}

		if (runsOnRef != null) {
			runsOnRef.updateSyntax(reparser, false);
			reparser.updateLocation(runsOnRef.getLocation());
		}

		if (block != null) {
			block.updateSyntax(reparser, false);
			reparser.updateLocation(block.getLocation());
		}

		if (altGuards != null) {
			altGuards.updateSyntax(reparser, false);
			reparser.updateLocation(altGuards.getLocation());
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	/**
	 * Removes the name bridging scope.
	 * */
	private void removeBridge() {
		if (bridgeScope != null) {
			bridgeScope.remove();
			bridgeScope = null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (formalParList != null) {
			formalParList.findReferences(referenceFinder, foundIdentifiers);
		}
		if (runsOnRef != null) {
			runsOnRef.findReferences(referenceFinder, foundIdentifiers);
		}
		if (block != null) {
			block.findReferences(referenceFinder, foundIdentifiers);
		}
		if (altGuards != null) {
			altGuards.findReferences(referenceFinder, foundIdentifiers);
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
		if (runsOnRef != null && !runsOnRef.accept(v)) {
			return false;
		}
		if (block != null && !block.accept(v)) {
			return false;
		}
		if (altGuards != null && !altGuards.accept(v)) {
			return false;
		}
		return true;
	}

	public int nofBranches() {
		return altGuards.getNofAltguards();
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final boolean cleanUp) {
		final StringBuilder source = aData.getSrc();

		final String genName = getGenName();
		formalParList.setGenName(genName);

		//FIXME handle location object

		final StringBuilder body = new StringBuilder();
		block.generateCode(aData, body);
		altGuards.generateCodeAltstep(aData, body);

		final StringBuilder formalParListCode = new StringBuilder();
		formalParList.generateCode(aData, formalParListCode);
		// FIXME generate code defval and shadow objects

		aData.addBuiltinTypeImport("TitanAlt_Status");
		aData.addBuiltinTypeImport("TTCN_Default");
		aData.addCommonLibraryImport("TTCN_Snapshot");

		final StringBuilder actualParameterList = formalParList.generateCodeActualParlist("");
		final StringBuilder fullParamaterList = formalParList.generateCode(aData);

		source.append(MessageFormat.format("private static final TitanAlt_Status {0}_instance({1})\n", genName, fullParamaterList));
		source.append("{\n");
		source.append(body);
		source.append("}\n\n");



		if(VisibilityModifier.Private.equals(getVisibilityModifier())) {
			source.append( "private" );
		} else {
			source.append( "public" );
		}
		source.append(MessageFormat.format(" static final void {0}({1})\n", genName, fullParamaterList));
		source.append("{\n");
		source.append("altstep_begin: for( ; ; ) {\n");
		source.append("boolean block_flag = false;\n");
		source.append("TitanAlt_Status altstep_flag = TitanAlt_Status.ALT_UNCHECKED;\n");
		source.append("TitanAlt_Status default_flag = TitanAlt_Status.ALT_UNCHECKED;\n");
		source.append("for( ; ; ) {\n");
		source.append("TTCN_Snapshot.takeNew(block_flag);\n");
		source.append("if (altstep_flag != TitanAlt_Status.ALT_NO) {\n");
		source.append(MessageFormat.format("altstep_flag = {0}_instance({1});\n", genName, actualParameterList));
		source.append("if (altstep_flag == TitanAlt_Status.ALT_YES || altstep_flag == TitanAlt_Status.ALT_BREAK) {\n");
		source.append("return;\n");
		source.append("} else if (altstep_flag == TitanAlt_Status.ALT_REPEAT) {\n");
		source.append("continue altstep_begin;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("if (default_flag != TitanAlt_Status.ALT_NO) {\n");
		source.append("default_flag = TTCN_Default.tryAltsteps();\n");
		source.append("if (default_flag == TitanAlt_Status.ALT_YES || default_flag == TitanAlt_Status.ALT_BREAK) {\n");
		source.append("return;\n");
		source.append("} else if (default_flag == TitanAlt_Status.ALT_REPEAT) {\n");
		source.append("continue altstep_begin;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("if (altstep_flag == TitanAlt_Status.ALT_NO && default_flag == TitanAlt_Status.ALT_NO) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"None of the branches can be chosen in altstep {0}\");\n", identifier.getDisplayName()));
		source.append("} else {\n");
		source.append("block_flag = true;\n");
		source.append("}\n");
		source.append("}\n");

		source.append("}\n");
		source.append("}\n\n");

		// class for keeping the altstep in the default context
		// the class is for internal use
		aData.addBuiltinTypeImport("Default_Base");
		source.append(MessageFormat.format("static final class {0}_Default extends Default_Base '{'\n", genName));
		for (int i = 0 ; i < formalParList.getNofParameters(); i++ ) {
			final FormalParameter formalParameter = formalParList.getParameterByIndex(i);
			source.append("private ");
			formalParameter.generateCodeObject(aData, source, "par_");
		}
		source.append(MessageFormat.format("public {0}_Default({1}) '{'\n", genName, fullParamaterList));
		source.append(MessageFormat.format("super(\"{0}\");\n", identifier.getDisplayName()));
		for (int i = 0 ; i < formalParList.getNofParameters(); i++ ) {
			final FormalParameter formalParameter = formalParList.getParameterByIndex(i);
			final String FormalParName = formalParameter.getIdentifier().getName();
			if (formalParameter.getAssignmentType() == Assignment_type.A_PAR_TIMER) {
				source.append(MessageFormat.format("par_{0} = {0};\n", FormalParName));
			} else {
				source.append(MessageFormat.format("par_{0}.assign({0});\n", FormalParName));
			}
		}
		source.append("}\n\n");

		final StringBuilder prefixedActualParameterList = formalParList.generateCodeActualParlist("par_");
		source.append("@Override\n");
		source.append("public final TitanAlt_Status call_altstep() {\n");
		source.append(MessageFormat.format("return {0}_instance({1});\n", genName, prefixedActualParameterList));
		source.append("}\n\n");

		source.append("}\n\n");//closing for the _Default class

		source.append(MessageFormat.format("private static final Default_Base activate_{0}({1}) '{'\n", genName, fullParamaterList));
		source.append(MessageFormat.format("return new {0}_Default({1});\n", genName, actualParameterList));
		source.append("}\n\n");
	}
}
