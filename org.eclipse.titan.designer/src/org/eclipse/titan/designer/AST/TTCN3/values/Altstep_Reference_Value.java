/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.values;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.types.Altstep_Type;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents a altstep reference value.
 * <p>
 * Can not be parsed.
 *
 * @author Kristof Szabados
 */
public final class Altstep_Reference_Value extends Value {

	private final Def_Altstep referredAltstep;

	public Altstep_Reference_Value(final Def_Altstep referredAltstep) {
		this.referredAltstep = referredAltstep;
	}

	@Override
	/** {@inheritDoc} */
	public Value_type getValuetype() {
		return Value_type.ALTSTEP_REFERENCE_VALUE;
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("refers(");
		builder.append(referredAltstep.getAssignmentName()).append(')');
		return builder.toString();
	}

	public Def_Altstep getReferredAltstep() {
		return referredAltstep;
	}

	@Override
	/** {@inheritDoc} */
	public IValue getReferencedSubValue(final CompilationTimeStamp timestamp, final Reference reference,
			final int actualSubReference, final IReferenceChain refChain) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (getIsErroneous(timestamp) || subreferences.size() <= actualSubReference) {
			return this;
		}

		final IType type = myGovernor.getTypeRefdLast(timestamp);
		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDVALUESUBREFERENCE, type.getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(
					FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(), type.getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(ParameterisedSubReference.INVALIDVALUESUBREFERENCE);
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean isUnfoldable(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue,
			final IReferenceChain referenceChain) {
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public Type_type getExpressionReturntype(final CompilationTimeStamp timestamp, final Expected_Value_type expectedValue) {
		return Type_type.TYPE_ALTSTEP;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkEquality(final CompilationTimeStamp timestamp, final IValue other) {
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final IValue last = other.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		return Value_type.ALTSTEP_REFERENCE_VALUE.equals(last.getValuetype())
				&& referredAltstep == ((Altstep_Reference_Value) last).getReferredAltstep();
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		// no members
		return true;
	}

	/** {@inheritDoc} */
	public boolean canGenerateSingleExpression() {
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateCodeInit(final JavaGenData aData, final StringBuilder source, final String name) {
		source.append(name);
		source.append(".assign( ");
		source.append(generateSingleExpression(aData));
		source.append( " );\n" );
		return source;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder generateSingleExpression(final JavaGenData aData) {
		aData.addBuiltinTypeImport("Default_Base");
		aData.addBuiltinTypeImport("TitanAlt_Status");

		final StringBuilder result = new StringBuilder();

		IType governor = myGovernor;
		if (governor == null) {
			governor = getExpressionGovernor(CompilationTimeStamp.getBaseTimestamp(), Expected_Value_type.EXPECTED_TEMPLATE);
		}
		if (governor == null || referredAltstep == null) {
			result.append("// FATAL ERROR while processing altstep reference value\n");
			return result;
		}

		final IType lastGovernor = governor.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		result.append(MessageFormat.format("new {0}(new {0}.function_pointer() '{'\n", governor.getGenNameValue(aData, result, myScope)));
		result.append("@Override\n");
		result.append("public String getId() {\n");
		result.append(MessageFormat.format("return \"{0}\";\n", referredAltstep.getFullName()));
		result.append("}\n");

		final Altstep_Type altstepType = (Altstep_Type) lastGovernor;
		final String altstepName = referredAltstep.getIdentifier().getName();
		final StringBuilder actualParList = altstepType.getFormalParameters().generateCodeActualParlist("");

		result.append("@Override\n");
		result.append("public void invoke_standalone(");
		altstepType.getFormalParameters().generateCode(aData, result);
		result.append(") {\n");
		result.append(MessageFormat.format("{0}({1});\n", altstepName, actualParList));
		result.append("}\n");

		result.append("@Override\n");
		result.append("public Default_Base activate(");
		altstepType.getFormalParameters().generateCode(aData, result);
		result.append(") {\n");
		result.append(MessageFormat.format("return activate_{0}({1});\n", altstepName, actualParList));
		result.append("}\n");

		result.append("@Override\n");
		result.append("public TitanAlt_Status invoke(");
		altstepType.getFormalParameters().generateCode(aData, result);
		result.append(") {\n");
		result.append(MessageFormat.format("return {0}_instance({1});\n", altstepName, actualParList));
		result.append("}\n");
		result.append("})\n");

		return result;
	}
}
