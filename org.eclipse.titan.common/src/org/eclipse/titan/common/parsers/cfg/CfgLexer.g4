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
	BITSTRING,
	BITSTRINGMATCH,
	CHARKEYWORD,
	COMMA,
	COMPLEMENTKEYWORD,
	CONCATCHAR,
	DNSNAME,
	DOT,
	DOTDOT,
	ENDCHAR,
	ENDCONTROLPART,
	ENDTESTCASE,
	ERROR_VERDICT,
	EXCLUSIVE,
	FAIL_VERDICT,
	FALSE,
	FLOAT,
	HEXFILTER,
	HEXSTRING,
	HEXSTRINGMATCH,
	IFPRESENTKEYWORD,
	INCONC_VERDICT,
	INFINITYKEYWORD,
	IPV6,
	KILLTIMER,
	LENGTHKEYWORD,
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
	MTCKEYWORD,
	NANKEYWORD,
	NATURAL_NUMBER,
	NO,
	NOCASEKEYWORD,
	NONE_VERDICT,
	NULLKEYWORD,
	NUMHCS,
	OBJIDKEYWORD,
	OCTETSTRING,
	OCTETSTRINGMATCH,
	OMITKEYWORD,
	PASS_VERDICT,
	PATTERNKEYWORD,
	PERMUTATIONKEYWORD,
	PLUS,
	RPAREN,
	SEMICOLON,
	SLASH,
	SQUARECLOSE,
	SQUAREOPEN,
	STAR,
	STRING,
	STRINGOP,
	SUBSETKEYWORD,
	SUPERSETKEYWORD,
	SYSTEMKEYWORD,
	TCPPORT,
	TRUE,
	TTCN3IDENTIFIER,
	UNIXSOCKETS,
	YES
}

// Common fragments

fragment FR_WS:		[ \t\r\n\f]+;
fragment FR_LINE_COMMENT:
	'//' ~[\r\n]*
|	'#' ~[\r\n]*
;
fragment FR_BLOCK_COMMENT:	'/*' .*? '*/';
fragment FR_DIGIT:		[0-9];
fragment FR_INT:		'0'|[1-9][0-9]*;
// float fractional part
fragment FR_FRAC:		'.' FR_DIGIT+;
// float exponent part
fragment FR_EXP:		[Ee] [+-]? FR_INT;
fragment FR_FLOAT:
(	FR_INT FR_FRAC
|	FR_INT FR_FRAC? FR_EXP
|	       FR_FRAC  FR_EXP
);
fragment FR_HOSTNAME:
	('A'..'Z' | 'a'..'z' | '0'..'9' | ':')
	(	'A'..'Z' | 'a'..'z' | '0'..'9' | ':' | '%' | '.'
	|	('_' | '-') ('A'..'Z' | 'a'..'z' | '0'..'9')
	)*
;
fragment FR_LETTER:	[A-Za-z];
fragment FR_TTCN3IDENTIFIER:	FR_LETTER (FR_LETTER | FR_DIGIT+ | '_')*;
fragment FR_BINDIGIT:	[01];
fragment FR_HEXDIGIT:	[0-9A-Fa-f];
fragment FR_OCTDIGIT:	FR_HEXDIGIT FR_HEXDIGIT;
fragment FR_BINDIGITMATCH:	( FR_BINDIGIT | '?' | '*' );
fragment FR_HEXDIGITMATCH:	( FR_HEXDIGIT | '?' | '*' );
fragment FR_OCTDIGITMATCH:	( FR_OCTDIGIT | '?' | '*' );
fragment FR_STRING:
'"'
(	'\\' .
|	'""'
|	~( '\\' | '"' )
)*
'"'
;
fragment FR_MACRO:
	'$' FR_TTCN3IDENTIFIER
|	'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? '}'
;
fragment FR_MACRO_ID:			'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'identifier' FR_WS? '}';
fragment FR_MACRO_INT:			'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'integer' FR_WS? '}';
fragment FR_MACRO_BOOL:			'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'boolean' FR_WS? '}';
fragment FR_MACRO_FLOAT:		'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'float' FR_WS? '}';
fragment FR_MACRO_EXP_CSTR:		'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'charstring' FR_WS? '}';
fragment FR_MACRO_HOSTNAME: 	'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'hostname' FR_WS? '}';
fragment FR_MACRO_BSTR:			'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'bitstring' FR_WS? '}';
fragment FR_MACRO_HSTR:			'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'hexstring' FR_WS? '}';
fragment FR_MACRO_OSTR:			'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'octetstring' FR_WS? '}';
fragment FR_MACRO_BINARY:		'$' '{' FR_WS? FR_TTCN3IDENTIFIER FR_WS? ',' FR_WS? 'binaryoctet' FR_WS? '}';

// DEFAULT MODE
// These lexer rules are used only before the first section

WS:	FR_WS -> channel(HIDDEN);
LINE_COMMENT: FR_LINE_COMMENT -> channel(HIDDEN);
BLOCK_COMMENT:	FR_BLOCK_COMMENT
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

WS1:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT1:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT1:	FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);
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

KILLTIMER1: ( 'killtimer' | 'Killtimer' | 'killTimer' | 'KillTimer' ) -> type(KILLTIMER);
LOCALADDRESS1: ( 'localaddress' | 'Localaddress' | 'localAddress' | 'LocalAddress') -> type(LOCALADDRESS);
NUMHCS1: ( 'numhcs' | 'Numhcs' | 'numHCs' | 'NumHCs' ) -> type(NUMHCS);
TCPPORT1: ( 'tcpport' | 'TCPport' | 'tcpPort' | 'TCPPort' ) -> type(TCPPORT);
UNIXSOCKETS1: ( 'UnixSocketsEnabled' | 'UnixSocketsenabled' | 'UnixsocketsEnabled' | 'Unixsocketsenabled' | 'unixSocketsEnabled' | 'unixSocketsenabled' | 'unixsocketsEnabled' | 'unixsocketsenabled' ) -> type(UNIXSOCKETS);
ASSIGNMENTCHAR1:	':'? '=' -> type(ASSIGNMENTCHAR);
YES1: 				( 'yes' | 'Yes' | 'YES' ) -> type(YES);
NO1: 				( 'no' | 'No' | 'NO' ) -> type(NO);
NATURAL_NUMBER1:	FR_INT -> type(NATURAL_NUMBER);
STRINGOP1:			'&'	'='? -> type(STRINGOP);
FLOAT1:				FR_FLOAT -> type(FLOAT);

DNSNAME1:
(	FR_HOSTNAME
	('/' FR_DIGIT+)?
) -> type(DNSNAME);
TTCN3IDENTIFIER1:	FR_TTCN3IDENTIFIER -> type(TTCN3IDENTIFIER);
STRING1:			FR_STRING -> type(STRING);
MACRO1:				FR_MACRO -> type(MACRO);
MACRO_HOSTNAME1: 	FR_MACRO_HOSTNAME -> type(MACRO_HOSTNAME);
MACRO_INT1:			FR_MACRO_INT -> type(MACRO_INT);
MACRO_EXP_CSTR1:	FR_MACRO_EXP_CSTR -> type(MACRO_EXP_CSTR);
MACRO_FLOAT1:		FR_MACRO_FLOAT -> type(MACRO_FLOAT);

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

WS2:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT2:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT2:	FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);

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

WS3:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT3:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT3:		FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);
SEMICOLON3:			';' -> type(SEMICOLON);
DOT3:				'.' -> type(DOT);
STAR3:				'*' -> type(STAR);
TTCN3IDENTIFIER3:	FR_TTCN3IDENTIFIER -> type(TTCN3IDENTIFIER);

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

WS4:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT4:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT4:	FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);

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

WS5:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT5:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT5:	FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);
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

TTCN3IDENTIFIER5:	FR_TTCN3IDENTIFIER -> type(TTCN3IDENTIFIER);
BEGINCHAR5:			'{'
{	pushInterval( interval_type.NORMAL );
} -> type(BEGINCHAR);
ENDCHAR5:			'}'
{	popInterval();
} -> type(ENDCHAR);
MACRORVALUE5:		[0-9|A-Z|a-z|.|_|-]+ -> type(MACRORVALUE);
ASSIGNMENTCHAR5:	':'? '=' -> type(ASSIGNMENTCHAR);
fragment FR_ESCAPE_WO_QUOTE5:	'\\' ( '\\' | '\'' | '?' | 'a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' );
STRING5:
(	FR_STRING
|	'\\"'
	(	FR_ESCAPE_WO_QUOTE5
	|	~( '\\' | '"' )
	)*
	'\\"'
) -> type(STRING);

MACRO_ID5:			FR_MACRO_ID -> type(MACRO_ID);
MACRO_INT5:			FR_MACRO_INT -> type(MACRO_INT);
MACRO_BOOL5:		FR_MACRO_BOOL -> type(MACRO_BOOL);
MACRO_FLOAT5:		FR_MACRO_FLOAT -> type(MACRO_FLOAT);
MACRO_EXP_CSTR5:	FR_MACRO_EXP_CSTR -> type(MACRO_EXP_CSTR);
MACRO_BSTR5:		FR_MACRO_BSTR -> type(MACRO_BSTR);
MACRO_HSTR5:		FR_MACRO_HSTR -> type(MACRO_HSTR);
MACRO_OSTR5:		FR_MACRO_OSTR -> type(MACRO_OSTR);
MACRO_BINARY5:		FR_MACRO_BINARY -> type(MACRO_BINARY);
MACRO_HOSTNAME5: 	FR_MACRO_HOSTNAME -> type(MACRO_HOSTNAME);
MACRO5:				FR_MACRO -> type(MACRO);
BITSTRING5:			'\'' FR_BINDIGIT* '\'' 'B' -> type(BITSTRING);
HEXSTRING5:			'\'' FR_HEXDIGIT* '\'' 'H' -> type(HEXSTRING);
OCTETSTRING5:		'\'' FR_OCTDIGIT* '\'' 'O' -> type(OCTETSTRING);
BITSTRINGMATCH5:	'\'' FR_BINDIGITMATCH* '\'' 'B' -> type(BITSTRINGMATCH);
HEXSTRINGMATCH5:	'\'' FR_HEXDIGITMATCH* '\'' 'H' -> type(HEXSTRINGMATCH);
OCTETSTRINGMATCH5:	'\'' FR_OCTDIGITMATCH* '\'' 'O' -> type(OCTETSTRINGMATCH);
COMMA5:				',' -> type(COMMA);
FSTRING:
(	'\\"' // \" is handled separately in the structured definitions
|	'\\' .   // Handle escaped characters
|	~[{}"\\$,:=\n\r\t #/]+  // Anything except {,},'"',\,$,',',:,=,#,/ and whitespace
|	'/'
);

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

WS6:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT6:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT6:		FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);
SEMICOLON6: 		';' -> type(SEMICOLON);
ASSIGNMENTCHAR6:	':'? '=' -> type(ASSIGNMENTCHAR);
STRING6:			FR_STRING -> type(STRING);
STRINGOP6:			'&' -> type(STRINGOP);
BEGINCONTROLPART6:	( 'begincontrolpart' | 'Begincontrolpart' | 'beginControlpart' | 'BeginControlpart'
| 'begincontrolPart' | 'BeginControlPart' | 'beginControlPart' | 'BegincontrolPart' ) -> type(BEGINCONTROLPART);
ENDCONTROLPART6:	( 'endcontrolpart' | 'Endcontrolpart' | 'endControlpart' | 'EndControlpart'
| 'endcontrolPart' | 'EndControlPart' | 'endControlPart' | 'EndcontrolPart' ) -> type(ENDCONTROLPART);
BEGINTESTCASE6:		( 'begintestcase' | 'Begintestcase' | 'beginTestcase' | 'BeginTestcase' | 'begintestCase'
| 'BeginTestCase' | 'beginTestCase' | 'BegintestCase' ) -> type(BEGINTESTCASE);
ENDTESTCASE6:		( 'endtestcase' | 'Endtestcase' | 'endTestcase' | 'EndTestcase' | 'endtestCase'
| 'EndTestCase' | 'endTestCase' | 'EndtestCase' ) -> type(ENDTESTCASE);

MACRO_EXP_CSTR6:	FR_MACRO_EXP_CSTR -> type(MACRO_EXP_CSTR);
MACRO6:				FR_MACRO -> type(MACRO);

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

WS7:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT7:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT7:		FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);
TTCN3IDENTIFIER7:	FR_TTCN3IDENTIFIER -> type(TTCN3IDENTIFIER);
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
NATURAL_NUMBER7:	FR_INT -> type(NATURAL_NUMBER);
SEMICOLON7:			';' -> type(SEMICOLON);
DOT7:				'.' -> type(DOT);
ASSIGNMENTCHAR7:	':'? '=' -> type(ASSIGNMENTCHAR);
LPAREN7:			'('
{	pushInterval( interval_type.PARAMETER );
} -> type(LPAREN);
RPAREN7:			')'
{	popInterval();
} -> type(RPAREN);
MTC7KEYWORD:		'mtc' -> type(MTCKEYWORD);
SYSTEM7KEYWORD:		'system' -> type(SYSTEMKEYWORD);
STRING7:			FR_STRING -> type(STRING);
STRINGOP7:			'&'	'='? -> type(STRINGOP);
MACRO7:				FR_MACRO -> type(MACRO);
MACRO_INT7:			FR_MACRO_INT -> type(MACRO_INT);
MACRO_ID7:			FR_MACRO_ID -> type(MACRO_ID);
MACRO_EXP_CSTR7:	FR_MACRO_EXP_CSTR -> type(MACRO_EXP_CSTR);

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

WS8:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT8:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT8:		FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);
TTCN3IDENTIFIER8:	FR_TTCN3IDENTIFIER -> type(TTCN3IDENTIFIER);
SEMICOLON8:			';' -> type(SEMICOLON);
ASSIGNMENTCHAR8:	':'? '=' -> type(ASSIGNMENTCHAR);
STAR8:				'*' -> type(STAR);
COMMA8:				',' -> type(COMMA);
NATURAL_NUMBER8:	FR_INT -> type(NATURAL_NUMBER);
FLOAT8:				FR_FLOAT -> type(FLOAT);

DNSNAME8:
(	FR_HOSTNAME
	('/' FR_DIGIT+)?
) -> type(DNSNAME);
MACRO_ID8:		FR_MACRO_ID -> type(MACRO_ID);

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

WS9:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT9:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT9:		FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);
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
EXCLUSIVE9:			'!' -> type(EXCLUSIVE);

NONE_VERDICT9:		'none' -> type(NONE_VERDICT);
PASS_VERDICT9:		'pass' -> type(PASS_VERDICT);
INCONC_VERDICT9:	'inconc' -> type(INCONC_VERDICT);
FAIL_VERDICT9:		'fail' -> type(FAIL_VERDICT);
ERROR_VERDICT9:		'error' -> type(ERROR_VERDICT);
CHARKEYWORD9:		'char' -> type(CHARKEYWORD);
OBJIDKEYWORD9:		'objid' -> type(OBJIDKEYWORD);
OMITKEYWORD9:		'omit' -> type(OMITKEYWORD);
NULLKEYWORD9:		( 'null' | 'NULL' ) -> type(NULLKEYWORD);
MTCKEYWORD9:		'mtc' -> type(MTCKEYWORD);
SYSTEMKEYWORD9:		'system' -> type(SYSTEMKEYWORD);
INFINITYKEYWORD9:	'infinity' -> type(INFINITYKEYWORD);
NANKEYWORD9:		'not_a_number' -> type(NANKEYWORD);
IFPRESENTKEYWORD9:	'ifpresent' -> type(IFPRESENTKEYWORD);
LENGTHKEYWORD9:		'length' -> type(LENGTHKEYWORD);
COMPLEMENTKEYWORD9:	'complement' -> type(COMPLEMENTKEYWORD);
PATTERNKEYWORD9:	'pattern' -> type(PATTERNKEYWORD);
PERMUTATIONKEYWORD9:'permutation' -> type(PERMUTATIONKEYWORD);
SUPERSETKEYWORD9:	'superset' -> type(SUPERSETKEYWORD);
SUBSETKEYWORD9:		'subset' -> type(SUBSETKEYWORD);
NOCASEKEYWORD9:		'@nocase' -> type(NOCASEKEYWORD);
TRUE9:				'true' -> type(TRUE);
FALSE9:				'false' -> type(FALSE);
ANYVALUE9:			'?' -> type(ANYVALUE);
TTCN3IDENTIFIER9:	FR_TTCN3IDENTIFIER -> type(TTCN3IDENTIFIER);
NATURAL_NUMBER9:	FR_INT -> type(NATURAL_NUMBER);
FLOAT9:				FR_FLOAT -> type(FLOAT);
BITSTRING9:			'\'' FR_BINDIGIT* '\'' 'B' -> type(BITSTRING);
HEXSTRING9:			'\'' FR_HEXDIGIT* '\'' 'H' -> type(HEXSTRING);
OCTETSTRING9:		'\'' FR_OCTDIGIT* '\'' 'O' -> type(OCTETSTRING);
BITSTRINGMATCH9:	'\'' FR_BINDIGITMATCH* '\'' 'B' -> type(BITSTRINGMATCH);
HEXSTRINGMATCH9:	'\'' FR_HEXDIGITMATCH* '\'' 'H' -> type(HEXSTRINGMATCH);
OCTETSTRINGMATCH9:	'\'' FR_OCTDIGITMATCH* '\'' 'O' -> type(OCTETSTRINGMATCH);
MACRO_ID9:			FR_MACRO_ID -> type(MACRO_ID);
MACRO_INT9:			FR_MACRO_INT -> type(MACRO_INT);
MACRO_BOOL9:		FR_MACRO_BOOL -> type(MACRO_BOOL);
MACRO_FLOAT9:		FR_MACRO_FLOAT -> type(MACRO_FLOAT);
MACRO_EXP_CSTR9:	FR_MACRO_EXP_CSTR -> type(MACRO_EXP_CSTR);
MACRO_BSTR9:		FR_MACRO_BSTR -> type(MACRO_BSTR);
MACRO_HSTR9:		FR_MACRO_HSTR -> type(MACRO_HSTR);
MACRO_OSTR9:		FR_MACRO_OSTR -> type(MACRO_OSTR);
MACRO_BINARY9:		FR_MACRO_BINARY -> type(MACRO_BINARY);
MACRO9:				FR_MACRO -> type(MACRO);
STRING9:			FR_STRING -> type(STRING);

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

WS10:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT10:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT10:		FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);
SEMICOLON10:			';' -> type(SEMICOLON);
STAR10:					'*' -> type(STAR);
ASSIGNMENTCHAR10:		':'? '=' -> type(ASSIGNMENTCHAR);

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

NATURAL_NUMBER10:	FR_INT -> type(NATURAL_NUMBER);
FLOAT10:			FR_FLOAT -> type(FLOAT);

TTCN3IDENTIFIER10:	FR_TTCN3IDENTIFIER -> type(TTCN3IDENTIFIER);

DNSNAME10:
(	FR_HOSTNAME
	('/' FR_DIGIT+)?
) -> type(DNSNAME);
MACRO_ID10:				FR_MACRO_ID -> type(MACRO_ID);
MACRO_HOSTNAME10: 		FR_MACRO_HOSTNAME -> type(MACRO_HOSTNAME);
MACRO10:				FR_MACRO -> type(MACRO);

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

WS11:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT11:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT11:	FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);

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
MTCKEYWORD11:		'mtc' -> type(MTCKEYWORD);
SYSTEMKEYWORD11:	'system' -> type(SYSTEMKEYWORD);
LOGGERPLUGINS: 'LoggerPlugins' | 'Loggerplugins' | 'loggerPlugins' | 'loggerplugins';

APPENDFILE: 'appendfile' | 'Appendfile' | 'appendFile' | 'AppendFile';
CONSOLEMASK: 'consolemask' | 'Consolemask' | 'consoleMask' | 'ConsoleMask';
DISKFULLACTION: 'diskfullaction' | 'diskfullAction' | 'diskFullaction' | 'diskFullAction' | 'Diskfullaction' | 'DiskfullAction' | 'DiskFullaction' | 'DiskFullAction';
DISKFULLACTIONVALUE_ERROR: 'error' | 'Error';
DISKFULLACTIONVALUE_STOP: 'stop' | 'Stop';
DISKFULLACTIONVALUE_DELETE: 'delete' | 'Delete';
DISKFULLACTIONVALUE_RETRY: 'retry' | 'Retry';
FILEMASK: 'filemask' | 'Filemask' | 'fileMask' | 'FileMask';
LOGFILENAME: 'filename' | 'Filename' | 'fileName' | 'FileName' | 'logfile' | 'Logfile' |'logFile' | 'LogFile';
EMERGENCYLOGGING: 'EmergencyLogging' | 'Emergencylogging' | 'emergencylogging' | 'emergencyLogging';
EMERGENCYLOGGINGBEHAVIOUR: 'EmergencyLoggingBehaviour' | 'EmergencyLoggingbehaviour' | 'Emergencyloggingbehaviour' | 'emergencyLoggingBehaviour' | 'emergencyloggingBehaviour'
| 'emergencyloggingbehaviour' | 'emergencyLogginglbehaviour' | 'EmergencyloggingBehaviour';
EMERGENCYLOGGINGMASK: 'EmergencyLoggingMask' | 'EmergencyLoggingmask' | 'Emergencyloggingmask' | 'emergencyLoggingMask' | 'emergencyloggingMask'
| 'emergencyloggingmask' | 'emergencyLoggingmask' | 'EmergencyloggingMask';
BUFFERALL: 'BufferAll' | 'Bufferall' | 'bufferAll' | 'bufferall';
BUFFERMASKED: 'BufferMasked' | 'Buffermasked' | 'bufferMasked' |'buffermasked';
LOGENTITYNAME: 'logentityname' | 'Logentityname' | 'logEntityname' | 'LogEntityname' | 'logentityName' | 'LogentityName' | 'logEntityName' | 'LogEntityName';
LOGEVENTTYPES: 'logeventtypes' | 'Logeventtypes' | 'logEventtypes' | 'LogEventtypes' | 'logeventTypes' | 'LogEventTypes' | 'logEventTypes' | 'LogeventTypes';
LOGFILENUMBER: 'logfilenumber' | 'logfileNumber' | 'logFilenumber' | 'logFileNumber' | 'Logfilenumber' | 'LogfileNumber' | 'LogFilenumber' | 'LogFileNumber';
LOGFILESIZE: 'logfilesize' | 'logfileSize' | 'logFilesize' | 'logFileSize' | 'Logfilesize' | 'LogfileSize' | 'LogFilesize' | 'LogFileSize';
MATCHINGHINTS: 'matchinghints' | 'Matchinghints' | 'matchingHints' | 'MatchingHints';
SOURCEINFOFORMAT: 'logsourceinfo' | 'Logsourceinfo' | 'logSourceinfo' | 'LogSourceinfo' | 'logsourceInfo' | 'LogsourceInfo' | 'logSourceInfo' | 'LogSourceInfo'
| 'sourceinfoformat' | 'Sourceinfoformat' | 'sourceInfoformat' | 'SourceInfoformat' | 'sourceinfoFormat' | 'SourceinfoFormat' | 'sourceInfoFormat' | 'SourceInfoFormat';
SOURCEINFOVALUE_NONE: 'none' | 'None' | 'NONE';
SOURCEINFOVALUE_SINGLE: 'single' | 'Single' | 'SINGLE';
SOURCEINFOVALUE_STACK: 'stack' | 'Stack' | 'STACK';
TIMESTAMPFORMAT: 'timestampformat' | 'Timestampformat' | 'timeStampformat' | 'TimeStampformat' | 'timestampFormat' | 'TimestampFormat' | 'timeStampFormat' | 'TimeStampFormat';
CONSOLETIMESTAMPFORMAT: 'consoletimestampformat' | 'Consoletimestampformat' | 'ConsoleTimestampformat' | 'ConsoleTimeStampformat' |
'ConsoleTimeStampFormat' | 'consoleTimestampformat' | 'consoleTimeStampformat' | 'consoleTimeStampFormat' | 'consoletimeStampformat' | 'consoletimestampFormat';
TIMESTAMPVALUE: 'time' | 'Time' | 'TIME' | 'datetime' | 'DateTime' | 'Datetime' | 'DATETIME' | 'seconds' | 'Seconds' | 'SECONDS';
YESNO: 'yes' | 'Yes' | 'YES' | 'no' | 'No' | 'NO';

SEMICOLON11:			';' -> type(SEMICOLON);
STAR11:					'*' -> type(STAR);
ASSIGNMENTCHAR11:		':'? '=' -> type(ASSIGNMENTCHAR);
DOT11:					'.' -> type(DOT);
BEGINCHAR11:			'{'
{	pushInterval( interval_type.NORMAL );
} -> type(BEGINCHAR);
ENDCHAR11:				'}'
{	popInterval();
} -> type(ENDCHAR);
COMMA11:				',' -> type(COMMA);
STRINGOP11:				'&'	'='? -> type(STRINGOP);
LOGICALOR11:			'|' -> type(LOGICALOR);
TRUE11:					'true' -> type(TRUE);
FALSE11:				'false' -> type(FALSE);
LPAREN11:				'('
{	pushInterval( interval_type.PARAMETER );
} -> type(LPAREN);
RPAREN11:				')'
{	popInterval();
} -> type(RPAREN);


TTCN3IDENTIFIER11:	FR_TTCN3IDENTIFIER -> type(TTCN3IDENTIFIER);
NATURAL_NUMBER11:	FR_INT -> type(NATURAL_NUMBER);
FLOAT11:			FR_FLOAT -> type(FLOAT);

MACRO_BOOL11:		FR_MACRO_BOOL -> type(MACRO_BOOL);
MACRO_ID11:			FR_MACRO_ID -> type(MACRO_ID);
MACRO_INT11:		FR_MACRO_INT -> type(MACRO_INT);
MACRO_EXP_CSTR11:	FR_MACRO_EXP_CSTR -> type(MACRO_EXP_CSTR);
MACRO11:			FR_MACRO -> type(MACRO);
STRING11:			FR_STRING -> type(STRING);

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

WS12:	FR_WS -> type(WS), channel(HIDDEN);
LINE_COMMENT12:	FR_LINE_COMMENT -> type(LINE_COMMENT), channel(HIDDEN);
BLOCK_COMMENT12:		FR_BLOCK_COMMENT
{	pushInterval( interval_type.MULTILINE_COMMENT );
	popInterval();
}	-> type(BLOCK_COMMENT), channel(HIDDEN);

CONCATCHAR12:			'&=' -> type(CONCATCHAR);
HEXFILTER12:			FR_HEXDIGIT+ -> type(HEXFILTER);
SEMICOLON12:			';' -> type(SEMICOLON);
ASSIGNMENTCHAR12:		':'? '=' -> type(ASSIGNMENTCHAR);
LOGICALOR12:			'|' -> type(LOGICALOR);
AND12:					'&' -> type(AND);
/* settings */
DISABLEPROFILER: 'DisableProfiler'; DISABLECOVERAGE: 'DisableCoverage'; DATABASEFILE: 'DatabaseFile';
AGGREGATEDATA: 'AggregateData'; STATISTICSFILE: 'StatisticsFile'; DISABLESTATISTICS: 'DisableStatistics';
STATISTICSFILTER: 'StatisticsFilter'; STARTAUTOMATICALLY: 'StartAutomatically';
NETLINETIMES: 'NetLineTimes'; NETFUNCTIONTIMES: 'NetFunctionTimes';

/* statistics filters (single) */
NUMBEROFLINES: 'NumberOfLines'; LINEDATARAW: 'LineDataRaw'; FUNCDATARAW: 'FuncDataRaw';
LINEAVGRAW: 'LineAvgRaw'; FUNCAVGRAW: 'FuncAvgRaw';
LINETIMESSORTEDBYMOD: 'LineTimesSortedByMod'; FUNCTIMESSORTEDBYMOD: 'FuncTimesSortedByMod';
LINETIMESSORTEDTOTAL: 'LineTimesSortedTotal'; FUNCTIMESSORTEDTOTAL: 'FuncTimesSortedTotal';
LINECOUNTSORTEDBYMOD: 'LineCountSortedByMod'; FUNCCOUNTSORTEDBYMOD: 'FuncCountSortedByMod';
LINECOUNTSORTEDTOTAL: 'LineCountSortedTotal'; FUNCCOUNTSORTEDTOTAL: 'FuncCountSortedTotal';
LINEAVGSORTEDBYMOD: 'LineAvgSortedByMod'; FUNCAVGSORTEDBYMOD: 'FuncAvgSortedByMod';
LINEAVGSORTEDTOTAL: 'LineAvgSortedTotal'; FUNCAVGSORTEDTOTAL: 'FuncAvgSortedTotal';
TOP10LINETIMES: 'Top10LineTimes'; TOP10FUNCTIMES: 'Top10FuncTimes';
TOP10LINECOUNT: 'Top10LineCount'; TOP10FUNCCOUNT: 'Top10FuncCount';
TOP10LINEAVG: 'Top10LineAvg'; TOP10FUNCAVG: 'Top10FuncAvg';
UNUSEDLINES: 'UnusedLines'; UNUSEDFUNC: 'UnusedFunc';

/* statistics filters (grouped) */
ALLRAWDATA: 'AllRawData';
LINEDATASORTEDBYMOD: 'LineDataSortedByMod'; FUNCDATASORTEDBYMOD: 'FuncDataSortedByMod';
LINEDATASORTEDTOTAL: 'LineDataSortedTotal'; FUNCDATASORTEDTOTAL: 'FuncDataSortedTotal';
LINEDATASORTED: 'LineDataSorted'; FUNCDATASORTED: 'FuncDataSorted'; ALLDATASORTED: 'AllDataSorted';
TOP10LINEDATA: 'Top10LineData'; TOP10FUNCDATA: 'Top10FuncData'; TOP10ALLDATA: 'Top10AllData';
UNUSEDATA: 'UnusedData'; ALL: 'All';

TRUE12:				'true' -> type(TRUE);
FALSE12:			'false' -> type(FALSE);
STRING12:			FR_STRING -> type(STRING);
TTCN3IDENTIFIER12:	FR_TTCN3IDENTIFIER -> type(TTCN3IDENTIFIER);
MACRO12:			FR_MACRO -> type(MACRO);
