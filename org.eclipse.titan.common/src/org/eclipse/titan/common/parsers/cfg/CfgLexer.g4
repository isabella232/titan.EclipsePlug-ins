lexer grammar CfgLexer;

@header {
import org.eclipse.titan.common.parsers.Interval.interval_type;
import org.eclipse.titan.common.parsers.cfg.CfgInterval.section_type;
}

@members{

	/**
	 * the LAST NON HIDDEN token
	 */
	private Token mNonHiddenToken = null;

	/**
	 * What character index in the stream did the LAST NON HIDDEN token start at?
	 */
	private int mNonHiddenTokenStartCharIndex = -1;

	/**
	 * The line on which the first character of the LAST NON HIDDEN token resides
	 */
	private int mNonHiddenTokenStartLine = -1;

	/**
	 * The character position of first character within the line
	 * of the LAST NON HIDDEN token
	 */
	private int mNonHiddenTokenStartCharPositionInLine = -1;

	/**
	 * Interval detector instance for CFG files,
	 * which can be used to set start and end of intervals
	 */
	private CfgIntervalDetector mIntervalDetector = new CfgIntervalDetector();

	/**
	 * Section intervals are the tokens between the section headers.
	 * As the last section interval is created, but no new section header is detected,
	 * it must be closed automatically when EOF is read by nextToken().
	 * This is true, if section interval closing is needed at EOF.
	 * Special case: if cfg file does not contain any sections,
	 * there is nothing to close at the end.
	 */
	private boolean mCloseLastInterval = false;

	public CfgInterval getRootInterval() {
		return (CfgInterval) mIntervalDetector.getRootInterval();
	}

	public void initRootInterval( final int aLength ) {
		mIntervalDetector.initRootInterval( aLength );
	}

	/**
	 * Sign the start of an interval
	 * @param aType interval type
	 * @param aSectionType CFG section interval type
	 */
	public void pushInterval( final interval_type aType, final section_type aSectionType ) {
		mIntervalDetector.pushInterval( _tokenStartCharIndex, _tokenStartLine, aType, aSectionType );
	}

	/**
	 * Sign the start of an interval, where interval is NOT a CFG section
	 * @param aType interval type
	 */
	public void pushInterval( final interval_type aType ) {
		pushInterval( aType, section_type.UNKNOWN );
	}

	/**
	 * Sign the start of an interval, where interval is a CFG section
	 * @param aSectionType CFG section interval type
	 */
	public void pushInterval( final section_type aSectionType ) {
		pushInterval( interval_type.NORMAL, aSectionType );
	}

	/**
	 * Sign the end of the interval, which is is the end of the last token.
	 */
	public void popInterval() {
		mIntervalDetector.popInterval( _input.index(), _interp.getLine() );
	}

	/**
	 * Sign the end of the interval, which is the last non hidden token
	 */
	public void popIntervalNonHidden() {
		mIntervalDetector.popInterval( mNonHiddenTokenStartCharIndex + mNonHiddenToken.getText().length(),
		                               mNonHiddenTokenStartLine );
	}

	@Override
	public Token nextToken() {
		final Token next = super.nextToken();
		if ( next.getChannel() == 0 ) {
			// non hidden
			mNonHiddenToken = _token;
			mNonHiddenTokenStartCharIndex = _tokenStartCharIndex;
			mNonHiddenTokenStartCharPositionInLine = _tokenStartCharPositionInLine;
			mNonHiddenTokenStartLine = _tokenStartLine;
		}
		if ( _hitEOF ) {
			if ( mCloseLastInterval ) {
				// close last section interval
				popIntervalNonHidden();
				mCloseLastInterval = false;
			}
		}
		return next;
	}

	@Override
	public void reset() {
		super.reset();
		mNonHiddenToken = null;
		mNonHiddenTokenStartCharIndex = -1;
		mNonHiddenTokenStartCharPositionInLine = -1;
		mNonHiddenTokenStartLine = -1;
	}

}

tokens {
	AND,
	ANYVALUE,
	ASSIGNMENTCHAR,
	BEGINCHAR,
	BEGINCONTROLPART,
	BEGINTESTCASE,
	BIN,
	BINMATCH,
	BINO,
	BITSTRING,
	BITSTRINGMATCH,
	BOOL,
	BS,
	CHARKeyword,
	COMMA,
	COMPLEMENTKEYWORD,
	CONCATCHAR,
	CSTR,
	DNSNAME,
	DOT,
	DOTDOT,
	ENDCHAR,
	ENDCONTROLPART,
	ENDTESTCASE,
	ERROR_VERDICT,
	FAIL_VERDICT,
	FALSE,
	FL,
	FLOAT,
	HEX,
	HEXFILTER,
	HEXMATCH,
	HEXSTRING,
	HEXSTRINGMATCH,
	HN,
	HS,
	ID,
	IDENTIFIER,
	IFPRESENTKeyword,
	INCONC_VERDICT,
	INFINITYKeyword,
	INT,
	IPV6,
	KILLTIMER,
	LENGTHKeyword,
	LOCALADDRESS,
	LOGICALOR,
	LOGICALOR,
	LPAREN,
	MACRO,
	MACRORVALUE,
	MACRO_BINARY,
	MACRO_BOOL,
	MACRO_BSTR,
	MACRO_EXP_CSTR,
	MACRO_FLOAT,
	MACRO_HOSTNAME,
	MACRO_HSTR,
	MACRO_ID,
	MACRO_INT,
	MACRO_OSTR,
	MINUS,
	MTC,
	NANKeyword,
	NO,
	NONE_VERDICT,
	NULLKeyword,
	NUMBER,
	NUMHCS,
	OBJIDKeyword,
	OCT,
	OCTETSTRING,
	OCTETSTRINGMATCH,
	OCTMATCH,
	OMITKeyword,
	OS,
	PASS_VERDICT,
	PATTERNKeyword,
	PERMUTATIONKeyword,
	PLUS,
	RPAREN,
	SEMICOLON,
	SLASH,
	SQUARECLOSE,
	SQUAREOPEN,
	STAR,
	STRING,
	STRINGOP,
	SUBSETKeyword,
	SUPERSETKeyword,
	SYSTEM,
	TCPPORT,
	TRUE,
	TTCN3IDENTIFIER,
	UNIXSOCKETS,
	YES
}

// DEFAULT MODE
// These lexer rules are used only before the first section

WS:		[ \t\r\n\f]+ -> channel(HIDDEN);

LINE_COMMENT:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> channel(HIDDEN);

BLOCK_COMMENT:	'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> channel(HIDDEN);

MAIN_CONTROLLER_SECTION:	'[MAIN_CONTROLLER]'
{	pushInterval( section_type.MAIN_CONTROLLER );
	mCloseLastInterval = true;
}	-> mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION:			'[INCLUDE]'
{	pushInterval( section_type.INCLUDE );
	mCloseLastInterval = true;
}	-> mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION:	'[ORDERED_INCLUDE]'
{	pushInterval( section_type.ORDERED_INCLUDE );
	mCloseLastInterval = true;
}	-> mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION:			'[EXECUTE]'
{	pushInterval( section_type.EXECUTE );
	mCloseLastInterval = true;
}	-> mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION:				'[DEFINE]'
{	pushInterval( section_type.DEFINE );
	mCloseLastInterval = true;
}	-> mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION:	'[EXTERNAL_COMMANDS]'
{	pushInterval( section_type.EXTERNAL_COMMANDS );
	mCloseLastInterval = true;
}	-> mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION:'[TESTPORT_PARAMETERS]'
{	pushInterval( section_type.TESTPORT_PARAMETERS );
	mCloseLastInterval = true;
}	-> mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION:				'[GROUPS]'
{	pushInterval( section_type.GROUPS );
	mCloseLastInterval = true;
}	-> mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION:	'[MODULE_PARAMETERS]'
{	pushInterval( section_type.MODULE_PARAMETERS );
	mCloseLastInterval = true;
}	-> mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION:			'[COMPONENTS]'
{	pushInterval( section_type.COMPONENTS );
	mCloseLastInterval = true;
}	-> mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION:			'[LOGGING]'
{	pushInterval( section_type.LOGGING );
	mCloseLastInterval = true;
}	-> mode(LOGGING_SECTION_MODE);
PROFILER_SECTION:			'[PROFILER]'
{	pushInterval( section_type.PROFILER );
	mCloseLastInterval = true;
}	-> mode(PROFILER_SECTION_MODE);

//main controller
mode MAIN_CONTROLLER_SECTION_MODE;
MAIN_CONTROLLER1:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION1:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION1:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION1:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION1:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION1:		'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION1:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION1: 				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION1:		'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION1:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION1:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION1:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS1:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT1:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT1:	'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);
fragment DOT1:	'.';
SEMICOLON1:		';' -> type(SEMICOLON);
PLUS1:			'+' -> type(PLUS);
MINUS1:			'-' -> type(MINUS);
STAR1:			'*' -> type(STAR);
SLASH1:			'/' -> type(SLASH);
LPAREN1:		'('
{	pushInterval( interval_type.PARAMETER );
} -> type(LPAREN);
RPAREN1:		')'
{	popInterval();
} -> type(RPAREN);

KILLTIMER1 : ( 'killtimer' | 'Killtimer' | 'killTimer' | 'KillTimer' ) -> type(KILLTIMER);
LOCALADDRESS1 : ( 'localaddress' | 'Localaddress' | 'localAddress' | 'LocalAddress') -> type(LOCALADDRESS);
NUMHCS1 : ( 'numhcs' | 'Numhcs' | 'numHCs' | 'NumHCs' ) -> type(NUMHCS);
TCPPORT1 : ( 'tcpport' | 'TCPport' | 'tcpPort' | 'TCPPort' ) -> type(TCPPORT);
UNIXSOCKETS1 : ( 'UnixSocketsEnabled' | 'UnixSocketsenabled' | 'UnixsocketsEnabled' | 'Unixsocketsenabled' | 'unixSocketsEnabled' | 'unixSocketsenabled' | 'unixsocketsEnabled' | 'unixsocketsenabled' ) -> type(UNIXSOCKETS);
ASSIGNMENTCHAR1:	':=' -> type(ASSIGNMENTCHAR);
YES1 : 				( 'yes' | 'Yes' | 'YES' ) -> type(YES);
NO1: 				( 'no' | 'No' | 'NO' ) -> type(NO);
NUMBER1 :			[0-9]+ -> type(NUMBER);
STRINGOP1:			'&'  (('='))? -> type(STRINGOP);
fragment FR_NUMBER1:[0-9];
FLOAT1:
(
	FR_NUMBER1+
	(
		'.' FR_NUMBER1+ (('E' | 'e') ('+' | '-')? FR_NUMBER1+)?
	)
|	'.' FR_NUMBER1+ (('E' | 'e') ('+' | '-')? FR_NUMBER1+)?
|	FR_NUMBER1+ ('E' | 'e') ('+' | '-')? FR_NUMBER1+
) -> type(FLOAT);
fragment FR_HOSTNAME1:
(
	'A'..'Z' | 'a'..'z' | '0'..'9' | ':')
  (
  	'A'..'Z' | 'a'..'z' | '0'..'9' | ':' | '%' | '.'
  	|	('_' | '-') ('A'..'Z' | 'a'..'z' | '0'..'9')
  )*
;
DNSNAME1:
(
  (FR_HOSTNAME1) ('/' FR_NUMBER1+)?
) -> type(DNSNAME);
fragment FR_LETTER1:	[A-Za-z];
fragment FR_TTCN3IDENTIFIER1:	FR_LETTER1 (FR_LETTER1 | FR_NUMBER1+ | '_')*;
TTCN3IDENTIFIER1:	FR_LETTER1 (FR_LETTER1 | FR_NUMBER1+ | '_')* -> type(TTCN3IDENTIFIER);
HN1:					'hostname' -> type(HN);
MACRO_HOSTNAME1: 		'$' '{' (WS1)? FR_TTCN3IDENTIFIER10 (WS1)? ',' (WS1)?  HN1 (WS1)? '}' -> type(MACRO_HOSTNAME);
INT1:					'integer' -> type(INT);
MACRO_INT1:				'$' '{' (WS1)? FR_TTCN3IDENTIFIER1 (WS1)? ',' (WS1)? INT1 (WS1)? '}' -> type(MACRO_INT);
MACRO1:
(
	'$' FR_TTCN3IDENTIFIER1
|	'$' '{' (WS1)? FR_TTCN3IDENTIFIER1 (WS1)? '}'
) -> type(MACRO);
CSTR1: 					'charstring' -> type(CSTR);
MACRO_EXP_CSTR1:		'$' '{' (WS1)? FR_TTCN3IDENTIFIER1 (WS1)? ',' (WS1)? CSTR1 (WS1)? '}' -> type(MACRO_EXP_CSTR);
STRING1:				'"' .*? '"' -> type(STRING);
FL1:					'float' -> type(FL);
MACRO_FLOAT1:			'$' '{' (WS1)? FR_TTCN3IDENTIFIER1 (WS1)? ',' (WS1)? FL1 (WS1)? '}' -> type(MACRO_FLOAT);

//include section
mode INCLUDE_SECTION_MODE;
MAIN_CONTROLLER2:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION2:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION2:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION2:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION2:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION2:		'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION2:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION2:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION2:		'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION2:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION2:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION2:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS2:	[ \t\r\n\f]+  -> type(WS),channel(HIDDEN);
LINE_COMMENT2:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT2:	'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);

//same as STRING, but it is handled differently
INCLUDE_FILENAME:		'"' .*? '"';

//execute section
mode EXECUTE_SECTION_MODE;
MAIN_CONTROLLER3:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION3:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION3:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION3:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION3:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION3:		'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION3:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION3:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION3:		'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION3:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION3:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION3:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS3:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT3:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT3:		'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);
SEMICOLON3:			';' -> type(SEMICOLON);
DOT3:				'.' -> type(DOT);
STAR3:				'*' -> type(STAR);
fragment LETTER3:	[A-Z|a-z];
fragment NUMBER3:	[0-9];
TTCN3IDENTIFIER3:	LETTER3 (LETTER3 | NUMBER3 | '_')* -> type(TTCN3IDENTIFIER);

//ordered include section
mode ORDERED_INCLUDE_SECTION_MODE;
MAIN_CONTROLLER4:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION4:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION4:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION4:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION4:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION4:		'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION4:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION4:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION4:		'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION4:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION4:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION4:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS4:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT4:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT4:	'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);

//same as STRING, but it is handled differently
ORDERED_INCLUDE_FILENAME:		'"' .*? '"';

// define section
mode DEFINE_SECTION_MODE;
MAIN_CONTROLLER5:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION5:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION5:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION5:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION5:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION5:		'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION5:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION5:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION5:		'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION5:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION5:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION5:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS5:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT5:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT5:	'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);
IPV6_5:
  ( 'A'..'F' | 'a'..'f' | '0'..'9' )*
  ':'
  ( 'A'..'F' | 'a'..'f' | '0'..'9' | ':' )+
  (
    ( '0'..'9' )
    ( '0'..'9' | '.' )*
  )?
  ( '%' ( 'A'..'Z' | 'a'..'z' | '0'..'9' )+ )?
  ( '/' ( '0'..'9' )+ )?
 -> type(IPV6);

fragment LETTER5:	[A-Z|a-z];
fragment NUMBER5:	[0-9];
fragment FR_TTCN3IDENTIFIER5:	LETTER5 (LETTER5 | NUMBER5 | '_')*;
TTCN3IDENTIFIER5:	FR_TTCN3IDENTIFIER5 -> type(TTCN3IDENTIFIER);
BEGINCHAR5:			'{'
{	pushInterval( interval_type.NORMAL );
} -> type(BEGINCHAR);
ENDCHAR5:			'}'
{	popInterval();
} -> type(ENDCHAR);
MACRORVALUE5:		[0-9|A-Z|a-z|.|_|-]+ -> type(MACRORVALUE);
ASSIGNMENTCHAR5:	':'? '=' -> type(ASSIGNMENTCHAR);
fragment ESCAPE_WO_QUOTE5 :	'\\' (  '\\' | '\'' | '?' | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' );
fragment ESCAPE5 :	'\\' (  '\\' | '\'' | '"' | '?' | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' );
STRING5:
(
	'"'
	(	ESCAPE5
	|	~('"' | '\n' | '\r')
	|	'\n'
	|	'\r'
	|	'\r\n'
	)*
	'"'
|
	'\\"'
	(	ESCAPE_WO_QUOTE5
	|	~( '\\' | '"' )
	)*
	'\\"'
) -> type(STRING);

ID5:				'identifier' -> type(ID);
MACRO_ID5:			'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)?  ID5 (WS5)? '}' -> type(MACRO_ID);
INT5:				'integer' -> type(INT);
MACRO_INT5:			'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)? INT5 (WS5)? '}' -> type(MACRO_INT);
BOOL5:				'boolean' -> type(BOOL);
MACRO_BOOL5:		'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)? BOOL5 (WS5)? '}' -> type(MACRO_BOOL);
FL5:				'float' -> type(FL);
MACRO_FLOAT5:		'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)? FL5 (WS5)? '}' -> type(MACRO_FLOAT);
CSTR5:				'charstring' -> type(CSTR);
MACRO_EXP_CSTR5:	'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)? CSTR5 (WS5)? '}' -> type(MACRO_EXP_CSTR);
BS5:				'bitstring' -> type(BS);
MACRO_BSTR5:		'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)? BS5 (WS5)? '}' -> type(MACRO_BSTR);
HS5:				'hexstring' -> type(HS);
MACRO_HSTR5:		'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)? HS5 (WS5)? '}' -> type(MACRO_HSTR);
OS5:				'octetstring' -> type(OS);
MACRO_OSTR5:		'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)? OS5 (WS5)? '}' -> type(MACRO_OSTR);
BINO5:				'binaryoctet' -> type(BINO);
MACRO_BINARY5:		'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)? BINO5 (WS5)? '}' -> type(MACRO_BINARY);
HN5:				'hostname' -> type(HN);
MACRO_HOSTNAME5: 	'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? ',' (WS5)?  HN5 (WS5)? '}' -> type(MACRO_HOSTNAME);
MACRO5:
(
	'$' FR_TTCN3IDENTIFIER5
|	'$' '{' (WS5)? FR_TTCN3IDENTIFIER5 (WS5)? '}'
) -> type(MACRO);
BIN5:				[01] -> type(BIN);
BITSTRING5:			'\'' (BIN5)* '\'' 'B' -> type(BITSTRING);
HEX5:				[0-9A-Fa-f] -> type(HEX);
HEXSTRING5:			'\'' (HEX5)* '\'' 'H' -> type(HEXSTRING);
OCT5:				HEX5 HEX5 -> type(OCT);
OCTETSTRING5:		'\'' (OCT5)* '\'' 'O' -> type(OCTETSTRING);
BINMATCH5:			( BIN5 | '?' | '*' ) -> type(BINMATCH);
BITSTRINGMATCH5:	'\'' (BINMATCH5)* '\'' 'B' -> type(BITSTRINGMATCH);
HEXMATCH5:			( HEX5 | '?' | '*' ) -> type(HEXMATCH);
HEXSTRINGMATCH5:	'\'' (HEXMATCH5)* '\'' 'H' -> type(HEXSTRINGMATCH);
OCTMATCH5:			( OCT5 | '?' | '*' ) -> type(OCTMATCH);
OCTETSTRINGMATCH5:	'\'' (OCTMATCH5)* '\'' 'O' -> type(OCTETSTRINGMATCH);


//external command section
mode EXTERNAL_COMMANDS_SECTION_MODE;
MAIN_CONTROLLER6:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION6:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION6:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION6:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION6:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION6:		'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION6:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION6:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION6:		'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION6:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION6:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION6:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS6:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT6:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT6:		'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);
SEMICOLON6: 		';' -> type(SEMICOLON);
ASSIGNMENTCHAR6:	':=' -> type(ASSIGNMENTCHAR);
STRING6:			'"' .*? '"' -> type(STRING);
STRINGOP6:			'&' -> type(STRINGOP);
BEGINCONTROLPART6:	( 'begincontrolpart' | 'Begincontrolpart' | 'beginControlpart' | 'BeginControlpart'
| 'begincontrolPart' | 'BeginControlPart' | 'beginControlPart' | 'BegincontrolPart' ) -> type(BEGINCONTROLPART);
ENDCONTROLPART6:	( 'endcontrolpart' | 'Endcontrolpart' | 'endControlpart' | 'EndControlpart'
| 'endcontrolPart' | 'EndControlPart' | 'endControlPart' | 'EndcontrolPart' ) -> type(ENDCONTROLPART);
BEGINTESTCASE6:		( 'begintestcase' | 'Begintestcase' | 'beginTestcase' | 'BeginTestcase' | 'begintestCase'
| 'BeginTestCase' | 'beginTestCase' | 'BegintestCase' ) -> type(BEGINTESTCASE);
ENDTESTCASE6:		( 'endtestcase' | 'Endtestcase' | 'endTestcase' | 'EndTestcase' | 'endtestCase'
| 'EndTestCase' | 'endTestCase' | 'EndtestCase' ) -> type(ENDTESTCASE);

fragment FR_LETTER6:	[A-Za-z];
fragment FR_NUMBER6:	[0-9];
fragment FR_DOT6:		'.';
fragment FR_TTCN3IDENTIFIER6:	FR_LETTER6 (FR_LETTER6 | FR_NUMBER6 | '_')*;
CSTR6:				'charstring' -> type(CSTR);
MACRO_EXP_CSTR6:	'$' '{' (WS6)? FR_TTCN3IDENTIFIER6 (WS6)? ',' (WS6)? CSTR6 (WS6)? '}' -> type(MACRO_EXP_CSTR);
MACRO6:
(
	'$' FR_TTCN3IDENTIFIER6
|	'$' '{' (WS6)? FR_TTCN3IDENTIFIER6 (WS6)? '}'
) -> type(MACRO);

//testport parameters
mode TESTPORT_PARAMETERS_SECTION_MODE;
MAIN_CONTROLLER7:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION7:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION7:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION7:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION7:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION7:		'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION7:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION7:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION7:		'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION7:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION7:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION7:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS7:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT7:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT7:		'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);
fragment FR_LETTER7:	[A-Za-z];
fragment FR_NUMBER7:	[0-9];
fragment FR_DOT7:		'.';
fragment FR_TTCN3IDENTIFIER7:	FR_LETTER7 (FR_LETTER7 | FR_NUMBER7 | '_')*;
TTCN3IDENTIFIER7:	FR_LETTER7 (FR_LETTER7 | FR_NUMBER7 | '_')* -> type(TTCN3IDENTIFIER);
STAR7:				'*' -> type(STAR);
PLUS7:				'+' -> type(PLUS);
MINUS7:				'-' -> type(MINUS);
SLASH7:				'/' -> type(SLASH);
SQUAREOPEN7:		'['
{	pushInterval( interval_type.INDEX );
} -> type(SQUAREOPEN);
SQUARECLOSE7:		']'
{	popInterval();
} -> type(SQUARECLOSE);
NUMBER7 :			[0-9]+ -> type(NUMBER);
SEMICOLON7:			';' -> type(SEMICOLON);
DOT7:				'.' -> type(DOT);
ASSIGNMENTCHAR7:	':=' -> type(ASSIGNMENTCHAR);
LPAREN7:			'('
{	pushInterval( interval_type.PARAMETER );
} -> type(LPAREN);
RPAREN7:			')'
{	popInterval();
} -> type(RPAREN);
MTC7:				'mtc' -> type(MTC);
SYSTEM7:			'system' -> type(SYSTEM);
fragment ESCAPE7 :	'\\' (  '\\' | '\'' | '"' | '?' | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' );
STRING7:
'"'
(
	ESCAPE5
|	~('"' | '\n' | '\r')
|	'\n'
|	'\r'
|	'\r\n'
)*
'"'
 -> type(STRING);
STRINGOP7:			'&'  (('='))? -> type(STRINGOP);
MACRO7:
(
	'$' FR_TTCN3IDENTIFIER7
|	'$' '{' (WS7)? FR_TTCN3IDENTIFIER7 (WS7)? '}'
) -> type(MACRO);
ID7: 'identifier' -> type(ID);
INT7: 'integer' -> type(INT);
CSTR7: 'charstring' -> type(CSTR);
MACRO_INT7:		'$' '{' (WS7)? FR_TTCN3IDENTIFIER7 (WS7)? ',' (WS7)? INT7 (WS7)? '}' -> type(MACRO_INT);
MACRO_ID7:		'$' '{' (WS7)? FR_TTCN3IDENTIFIER7 (WS7)? ',' (WS7)?  ID7 (WS7)? '}' -> type(MACRO_ID);
MACRO_EXP_CSTR7:'$' '{' (WS7)? FR_TTCN3IDENTIFIER7 (WS7)? ',' (WS7)? CSTR7 (WS7)? '}' -> type(MACRO_EXP_CSTR);

//groups parameters
mode GROUPS_SECTION_MODE;
MAIN_CONTROLLER8:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION8:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION8:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION8:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION8:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION8:		'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION8:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION8:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION8:		'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION8:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION8:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION8:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS8:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT8:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT8:		'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);
fragment FR_LETTER8:	[A-Za-z];
fragment FR_NUMBER8:	[0-9];
fragment FR_DOT8:		'.';
fragment FR_TTCN3IDENTIFIER8:	FR_LETTER8 (FR_LETTER8 | FR_NUMBER8+ | '_')*;
TTCN3IDENTIFIER8:	FR_LETTER8 (FR_LETTER8 | FR_NUMBER8+ | '_')* -> type(TTCN3IDENTIFIER);
SEMICOLON8:			';' -> type(SEMICOLON);
ASSIGNMENTCHAR8:	':=' -> type(ASSIGNMENTCHAR);
STAR8:				'*' -> type(STAR);
COMMA8:				',' -> type(COMMA);
NUMBER8:			[0-9]+ -> type(NUMBER);
FLOAT8:
(
	FR_NUMBER8+
	(
		'.' FR_NUMBER8+ (('E' | 'e') ('+' | '-')? FR_NUMBER8+)?
	)
|	'.' FR_NUMBER8+ (('E' | 'e') ('+' | '-')? FR_NUMBER8+)?
|	FR_NUMBER8+ ('E' | 'e') ('+' | '-')? FR_NUMBER8+
) -> type(FLOAT);
fragment FR_HOSTNAME8:
(
	'A'..'Z' | 'a'..'z' | '0'..'9' | ':')
  (
  	'A'..'Z' | 'a'..'z' | '0'..'9' | ':' | '%' | '.'
  	|	('_' | '-') ('A'..'Z' | 'a'..'z' | '0'..'9')
  )*
;
DNSNAME8:
(
  (FR_HOSTNAME8) ('/' FR_NUMBER8+)?
) -> type(DNSNAME);
ID8: 'identifier' -> type(ID);
MACRO_ID8:		'$' '{' (WS8)? FR_TTCN3IDENTIFIER8 (WS8)? ',' (WS8)?  ID8 (WS8)? '}' -> type(MACRO_ID);

//module parameters
mode MODULE_PARAMETERS_SECTION_MODE;
MAIN_CONTROLLER9:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION9:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION9:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION9:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION9:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION9:		'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION9:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION9:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION9:		'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION9:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION9:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION9:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS9:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT9:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT9:		'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);
SEMICOLON9:			';' -> type(SEMICOLON);
ASSIGNMENTCHAR9:	':'? '=' -> type(ASSIGNMENTCHAR);
CONCATCHAR9:		'&=' -> type(CONCATCHAR);
DOT9:				'.' -> type(DOT);
STAR9:				'*' -> type(STAR);
LPAREN9:			'('
{	pushInterval( interval_type.PARAMETER );	} -> type(LPAREN);
RPAREN9:			')'
{	popInterval();	} -> type(RPAREN);
DOTDOT9:			'..' -> type(DOTDOT);
PLUS9:				'+' -> type(PLUS);
MINUS9:				'-' -> type(MINUS);
SLASH9:				'/' -> type(SLASH);
BEGINCHAR9:			'{'
{	pushInterval( interval_type.NORMAL );	} -> type(BEGINCHAR);
ENDCHAR9:			'}'
{	popInterval();	} -> type(ENDCHAR);
STRINGOP9:			'&' -> type(STRINGOP);
COMMA9:				',' -> type(COMMA);
SQUAREOPEN9:		'['
{	pushInterval( interval_type.INDEX );	} -> type(SQUAREOPEN);
SQUARECLOSE9:		']'
{	popInterval();	} -> type(SQUARECLOSE);
AND9:				'&' -> type(AND);
fragment ESCAPE9 :	'\\' (  '\\' | '\'' | '"' | '?' | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' );

NONE_VERDICT9:		'none' -> type(NONE_VERDICT);
PASS_VERDICT9:		'pass' -> type(PASS_VERDICT);
INCONC_VERDICT9:	'inconc' -> type(INCONC_VERDICT);
FAIL_VERDICT9:		'fail' -> type(FAIL_VERDICT);
ERROR_VERDICT9:		'error' -> type(ERROR_VERDICT);
CHARKeyword9:		'char' -> type(CHARKeyword);
OBJIDKeyword9:		'objid' -> type(OBJIDKeyword);
OMITKeyword9:		'omit' -> type(OMITKeyword);
NULLKeyword9:		( 'null' | 'NULL' ) -> type(NULLKeyword);
MTCKeyword9:		'mtc' -> type(MTCKeyword);
SYSTEMKeyword9:		'system' -> type(SYSTEMKeyword);
INFINITYKeyword9:	'infinity' -> type(INFINITYKeyword);
NANKeyword9:		'not_a_number' -> type(NANKeyword);
IFPRESENTKeyword9:	'ifpresent' -> type(IFPRESENTKeyword);
LENGTHKeyword9:		'length' -> type(LENGTHKeyword);
COMPLEMENTKEYWORD9:	'complement' -> type(COMPLEMENTKEYWORD);
PATTERNKeyword9:	'pattern' -> type(PATTERNKeyword);
PERMUTATIONKeyword9:'permutation' -> type(PERMUTATIONKeyword);
SUPERSETKeyword9:	'superset' -> type(SUPERSETKeyword);
SUBSETKeyword9:		'subset' -> type(SUBSETKeyword);
TRUE9:				'true' -> type(TRUE);
FALSE9:				'false' -> type(FALSE);
ANYVALUE9:			'?' -> type(ANYVALUE);
fragment FR_LETTER9:[A-Za-z];
fragment FR_NUMBER9:[0-9];
fragment FR_TTCN3IDENTIFIER9:	FR_LETTER9 (FR_LETTER9 | FR_NUMBER9+ | '_')*;
TTCN3IDENTIFIER9:	FR_TTCN3IDENTIFIER9 -> type(TTCN3IDENTIFIER);
ID9:				'identifier' -> type(ID);
MACRO_ID9:			'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? ',' (WS9)?  ID9 (WS9)? '}' -> type(MACRO_ID);
NUMBER9 :			[0-9]+ -> type(NUMBER);
INT9:				'integer' -> type(INT);
MACRO_INT9:			'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? ',' (WS9)? INT9 (WS9)? '}' -> type(MACRO_INT);
BOOL9:				'boolean' -> type(BOOL);
MACRO_BOOL9:		'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? ',' (WS9)? BOOL9 (WS9)? '}' -> type(MACRO_BOOL);
FL9:				'float' -> type(FL);
MACRO_FLOAT9:		'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? ',' (WS9)? FL9 (WS9)? '}' -> type(MACRO_FLOAT);
CSTR9:				'charstring' -> type(CSTR);
MACRO_EXP_CSTR9:	'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? ',' (WS9)? CSTR9 (WS9)? '}' -> type(MACRO_EXP_CSTR);

FLOAT9:
(
	FR_NUMBER9+
	(
		'.' FR_NUMBER9+ (('E' | 'e') ('+' | '-')? FR_NUMBER9+)?
	)
|	'.' FR_NUMBER9+ (('E' | 'e') ('+' | '-')? FR_NUMBER9+)?
|	FR_NUMBER9+ ('E' | 'e') ('+' | '-')? FR_NUMBER9+
) -> type(FLOAT);
BIN9:				[01] -> type(BIN);
BITSTRING9:			'\'' (BIN9)* '\'' 'B' -> type(BITSTRING);
BS9:				'bitstring' -> type(BS);
MACRO_BSTR9:		'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? ',' (WS9)? BS9 (WS9)? '}' -> type(MACRO_BSTR);
HEX9:				[0-9A-Fa-f] -> type(HEX);
HEXSTRING9:			'\'' (HEX9)* '\'' 'H' -> type(HEXSTRING);
HS9:				'hexstring' -> type(HS);
MACRO_HSTR9:		'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? ',' (WS9)? HS9 (WS9)? '}' -> type(MACRO_HSTR);
OCT9:				HEX9 HEX9 -> type(OCT);
OCTETSTRING9:		'\'' (OCT9)* '\'' 'O' -> type(OCTETSTRING);
OS9:				'octetstring' -> type(OS);
MACRO_OSTR9:		'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? ',' (WS9)? OS9 (WS9)? '}' -> type(MACRO_OSTR);
BINO9:				'binaryoctet' -> type(BINO);
MACRO_BINARY9:		'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? ',' (WS9)? BINO9 (WS9)? '}' -> type(MACRO_BINARY);
BINMATCH9:			( BIN9 | '?' | '*' ) -> type(BINMATCH);
BITSTRINGMATCH9:	'\'' (BINMATCH9)* '\'' 'B' -> type(BITSTRINGMATCH);
HEXMATCH9:			( HEX9 | '?' | '*' ) -> type(HEXMATCH);
HEXSTRINGMATCH9:	'\'' (HEXMATCH9)* '\'' 'H' -> type(HEXSTRINGMATCH);
OCTMATCH9:			( OCT9 | '?' | '*' ) -> type(OCTMATCH);
OCTETSTRINGMATCH9:	'\'' (OCTMATCH9)* '\'' 'O' -> type(OCTETSTRINGMATCH);
MACRO9:
(
	'$' FR_TTCN3IDENTIFIER9
|	'$' '{' (WS9)? FR_TTCN3IDENTIFIER9 (WS9)? '}'
) -> type(MACRO);
//STRING9:			'"' .*? '"' -> type(STRING);
STRING9:
'"'
(
	ESCAPE9
|	~('"' | '\n' | '\r')
|	'\n'
|	'\r'
|	'\r\n'
)*
'"'
 -> type(STRING);

//components section
mode COMPONENTS_SECTION_MODE;
MAIN_CONTROLLER10:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION10:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION10:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION10:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION10:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION10:	'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION10:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION10:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION10:	'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION10:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION10:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION10:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS10:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT10:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT10:		'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);
SEMICOLON10:			';' -> type(SEMICOLON);
STAR10:					'*' -> type(STAR);
ASSIGNMENTCHAR10:		':=' -> type(ASSIGNMENTCHAR);
fragment FR_LETTER10:	[A-Za-z];
fragment FR_NUMBER10:	[0-9];
fragment FR_TTCN3IDENTIFIER10:	FR_LETTER10 (FR_LETTER10 | FR_NUMBER10+ | '_')*;

IPV6_10:
  ( 'A'..'F' | 'a'..'f' | '0'..'9' )*
  ':'
  ( 'A'..'F' | 'a'..'f' | '0'..'9' | ':' )+
  (
    ( '0'..'9' )
    ( '0'..'9' | '.' )*
  )?
  ( '%' ( 'A'..'Z' | 'a'..'z' | '0'..'9' )+ )?
  ( '/' ( '0'..'9' )+ )?
 -> type(IPV6);

NUMBER10:				[0-9]+ -> type(NUMBER);
FLOAT10:
(
	FR_NUMBER10+
	(
		'.' FR_NUMBER10+ (('E' | 'e') ('+' | '-')? FR_NUMBER10+)?
	)
|	'.' FR_NUMBER10+ (('E' | 'e') ('+' | '-')? FR_NUMBER10+)?
|	FR_NUMBER10+ ('E' | 'e') ('+' | '-')? FR_NUMBER10+
) -> type(FLOAT);

TTCN3IDENTIFIER10:	FR_LETTER10 (FR_LETTER10 | FR_NUMBER10+ | '_')* -> type(TTCN3IDENTIFIER);
fragment FR_HOSTNAME10:
(
	'A'..'Z' | 'a'..'z' | '0'..'9' | ':')
  (
  	'A'..'Z' | 'a'..'z' | '0'..'9' | ':' | '%' | '.'
  	|	('_' | '-') ('A'..'Z' | 'a'..'z' | '0'..'9')
  )*
;

DNSNAME10:
(
  (FR_HOSTNAME10) ('/' FR_NUMBER10+)?
) -> type(DNSNAME);
ID10:					'identifier' -> type(ID);
MACRO_ID10:				'$' '{' (WS10)? FR_TTCN3IDENTIFIER10 (WS10)? ',' (WS10)?  ID10 (WS10)? '}' -> type(MACRO_ID);
HN10:					'hostname' -> type(HN);
MACRO_HOSTNAME10: 		'$' '{' (WS10)? FR_TTCN3IDENTIFIER10 (WS10)? ',' (WS10)?  HN10 (WS10)? '}' -> type(MACRO_HOSTNAME);
MACRO10:
(
	'$' FR_TTCN3IDENTIFIER10
|	'$' '{' (WS10)? FR_TTCN3IDENTIFIER10 (WS10)? '}'
) -> type(MACRO);

//logging section
mode LOGGING_SECTION_MODE;
MAIN_CONTROLLER11:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION11:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION11:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION11:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION11:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION11:	'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION11:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION11:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION11:	'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION11:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION11:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION11:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS11:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT11:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT11:		'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);

TTCN_EXECUTOR1:		'TTCN_EXECUTOR';	TTCN_ERROR1:	'TTCN_ERROR';		TTCN_WARNING1:		'TTCN_WARNING';
TTCN_PORTEVENT1:	'TTCN_PORTEVENT';	TTCN_TIMEROP1:	'TTCN_TIMEROP';		TTCN_VERDICTOP1:	'TTCN_VERDICTOP';
TTCN_DEFAULTOP1:	'TTCN_DEFAULTOP';	TTCN_ACTION1:	'TTCN_ACTION';		TTCN_TESTCASE1:		'TTCN_TESTCASE';
TTCN_FUNCTION1:		'TTCN_FUNCTION';	TTCN_USER1:		'TTCN_USER';		TTCN_STATISTICS1:	'TTCN_STATISTICS';
TTCN_PARALLEL1:		'TTCN_PARALLEL';	TTCN_MATCHING1:	'TTCN_MATCHING';	TTCN_DEBUG1:	 	'TTCN_DEBUG';

TTCN_EXECUTOR2:		'EXECUTOR';		TTCN_ERROR2:	'ERROR';		TTCN_WARNING2:		'WARNING';
TTCN_PORTEVENT2:	'PORTEVENT';	TTCN_TIMEROP2:	'TIMEROP';		TTCN_VERDICTOP2:	'VERDICTOP';
TTCN_DEFAULTOP2:	'DEFAULTOP';	TTCN_ACTION2:	'ACTION';		TTCN_TESTCASE2:		'TESTCASE';
TTCN_FUNCTION2:		'FUNCTION';		TTCN_USER2:		'USER';			TTCN_STATISTICS2:	'STATISTICS';
TTCN_PARALLEL2:		'PARALLEL';		TTCN_MATCHING2:	'MATCHING';		TTCN_DEBUG2:		'DEBUG';
LOG_ALL: 'LOG_ALL';	LOG_NOTHING: 'LOG_NOTHING';

/* loggingbit second level*/
ACTION_UNQUALIFIED: 'ACTION_UNQUALIFIED'; DEBUG_ENCDEC: 'DEBUG_ENCDEC';
DEBUG_TESTPORT: 'DEBUG_TESTPORT'; DEBUG_UNQUALIFIED: 'DEBUG_UNQUALIFIED';
DEFAULTOP_ACTIVATE: 'DEFAULTOP_ACTIVATE'; DEFAULTOP_DEACTIVATE: 'DEFAULTOP_DEACTIVATE';
DEFAULTOP_EXIT: 'DEFAULTOP_EXIT'; DEFAULTOP_UNQUALIFIED: 'DEFAULTOP_UNQUALIFIED';
ERROR_UNQUALIFIED: 'ERROR_UNQUALIFIED'; EXECUTOR_COMPONENT: 'EXECUTOR_COMPONENT';
EXECUTOR_CONFIGDATA: 'EXECUTOR_CONFIGDATA'; EXECUTOR_EXTCOMMAND: 'EXECUTOR_EXTCOMMAND';
EXECUTOR_LOGOPTIONS: 'EXECUTOR_LOGOPTIONS'; EXECUTOR_RUNTIME: 'EXECUTOR_RUNTIME';
EXECUTOR_UNQUALIFIED: 'EXECUTOR_UNQUALIFIED'; FUNCTION_RND: 'FUNCTION_RND';
FUNCTION_UNQUALIFIED: 'FUNCTION_UNQUALIFIED'; MATCHING_DONE: 'MATCHING_DONE';
MATCHING_MCSUCCESS: 'MATCHING_MCSUCCESS'; MATCHING_MCUNSUCC: 'MATCHING_MCUNSUCC';
MATCHING_MMSUCCESS: 'MATCHING_MMSUCCESS'; MATCHING_MMUNSUCC: 'MATCHING_MMUNSUCC';
MATCHING_PCSUCCESS: 'MATCHING_PCSUCCESS'; MATCHING_PCUNSUCC: 'MATCHING_PCUNSUCC';
MATCHING_PMSUCCESS: 'MATCHING_PMSUCCESS'; MATCHING_PMUNSUCC: 'MATCHING_PMUNSUCC';
MATCHING_PROBLEM: 'MATCHING_PROBLEM'; MATCHING_TIMEOUT: 'MATCHING_TIMEOUT';
MATCHING_UNQUALIFIED: 'MATCHING_UNQUALIFIED'; PARALLEL_PORTCONN: 'PARALLEL_PORTCONN';
PARALLEL_PORTMAP: 'PARALLEL_PORTMAP'; PARALLEL_PTC: 'PARALLEL_PTC';
PARALLEL_UNQUALIFIED: 'PARALLEL_UNQUALIFIED'; PORTEVENT_DUALRECV: 'PORTEVENT_DUALRECV';
PORTEVENT_DUALSEND: 'PORTEVENT_DUALSEND'; PORTEVENT_MCRECV: 'PORTEVENT_MCRECV';
PORTEVENT_MCSEND: 'PORTEVENT_MCSEND'; PORTEVENT_MMRECV: 'PORTEVENT_MMRECV';
PORTEVENT_MMSEND: 'PORTEVENT_MMSEND'; PORTEVENT_MQUEUE: 'PORTEVENT_MQUEUE';
PORTEVENT_PCIN: 'PORTEVENT_PCIN'; PORTEVENT_PCOUT: 'PORTEVENT_PCOUT';
PORTEVENT_PMIN: 'PORTEVENT_PMIN'; PORTEVENT_PMOUT: 'PORTEVENT_PMOUT';
PORTEVENT_PQUEUE: 'PORTEVENT_PQUEUE'; PORTEVENT_STATE: 'PORTEVENT_STATE';
PORTEVENT_UNQUALIFIED: 'PORTEVENT_UNQUALIFIED'; STATISTICS_UNQUALIFIED: 'STATISTICS_UNQUALIFIED';
STATISTICS_VERDICT: 'STATISTICS_VERDICT'; TESTCASE_FINISH: 'TESTCASE_FINISH';
TESTCASE_START: 'TESTCASE_START'; TESTCASE_UNQUALIFIED: 'TESTCASE_UNQUALIFIED';
TIMEROP_GUARD: 'TIMEROP_GUARD'; TIMEROP_READ: 'TIMEROP_READ';
TIMEROP_START: 'TIMEROP_START'; TIMEROP_STOP: 'TIMEROP_STOP';
TIMEROP_TIMEOUT: 'TIMEROP_TIMEOUT'; TIMEROP_UNQUALIFIED: 'TIMEROP_UNQUALIFIED';
USER_UNQUALIFIED: 'USER_UNQUALIFIED'; VERDICTOP_FINAL: 'VERDICTOP_FINAL';
VERDICTOP_GETVERDICT: 'VERDICTOP_GETVERDICT'; VERDICTOP_SETVERDICT: 'VERDICTOP_SETVERDICT';
VERDICTOP_UNQUALIFIED: 'VERDICTOP_UNQUALIFIED'; WARNING_UNQUALIFIED: 'WARNING_UNQUALIFIED';

COMPACT: 'Compact' | 'compact';
DETAILED: 'Detailed' | 'detailed';
SUBCATEGORIES: 'SubCategories' | 'Subcategories' | 'subCategories' | 'subcategories';
MTCKeyword: 'mtc';  SYSTEMKeyword: 'system';
LOGGERPLUGINS: 'LoggerPlugins' | 'Loggerplugins' | 'loggerPlugins' | 'loggerplugins';

APPENDFILE: 'appendfile' | 'Appendfile' | 'appendFile' | 'AppendFile';
CONSOLEMASK: 'consolemask' | 'Consolemask' | 'consoleMask' | 'ConsoleMask';
DISKFULLACTION: 'diskfullaction' | 'diskfullAction' | 'diskFullaction' | 'diskFullAction' | 'Diskfullaction' | 'DiskfullAction' | 'DiskFullaction' | 'DiskFullAction';
DISKFULLACTIONVALUE: 'error' | 'Error' | 'stop' | 'Stop' | 'delete' | 'Delete';
DISKFULLACTIONVALUERETRY: 'retry' | 'Retry';
FILEMASK: 'filemask' | 'Filemask' | 'fileMask' | 'FileMask';
LOGFILENAME: 'filename' | 'Filename' | 'fileName' | 'FileName' | 'logfile' | 'Logfile' |'logFile' | 'LogFile';
EMERGENCYLOGGING: 'EmergencyLogging' | 'Emergencylogging' | 'emergencylogging' | 'emergencyLogging';
EMERGENCYLOGGINGBEHAVIOUR: 'EmergencyLoggingBehaviour' | 'EmergencyLoggingbehaviour' | 'Emergencyloggingbehaviour' | 'emergencyLoggingBehaviour' | 'emergencyloggingBehaviour'
| 'emergencyloggingbehaviour' | 'emergencyLogginglbehaviour' | 'EmergencyloggingBehaviour';
EMERGENCYLOGGINGMASK: 'EmergencyLoggingMask' | 'EmergencyLoggingmask' | 'Emergencyloggingmask' | 'emergencyLoggingMask' | 'emergencyloggingMask'
| 'emergencyloggingmask' | 'emergencyLoggingmask' | 'EmergencyloggingMask';
BUFFERALLORBUFFERMASKED: 'BufferAll' | 'Bufferall' | 'bufferAll' | 'bufferall' | 'BufferMasked' | 'Buffermasked' | 'bufferMasked' |'buffermasked';
LOGENTITYNAME: 'logentityname' | 'Logentityname' | 'logEntityname' | 'LogEntityname' | 'logentityName' | 'LogentityName' | 'logEntityName' | 'LogEntityName';
LOGEVENTTYPES: 'logeventtypes' | 'Logeventtypes' | 'logEventtypes' | 'LogEventtypes' | 'logeventTypes' | 'LogEventTypes' | 'logEventTypes' | 'LogeventTypes';
LOGFILENUMBER: 'logfilenumber' | 'logfileNumber' | 'logFilenumber' | 'logFileNumber' | 'Logfilenumber' | 'LogfileNumber' | 'LogFilenumber' | 'LogFileNumber';
LOGFILESIZE: 'logfilesize' | 'logfileSize' | 'logFilesize' | 'logFileSize' | 'Logfilesize' | 'LogfileSize' | 'LogFilesize' | 'LogFileSize';
MATCHINGHINTS: 'matchinghints' | 'Matchinghints' | 'matchingHints' | 'MatchingHints';
SOURCEINFOFORMAT: 'logsourceinfo' | 'Logsourceinfo' | 'logSourceinfo' | 'LogSourceinfo' | 'logsourceInfo' | 'LogsourceInfo' | 'logSourceInfo' | 'LogSourceInfo'
| 'sourceinfoformat' | 'Sourceinfoformat' | 'sourceInfoformat' | 'SourceInfoformat' | 'sourceinfoFormat' | 'SourceinfoFormat' | 'sourceInfoFormat' | 'SourceInfoFormat';
SOURCEINFOVALUE: 'none' | 'None' | 'NONE' | 'single' | 'Single' | 'SINGLE' | 'stack' | 'Stack' | 'STACK';
TIMESTAMPFORMAT: 'timestampformat' | 'Timestampformat' | 'timeStampformat' | 'TimeStampformat' | 'timestampFormat' | 'TimestampFormat' | 'timeStampFormat' | 'TimeStampFormat';
CONSOLETIMESTAMPFORMAT: 'consoletimestampformat' | 'Consoletimestampformat' | 'ConsoleTimestampformat' | 'ConsoleTimeStampformat' |
'ConsoleTimeStampFormat' | 'consoleTimestampformat' | 'consoleTimeStampformat' | 'consoleTimeStampFormat' | 'consoletimeStampformat' | 'consoletimestampFormat';
TIMESTAMPVALUE: 'time' | 'Time' | 'TIME' | 'datetime' | 'DateTime' | 'Datetime' | 'DATETIME' | 'seconds' | 'Seconds' | 'SECONDS';
YESNO: 'yes' | 'Yes' | 'YES' | 'no' | 'No' | 'NO';

SEMICOLON11:			';' -> type(SEMICOLON);
STAR11:					'*' -> type(STAR);
ASSIGNMENTCHAR11:		':=' -> type(ASSIGNMENTCHAR);
DOT11:					'.' -> type(DOT);
BEGINCHAR11:			'{'
{	pushInterval( interval_type.NORMAL );
} -> type(BEGINCHAR);
ENDCHAR11:				'}'
{	popInterval();
} -> type(ENDCHAR);
COMMA11:				',' -> type(COMMA);
STRINGOP11:				'&'  (('='))? -> type(STRINGOP);
LOGICALOR11:			'|' -> type(LOGICALOR);
TRUE11:					'true' -> type(TRUE);
FALSE11:				'false' -> type(FALSE);
LPAREN11:				'('
{	pushInterval( interval_type.PARAMETER );
} -> type(LPAREN);
RPAREN11:				')'
{	popInterval();
} -> type(RPAREN);


fragment FR_LETTER11:	[A-Za-z];
fragment FR_NUMBER11:	[0-9];
fragment FR_TTCN3IDENTIFIER11:	FR_LETTER11 (FR_LETTER11 | FR_NUMBER11+ | '_')*;
TTCN3IDENTIFIER11:	FR_LETTER11 (FR_LETTER11 | FR_NUMBER11+ | '_')* -> type(TTCN3IDENTIFIER);
NUMBER11:				[0-9]+ -> type(NUMBER);
FLOAT11:
(
	FR_NUMBER11+
	(
		'.' FR_NUMBER11+ (('E' | 'e') ('+' | '-')? FR_NUMBER11+)?
	)
|	'.' FR_NUMBER11+ (('E' | 'e') ('+' | '-')? FR_NUMBER11+)?
|	FR_NUMBER11+ ('E' | 'e') ('+' | '-')? FR_NUMBER11+
) -> type(FLOAT);

BOOL11:				'boolean' -> type(BOOL);
MACRO_BOOL11:		'$' '{' (WS11)? FR_TTCN3IDENTIFIER11 (WS11)? ',' (WS11)?  BOOL11 (WS11)? '}' -> type(MACRO_BOOL);
ID11:				'identifier' -> type(ID);
MACRO_ID11:			'$' '{' (WS11)? FR_TTCN3IDENTIFIER11 (WS11)? ',' (WS11)?  ID11 (WS11)? '}' -> type(MACRO_ID);
INT11:				'integer' -> type(INT);
MACRO_INT11:		'$' '{' (WS11)? FR_TTCN3IDENTIFIER11 (WS11)? ',' (WS11)? INT11 (WS11)? '}' -> type(MACRO_INT);
CSTR11:				'charstring' -> type(CSTR);
MACRO_EXP_CSTR11:	'$' '{' (WS11)? FR_TTCN3IDENTIFIER11 (WS11)? ',' (WS11)? CSTR11 (WS11)? '}' -> type(MACRO_EXP_CSTR);
MACRO11:
(
	'$' FR_TTCN3IDENTIFIER11
|	'$' '{' (WS11)? FR_TTCN3IDENTIFIER11 (WS11)? '}'
) -> type(MACRO);
STRING11:			'"' .*? '"' -> type(STRING);

//profiler section
mode PROFILER_SECTION_MODE;
MAIN_CONTROLLER12:				'[MAIN_CONTROLLER]'
{	popIntervalNonHidden();
	pushInterval( section_type.MAIN_CONTROLLER );
}	-> type(MAIN_CONTROLLER_SECTION),mode(MAIN_CONTROLLER_SECTION_MODE);
INCLUDE_SECTION12:				'[INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.INCLUDE );
}	-> type(INCLUDE_SECTION),mode(INCLUDE_SECTION_MODE);
ORDERED_INCLUDE_SECTION12:		'[ORDERED_INCLUDE]'
{	popIntervalNonHidden();
	pushInterval( section_type.ORDERED_INCLUDE );
}	-> type(ORDERED_INCLUDE_SECTION),mode(ORDERED_INCLUDE_SECTION_MODE);
EXECUTE_SECTION12:				'[EXECUTE]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXECUTE );
}	-> type(EXECUTE_SECTION),mode(EXECUTE_SECTION_MODE);
DEFINE_SECTION12:				'[DEFINE]'
{	popIntervalNonHidden();
	pushInterval( section_type.DEFINE );
}	-> type(DEFINE_SECTION),mode(DEFINE_SECTION_MODE);
EXTERNAL_COMMANDS_SECTION12:	'[EXTERNAL_COMMANDS]'
{	popIntervalNonHidden();
	pushInterval( section_type.EXTERNAL_COMMANDS );
}	-> type(EXTERNAL_COMMANDS_SECTION),mode(EXTERNAL_COMMANDS_SECTION_MODE);
TESTPORT_PARAMETERS_SECTION12:	'[TESTPORT_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.TESTPORT_PARAMETERS );
}	-> type(TESTPORT_PARAMETERS_SECTION),mode(TESTPORT_PARAMETERS_SECTION_MODE);
GROUPS_SECTION12:				'[GROUPS]'
{	popIntervalNonHidden();
	pushInterval( section_type.GROUPS );
}	-> type(GROUPS_SECTION),mode(GROUPS_SECTION_MODE);
MODULE_PARAMETERS_SECTION12:	'[MODULE_PARAMETERS]'
{	popIntervalNonHidden();
	pushInterval( section_type.MODULE_PARAMETERS );
}	-> type(MODULE_PARAMETERS_SECTION),mode(MODULE_PARAMETERS_SECTION_MODE);
COMPONENTS_SECTION12:			'[COMPONENTS]'
{	popIntervalNonHidden();
	pushInterval( section_type.COMPONENTS );
}	-> type(COMPONENTS_SECTION),mode(COMPONENTS_SECTION_MODE);
LOGGING_SECTION12:				'[LOGGING]'
{	popIntervalNonHidden();
	pushInterval( section_type.LOGGING );
}	-> type(LOGGING_SECTION),mode(LOGGING_SECTION_MODE);
PROFILER_SECTION12:				'[PROFILER]'
{	popIntervalNonHidden();
	pushInterval( section_type.PROFILER );
}	-> type(PROFILER_SECTION),mode(PROFILER_SECTION_MODE);

WS12:	[ \t\r\n\f]+ -> type(WS),channel(HIDDEN);
LINE_COMMENT12:
(
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) -> type(LINE_COMMENT),channel(HIDDEN);
BLOCK_COMMENT12:		'/*' .*? '*/'
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT),channel(HIDDEN);

CONCATCHAR12:			'&=' -> type(CONCATCHAR);
HEX12:					[0-9|A-F|a-f] -> type(HEX);
HEXFILTER12:			(HEX12)+ -> type(HEXFILTER);
SEMICOLON12:			';' -> type(SEMICOLON);
ASSIGNMENTCHAR12:		':=' -> type(ASSIGNMENTCHAR);
LOGICALOR12:			'|' -> type(LOGICALOR);
AND12:					'&' -> type(AND);
/* settings */
DISABLEPROFILER : 'DisableProfiler'; DISABLECOVERAGE : 'DisableCoverage'; DATABASEFILE : 'DatabaseFile';
AGGREGATEDATA : 'AggregateData'; STATISTICSFILE : 'StatisticsFile'; DISABLESTATISTICS : 'DisableStatistics';
STATISTICSFILTER : 'StatisticsFilter'; STARTAUTOMATICALLY : 'StartAutomatically';
NETLINETIMES : 'NetLineTimes'; NETFUNCTIONTIMES : 'NetFunctionTimes';

/* statistics filters (single) */
NUMBEROFLINES : 'NumberOfLines'; LINEDATARAW : 'LineDataRaw'; FUNCDATARAW : 'FuncDataRaw';
LINEAVGRAW : 'LineAvgRaw'; FUNCAVGRAW : 'FuncAvgRaw';
LINETIMESSORTEDBYMOD : 'LineTimesSortedByMod'; FUNCTIMESSORTEDBYMOD : 'FuncTimesSortedByMod';
LINETIMESSORTEDTOTAL : 'LineTimesSortedTotal'; FUNCTIMESSORTEDTOTAL : 'FuncTimesSortedTotal';
LINECOUNTSORTEDBYMOD : 'LineCountSortedByMod'; FUNCCOUNTSORTEDBYMOD : 'FuncCountSortedByMod';
LINECOUNTSORTEDTOTAL : 'LineCountSortedTotal'; FUNCCOUNTSORTEDTOTAL : 'FuncCountSortedTotal';
LINEAVGSORTEDBYMOD : 'LineAvgSortedByMod'; FUNCAVGSORTEDBYMOD : 'FuncAvgSortedByMod';
LINEAVGSORTEDTOTAL : 'LineAvgSortedTotal'; FUNCAVGSORTEDTOTAL : 'FuncAvgSortedTotal';
TOP10LINETIMES : 'Top10LineTimes'; TOP10FUNCTIMES : 'Top10FuncTimes';
TOP10LINECOUNT : 'Top10LineCount'; TOP10FUNCCOUNT : 'Top10FuncCount';
TOP10LINEAVG : 'Top10LineAvg'; TOP10FUNCAVG : 'Top10FuncAvg';
UNUSEDLINES : 'UnusedLines'; UNUSEDFUNC : 'UnusedFunc';

/* statistics filters (grouped) */
ALLRAWDATA : 'AllRawData';
LINEDATASORTEDBYMOD : 'LineDataSortedByMod'; FUNCDATASORTEDBYMOD : 'FuncDataSortedByMod';
LINEDATASORTEDTOTAL : 'LineDataSortedTotal'; FUNCDATASORTEDTOTAL : 'FuncDataSortedTotal';
LINEDATASORTED : 'LineDataSorted'; FUNCDATASORTED : 'FuncDataSorted'; ALLDATASORTED : 'AllDataSorted';
TOP10LINEDATA : 'Top10LineData'; TOP10FUNCDATA : 'Top10FuncData'; TOP10ALLDATA : 'Top10AllData';
UNUSEDATA : 'UnusedData'; ALL : 'All';

TRUE12:							'true' -> type(TRUE);
FALSE12:						'false' -> type(FALSE);
STRING12:						'"' .*? '"' -> type(STRING);
fragment FR_LETTER12:			[A-Za-z];
fragment FR_NUMBER12:			[0-9];
fragment FR_TTCN3IDENTIFIER12:	FR_LETTER12 (FR_LETTER12 | FR_NUMBER12+ | '_')*;
TTCN3IDENTIFIER12:	FR_LETTER12 (FR_LETTER12 | FR_NUMBER12+ | '_')* -> type(TTCN3IDENTIFIER);
IDENTIFIER12:		FR_TTCN3IDENTIFIER12 -> type(IDENTIFIER);

MACRO12:
(
	'$' FR_TTCN3IDENTIFIER12
|	'$' '{' (WS12)? FR_TTCN3IDENTIFIER12 (WS12)? '}'
) -> type(MACRO);
