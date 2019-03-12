/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the parameter redirection of a getcall/getreply operation.
 * <p>
 * Provided with assignment list notation.
 *
 * @author Kristof Szabados
 * */
public final class AssignmentList_Parameter_Redirection extends Parameter_Redirection {
	private static final String FULLNAMEPART = ".parameterassignments";

	private final Parameter_Assignments assignments;

	// calculated field
	private Variable_Entries entries;

	/**
	 * Constructs the assignment list style parameter redirection with the
	 * parameter assignments.
	 *
	 * @param assignments
	 *                the assignments to manage.
	 * */
	public AssignmentList_Parameter_Redirection(final Parameter_Assignments assignments) {
		this.assignments = assignments;

		if (assignments != null) {
			assignments.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (assignments == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (assignments != null) {
			assignments.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (assignments != null) {
			assignments.setCodeSection(codeSection);
		}
		if (entries != null) {
			entries.setCodeSection(codeSection);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean hasDecodedModifier() {
		for (int i = 0, size = entries.getNofEntries(); i < size; i++) {
			final Variable_Entry entry = entries.getEntryByIndex(i);
			if (entry.isDecoded()) {
				return true;
			}
		}

		return false;
	}

	@Override
	/** {@inheritDoc} */
	public void checkErroneous(final CompilationTimeStamp timestamp) {
		final HashMap<String, Parameter_Assignment> parameterMap = new HashMap<String, Parameter_Assignment>();
		for (int i = 0, size = assignments.getNofParameterAssignments(); i < size; i++) {
			final Parameter_Assignment assignment = assignments.getParameterAssignmentByIndex(i);
			final String name = assignment.getIdentifier().getName();
			if (parameterMap.containsKey(name)) {
				assignment.getLocation().reportSemanticError(
						MessageFormat.format("Duplicate redirect for parameter `{0}''", assignment.getIdentifier()
								.getDisplayName()));
				final Location otherLocation = parameterMap.get(name).getLocation();
				otherLocation.reportSemanticWarning(MessageFormat.format(
						"A variable entry for parameter `{0}'' is already given here", assignment.getIdentifier()
						.getDisplayName()));
			} else {
				parameterMap.put(name, assignment);
			}

			checkVariableReference(timestamp, assignment.getReference(), null);
			final Value stringEncoding = assignment.getStringEncoding();
			if (stringEncoding != null) {
				stringEncoding.checkStringEncoding(timestamp, null);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final Signature_Type signature, final boolean isOut) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		final SignatureFormalParameterList parameterList = signature.getParameterList();
		if (parameterList.getNofParameters() == 0) {
			getLocation().reportSemanticError(MessageFormat.format(SIGNATUREWITHOUTPARAMETERS, signature.getTypename()));
			checkErroneous(timestamp);
			return;
		}

		boolean errorFlag = false;
		final HashMap<String, Parameter_Assignment> parameterMap = new HashMap<String, Parameter_Assignment>();
		for (int i = 0, size = assignments.getNofParameterAssignments(); i < size; i++) {
			final Parameter_Assignment assignment = assignments.getParameterAssignmentByIndex(i);
			final String name = assignment.getIdentifier().getName();
			if (parameterMap.containsKey(name)) {
				assignment.getLocation().reportSemanticError(
						MessageFormat.format("Duplicate redirect for parameter `{0}''", assignment.getIdentifier()
								.getDisplayName()));
				final Location otherLocation = parameterMap.get(name).getLocation();
				otherLocation.reportSemanticWarning(MessageFormat.format(
						"A variable entry for parameter `{0}'' is already given here", assignment.getIdentifier()
						.getDisplayName()));
				errorFlag = true;
			} else {
				parameterMap.put(name, assignment);
			}

			if (parameterList.hasParameterWithName(name)) {
				final SignatureFormalParameter parameterTemplate = parameterList.getParameterByName(name);
				if (isOut) {
					if (SignatureFormalParameter.ParamaterDirection.PARAM_IN == parameterTemplate.getDirection()) {
						final String message = MessageFormat.format(
								"Parameter `{0}'' of signature `{1}'' has `in'' direction", assignment
								.getIdentifier().getDisplayName(), signature.getTypename());
						assignment.getLocation().reportSemanticError(message);
						errorFlag = true;
					}
				} else {
					if (SignatureFormalParameter.ParamaterDirection.PARAM_OUT == parameterTemplate.getDirection()) {
						final String message = MessageFormat.format(
								"Parameter `{0}'' of signature `{1}'' has `out'' direction", assignment
								.getIdentifier().getDisplayName(), signature.getTypename());
						assignment.getLocation().reportSemanticError(message);
						errorFlag = true;
					}
				}

				if (assignment.isDecoded()) {
					final Value stringEncoding = assignment.getStringEncoding();
					final Type parType = parameterTemplate.getType();
					//boolean isErroneous = false;
					final IType refdLast = parType.getTypeRefdLast(timestamp);
					switch (refdLast.getTypetypeTtcn3()) {
					case TYPE_BITSTRING:
					case TYPE_HEXSTRING:
					case TYPE_OCTETSTRING:
					case TYPE_CHARSTRING:
						if (stringEncoding != null) {
							stringEncoding.getLocation().reportSemanticError("The encoding format parameter for the '@decoded' modifier is only available to parameter redirects of universal charstrings");
							errorFlag = true;
						}
						break;
					case TYPE_UCHARSTRING:
						if (stringEncoding != null) {
							stringEncoding.checkStringEncoding(timestamp, null);
						}
						break;
					default:
						assignment.getLocation().reportSemanticError("The '@decoded' modifier is only available to parameter redirects of string types.");
						errorFlag = true;
						break;
					}

					final Reference variableReference = assignment.getReference();
					final IType varType = variableReference.checkVariableReference(timestamp);
					if (!errorFlag && varType != null) {
						// store the variable type in case it's decoded (since this cannot
						// be extracted from the value type with the sub-references)
						final IType declarationType = varType.getTypeRefdLast(timestamp);
						assignment.setDeclarationType(declarationType);
						varType.checkCoding(timestamp, false, variableReference.getMyScope().getModuleScope(), false, variableReference.getLocation());
					}
				} else {
					checkVariableReference(timestamp, assignment.getReference(), parameterTemplate.getType());
				}
			} else {
				assignment.getLocation().reportSemanticError(
						MessageFormat.format("Signature `{0}'' does not have parameter named `{1}''",
								signature.getTypename(), assignment.getIdentifier().getDisplayName()));
				errorFlag = true;
				checkVariableReference(timestamp, assignment.getReference(), null);
				final Value stringEncoding = assignment.getStringEncoding();
				if (stringEncoding != null) {
					stringEncoding.checkStringEncoding(timestamp, null);
				}
			}
		}

		if (!errorFlag) {
			// converting the AssignmentList to VariableList
			entries = new Variable_Entries();
			final int upperLimit = isOut ? parameterList.getNofOutParameters() : parameterList.getNofInParameters();
			for (int i = 0; i < upperLimit; i++) {
				final SignatureFormalParameter parameter = isOut ? parameterList.getOutParameterByIndex(i) : parameterList
						.getInParameterByIndex(i);
				final String name = parameter.getIdentifier().getName();
				if (parameterMap.containsKey(name)) {
					final Parameter_Assignment parAssignment = parameterMap.get(name);
					entries.add(new Variable_Entry(parAssignment.getReference(), parAssignment.isDecoded(), parAssignment.getStringEncoding(), parAssignment.getDeclarationType()));
				} else {
					entries.add(new Variable_Entry());
				}
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		assignments.updateSyntax(reparser, isDamaged);
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (assignments == null) {
			return;
		}

		assignments.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (assignments != null && !assignments.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final ExpressionStruct expression, final TemplateInstance matched_ti, final String lastGenTIExpression, final boolean is_out) {
		internalGenerateCode(aData, expression, entries, matched_ti, lastGenTIExpression, is_out);
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeDecoded(final JavaGenData aData, final StringBuilder source, final TemplateInstance matched_ti, final String tempID, final boolean is_out) {
		internalGenerateCodeDecoded(aData, source, entries, matched_ti, tempID, is_out);
	}
}
