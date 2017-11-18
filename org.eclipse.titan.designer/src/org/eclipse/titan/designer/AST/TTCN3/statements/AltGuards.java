/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.designer.GeneralConstants;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard.altguard_type;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock.ReturnStatus_type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * The AltGuards class represents the list of branches in a TTCN3
 * altstep/alt/interleave statement.
 *
 * @see AltGuard
 * @see Alt_Statement
 * @see Interleave_Statement
 * @see Def_Altstep
 *
 * @author Kristof Szabados
 * */
public final class AltGuards extends ASTNode implements IIncrementallyUpdateable {
	private static final String SHADOWEDBYELSE = "Control never reaches this branch of alternative because of a previous [else] branch";

	private static final String FULLNAMEPART = ".alt_guard_";
	private final ArrayList<AltGuard> altGuards;

	private boolean hasRepeat = false;
	private String label;
	private boolean isAltstep = false;

	/**
	 * The location of the whole assignment. This location encloses the
	 * assignment fully, as it is used to report errors to.
	 **/
	private Location location;

	public AltGuards() {
		altGuards = new ArrayList<AltGuard>();
		location = NULL_Location.INSTANCE;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (int i = 0, size = altGuards.size(); i < size; i++) {
			if (altGuards.get(i) == child) {
				return builder.append(FULLNAMEPART).append(String.valueOf(i + 1));
			}
		}

		return builder;
	}

	public void addAltGuard(final AltGuard altGuard) {
		if (altGuard != null) {
			altGuards.add(altGuard);
			altGuard.setFullNameParent(this);
		}
	}

	/**
	 * @return the number of altguards.
	 * */
	public int getNofAltguards() {
		return altGuards.size();
	}

	/**
	 * Returns the altguard at the specified position in this altguard list.
	 *
	 * @param i
	 *                the index of the altguard to return.
	 *
	 * @return the altguard at the index.
	 * */
	public AltGuard getAltguardByIndex(final int i) {
		return altGuards.get(i);
	}

	/**
	 * Sets the scope of the guard statement.
	 *
	 * @param scope
	 *                the scope to be set.
	 * */
	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		altGuards.trimToSize();
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).setMyScope(scope);
		}
	}

	/**
	 * Sets the code_section attribute for the statement to the provided value.
	 *
	 * @param codeSection the code section where this statement should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).setCodeSection(codeSection);
		}
	}

	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).setMyStatementBlock(statementBlock, index);
		}
	}

	public void setMyDefinition(final Definition definition) {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).setMyDefinition(definition);
		}
	}

	public void setMyAltguards(final AltGuards altGuards) {
		for (int i = 0, size = this.altGuards.size(); i < size; i++) {
			this.altGuards.get(i).setMyAltguards(altGuards);
		}
	}

	public void repeatFound() {
		hasRepeat = true;
	}

	public String getLabel() {
		return label;
	}

	public void setIsAltstep() {
		isAltstep = true;
	}

	public boolean getIsAltstep() {
		return isAltstep;
	}

	/**
	 * Checks whether there is an else branch among the altguards.
	 *
	 * @return true if there is an else branch, false otherwise.
	 * */
	public boolean hasElse() {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			final AltGuard guard = altGuards.get(i);
			if (guard.getType() == altguard_type.AG_ELSE) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether the altguards have a return statement, either directly
	 * or embedded.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 *
	 * @return the return status of the altguards.
	 * */
	public StatementBlock.ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		StatementBlock.ReturnStatus_type result = StatementBlock.ReturnStatus_type.RS_MAYBE;

		for (int i = 0, size = altGuards.size(); i < size; i++) {
			final AltGuard guard = altGuards.get(i);
			switch (guard.hasReturn(timestamp)) {
			case RS_NO:
				if (result == StatementBlock.ReturnStatus_type.RS_YES) {
					return StatementBlock.ReturnStatus_type.RS_MAYBE;
				}

				result = StatementBlock.ReturnStatus_type.RS_NO;
				break;
			case RS_YES:
				if (result == StatementBlock.ReturnStatus_type.RS_NO) {
					return StatementBlock.ReturnStatus_type.RS_MAYBE;
				}

				result = StatementBlock.ReturnStatus_type.RS_YES;
				break;
			default:
				return StatementBlock.ReturnStatus_type.RS_MAYBE;
			}

			if (guard instanceof Else_Altguard) {
				break;
			}
		}

		return result;
	}

	/**
	 * Used when generating code for interleaved statement.
	 * If the block has no receiving statements, then the general code generation can be used
	 *  (which may use blocks).

	 * */
	public boolean hasReceivingStatement() {
		for (int i = 0; i < altGuards.size(); i++) {
			if (altGuards.get(i).getStatementBlock().hasReceivingStatement(0)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Does the semantic checking of the alt guard list.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		boolean unreachableFound = false;
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			final AltGuard guard = altGuards.get(i);
			guard.check(timestamp);

			if (unreachableFound) {
				guard.getLocation().reportConfigurableSemanticProblem(
						Platform.getPreferencesService().getString(ProductConstants.PRODUCT_ID_DESIGNER,
								PreferenceConstants.REPORTUNNECESSARYCONTROLS, GeneralConstants.WARNING, null),
								SHADOWEDBYELSE);
			}
			if (altguard_type.AG_ELSE.equals(guard.getType())) {
				unreachableFound = true;
			}
		}
	}

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).checkAllowedInterleave();
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 */
	public void postCheck() {
		for (int i = 0, size = altGuards.size(); i < size; i++) {
			altGuards.get(i).postCheck();
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			boolean enveloped = false;
			int nofDamaged = 0;
			int leftBoundary = location.getOffset();
			int rightBoundary = location.getEndOffset();
			final int damageOffset = reparser.getDamageStart();

			for (int i = 0, size = altGuards.size(); i < size && !enveloped; i++) {
				final AltGuard altGuard = altGuards.get(i);
				final Location temporalLocation = altGuard.getLocation();

				if (reparser.envelopsDamage(temporalLocation)) {
					enveloped = true;
					leftBoundary = temporalLocation.getOffset();
					rightBoundary = temporalLocation.getEndOffset();
				} else if (reparser.isDamaged(temporalLocation)) {
					nofDamaged++;
				} else {
					if (temporalLocation.getEndOffset() < damageOffset && temporalLocation.getEndOffset() > leftBoundary) {
						leftBoundary = temporalLocation.getEndOffset();
					}
					if (temporalLocation.getOffset() >= damageOffset && temporalLocation.getOffset() < rightBoundary) {
						rightBoundary = temporalLocation.getOffset();
					}
				}
			}

			if (nofDamaged != 0) {
				throw new ReParseException();
			}

			for (Iterator<AltGuard> iterator = altGuards.iterator(); iterator.hasNext();) {
				final AltGuard altGuard = iterator.next();
				final Location temporalLocation = altGuard.getLocation();

				if (reparser.isAffectedAppended(temporalLocation)) {
					altGuard.updateSyntax(reparser, enveloped && reparser.envelopsDamage(temporalLocation));
					reparser.updateLocation(altGuard.getLocation());
				}
			}

			return;
		}

		for (int i = 0, size = altGuards.size(); i < size; i++) {
			final AltGuard guard = altGuards.get(i);

			guard.updateSyntax(reparser, false);
			reparser.updateLocation(guard.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (altGuards == null) {
			return;
		}

		for (AltGuard ag : altGuards) {
			ag.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (altGuards != null) {
			for (AltGuard ag : altGuards) {
				if (!ag.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Generate code for an alt
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source code generated
	 */
	public void generateCodeAlt( final JavaGenData aData, final StringBuilder source ) {
		aData.addBuiltinTypeImport("TitanAlt_Status");

		boolean labelNeeded = hasRepeat;
		boolean hasElseBranch = false;

		for (int i = 0; i < altGuards.size(); i++) {
			final AltGuard altGuard = altGuards.get(i);
			switch (altGuard.getType()) {
			case AG_OP:
				if (((Operation_Altguard)altGuard).getGuardStatement().canRepeat()) {
					labelNeeded = true;
				}
				break;
			case AG_REF:
			case AG_INVOKE:
				labelNeeded = true;
				break;
			case AG_ELSE:
				hasElseBranch = true;
				break;
			default:
				//Otherwise fatal error
				source.append("FATAL ERROR: unknown altguard type encountered: " + altGuard.getClass().getSimpleName() + "\n");
				return;
			}

			if (hasElseBranch) {
				break;
			}
		}

		// if there is no [else] branch the defaults may return ALT_REPEAT
		if (!hasElseBranch) {
			labelNeeded = true;
		}

		// opening bracket of the statement block
		label = aData.getTemporaryVariableName();
		if (labelNeeded) {
			source.append(label).append(":\n");
		}
		source.append("for ( ; ; ) {\n");

		// temporary variables used for caching of status codes
		for (int i = 0; i < altGuards.size(); i++) {
			final AltGuard altGuard = altGuards.get(i);
			if (altGuard.getType().equals(altguard_type.AG_ELSE)) {
				break;
			}

			source.append(MessageFormat.format("TitanAlt_Status {0}_alt_flag_{1} = ", label, i));
			if(altGuard.getGuardExpression() == null) {
				source.append("TitanAlt_Status.ALT_MAYBE");
			} else {
				source.append("TitanAlt_Status.ALT_UNCHECKED");
			}
			source.append(";\n");
		}
		if (!hasElseBranch) {
			source.append(MessageFormat.format("TitanAlt_Status {0}_default_flag = TitanAlt_Status.ALT_MAYBE;\n", label));
		}

		// the first snapshot is taken in non-blocking mode
		aData.addCommonLibraryImport("TTCN_Snapshot");
		source.append("TTCN_Snapshot.takeNew(false);\n");
		// and opening infinite for() loop
		source.append("for ( ; ; ) {\n");

		for (int i = 0; i < altGuards.size(); i++) {
			final AltGuard altGuard = altGuards.get(i);
			final altguard_type altGuardType = altGuard.getType();
			if (altGuardType.equals(altguard_type.AG_ELSE)) {
				source.append("TTCN_Snapshot.elseBranchReached();\n");
				StatementBlock block = altGuard.getStatementBlock();
				if (block.getSize() > 0) {
					source.append("{\n");
					//FIXME handle debugger
					block.generateCode(aData, source);
					source.append("}\n");
				}
				if (block.hasReturn(CompilationTimeStamp.getBaseTimestamp()) != ReturnStatus_type.RS_YES) {
					source.append("break;\n");
				} 
				// do not generate code for further branches
				break;
			} else {
				final IValue guardExpression = altGuard.getGuardExpression();
				if (guardExpression != null) {
					source.append(MessageFormat.format("if ({0}_alt_flag_{1} == TitanAlt_Status.ALT_UNCHECKED) '{'\n", label, i));
					final ExpressionStruct expression = new ExpressionStruct();
					guardExpression.generateCodeExpression(aData, expression, true);
					source.append(expression.preamble);
					source.append(MessageFormat.format("if(TitanBoolean.getNative({0})) '{'\n", expression.expression));
					source.append(MessageFormat.format("{0}_alt_flag_{1} = TitanAlt_Status.ALT_MAYBE;\n", label, i));
					source.append("} else {\n");
					source.append(MessageFormat.format("{0}_alt_flag_{1} = TitanAlt_Status.ALT_NO;\n", label, i));
					source.append("}\n");
					source.append("}\n");
				}

				source.append(MessageFormat.format("if ({0}_alt_flag_{1} == TitanAlt_Status.ALT_MAYBE) '{'\n", label, i));
				boolean canRepeat = false;
				final ExpressionStruct expression = new ExpressionStruct();
				expression.expression.append(MessageFormat.format("{0}_alt_flag_{1} = ", label, i));
				switch(altGuardType) {
				case AG_OP: {
					// the guard operation is a receiving statement
					final Statement statement = ((Operation_Altguard)altGuard).getGuardStatement();
					//TODO update location
					statement.generateCodeExpression(aData, expression);
					canRepeat = statement.canRepeat();
					}
					break;
				case AG_REF: {
					// the guard operation is an altstep instance
					final Reference reference = ((Referenced_Altguard)altGuard).getGuardReference();
					//TODO update location
					final Assignment altstep = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
					expression.expression.append(MessageFormat.format("{0}_instance(", altstep.getGenNameFromScope(aData, source, myScope, "")));
					final ISubReference subreference = reference.getSubreferences().get(0);
					((ParameterisedSubReference) subreference).getActualParameters().generateCodeAlias(aData, expression);
					source.append(')');
					canRepeat = true;
					}
					break;
				case AG_INVOKE: {
					// the guard operation is an altstep invocation
					//TODO update location
					((Invoke_Altguard)altGuard).generateCodeInvokeInstance(aData, expression);
					canRepeat = true;
					}
					break;
				default:
					source.append("FATAL ERROR: unknown altguard type encountered: " + altGuard.getClass().getSimpleName() + "\n");
					return;
				}
				expression.mergeExpression(source);
				if(canRepeat) {
					source.append(MessageFormat.format("if ({0}_alt_flag_{1} == TitanAlt_Status.ALT_REPEAT) continue {2};\n", label, i, label));
				}

				if(altGuardType.equals(altguard_type.AG_REF) || altGuardType.equals(altguard_type.AG_INVOKE)) {
					source.append(MessageFormat.format("if ({0}_alt_flag_{1} == TitanAlt_Status.ALT_BREAK) break;\n", label, i));
				}

				// execution of statement block if the guard was successful
				source.append(MessageFormat.format("if ({0}_alt_flag_{1} == TitanAlt_Status.ALT_YES) ", label, i));
				final StatementBlock block = altGuard.getStatementBlock();
				if (block != null && block.getSize() > 0) {
					source.append("{\n");
					//TODO handle debugger
					block.generateCode(aData, source);
					if (!ReturnStatus_type.RS_YES.equals(block.hasReturn(CompilationTimeStamp.getBaseTimestamp()))) {
						source.append("break;\n");
					}
					source.append("}\n");
				} else {
					source.append("break;\n");
				}
				source.append("}\n");
			}
		}

		if( !hasElseBranch) {
			aData.addCommonLibraryImport("TTCN_Default");
			source.append(MessageFormat.format("if ({0}_default_flag == TitanAlt_Status.ALT_MAYBE) '{'\n", label));
			source.append(MessageFormat.format("{0}_default_flag = TTCN_Default.tryAltsteps();\n", label));
			source.append(MessageFormat.format("if ({0}_default_flag == TitanAlt_Status.ALT_YES || {0}_default_flag == TitanAlt_Status.ALT_BREAK) '{'\n", label));
			source.append("break;\n");
			source.append(MessageFormat.format("} else if({0}_default_flag == TitanAlt_Status.ALT_REPEAT) '{'\n", label));
			source.append(MessageFormat.format("continue {0};\n", label));
			source.append("}\n");
			source.append("}\n");
			//TODO location update
			// error handling and taking the next snapshot in blocking mode
			source.append("if ( ");
			for (int i = 0; i < altGuards.size(); i++) {
				source.append(MessageFormat.format("{0}_alt_flag_{1} == TitanAlt_Status.ALT_NO &&", label, i));
			}
			source.append(MessageFormat.format("{0}_default_flag == TitanAlt_Status.ALT_NO) '{'\n", label));
			source.append("throw new TtcnError(\"None of the branches can be chosen in the alt statement");
			//TODO translate_string
			if(location != null && location.getFile() != null) {
				source.append(MessageFormat.format("in file {0} at line {1}", location.getFile().getName(), location.getLine()));
			}
			source.append("\");\n");
			source.append("}\n");

			source.append("TTCN_Snapshot.takeNew(true);\n");
		}

		source.append("}\n");
		source.append("break;\n");
		source.append("}\n");
	}

	/**
	 * Generate code for an altstep
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source code generated
	 */
	public void generateCodeAltstep(final JavaGenData aData, final StringBuilder source ) {
		final boolean hasElse = hasElse();
		if (!hasElse) {
			source.append("TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;\n");
		}

		for (int i = 0; i < altGuards.size(); i++) {
			final AltGuard altGuard = altGuards.get(i);
			final altguard_type altGuardType = altGuard.getType();
			if (altGuardType.equals(altguard_type.AG_ELSE)) {
				source.append("TTCN_Snapshot.elseBranchReached();\n");
				final StatementBlock block = altGuard.getStatementBlock();
				if (block.getSize() > 0) {
					source.append("{\n");
					//TODO debugger
					block.generateCode(aData, source);
					source.append("}\n");
				}
				if (block.hasReturn(CompilationTimeStamp.getBaseTimestamp()) != ReturnStatus_type.RS_YES) {
					source.append("return TitanAlt_Status.ALT_YES;\n");
				}
				break;
			} else {
				final AtomicInteger blockCount = new AtomicInteger(0);
				final IValue guardExpression = altGuard.getGuardExpression();
				if (guardExpression != null) {
					guardExpression.generateCodeTmp(aData, source, "if (", blockCount);
					source.append(") {\n");
					blockCount.incrementAndGet();
				}

				boolean canRepeat = false;
				final ExpressionStruct expression = new ExpressionStruct();
				switch(altGuardType) {
				case AG_OP: {
					final Statement statement = ((Operation_Altguard)altGuard).getGuardStatement();
					//TODO update location
					statement.generateCodeExpression(aData, expression);
					canRepeat = statement.canRepeat();
					}
					break;
				case AG_REF: {
					// the guard operation is an altstep instance
					final Reference reference = ((Referenced_Altguard)altGuard).getGuardReference();
					//TODO update location
					final Assignment altstep = reference.getRefdAssignment(CompilationTimeStamp.getBaseTimestamp(), false);
					expression.expression.append(MessageFormat.format("{0}_instance(", altstep.getGenNameFromScope(aData, source, myScope, "")));
					final ISubReference subreference = reference.getSubreferences().get(0);
					((ParameterisedSubReference) subreference).getActualParameters().generateCodeAlias(aData, expression);
					source.append(')');
					canRepeat = true;
					}
					break;
				case AG_INVOKE: {
					// the guard operation is an altstep invocation
					//TODO update location
					((Invoke_Altguard)altGuard).generateCodeInvokeInstance(aData, expression);
					canRepeat = true;
					}
					break;
				default:
					source.append("FATAL ERROR: unknown altguard type encountered: " + altGuard.getClass().getSimpleName() + "\n");
					return;
				}
				if (expression.preamble.length() > 0 || expression.postamble.length() > 0) {
					if (blockCount.get() == 0) {
						source.append("{\n");
						blockCount.set(blockCount.get() + 1);
					}

					final String tempId = aData.getTemporaryVariableName();
					source.append(MessageFormat.format("TitanAlt_Status {0};\n", tempId));
					source.append("{\n");
					source.append(expression.preamble);
					source.append(MessageFormat.format("{0}.assign({1});\n", tempId, expression.expression));
					source.append(expression.postamble);
					source.append("}\n");
					source.append(MessageFormat.format("switch ({0}) '{'\n", tempId));
				} else {
					source.append(MessageFormat.format("switch ({0}) '{'\n", expression.expression));
				}

				source.append("case ALT_YES:\n");
				final StatementBlock block = altGuard.getStatementBlock();
				if (block != null && block.getSize() > 0) {
					source.append("{\n");
					//TODO handle debugger
					block.generateCode(aData, source);
					source.append("}\n");
				}
				if (block == null || block.hasReturn(CompilationTimeStamp.getBaseTimestamp()) != ReturnStatus_type.RS_YES) {
					source.append("return TitanAlt_Status.ALT_YES;\n");
				}
				if (canRepeat) {
					source.append("case ALT_REPEAT:\n");
					source.append("return TitanAlt_Status.ALT_REPEAT;\n");
				}
				if (altGuardType == altguard_type.AG_REF || altGuardType == altguard_type.AG_INVOKE) {
					source.append("case ALT_BREAK:\n");
					source.append("return TitanAlt_Status.ALT_BREAK;\n");
				}
				if (!hasElse) {
					source.append("case ALT_MAYBE:\n");
					source.append("returnValue = TitanAlt_Status.ALT_MAYBE;\n");
				}
				source.append("default:\n");
				source.append("break;\n");
				source.append("}\n");

				// closing statement blocks
				for(int j = 0 ; j < blockCount.get(); j++) {
					source.append("}\n");
				}
			}
		}

		if (!hasElse) {
			source.append("return returnValue;\n");
		}
	}

	/**
	 * Generate code for an altstep (as the body of a call statement)
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source code generated
	 * @param tempId temporary id used as prefix of local variables
	 * @param inInterleave is it used in interleave?
	 */
	public void generateCodeCallBody(final JavaGenData aData, final StringBuilder source, final String tempId, final boolean inInterleave) {
		if (hasRepeat) {
			source.append(MessageFormat.format("{0}:", tempId));
		}
		// temporary variables used for caching of status codes
		for (int i = 0; i < altGuards.size(); i++) {
			final AltGuard altGuard = altGuards.get(i);

			source.append(MessageFormat.format("TitanAlt_Status {0}_alt_flag_{1} = ", tempId, i));
			if(altGuard.getGuardExpression() == null) {
				source.append("TitanAlt_Status.ALT_MAYBE");
			} else {
				source.append("TitanAlt_Status.ALT_UNCHECKED");
			}
			source.append(";\n");
		}
		// the first snapshot is taken in non-blocking mode
		// and opening infinite for() loop
		// the first snapshot is taken in non-blocking mode
		aData.addCommonLibraryImport("TTCN_Snapshot");
		source.append("TTCN_Snapshot.takeNew(false);\n");
		// and opening infinite for() loop
		source.append("for ( ; ; ) {\n");
		for (int i = 0; i < altGuards.size(); i++) {
			final AltGuard altGuard = altGuards.get(i);
			if (!(altGuard instanceof Operation_Altguard)) {
				//FATAL ERROR
				continue;
			}

			final IValue guardExpression = altGuard.getGuardExpression();
			if (guardExpression != null) {
				source.append(MessageFormat.format("if ( {0}_alt_flag_{1} == TitanAlt_Status.ALT_UNCHECKED) '{'\n", tempId, i));
				final ExpressionStruct expression = new ExpressionStruct();
				guardExpression.generateCodeExpression(aData, expression, true);
				source.append(expression.preamble);
				source.append(MessageFormat.format("if (TitanBoolean.getNative({0})) '{'\n", expression.expression));
				source.append(MessageFormat.format("{0}_alt_flag_{1} = TitanAlt_Status.ALT_MAYBE;\n", tempId, i));
				source.append("} else {\n");
				source.append(MessageFormat.format("{0}_alt_flag_{1} = TitanAlt_Status.ALT_NO;\n", tempId, i));
				source.append("}\n");
				source.append(expression.postamble);
				source.append("}\n");
			}

			// evaluation of guard operation
			source.append(MessageFormat.format("if ( {0}_alt_flag_{1} == TitanAlt_Status.ALT_MAYBE) '{'\n", tempId, i));
			final ExpressionStruct expression = new ExpressionStruct();
			expression.expression.append(MessageFormat.format("{0}_alt_flag_{1} = ", tempId, i));
			final Statement statement = ((Operation_Altguard) altGuard).getGuardStatement();
			statement.generateCodeExpression(aData, expression);
			expression.mergeExpression(source);

			// execution of statement block if the guard was successful
			source.append(MessageFormat.format("if ( {0}_alt_flag_{1} == TitanAlt_Status.ALT_YES) '{'\n", tempId, i));
			final StatementBlock block = ((Operation_Altguard) altGuard).getStatementBlock();
			if (inInterleave) {
				if (block != null && block.getSize() > 0) {
					if (block.hasReceivingStatement(0)) {
						source.append(MessageFormat.format("continue {0}_branch{1};\n", tempId, i));
					} else {
						source.append("{\n");
						block.generateCode(aData, source);
						source.append(MessageFormat.format("continue {0}_end;\n", tempId));
						source.append("}\n");
					}
				} else {
					source.append(MessageFormat.format("continue {0}_end;\n", tempId));
				}
			} else {
				if (block != null && block.getSize() > 0) {
					source.append("{\n");
					block.generateCode(aData, source);
					if (block.hasReturn(CompilationTimeStamp.getBaseTimestamp()) != StatementBlock.ReturnStatus_type.RS_YES) {
						source.append("break;\n");
					}
					source.append("}\n");
				}
			}
			source.append("}\n");
			source.append("}\n");
		}

		// error handling and taking the next snapshot in blocking mode
		source.append("if (");
		for (int i = 0; i < altGuards.size(); i++) {
			if (i > 0) {
				source.append(" && ");
			}
			source.append(MessageFormat.format("{0}_alt_flag_{1} == TitanAlt_Status.ALT_NO", tempId, i));
		}
		source.append(") {\n");
		source.append("throw new TtcnError(\"None of the branches can be chosen in the response and exception handling part of call statement in file");
		//TODO translate_string
		if(location != null && location.getFile() != null) {
			source.append(MessageFormat.format("in file {0} at line {1}", location.getFile().getName(), location.getLine()));
		}
		source.append("\");\n");
		source.append("}\n");

		source.append("TTCN_Snapshot.takeNew(true);\n");
		source.append("}\n");
	}

	public void setMyLaicStmt(final AltGuards pAltGuards, final Statement pLoopStmt) {
		for (AltGuard altGuard : altGuards) {
			altGuard.setMyLaicStmt(pAltGuards, pLoopStmt);
		}
	}
}
