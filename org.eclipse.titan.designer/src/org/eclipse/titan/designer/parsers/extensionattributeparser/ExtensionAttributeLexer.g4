lexer grammar ExtensionAttributeLexer;

@header {
import org.eclipse.titan.designer.core.LoadBalancingUtilities;
}

/*
******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************
*/
@members{}

/*
 * @author Kristof Szabados
 * */


WS:
[ \t\r\n\f]+ -> channel(HIDDEN);

LINE_COMMENT:
(	'//' ~[\r\n]*
|	'#' ~[\r\n]*
) ->channel(HIDDEN)
;

BLOCK_COMMENT:
'/*' .*? '*/' -> channel(HIDDEN)
;

// originally tokens
PROTOTYPE: 'prototype';
BACKTRACK: 'backtrack';
CONVERT: 'convert';
FAST: 'fast';
SLIDING: 'sliding';
ENCODE: 'encode';
DECODE: 'decode';
BER: 'BER';
PER: 'PER';
XER: 'XER';
RAW: 'RAW';
TEXT: 'TEXT';
JSON: 'JSON';
OER: 'OER';
TRANSPARENT: 'transparent';
ERRORBEHAVIOR: 'errorbehavior';
PRINTING: 'printing';
COMPACT: 'compact';
PRETTY: 'pretty';
INTERNAL: 'internal';
PROVIDER: 'provider';
USER: 'user';
ADDRESS: 'address';
ANYTYPE: 'anytype';
BITSTRING: 'bitstring';
BOOLEAN: 'boolean';
CHARSTRING: 'charstring';
DEFAULT: 'default';
EXTENDS: 'extends';
	//nincs char tipus
FLOAT: 'float';
HEXSTRING: 'hexstring';
INTEGER: 'integer';
OBJECTIDENTIFIER: 'objid';
OCTETSTRING: 'octetstring';
UNIVERSAL: 'universal';
VERDICTTYPE: 'verdicttype';
DISCARD: 'discard';
SIMPLE: 'simple';
FUNCTION: 'function';
IN: 'in';
OUT: 'out';
DONE: 'done';
VERSION: 'version';
REQUIRES: 'requires';
REQ_TITAN: 'requiresTITAN';



NUMBER
: ('0'..'9')+
;

IDENTIFIER
:  ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
;

SLASH: '/';

LPAREN: '(';

RPAREN: ')';

LANGLE: '<';

RANGLE: '>';

SEMICOLON:  ';';

COLON:  ':';

COMMA:  ',';

DOT:  '.';

REDIRECTSYMBOL:  '->';

DASH:  '-';

OROP:  '|';

ANDOP:  '&';
