lexer grammar VariantAttributeLexer;

@header {
import org.eclipse.titan.designer.core.LoadBalancingUtilities;
}

/*
******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/
@members{}

/*
 * @author Kristof Szabados
 * */

//Default mode

WS: [ \t\f]+ -> channel(HIDDEN);

NEWLINE: [\r\n] -> channel(HIDDEN);

LINE_COMMENT: ( '//' ~[\r\n]* ) ->channel(HIDDEN);

BLOCK_COMMENT:	'/*' .*? '*/' ->channel(HIDDEN);

DOT: '.';
COLON: ':';
SEMICOLON: ';';
COMMA: ',';
LPAREN: '(';
RPAREN: ')';
BEGINCHAR: '{';
ENDCHAR: '}';
ASSIGN: '=';
PLUS: '+';
MINUS: '-';

fragment BIN: [01];
fragment HEX: [0-9A-Fa-f];
fragment OCT: HEX HEX;

BSTRING:	'\'' WS? ( BIN        WS? )* '\'B';
HSTRING:	'\'' WS? ( HEX        WS? )* '\'H';
OSTRING:	'\'' WS? ( OCT        WS? )* '\'O';
XSTRING:	'\'' ~[\']* '\'';

fragment DIGIT: [0-9];
fragment INT: [1-9] DIGIT* | '0';

NUMBER: INT;

FLOATVALUE:
(	INT '.' DIGIT+
|	INT ( '.' DIGIT+ )? [Ee] [+-]? INT
);

DQUOTE: ('""' | '\\"');

CHAR: (['"\\'] | '\\'['"'] | DQUOTE DQUOTE);



TRUE: 'true';
FALSE: 'false';
NONE: 'none';
PASS: 'pass';
INCONC: 'inconc';
FAIL: 'fail';
ERROR: 'error';
Null: 'Null';
NULL: 'NULL';
OMIT: 'omit';

CSTRING: DQUOTE CHAR* DQUOTE;

/* RAW encoder entering keywords */
PADDINGKeyword: 'PADDING' -> mode(RawCodec_MODE);
PREPADDINGKeyword: 'PREPADDING' -> mode(RawCodec_MODE);
PADDINGPATTERNKeyword: 'PADDING_PATTERN' -> mode(RawCodec_MODE);
PADDALLKeyword: 'PADDALL';
FIELDORDERKeyword: 'FIELDORDER' -> mode(RawCodec_MODE);
EXTENSION_BITKeyword: 'EXTENSION_BIT' -> mode(RawCodec_MODE);
EXTENSION_BIT_GROUPKeyword: 'EXTENSION_BIT_GROUP' -> mode(RawCodec_MODE);
LENGTHTOKeyword: 'LENGTHTO' -> mode(RawCodec_MODE);
POINTERTOKeyword: 'POINTERTO' -> mode(RawCodec_MODE);
UNITKeyword: 'UNIT' -> mode(RawCodec_MODE);
PTRUNITKeyword: 'PTRUNIT' -> mode(RawCodec_MODE);
REPEATABLEKeyword: 'REPEATABLE' -> mode(RawCodec_MODE);
PTROFFSETKeyword: 'PTROFFSET' -> mode(RawCodec_MODE);
LENGTHINDEXKeyword: 'LENGTHINDEX' -> mode(RawCodec_MODE);
TAGKeyword: 'TAG' -> mode(RawCodec_MODE);
CROSSTAGKeyword: 'CROSSTAG' -> mode(RawCodec_MODE);
PRESENCEKeyword: 'PRESENCE' -> mode(RawCodec_MODE);
FIELDLENGTHKeyword: 'FIELDLENGTH' -> mode(RawCodec_MODE);
FORMATKeyword: 'FORMAT' -> type(FIELDLENGTHKeyword), mode(RawCodec_MODE);
ALIGNKeyword: 'ALIGN' -> mode(RawCodec_MODE);
BYTEORDERKeyword: 'BYTEORDER' -> mode(RawCodec_MODE);
COMPKeyword: 'COMP' -> mode(RawCodec_MODE);
BITORDERKeyword: 'BITORDER' -> mode(RawCodec_MODE);
BITORDERINFIELDKeyword: 'BITORDERINFIELD' -> mode(RawCodec_MODE);
BITORDERINOCTETKeyword: 'BITORDERINOCTET' -> mode(RawCodec_MODE);
HEXORDERKeyword: 'HEXORDER' -> mode(RawCodec_MODE);
TOPLEVELKeyword: 'TOPLEVEL' -> mode(RawCodec_MODE);
IntXKeyword: 'IntX';
BitKeyword: 'bit';
BitsKeyword: 'bits';
UNSIGNEDKeyword: 'unsigned';
UTF8Keyword: 'UTF-8';
UTF16Keyword: 'UTF-16';
IEEE754FLOATKeyword: 'IEEE754 float';
IEEE754DOUBLEKeyword: 'IEEE754 double';


/* TEXT encoder entering keywords */
BEGINKeyword: 'BEGIN' -> mode(TextCodec_MODE);
ENDKeyword: 'END' -> mode(TextCodec_MODE);
SEPARATORKeyword: 'SEPARATOR' -> mode(TextCodec_MODE);
TEXT_CODINGKeyword: 'TEXT_CODING' -> mode(TextCodec_MODE);

/* XER encoder keywords (not entering) */
ABSTRACTKeyword: 'abstract';
ANYATTRIBUTESKeyword: 'anyAttributes';
ANYELEMENTKeyword: 'anyElement';
ATTRIBUTEKeyword: 'attribute';
ATTRIBUTEFROMUALIFIED: 'attributeFromQualified';
BLOCKKeyword: 'block';
CONTROLNAMESPACEKeyword: 'controlNamespace';
DEFAULTFOREMPTYKeyword: 'defaultForEmpty';
ELEMENTKeyword: 'element';
ELEMENTFORMQUALIFIEDKeyword: 'elementFormQualified';
EMBEDVALUESKeyword: 'embedValues';
FORMKeyword: 'form';
FRACTIONDIGITSKeyword: 'fractionDigits';
LISTKeyword: 'list';
NAMEKeyword: 'name';
NAMESPACEKeyword: 'namespace';
TEXTKeyword: 'text';
UNTAGGEDKeyword: 'untagged';
USENILKeyword: 'useNil';
USENUMBERKeyword: 'useNumber';
USEORDERKeyword: 'useOrder';
USETYPEKeyword: 'useType';
USEUNIONKeyword: 'useUnion';
WHITESPACEKeyword: 'whitespace';
XSDKeyword: 'XSD';
ASKeyword: 'as';
ALLKeyword: 'all';
INKeyword: 'in';
/*name mangling */
CAPITALIZEDKeyword: 'capitalized';
UNCAPITALIZEDKeyword: 'uncapitalized';
LOWERCASEDKeyword: 'lowercased';
UPPERCASEDKeyword: 'uppercased';
QUALIFIEDKeyword: 'qualified';
UNQUALIFIEDKeyword: 'unqualified';
EXCEPTKeyword: 'except';
FROMKeyword: 'from';
PREFIXKeyword: 'prefix';
PRESERVEKeyword: 'preserve';
COLLAPSEKeyword: 'collapse';
REPLACEKeyword: 'replace';

/* XSD:something */
XSDstring: 'string';
XSDnormalizedString: 'normalizedString';
XSDtoken: 'token';
XSDName: 'Name';
XSDNMToken: 'NMTOKEN';
XSDNCName: 'NCName';
XSDID: 'ID';
XSDIDREF: 'IDREF';
XSDENTITY: 'ENTITY';
XSDhexBinary: 'hexBinary';
XSDbase64Binary: 'base64Binary';
XSDanyURI: 'anyURI';
XSDlanguage: 'language';
XSDinteger: 'integer';
XSDpositiveInteger: 'positiveInteger';
XSDnonPositiveInteger: 'nonPositiveInteger';
XSDnegativeInteger: 'negativeInteger';
XSDnonNegativeInteger: 'nonNegativeInteger';
//XSDlong is recognized by BER as long -> LONGKeyword
XSDunsignedLong: 'unsignedLong';
XSDint: 'int';
XSDunsignedInt: 'unsignedInt';
//XSDshort already taken by BER as short -> SHORTKeyword
XSDunsignedShort: 'unsignedShort';
XSDbyte: 'byte';
XSDunsignedByte: 'unsignedByte';
XSDdecimal: 'decimal';
XSDfloat: 'float';
XSDdouble: 'double';
XSDduration: 'duration';
XSDdateTime: 'dateTime';
XSDtime: 'time';
XSDdate: 'date';
XSDgYearMonth: 'gYearMonth';
XSDgYear: 'gYear';
XSDgMonthDay: 'gMonthDay';
XSDgDay: 'gDay';
XSDgMonth: 'gMonth';
XSDNMTOKENS: 'NMTOKENS';
XSDIDREFS: 'IDREFS';
XSDENTITIES: 'ENTITIES';
XSDQName: 'QName';
XSDboolean: 'boolean';
XSDanySimpleType: 'anySimpleType';
XSDanyType: 'anyType';

/* tokens for new JSON attributes (standard-compliant) */
ASVALUEKeyword: 'asValue';
DEFAULTKeyword: 'default'  -> mode(JsonCodec_MODE);
NUMBERKeyword: 'number';
EXTENDKeyword: 'extend'  -> mode(JsonCodec_MODE);
METAINFOKeyword: 'metainfo';
FORKeyword: 'for';
UNBOUNDKeyword: 'unbound';
CHOSENKeyword: 'chosen';
JSONOTHERWISEKeyword: 'otherwise';

/* BER encoder keywords (not entering) */
LENGTHKeyword: 'length';
ACCEPTKeyword: 'accept';
LONGKeyword: 'long';
SHORTKeyword: 'short';
INDEFINITEKeyword: 'indefinite';
DEFINITEKeyword: 'definite';

/* JSON codec entering keyword */
JSONKeyword: 'JSON' -> mode(JsonCodec_MODE);

IDENTIFIER:
( ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
);

mode RawCodec_MODE;

WS1: [ \t\f]+ -> channel(HIDDEN);

NEWLINE1: [\r\n] -> channel(HIDDEN);

LINE_COMMENT1: ( '//' ~[\r\n]* ) ->channel(HIDDEN);

BLOCK_COMMENT1:	'/*' .*? '*/' ->channel(HIDDEN);

DOT1: '.' -> type(DOT);
SEMICOLON1: ';' -> type(SEMICOLON);
COMMA1: ',' -> type(COMMA);
LPAREN1: '(' -> type(LPAREN);
RPAREN1: ')' -> type(RPAREN), mode(DEFAULT_MODE);
BEGINCHAR1: '{' -> type(BEGINCHAR);
ENDCHAR1: '}' -> type(ENDCHAR);
ASSIGN1: '=' -> type(ASSIGN);

//fragment BIN: [01];
//fragment HEX: [0-9A-Fa-f];
//fragment OCT: HEX HEX;

BSTRING1:	'\'' WS? ( BIN        WS? )* '\'B' -> type(BSTRING);
HSTRING1:	'\'' WS? ( HEX        WS? )* '\'H' -> type(HSTRING);
OSTRING1:	'\'' WS? ( OCT        WS? )* '\'O' -> type(OSTRING);

//fragment DIGIT: [0-9];
//fragment INT: [1-9] DIGIT* | '0';

NUMBER1: INT  -> type(NUMBER);

FLOATVALUE1:
(	INT '.' DIGIT+
|	INT ( '.' DIGIT+ )? [Ee] [+-]? INT
) -> type(FLOATVALUE);

DQUOTE1: ('""' | '\\"') -> type(DQUOTE);

CHAR1: (['"\\'] | '\\'['"'] | DQUOTE DQUOTE) -> type(CHAR);

//MATCH_CHAR1: ('\'\\'| '\'\'' | '\\' '\n') -> type(MATCH_CHAR); //?


TRUE1: 'true' -> type(TRUE);
FALSE1: 'false' -> type(FALSE);
NONE1: 'none' -> type(NONE);
PASS1: 'pass' -> type(PASS);
INCONC1: 'inconc' -> type(INCONC);
FAIL1: 'fail' -> type(FAIL);
ERROR1: 'error' -> type(ERROR);
Null1: 'null' -> type(Null);
NULL1: 'NULL' -> type(NULL);
OMIT1: 'omit' -> type(OMIT);

CSTRING1: DQUOTE CHAR* DQUOTE  -> type(CSTRING);

YES: 'yes';
NO: 'no';
REVERSE: 'reverse';
MSB: 'msb';
LSB: 'lsb';
BITS: 'bits';
BIT: 'bit';
OCTETS: 'octets';
OCTET: 'octet';
NIBBLE:'nibble';
WORD16: 'word16';
DWORD32: 'dword32';
ELEMENTS: 'elements';
VARIABLE: 'variable';
NULL_TERMINATED: 'null_terminated';
IEEE754_duble: 'IEEE754 double';
IEEE754_float: 'IEEE754 float';
LEFT: 'left';
RIGHT: 'right';
NOSIGN: 'nosign' ->type(UNSIGNEDKeyword);
COMPL: '2scompl';
SIGNBIT: 'signbit';
FIRST: 'first';
LAST: 'last';
LOW: 'low';
HIGH: 'high';
OTHERWISE: 'OTHERWISE';

IDENTIFIER1:
( ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
) -> type(IDENTIFIER);

//text codec section
mode TextCodec_MODE;
WS2: [ \t\f]+ -> channel(HIDDEN);

NEWLINE2: [\r\n] -> channel(HIDDEN);

LINE_COMMENT2: ( '//' ~[\r\n]* ) ->channel(HIDDEN);

BLOCK_COMMENT2:	'/*' .*? '*/' ->channel(HIDDEN);

NUMBER2: INT  -> type(NUMBER);

LPAREN2: '(' -> type(LPAREN);
RPAREN2: ')' -> type(RPAREN), mode(DEFAULT_MODE);
BEGINCHAR2: '{' -> type(BEGINCHAR);
ENDCHAR2: '}' -> type(ENDCHAR);
COMMA2: ',' -> type(COMMA);
COLON2: ':' -> type(COLON);
SEMICOLON2: ';' -> type(SEMICOLON);
ASSIGN2: '=' -> type(ASSIGN);
MINUS2: '-' -> type(MINUS);

//MATCH_CHAR: (['\'''\\']| '\'\'' | '\\' '\n'); //?
//TODO this allows some more characters than the compiler
XToken:
'\''
(  '\\' .
|  ~('\'')
)*
'\'';

LENGTHToken: 'length';
REPEATToken: 'repeatable';
CONVERTToken: 'convert';
LOWERToken: 'lower_case';
UPPERToken: 'upper_case';
JUSTToken: 'just';
LEFTToken: 'left';
RIGHTToken: 'right';
CENTERToken: 'center';
LEADINGToken: 'leading0';
TRUEToken: 'true';
FALSEToken: 'false';
SENSITIVEToken: 'case_sensitive';
INSENSITIVEToken: 'case_insensitive';

IDENTIFIER2:
( ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
) -> type(IDENTIFIER);

//XER codec section
//mode XERCodec_MODE;

//JSON codec section
mode JsonCodec_MODE;

WS4: [ \t\f]+ -> channel(HIDDEN);

BLOCK_COMMENT4:	'/*' .*? '*/' ->channel(HIDDEN);

COLON4: ':' -> type(COLON);

OMITKeyword: 'omit';
ASKeyword4: 'as' -> type(ASKeyword);
NullKeyword: 'null';
NAMEKeyword4: 'name' -> type(NAMEKeyword);
VALUEKeyword: 'value';
DEFAULTKeyword2: 'default' -> type(DEFAULTKeyword);
EXTENDKeyword2: 'extend' -> type(EXTENDKeyword);
METAINFOKeyword2: 'metainfo' -> type(METAINFOKeyword);
FORKeyword2: 'for' -> type(FORKeyword);
UNBOUNDKeyword2: 'unbouond' -> type(UNBOUNDKeyword);
NUMBERKeyword2: 'number' -> type(NUMBERKeyword);
JSONValueStart: '(' -> mode(JsonValue_MODE);

AliasToken:
( ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
);

//JSON value section
mode JsonValue_MODE;
JSONValueSegment:
( '\\\\)'
| '\\' .
| '""'
);

JSONValueEnd: ')' -> mode(JsonCodec_MODE);
//...


