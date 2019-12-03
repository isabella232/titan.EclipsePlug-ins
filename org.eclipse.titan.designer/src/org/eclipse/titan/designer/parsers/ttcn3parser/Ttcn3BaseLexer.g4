lexer grammar Ttcn3BaseLexer;

/*
 ******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************
*/

/*
 * author Arpad Lovassy
 */

@header {
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerUtilities;

import org.eclipse.titan.designer.core.LoadBalancingUtilities;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.IntervalDetector;
import org.eclipse.titan.common.parsers.Interval.interval_type;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.designer.AST.Location;
}

@members {
  protected boolean isTTCNPP = false;
  private int tokenCounter = 0;

  public void setTTCNPP() {
  	isTTCNPP = true;
  }

  private IFile actualFile = null;

  public void setActualFile(IFile file) {
    actualFile = file;
  }

  private int actualLine = 1;

  public void setActualLine(int line) {
    actualLine = line;
  }

  /**
   * Creates and places a task marker on the provided location.
   *
   * @param taskMarker the data of the marker needed for this operation
   * */
  public void createTaskMarker(TITANMarker taskMarker){
    if (actualFile == null) {
      return;
    }

    ParserMarkerSupport.createTaskMarker(actualFile, taskMarker);
  }

  /**
   * Creates and places a task marker on the provided location.
   *
   * @param taskMarker the data of the marker needed for this operation
   */
  public void createWarningMarker(TITANMarker warningMarker) {
    if (actualFile == null) {
      return;
    }

    ParserMarkerSupport.createWarningMarker(actualFile, warningMarker);
  }

  IntervalDetector intervalDetector = new IntervalDetector();

  public Interval getRootInterval() {
    return intervalDetector.getRootInterval();
  }

  public void initRootInterval(int length) {
    intervalDetector.initRootInterval(length);
  }

  /**
   * true, if todo/fixme markers can be placed by the lexer,
   *       typically it is set if full parsing is done
   * false otherwise
   */
  private boolean mCommentTodo = false;

  public void setCommentTodo( boolean aCommentTodo ) {
    mCommentTodo = aCommentTodo;
  }

  //TODO: we will need it later for the performance
  /** Used to preload the class, also loading the TTCN-3 lexer. */
  public static void preLoad() {
  }

	/** pattern for matching todo/fixme in a comment line */
	final static Pattern PATTERN_TODO_FIXME = Pattern.compile("((TODO|FIXME).*?)\\s*(?=(TODO|FIXME|$))");

	/**
	 * Extracts todo and fixme information from comment text
	 * @param aCommentText the full text of the comment token
	 * @param aMultiLine type of comment. true: / * ... * /, false: / / ... \n
	 */
	private void detectTasks( final String aCommentText, final boolean aMultiLine ) {
		if ( !mCommentTodo ) {
			return;
		}
		// remove comment boundary characters
		String commentText;
		if ( aMultiLine ) {
			commentText = aCommentText.substring( 2, aCommentText.length() - 2 );
		} else {
			commentText = aCommentText.substring( 2 );
		}

		String commentLines[] = commentText.split("\\r?\\n");
		for( int i = 0; i < commentLines.length; i++ ) {
			String commentLine = commentLines[ i ];
			Matcher m = PATTERN_TODO_FIXME.matcher(commentLine);
			while ( m.find() ) {
				String text = m.group( 1 );
				if ( text != null ) {
					createTaskMarker( new TITANMarker( text, actualLine + i + _tokenStartLine, -1, -1,
							IMarker.SEVERITY_INFO, text.startsWith("TODO") ? IMarker.PRIORITY_NORMAL : IMarker.PRIORITY_HIGH ) );
				}
			}
		}
	}

	/** binstr is valid bitstring */
	boolean valid_bit = true;
	/** binstr is valid octetstring */
	boolean valid_oct = true;
	/** binstr is not a valid octetstr but a valid hexstr */
	boolean half_oct = false;
	/** binstr contains matching symbol */
	boolean contains_match = false;
	/** binstr contains whitespace characters */
	boolean contains_ws = false;
	/** token start index */
	int startIndex = 0;

	private List<TITANMarker> warningsAndErrors = new ArrayList<TITANMarker>();

	public List<TITANMarker> getWarningsAndErrors() {
		return warningsAndErrors;
	}

	public TITANMarker createMarker( final String aMessage, final Token aStartToken, final Token aEndToken, final int aSeverity, final int aPriority ) {
		TITANMarker marker = new TITANMarker(
			aMessage,
			(aStartToken != null) ? aStartToken.getLine() : -1,
			(aStartToken != null) ? aStartToken.getStartIndex() : -1,
			(aEndToken != null) ? aEndToken.getStopIndex() + 1 : -1,
			aSeverity, aPriority );
		return marker;
	}

	public TITANMarker createMarker( final String aMessage, final int line, final int start, final int stop, final int aSeverity, final int aPriority ) {
		TITANMarker marker = new TITANMarker(
			aMessage,
			line, start, stop,
			aSeverity, aPriority );
		return marker;
	}

	private void reportWarning( final String aMessage, final Token aStartToken, final Token aEndToken ) {
		TITANMarker marker = createMarker( aMessage, aStartToken, aEndToken, IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL );
		warningsAndErrors.add(marker);
	}

	private void reportWarning( final String aMessage, final int line, final int start, final int stop ) {
		TITANMarker marker = createMarker( aMessage, line, start, stop, IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL );
		warningsAndErrors.add(marker);
	}

	private void reportError( final String aMessage, final Token aStartToken, final Token aEndToken ) {
		TITANMarker marker = createMarker( aMessage, aStartToken, aEndToken, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL );
		warningsAndErrors.add(marker);
	}

	private void reportError( final String aMessage, final int line, final int start, final int stop ) {
		TITANMarker marker = createMarker( aMessage, line, start, stop, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL );
		warningsAndErrors.add(marker);
	}

	private void warning( final String msg ) {
		final int line = getLine();
		final int stopIndex = getCharIndex();
		reportWarning( msg, line, startIndex, stopIndex );
	}

	private void error( final String msg ) {
		final int line = getLine();
		final int stopIndex = getCharIndex();
		reportError( msg, line, startIndex, stopIndex );
	}

	/**
	 * Report error for invalid character and shows marker only for one character
	 * @param msg error message
	 */
	private void errorChar( final String msg ) {
		final int line = getLine();
		final int stopIndex = getCharIndex();
		reportError( msg, line, stopIndex - 1, stopIndex );
	}

}

/*------------------------------------------- Keywords -------------------------------------------*/

tokens {
  ACTION,                     ACTIVATE,                   ADDRESS,
  ALIVE,                      ALL,                        ALT,
  ALTSTEP,                    AND,                        AND4B,
  ANY,                        ANYTYPE,                    ANY2UNISTR,
  APPLY,

  BITSTRING,                  BOOLEAN,                    BREAK,

  CALL,                       CASE,                       CATCH,
  CHARKEYWORD,                CHARSTRING,                 CHECK,
  CHECKSTATE,                 CLEAR,                      COMPLEMENTKEYWORD,
  COMPONENT,                  CONNECT,                    CONST,
  CONTINUE,                   CONTROL,                    CREATE,

  DEACTIVATE,                 DEFAULT,                    DECMATCH,
  DECVALUE,                   DECVALUE_UNICHAR,           DEREFERS,
  DISCONNECT,                 DISPLAY,                    DO,
  DONE,

  ELSE,                       ENCODE,                     ENCVALUE,
  ENCVALUE_UNICHAR,           ENUMERATED,                 ERROR,
  EXCEPT,                     EXCEPTION,                  EXECUTE,
  EXTENDS,                    EXTENSION,                  EXTERNAL,

  FAIL,                       FALSE,                      FLOAT,
  FOR,                        FRIEND,                     FROM,
  FUNCTION,

  GETCALL,                    GETREF,                     GETREPLY,
  GETVERDICT,                 GOTO,                       GROUP,

  HALT,                       HEXSTRING,                  HOSTID,

  IF,                         IFPRESENT,                  IMPORT,
  IN,                         INCONC,                     INFINITY,
  INOUT,                      INTEGER,                    INTERLEAVE,
  ISTEMPLATEKIND,

  KILL,                       KILLED,

  LABEL,                      LANGUAGE,                   LENGTH,
  LOG,

  MAP,                        MATCH,                      MESSAGE,
  MIXED,                      MOD,                        MODIFIES,
  MODULE,                     MODULEPAR,                  MTC,

  NOBLOCK,                    NONE,
  NOT,                        NOT4B,                      NOW,
  NOWAIT,                     NOT_A_NUMBER,               NULL1,
  NULL2,

  OBJECTIDENTIFIERKEYWORD,    OCTETSTRING,                OF,
  OMIT,                       ON,                         OPTIONAL,
  OR,                         OR4B,                       OUT,
  OVERRIDEKEYWORD,

  PARAM,                      PASS,                       PATTERNKEYWORD,
  PERMUTATION,                PORT,                       PUBLIC,
  PRESENT,                    PRIVATE,                    PROCEDURE,

  RAISE,                      READ,                       REALTIME,
  RECEIVE,                    RECORD,                     RECURSIVE,
  REFERS,                     REM,                        REPEAT,
  REPLY,                      RETURN,                     RUNNING,
  RUNS,

  SELECT,                     SELF,                       SEND,
  SENDER,                     SET,                        SETVERDICT,
  SETSTATE,                   SIGNATURE,                  START,
  STOP,                       SUBSET,                     SUPERSET,
  SYSTEM,

  TEMPLATE,                   TESTCASE,                   TIMEOUT,
  TIMER,                      TIMESTAMP,                  TO,
  TRIGGER,                    TRUE,                       TYPE,

  UNION,                      UNIVERSAL,                  UNMAP,

  VALUE,                      VALUEOF,                    VAR,
  VARIANT,                    VERDICTTYPE,

  WHILE,                      WITH,

  XOR,                        XOR4B,

  /*------------------------------ Predefined function identifiers --------------------------------*/

  BIT2HEX,                    BIT2INT,                    BIT2OCT,
  BIT2STR,                    BSON2JSON,

  CBOR2JSON,                  CHAR2INT,                   CHAR2OCT,

  DECODE_BASE64,              DECOMP,

  ENCODE_BASE64,              ENUM2INT,

  FLOAT2INT,                  FLOAT2STR,

  GET_STRINGENCODING,

  HEX2BIT,                    HEX2INT,                    HEX2OCT,
  HEX2STR,

  INT2BIT,                    INT2CHAR,                   INT2ENUM,
  INT2FLOAT,                  INT2HEX,                    INT2OCT,
  INT2STR,                    INT2UNICHAR,                ISBOUND,
  ISCHOSEN,                   ISPRESENT,                  ISVALUE,

  JSON2BSON,                  JSON2CBOR,

  LENGTHOF,                   LOG2STR,

  OCT2BIT,                    OCT2CHAR,                   OCT2HEX,
  OCT2INT,                    OCT2STR,                    OCT2UNICHAR,

  REGEXP,                     REMOVE_BOM,                 RND,
  REPLACE,

  SETENCODE,                  SIZEOF,                     STR2BIT,
  STR2FLOAT,                  STR2HEX,                    STR2INT,
  STR2OCT,                    STRING2TTCN,                SUBSTR,

  TESTCASENAME,               TTCN2STRING,

  UNICHAR2CHAR,               UNICHAR2INT,                UNICHAR2OCT,

  /* general macro, used for code completion, see TTCN3KeywordLessLexer */
  MACRO,

  /*------------------------------ Binary string tokens --------------------------------*/

  BSTRING,
  BSTRINGMATCH,
  //HSTRING,	// already defined in rule HSTRING
  HSTRINGMATCH,
  OSTRING,
  OSTRINGMATCH,
  BHOSTRING_WRONG

}

WS:	[ \t\r\n\f]+	-> channel(HIDDEN);

LINE_COMMENT:	'//' ~[\r\n]*
{
	detectTasks(getText(), false);
} -> channel(HIDDEN);

BLOCK_COMMENT:	'/*' .*? '*/'
{
	intervalDetector.pushInterval(_tokenStartCharIndex, _tokenStartLine, interval_type.MULTILINE_COMMENT);
	intervalDetector.popInterval(_input.index(), _interp.getLine());
	detectTasks(getText(), true);
} -> channel(HIDDEN);

//TODO: check that nothing else preceeds it in current line
PREPROCESSOR_DIRECTIVE:
(   '#'
	(
		{ isTTCNPP }? (
			~('\n'|'\r'|'\\')
			|	{ _input.LA(2)!='\n' && _input.LA(2)!='\r' }? '\\'
			|	( '\\\n' | '\\\r' | '\\\r\n' )
		)*
	|
		{ !isTTCNPP }? ( (' '|'\t')* ('0'..'9')+ (' '|'\t')+ CSTRING ('0'..'9'|' '|'\t')* )
	)
)
{if (!isTTCNPP) {skip();};};

IDENTIFIER:
	[A-Za-z][A-Za-z0-9_]*
;

ASSIGNMENTCHAR:	':=';

PORTREDIRECTSYMBOL:	'->';

CSTRING:
'"'
(	'\\' .
|	'""'
|	~( '\\' | '"' )
)*
'"'
;

//[uU][+]?[0-9A-Fa-f]{1,8} but the optional + sign is handled with identifiers
UID:
	[uU][+][0-9a-fA-F]+
;

fragment BIN: [01];
fragment BINORMATCH: BIN | '?' | '*';
fragment HEX: [0-9A-Fa-f];
fragment HEXORMATCH: HEX | '?' | '*';
fragment OCT: HEX WS? HEX;
fragment OCTORMATCH: OCT | '?' | '*';

// Corresponds to titan/compiler2/ttcn3/compiler.l
// TTCN-3 standard is more strict, it does NOT allow whitespaces between the bin/hex/oct digits

// BHOSTRING_WRONG is for for erroneous cases for [BHO]STRING(MATCH)? rules
//  - wrong character between the quotes
//  - odd number of hex digits in case of OSTRING(MATCH)?)
// These tokens are not used in any parser rules, but these cases must be parser errors instead of lexer errors

HSTRING:
{	valid_bit = true;
	valid_oct = true;
	half_oct = false;
	contains_match = false;
	contains_ws = false;
	startIndex = getCharIndex();
}
	'\''
	(	[01]		{	half_oct = !half_oct;	}
	|	[2-9A-Fa-f]	{	valid_bit = false;
						half_oct = !half_oct;
					}
	|	[?*]		{	contains_match = true;
						if (half_oct) {
							valid_oct = false;
						}
					}
	|	[ \t\r\n\f]
		// a whitespace character. WS is not applicable here, as it can contain any number of characters, and using more Antlr * operators
		// in the lexer can consume a lot of memory, and it can lead to OutOfMemoryError in case of long strings.
					{	contains_ws = true;	}
	|	~['0-9A-Fa-f?* \t\r\n\f]
		// We make sure, that the options do NOT have any intersection
					{	errorChar("Invalid character `" + (char)_input.LA(0) + "' in binary string");
						setType( BHOSTRING_WRONG );
					}
	)*
	(	'\''
		(	[Bb]	{	if ( _input.LA(0) == 'b' ) {
							warning("The last character of a bitstring literal should be `B' instead of `b'");
						}
						if (valid_bit) {
							if (contains_ws) {
								warning("Bitstring " + ( contains_match ? "match" : "value" ) + " contains whitespace and/or newline character(s)");
							}
							setType( contains_match ? BSTRINGMATCH : BSTRING );
						} else {
							error("Bitstring value contains invalid character");
							setType( BHOSTRING_WRONG );
						}
					}
		|	[Hh]	{	if ( _input.LA(0) == 'h' ) {
							warning("The last character of a hexstring literal should be `H' instead of `h'");
						}
						if (contains_ws) {
							warning("Hexstring " + ( contains_match ? "match" : "value" ) + " contains whitespace and/or newline character(s)");
						}
       					setType( contains_match ? HSTRINGMATCH : HSTRING );
					}
		|	[Oo]	{	if ( _input.LA(0) == 'o' ) {
							warning("The last character of a octetstring literal should be `O' instead of `o'");
						}
						if (valid_oct && !half_oct) {
							if (contains_ws) {
								warning("Octetstring " + ( contains_match ? "match" : "value" ) + " contains whitespace and/or newline character(s)");
							}
							setType( contains_match ? OSTRINGMATCH : OSTRING );
						} else if (contains_match) {
							error("Octetstring match contains half octet(s)");
							setType( BHOSTRING_WRONG );
						} else {
							error("Octetstring value contains odd number of hexadecimal digits");
							setType( BHOSTRING_WRONG );
						}
					}
		|			{	error("Invalid binary string literal. Expecting `B', `H' or `O' after the closing `''");
						setType( BHOSTRING_WRONG );
					}
		)
	|		{	error("Unterminated binary string literal");
				setType( BHOSTRING_WRONG );
			}
	)
;

// Macros
MACRO_MODULEID:			'%moduleId' | '__MODULE__';
MACRO_DEFINITION_ID:	'%definitionId';
MACRO_TESTCASEID:		'%testcaseId' | '__TESTCASE__';
MACRO_FILENAME:			'%fileName';
MACRO_LINENUMBER:		'%lineNumber';
MACRO_FILEPATH:			'__FILE__';
MACRO_BFILENAME:		'__BFILE__';
MACRO_LINENUMBER_C:		'__LINE__';
MACRO_SCOPE:			'__SCOPE__';

// TITAN specific keywords
TITANSPECIFICTRY:	'@try';
TITANSPECIFICCATCH:	'@catch';
TITANSPECIFICUPDATEKEYWORD: '@update';

// modifier keywords
NOCASEKEYWORD:			'@nocase';
LAZYKEYWORD:			'@lazy';
DECODEDKEYWORD:			'@decoded';
DETERMINISTICKEYWORD:	'@deterministic';
FUZZYKEYWORD:			'@fuzzy';
INDEXKEYWORD:			'@index';
LOCALKEYWORD:			'@local';

fragment DIGIT: [0-9];

fragment INT: [1-9] DIGIT* | '0';

NUMBER: INT;

// Corresponds to FLOAT in titan/compiler2/ttcn3/compiler.l
// TTCN-3 standard is more strict, it allows only 'E' '-'? in the exponent part
FLOATVALUE:
(	INT '.' DIGIT+
|	INT ( '.' DIGIT+ )? [Ee] [+-]? INT
);

RANGEOP: '..';

DOT: '.';

SEMICOLON: ';';

COMMA: ',';

COLON: ':';

BEGINCHAR:
	'{'
{
  intervalDetector.pushInterval(_tokenStartCharIndex, _tokenStartLine, interval_type.NORMAL);
};

ENDCHAR:
	'}'
{
  intervalDetector.popInterval(_tokenStartCharIndex, _tokenStartLine);
};

SQUAREOPEN:
	'['
{
  intervalDetector.pushInterval(_tokenStartCharIndex, _tokenStartLine, interval_type.INDEX);
};

SQUARECLOSE:
	']'
{
  intervalDetector.popInterval(_tokenStartCharIndex, _tokenStartLine);
};

LPAREN:
	'('
{
  intervalDetector.pushInterval(_tokenStartCharIndex, _tokenStartLine, interval_type.PARAMETER);
};

RPAREN:
	')'
{
  intervalDetector.popInterval(_tokenStartCharIndex, _tokenStartLine);
};

LESSTHAN: '<';

MORETHAN: '>';

NOTEQUALS: '!=';

MOREOREQUAL: '>=';

LESSOREQUAL: '<=';

EQUAL: '==';

PLUS: '+';

MINUS: '-';

STAR: '*';

SLASH: '/';

EXCLAMATIONMARK: '!';

QUESTIONMARK: '?';

SHIFTLEFT: '<<';

SHIFTRIGHT: '>>';

ROTATELEFT: '<@';

ROTATERIGHT: '@>';

STRINGOP: '&';

//TODO: remove if not needed
LEXERPLACEHOLDER: 'meaningless text just to have a last token' ;
