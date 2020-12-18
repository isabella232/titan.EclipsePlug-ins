/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType.ValueCheckingOptions;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.RunsOnScope;
import org.eclipse.titan.designer.AST.TTCN3.types.UniversalCharstring_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Charstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * The Setencode_Statement class represents a TTCN3 setencode statement.
 *
 * @author Gergo Ujhelyi
 */
public class Setencode_Statement extends Statement {

	private static final String OPERANDERROR1 = "The type argument has no encoding rules defined";

	private static final String FULLNAMEPART1 = ".type";
	private static final String FULLNAMEPART2 = ".encoding";
	private static final String STATEMENT_NAME = "setencode";

	private Type type;
	private Value encoding;

	public Setencode_Statement(final Type type, final Value value) {
		this.type = type;
		this.encoding = value;

		if (type != null) {
			type.setFullNameParent(this);
		}

		if (value != null) {
			value.setFullNameParent(this);
		}
	}

	@Override
	public Statement_type getType() {
		return Statement_type.S_SETENCODE;
	}

	@Override
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (type == child) {
			return builder.append(FULLNAMEPART1);
		} else if (encoding == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (type != null) {
			type.setMyScope(scope);
		}
		if (encoding != null) {
			encoding.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void setCodeSection(final CodeSectionType codeSection) {
		if (encoding != null) {
			encoding.setCodeSection(codeSection);
		}
	}

	//TODO: better variable names than C++ variable names
	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		boolean typeError = false;
		type.check(timestamp);
		final Type codingTableType = (Type) type.getTypeWithCodingTable(timestamp, false);
		if (codingTableType == null) {
			type.getLocation().reportSemanticError(OPERANDERROR1);
			typeError = true;
		} else if (codingTableType.getCodingTable().size() == 1) {
			type.getLocation().reportSemanticWarning("The type argument has only one encoding rule defined. The 'setencode' statement will be ignored");
		}

		if (encoding != null) {
			final Value encodingString = this.encoding;
			IValue lastValue = encodingString.setLoweridToReference(timestamp);
			final UniversalCharstring_Type tempType = new UniversalCharstring_Type();
			tempType.checkThisValue(timestamp, encodingString, null, new ValueCheckingOptions(Expected_Value_type.EXPECTED_DYNAMIC_VALUE, false, false, false, false, false));
			if (!typeError && !encodingString.getIsErroneous(timestamp) && !encodingString.isUnfoldable(timestamp)) {
				lastValue = lastValue.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
				boolean errorFound = false;
				if (Value_type.UNIVERSALCHARSTRING_VALUE.equals(lastValue.getValuetype())) {
					errorFound = ((UniversalCharstring_Value)lastValue).checkDynamicEncodingString(timestamp, type);
				} else if (Value_type.CHARSTRING_VALUE.equals(lastValue.getValuetype())) {
					errorFound = ((Charstring_Value)lastValue).checkDynamicEncodingString(timestamp, type);
				}
				if (errorFound) {
					encodingString.getLocation().reportSemanticError(MessageFormat.format("The encoding string does not match any encodings of type `{0}''", type.getTypename()));
				}
			}
		}

		final RunsOnScope runs_on_scope = myStatementBlock.getScopeRunsOn();
		if (runs_on_scope == null) {
			getLocation().reportSemanticError("'self.setencode' must be in a definition with a runs-on clause");
		} else if (!typeError && codingTableType.getCodingTable().size() >= 2){
			//TODO add default coding
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (type != null) {
			type.updateSyntax(reparser, false);
			reparser.updateLocation(type.getLocation());
		}

		if (encoding != null) {
			encoding.updateSyntax(reparser, false);
			reparser.updateLocation(encoding.getLocation());
		}
	}

	@Override
	protected boolean memberAccept(final ASTVisitor v) {
		if (type != null && !type.accept(v)) {
			return false;
		}

		if (encoding != null && !encoding.accept(v)) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		if (type.getTypeWithCodingTable(CompilationTimeStamp.getBaseTimestamp(), false).getCodingTable().size() == 1) {
			// the 'setencode' statement is ignored if the type has only one encoding
			return;
		}

		final ExpressionStruct expression = new ExpressionStruct();
		encoding.generateCodeExpression(aData, expression, true);
		if (expression.preamble != null) {
			source.append(expression.preamble);
		}
		source.append(MessageFormat.format("{0}_default_coding.operator_assign( {1} );\n", type.getGenNameDefaultCoding(aData, source, getMyScope()), expression.expression));
		if (expression.postamble != null) {
			source.append(expression.postamble);
		}
	}
}
