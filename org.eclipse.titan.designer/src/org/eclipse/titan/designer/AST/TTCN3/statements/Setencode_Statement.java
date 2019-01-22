/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.IType.Coding_Type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalChar;
import org.eclipse.titan.designer.AST.TTCN3.values.UniversalCharstring;
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
 * 
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
	public void check(CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		boolean type_error = false;
		type.check(timestamp);
		Type t_ct = (Type) type.getTypeWithCodingTable(timestamp, false);
		if (t_ct == null) {
			type.getLocation().reportSemanticError(OPERANDERROR1);
			type_error = true;
		} else if (t_ct.getCodingTable().size() == 1) {
			type.getLocation().reportSemanticWarning("The type argument has only one encoding rule defined. The 'setencode' statement will be ignored");
		}

		if (encoding != null) {
			Value enc_str = this.encoding;
			enc_str.setLoweridToReference(timestamp);
			final Type_type tempType = enc_str.getExpressionReturntype(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);

			if (!type_error && tempType.equals(Type_type.TYPE_UCHARSTRING) && !enc_str.isUnfoldable(timestamp)) {
				final IValue last = encoding.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
				if (Value_type.UNIVERSALCHARSTRING_VALUE.equals(last.getValuetype())) {
					boolean val_error = false;
					final UniversalCharstring_Value ucs_value = (UniversalCharstring_Value)last;
					UniversalCharstring us = ucs_value.getValue();
					for (int i = 0; i < us.length(); i++) {
						final UniversalChar uc = us.get(i);
						if (uc.group() != 0 || uc.plane() != 0 || uc.row() != 0) {
							val_error = true;
							break;
						}
					}
					if (!val_error) {
						String s = us.getString();
						IType.MessageEncoding_type coding = Type.getEncodingType(s);
						boolean built_in = (coding != IType.MessageEncoding_type.PER && coding != IType.MessageEncoding_type.CUSTOM);
						val_error = true;
						List<Coding_Type> ct = t_ct.getCodingTable();
						for (int i = 0; i < ct.size(); i++) {
							if (built_in == ct.get(i).builtIn && ((built_in && coding == ct.get(i).builtInCoding) || (!built_in && s == ct.get(i).customCoding.name))) {
								val_error = false;
								break;
							}
						}
					}
					if (val_error) {
						encoding.getLocation().reportSemanticError(MessageFormat.format("The encoding string does not match any encodings of type `{0}'", type.getTypename()));
					}
				}
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	public void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException {
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
	protected boolean memberAccept(ASTVisitor v) {
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
