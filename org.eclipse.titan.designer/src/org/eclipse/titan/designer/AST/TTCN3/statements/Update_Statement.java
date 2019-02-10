/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ErroneousAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.MultipleWithAttributes;
import org.eclipse.titan.designer.AST.TTCN3.attributes.SingleWithAttribute;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;

public class Update_Statement extends Statement {

	private static final String STATEMENT_NAME = "@update";
	private static final String FULLNAMEPART1 = ".ref";
	private static final String FULLNAMEPART2 = ".<attribpath>";

	private Reference ref;
	private MultipleWithAttributes attr;
	private ErroneousAttributes err_attrib;

	public Update_Statement(final Reference reference, final MultipleWithAttributes attributes) {
		if (reference != null) {
			this.ref = reference;
		}

		if (attributes != null) {
			this.attr = attributes;
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_UPDATE;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (ref == child) {
			return builder.append(FULLNAMEPART1);
		}
		builder.append(FULLNAMEPART2);

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (ref != null) {
			ref.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (ref != null) {
			ref.setCodeSection(codeSection);
		}
	}

	@Override
	public void check(CompilationTimeStamp timestamp) {
		boolean useRuntime2 = false;
		try {
			if ("true".equals(getLocation().getFile().getProject().getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
					MakefileCreationData.FUNCTIONTESTRUNTIME_PROPERTY)))) {
				useRuntime2 = true;
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while reading persistent property", e);
		}

		//TODO: check runtime later
		Assignment refd_ass = ref.getRefdAssignment(timestamp, false);
		switch (refd_ass.getAssignmentType()) {
		case A_CONST:
		case A_TEMPLATE:
			break; //OK
		default:
			this.getLocation().reportSemanticError(MessageFormat.format("Reference to constant or template definition was expected instead of {0}", refd_ass.getAssignmentName()));
			return;
		}
		IType ref_type = refd_ass.getType(timestamp);
		if (ref_type != null && ref_type.getIsErroneous(timestamp)) {
			ref.getLocation().reportSemanticError(MessageFormat.format("Type `{0}' cannot have erroneous attributes", ref_type.getTypename()));
		}
		if (attr != null) {
			err_attrib = Definition.checkErroneousAttributes(attr, ref_type, myScope, ref_type.getFullName(), false, timestamp, ref);
		}
		//TODO: Runtime 2
		/*if (ref.getSubreferences() != null) {
			ref.getLocation().reportSemanticError("Field names and array indexes are not allowed in this context");
		}*/
	}

	@Override
	public void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (ref != null) {
			ref.updateSyntax(reparser, false);
			reparser.updateLocation(ref.getLocation());
		}

		if (attr != null) {
			attr.updateSyntax(reparser, false);
			reparser.updateLocation(attr.getLocation());
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (ref != null && !ref.accept(v)) {
			return false;
		}

		if (attr != null && !attr.accept(v)) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		source.append("//TODO: update statement\n ");
	}

}
