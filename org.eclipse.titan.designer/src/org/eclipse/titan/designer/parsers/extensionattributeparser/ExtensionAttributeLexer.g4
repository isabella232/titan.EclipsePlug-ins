lexer grammar ExtensionAttributeLexer;

@header {
import org.eclipse.titan.designer.core.LoadBalancingUtilities;
}

/*
******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
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

/*------------------------------------------- TTCN-3 Keywords listed to be excluded -------------------------------------------*/

  ACTION: 'action';                 ACTIVATE: 'activate';             ALIVE: 'alive';
  ALL: 'all';                       ALT: 'alt';                       ALTSTEP: 'altstep';
  AND: 'and';                       AND4B: 'and4b';                   ANY: 'any';
  ANY2UNISTR: 'any2unistr';         APPLY: 'apply';

  BREAK: 'break';

  CALL: 'call';                     CASE: 'case';                     CATCH: 'catch';
  CHARKEYWORD: 'char';              CHECK: 'check';
  CHECKSTATE: 'checkstate';         CLEAR: 'clear';                   COMPLEMENTKEYWORD: 'complement';
  COMPONENT: 'component';           CONNECT: 'connect';               CONST: 'const';
  CONTINUE: 'continue';             CONTROL: 'control';               CREATE: 'create';

  DEACTIVATE: 'deactivate';         DECMATCH: 'decmatch';
  DECVALUE: 'decvalue';             DECVALUE_UNICHAR: 'decvalue_unichar';  DEREFERS: 'derefers';
  DISCONNECT: 'disconnect';         DISPLAY: 'display';               DO: 'do';

  ELSE: 'else';                     ENCVALUE: 'encvalue';              ENCVALUE_UNICHAR: 'encvalue_unichar';
  ENUMERATED: 'enumerated';         ERROR: 'error';
  EXCEPT: 'except';                 EXCEPTION: 'exception';           EXECUTE: 'execute';
  EXTENSION: 'extension';           EXTERNAL: 'external';

  FAIL: 'fail';                     FALSE: 'false';
  FOR: 'for';                       FRIEND: 'friend';                 FROM: 'from';

  GETCALL: 'getcall';               GETREF: 'getref';                 GETREPLY: 'getreply';
  GETVERDICT: 'getverdict';         GOTO: 'goto';                     GROUP: 'group';

  HALT: 'halt';                     HOSTID: 'hostid';

  IF: 'if';                         IFPRESENT: 'ifpresent';           IMPORT: 'import';
  INCONC: 'inconc';                 INFINITY: 'infinity';
  INOUT: 'inout';                   INTERLEAVE: 'interleave';
  ISTEMPLATEKIND: 'istemplatekind';

  KILL: 'kill';                     KILLED: 'killed';

  LABEL: 'label';                   LANGUAGE: 'language';             LENGTH: 'length';
  LOG: 'log';

  MAP: 'map';                       MATCH: 'match';                   MESSAGE: 'message';
  MIXED: 'mixed';                   MOD: 'mod';                       MODIFIES: 'modifies';
  MODULE: 'module';                 MODULEPAR: 'modulepar';           MTC: 'mtc';

  NOBLOCK: 'noblock';               NONE: 'none';
  NOT: 'not';                       NOT4B: 'not4b';                   NOWAIT: 'nowait';
  NOT_A_NUMBER: 'not_a_number';     NULL1: 'null';                    NULL2: 'NULL';

  OF: 'of';                         OMIT: 'omit';                     ON: 'on';
  OPTIONAL: 'optional';             OR: 'or';                         OR4B: 'or4b';
  OVERRIDEKEYWORD: 'override';

  PARAM: 'param';                   PASS: 'pass';                     PATTERNKEYWORD: 'pattern';
  PERMUTATION: 'permutation';       PORT: 'port';                     PUBLIC: 'public';
  PRESENT: 'present';               PRIVATE: 'private';               PROCEDURE: 'procedure';

  RAISE: 'raise';                   READ: 'read';                     RECEIVE: 'receive';
  RECORD: 'record';                 RECURSIVE: 'recursive';           REFERS: 'refers';
  REM: 'rem';                       REPEAT: 'repeat';                 REPLY: 'reply';
  RETURN: 'return';                 RUNNING: 'running';               RUNS: 'runs';

  SELECT: 'select';                 SELF: 'self';                     SEND: 'send';
  SENDER: 'sender';                 SET: 'set';                       SETVERDICT: 'setverdict';
  SETSTATE: 'setstate';             SIGNATURE: 'signature';           START: 'start';
  STOP: 'stop';                     SUBSET: 'subset';                 SUPERSET: 'superset';
  SYSTEM: 'system';

  TEMPLATE: 'template';             TESTCASE: 'testcase';             TIMEOUT: 'timeout';
  TIMER: 'timer';                   TO: 'to';                         TRIGGER: 'trigger';
  TRUE: 'true';                     TYPE: 'type';

  UNION: 'union';                   UNMAP: 'unmap';

  VALUE: 'value';                   VALUEOF: 'valueof';               VAR: 'var';
  VARIANT: 'variant';

  WHILE: 'while';                   WITH: 'with';

  XOR: 'xor';                       XOR4B: 'xor4b';


  /*------------------------------ TTCN-3 Predefined function identifiers listed to be excluded --------------------------------*/

  BIT2HEX: 'bit2hex';               BIT2INT: 'bit2int';               BIT2OCT: 'bit2oct';
  BIT2STR: 'bit2str';

  CHAR2INT: 'char2int';             CHAR2OCT: 'char2oct';

  DECODE_BASE64: 'decode_base64';   DECOMP: 'decomp';

  ENCODE_BASE64: 'encode_base64';   ENUM2INT: 'enum2int';

  FLOAT2INT: 'float2int';           FLOAT2STR: 'float2str';

  GET_STRINGENCODING: 'get_stringencoding';

  HEX2BIT: 'hex2bit';               HEX2INT: 'hex2int';               HEX2OCT: 'hex2oct';
  HEX2STR: 'hex2str';

  INT2BIT: 'int2bit';               INT2CHAR: 'int2char';             INT2ENUM: 'int2enum';
  INT2FLOAT: 'int2float';           INT2HEX: 'int2hex';               INT2OCT: 'int2oct';
  INT2STR: 'int2str';               INT2UNICHAR: 'int2unichar';       ISBOUND: 'isbound';
  ISCHOSEN: 'ischosen';             ISPRESENT: 'ispresent';           ISVALUE: 'isvalue';

  LENGTHOF: 'lengthof';             LOG2STR: 'log2str';

  OCT2BIT: 'oct2bit';               OCT2CHAR: 'oct2char';             OCT2HEX: 'oct2hex';
  OCT2INT: 'oct2int';               OCT2STR: 'oct2str';               OCT2UNICHAR: 'oct2unichar';

  REGEXP: 'regexp';                 REMOVE_BOM: 'remove_bom';         RND: 'rnd';
  REPLACE: 'replace';

  SIZEOF: 'sizeof';                 STR2BIT: 'str2bit';               STR2FLOAT: 'str2float';
  STR2HEX: 'str2hex';               STR2INT: 'str2int';               STR2OCT: 'str2oct';
  STRING2TTCN: 'string2ttcn';       SUBSTR: 'substr';

  TESTCASENAME: 'testcasename';     TTCN2STRING: 'ttcn2string';

  UNICHAR2CHAR: 'unichar2char';     UNICHAR2INT: 'unichar2int';       UNICHAR2OCT: 'unichar2oct';

/* ---- actual keyword ---- */

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
