/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IAppendableSyntax;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock.ReturnStatus_type;
import org.eclipse.titan.designer.AST.TTCN3.types.Array_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.SequenceOf_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;

/**
 * The Statement class represents a general TTCN3 statement.
 * <p>
 * This class is used a base class for all the specific TTCN3 statements.
 *
 * @author Kristof Szabados
 * */
public abstract class Statement extends ASTNode implements ILocateableNode, IAppendableSyntax, IIncrementallyUpdateable {

	public enum Statement_type {
		// ambiguous statements
		/** start undefined. */
		S_START_UNKNOWN,
		/** stop undefined. */
		S_STOP_UNKNOWN,
		/** unknown instance. */
		S_UNKNOWN_INSTANCE,
		/** unknonwn applied inst. */
		S_UNKNOWN_APPLIED_INSTANE,
		// basic statements
		/** definition. */
		S_DEF,
		/** assignment. */
		S_ASSIGNMENT,
		/** while loop. */
		S_WHILE,
		/** do-while loop. */
		S_DOWHILE,
		/** for loop. */
		S_FOR,
		/** if statement. */
		S_IF,
		/** statement block. */
		S_BLOCK,
		/** select statement. */
		S_SELECT,
		/** try - catch.*/
		S_TRY_CATCH,
		/** log(...). */
		S_LOG,
		/** label identifier. */
		S_LABEL,
		/** goto identifier. */
		S_GOTO,
		/** function instance. */
		S_FUNCTION_INSTANCE,
		/** function applied. */
		S_FUNCTION_APPLIED,
		/** stop execution. */
		S_STOP_EXECUTION,
		/** testcase.stop */
		S_TESTCASE_STOP,
		/** break. */
		S_BREAK,
		/** continue. */
		S_CONTINUE,
		// behavior statements
		/** repeat. */
		S_REPEAT,
		/** alt construct. */
		S_ALT,
		/** interleave construct. */
		S_INTERLEAVE,
		/** call operation of ports. */
		S_CALL,
		/** altstep instance. */
		S_ALTSTEP_INSTANCE,
		/** altstep applied. */
		S_ALTSTEP_APPLIED,
		/** return. */
		S_RETURN,
		// default statements
		/** activate. */
		S_ACTIVATE,
		/** activate references. */
		S_ACTIVATE_REFERENCED,
		/** deactivate. */
		S_DEACTIVATE,
		// communication (port) statements
		/** send. */
		S_SEND,
		/** reply. */
		S_REPLY,
		/** raise. */
		S_RAISE,
		/** getcall. */
		S_GETCALL,
		/** getreply. */
		S_GETREPLY,
		/** catch. */
		S_CATCH,
		/** check. */
		S_CHECK,
		/** check getcall. */
		S_CHECK_GETCALL,
		/** check getreply. */
		S_CHECK_GETREPLY,
		/** check catch. */
		S_CHECK_CATCH,
		/** trigger. */
		S_TRIGGER,
		/** receive. */
		S_RECEIVE,
		/** check receive. */
		S_CHECK_RECEIVE,
		/** clear. */
		S_CLEAR_PORT,
		/** start port. */
		S_START_PORT,
		/** stop port. */
		S_STOP_PORT,
		/** halt port. */
		S_HALT_PORT,
		// component statements
		/** start component. */
		S_START_COMPONENT,
		/** start refd component. */
		S_START_REFERENCED_COMPONENT,
		/** stop component. */
		S_STOP_COMPONENT,
		/** component done. */
		S_DONE,
		/** kill. */
		S_KILL,
		/** killed. */
		S_KILLED,
		// configuration statements
		/** map. */
		S_MAP,
		/** unmap. */
		S_UNMAP,
		/** connect. */
		S_CONNECT,
		/** disconnect. */
		S_DISCONNECT,
		// timer statements
		/** start timer. */
		S_START_TIMER,
		/** stop timer. */
		S_STOP_TIMER,
		/** timer timeout. */
		S_TIMEOUT,
		// verdict statment
		/** setverdict(value). */
		S_SETVERDICT,
		// SUT statement
		/** action(...). */
		S_ACTION,
		// control statement
		/** testcase instance. */
		S_TESTCASE_INSTANCE,
		/** refd testcase instance */
		S_REFERENCED_TESTCASE_INSTANCE,
		/** string2ttcn predef. func */
		S_STRING2TTCN,
		/** int2enum predef. func. */
		S_INT2ENUM,
		// profiler statement
		/** start profiler. */
		S_START_PROFILER,
		/** stop profiler. */
		S_STOP_PROFILER
		//FIXME add support update, setstate, setencode statements
	}

	/** the statementblock in which this statement resides. */
	protected StatementBlock myStatementBlock;

	/**
	 * The location of the whole statement. This location encloses the
	 * statement fully, as it is used to report errors to.
	 **/
	protected Location location;

	/** the time when this statement was check the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	protected boolean isErroneous;

	protected Statement() {
		isErroneous = false;
		location = NULL_Location.INSTANCE;
	}

	@Override
	/** {@inheritDoc} */
	public final void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public final Location getLocation() {
		return location;
	}

	/**
	 * @return the exact type of the statement.
	 * */
	public abstract Statement_type getType();

	public final CompilationTimeStamp getLastTimeChecked() {
		return lastTimeChecked;
	}

	public final boolean getIsErroneous() {
		return isErroneous;
	}

	public final void setIsErroneous() {
		isErroneous = true;
	}

	/**
	 * @return the name of this statement.
	 * */
	public abstract String getStatementName();

	/**
	 * Sets the statementblock in which this statement was be found.
	 *
	 * @param statementBlock
	 *                the statementblock containing this statement.
	 * @param index
	 *                the index of this statement in the statement block.
	 * */
	public void setMyStatementBlock(final StatementBlock statementBlock, final int index) {
		myStatementBlock = statementBlock;
	}

	/** @return the parent statement block of the actual statement */
	public final StatementBlock getMyStatementBlock() {
		return myStatementBlock;
	}

	/**
	 * Add the provided definition down to all statements.
	 *
	 * @param definition
	 *                the definition the statement is located within.
	 * */
	public void setMyDefinition(final Definition definition) {
		//empty by default
	}

	/**
	 * Sets the altguards for the statement in which the statement actually
	 * is.
	 *
	 * @param altGuards
	 *                the altguards to which the statement belongs to.
	 * */
	public void setMyAltguards(final AltGuards altGuards) {
		//empty by default
	}

	/**
	 * Used to tell break and continue statements if they are located with an altstep, a loop or none.
	 *
	 * @param pAltGuards the altguards set only within altguards
	 * @param pLoopStmt the loop statement, set only within loops.
	 * */
	protected void setMyLaicStmt(final AltGuards pAltGuards, final Statement pLoopStmt) {
		//empty by default
	}

	/**
	 * Checks if the statement has a terminating potential.
	 * <p>
	 * Infinite loops also have this potential as the only way to exit them
	 * is to terminate the program.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 *
	 * @return true if the statement can terminate the current statement
	 *         block, false otherwise.
	 * */
	public boolean isTerminating(final CompilationTimeStamp timestamp) {
		return false;
	}

	/**
	 * Checks whether the statement has a return statement, either directly
	 * or embedded.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 *
	 * @return the return status of the statement.
	 * */
	public ReturnStatus_type hasReturn(final CompilationTimeStamp timestamp) {
		if (isTerminating(timestamp)) {
			return ReturnStatus_type.RS_YES;
		}

		return ReturnStatus_type.RS_NO;
	}

	/**
	 * Used when generating code for interleaved statement.
	 * If the block has no receivingv statements, then the general code generation can be used
	 *  (which may use blocks).
	 * */
	public boolean hasReceivingStatement() {
		return false;
	}

	/**
	 * Indicates whether the java equivalent of the statement can
	 *  return ALT_REPEAT. Applicable to receiving statements only.
	 * */
	public boolean canRepeat() {
		//FIXME fatal error
		return false;
	}

	/**
	 * Does the semantic checking of the statement.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * */
	public abstract void check(final CompilationTimeStamp timestamp);

	/**
	 * Checks if some statements are allowed in an interleave or not
	 * */
	public void checkAllowedInterleave() {
		//empty by default
	}

	/**
	 * Does the semantic checking of index redirections.
	 * 
	 * TODO this might be a too general location for this function.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle.
	 * @param indexReference
	 *                the reference to the redirection variable to analyze.
	 * @param arrayDimensions
	 *                the dimensions of the referenced definition.
	 * @param anyFrom
	 *                is the statement located in any from clause (might not possible because of parser rules).
	 * @param arrayKind
	 *                the kind of the array as string to be used in error messages.
	 * */
	public static void checkIndexRedirection(final CompilationTimeStamp timestamp, final Reference indexReference, final ArrayDimensions arrayDimensions, final boolean anyFrom, final String arrayKind) {
		if (!anyFrom) {
			indexReference.getLocation().reportSemanticError("Index redirect cannot be used without the 'any from' clause");
		}

		final IType referenceType = indexReference.checkVariableReference(timestamp);
		if (referenceType != null) {
			final int nofDimensions = arrayDimensions == null ? 0 : arrayDimensions.size();
			IType lastType = referenceType.getTypeRefdLast(timestamp);
			final Type_type tt = lastType.getTypetypeTtcn3();
			switch (tt) {
			case TYPE_INTEGER:
				if (nofDimensions > 1) {
					indexReference.getLocation().reportSemanticError(MessageFormat.format("Indices of multi-dimensional {0} arrays can only be redirected to an integer array or a record of integers", arrayKind));
				} else if (nofDimensions == 1 && referenceType.getSubtype() != null) {
					// make sure all possible indices are allowed by the subtype
					final ArrayDimension dimension = arrayDimensions.get(0);
					for (int i = 0; i < dimension.getSize(); i++) {
						Integer_Value value = new Integer_Value(dimension.getOffset() + i);
						referenceType.getSubtype().checkThisValue(timestamp, value);
					}
				}
				break;
			case TYPE_ARRAY:
				if (nofDimensions == 1) {
					indexReference.getLocation().reportSemanticError(MessageFormat.format("Indices of one-dimensional {0} arrays can only be redirected to an integer", arrayKind));
				} else {
					final IType ofType = ((Array_Type)lastType).getElementType();

					final Type_type tt_elem = ofType.getTypeRefdLast(timestamp).getTypetypeTtcn3();
					if (tt == tt_elem) {
						indexReference.getLocation().reportSemanticError("The array in the index redirect must be one-dimensional");
					} else if (tt_elem != Type_type.TYPE_INTEGER) {
						indexReference.getLocation().reportSemanticError(MessageFormat.format("The element type of {0} in an index redirect must be integer", tt == Type_type.TYPE_ARRAY ? "an array" : "a 'record of'"));
					}

					if (nofDimensions != 0) {
						final ArrayDimension dimension = ((Array_Type)lastType).getDimension();
						if (dimension.getSize() != nofDimensions) {
							indexReference.getLocation().reportSemanticError(MessageFormat.format("Size of integer array is invalid: the {0} array has {1} dimensions, but the integer array has {2} element{3}", arrayKind, nofDimensions, dimension.getSize(), dimension.getSize() == 1 ? "" : "s"));
						} else if (referenceType.getSubtype() != null) {
							// make sure all possible indices are allowed by the element type's subtype
							for (int i = 0; i < nofDimensions; i++) {
								ArrayDimension dimension_i = arrayDimensions.get(i);
								for (int j = 0; j < dimension_i.getSize(); j++) {
									Integer_Value value = new Integer_Value(dimension_i.getOffset() + j);
									ofType.getSubtype().checkThisValue(timestamp, value);
								}
							}
						}
					}
				}
				break;
			case TYPE_SEQUENCE_OF:
				if (nofDimensions == 1) {
					indexReference.getLocation().reportSemanticError(MessageFormat.format("Indices of one-dimensional {0} arrays can only be redirected to an integer", arrayKind));
				} else {
					final IType ofType = ((SequenceOf_Type)lastType).getOfType();

					final Type_type tt_elem = ofType.getTypeRefdLast(timestamp).getTypetypeTtcn3();
					if (tt == tt_elem) {
						indexReference.getLocation().reportSemanticError("The 'record of' in the index redirect must be one-dimensional");
					} else if (tt_elem != Type_type.TYPE_INTEGER) {
						indexReference.getLocation().reportSemanticError(MessageFormat.format("The element type of {0} in an index redirect must be integer", tt == Type_type.TYPE_ARRAY ? "an array" : "a 'record of'"));
					}

					if (nofDimensions != 0 && ofType.getSubtype() != null) {
						if (ofType.getSubtype().lengthAllowed(nofDimensions)) {
							indexReference.getLocation().reportSemanticError(MessageFormat.format("This index redirect would result in a record of integer of length {0}, which is not allowed by the length restrictions of type `{1}''", nofDimensions, referenceType.getTypename()));
						} else {
							// make sure all possible indices are allowed by the element type's subtype
							for (int i = 0; i < nofDimensions; i++) {
								ArrayDimension dimension_i = arrayDimensions.get(i);
								for (int j = 0; j < dimension_i.getSize(); j++) {
									Integer_Value value = new Integer_Value(dimension_i.getOffset() + j);
									ofType.getSubtype().checkThisValue(timestamp, value);
								}
							}
						}
					}
				}
				break;
			default:
				indexReference.getLocation().reportSemanticError(MessageFormat.format("Indices of {0} arrays can only be redirected to an integer, an integer array or a record of integers", arrayKind));
				break;
			}
		}
	}

	/**
	 * Checks the properties of the statement, that can only be checked
	 * after the semantic check was completely run.
	 * <p>
	 * The default behavior does not define any operation.
	 * */
	public void postCheck() {
		//empty by default
	}

	@Override
	/** {@inheritDoc} */
	public List<Integer> getPossibleExtensionStarterTokens() {
		// By default statements can not be extended except these 3 tokens
		final List<Integer> result = new ArrayList<Integer>();
		result.add(Ttcn3Lexer.SEMICOLON);
		result.add(Ttcn3Lexer.LINE_COMMENT);
		result.add(Ttcn3Lexer.WS);
		return result;
	}

	@Override
	/** {@inheritDoc} */
	public List<Integer> getPossiblePrefixTokens() {
		// By default statements can not be prepended
		return new ArrayList<Integer>(0);
	}

	/**
	 * Handles the incremental parsing of this statement.
	 *
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	public abstract void updateSyntax(TTCN3ReparseUpdater reparser, boolean isDamaged) throws ReParseException;

	/**
	 * Sets the code_section attribute for the statement to the provided value.
	 *
	 * @param codeSection the code section where this statement should be generated.
	 * */
	public void setCodeSection(final CodeSectionType codeSection) {
		//Do nothing by default
		//FIXME implement in child classes
	}

	//TODO: use abstract method in abstract class to make sure, that all child class have separate implementation
	/**
	 * Add generated java code on this level
	 * @param aData the generated java code with other info
	 */
	//public abstract void generateCode( final JavaGenData aData, final StringBuilder source );

	//TODO: remove
	/**
	 * Generate code for this statement.
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param source the source code generated
	 */
	public void generateCode( final JavaGenData aData, final StringBuilder source ) {
		//default implementation
		source.append( "\t\t" );
		source.append( "//TODO: " );
		source.append( getClass().getSimpleName() );
		source.append( ".generateCode() is not implemented!\n" );
	}

	/**
	 * Some statements can be used in altguards.
	 * In which situation they have to be generated as an expression.
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression to generate the source to
	 * @param callTimer the name of the call timer if this statement is inside a call operations body, expect null otherwise
	 */
	public void generateCodeExpression( final JavaGenData aData, final ExpressionStruct expression, final String callTimer) {
		ErrorReporter.INTERNAL_ERROR("Code generator reached invalid guard statement `" + getFullName() + "''");
		expression.expression.append("FATAL_ERROR encountered");
	}

	/**
	 * Generate the code for the index redirection part of statements (that have one).
	 *
	 * @param aData the structure to put imports into and get temporal variable names from.
	 * @param expression the expression to generate the source to.
	 * @param indexRedirection the reference to use as the redirection target.
	 * @param scope the scope of the statement.
	 */
	public static void generateCodeIndexRedirect(final JavaGenData aData, final ExpressionStruct expression, final Reference indexRedirection, final Scope scope) {
		ExpressionStruct refExpression = new ExpressionStruct();
		indexRedirection.generateCode(aData, refExpression);
		if (refExpression.preamble.length() > 0) {
			expression.preamble.append(refExpression.preamble);
		}

		final String tempId = aData.getTemporaryVariableName();
		IType typeReference = indexRedirection.checkVariableReference(CompilationTimeStamp.getBaseTimestamp());
		expression.preamble.append(MessageFormat.format("final {0} {1} = {2};\n", typeReference.getGenNameValue(aData, expression.expression, scope), tempId, refExpression.expression));

		aData.addBuiltinTypeImport("Index_Redirect");

		expression.expression.append("new Index_Redirect() {\n");
		expression.expression.append("@Override\n");
		expression.expression.append("public void addIndex(int p_index) {\n");
		expression.expression.append("super.addIndex(p_index);\n");
		IType last = typeReference.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		switch(last.getTypetypeTtcn3()) {
		case TYPE_INTEGER:
			expression.expression.append(MessageFormat.format("{0}.assign(p_index);\n", tempId));
			break;
		case TYPE_SEQUENCE_OF:
			expression.expression.append(MessageFormat.format("{0}.getAt(pos).assign(p_index);\n", tempId));
			break;
		case TYPE_ARRAY: {
			ArrayDimension dimension = ((Array_Type)last).getDimension();
			long offset = dimension.getOffset();
			String offsetString;
			if (offset == 0) {
				offsetString = "";
			} else if (offset < 0) {
				offsetString = MessageFormat.format(" - {0}", -offset);
			} else {
				offsetString = MessageFormat.format(" + {0}", offset);
			}
			expression.expression.append(MessageFormat.format("{0}.getAt(pos{1}).assign(p_index);\n", tempId, offsetString));
			break;
		}
		default:
			//FATAL error
			break;
		}

		expression.expression.append("}\n");
		expression.expression.append("}\n");
	}
}
