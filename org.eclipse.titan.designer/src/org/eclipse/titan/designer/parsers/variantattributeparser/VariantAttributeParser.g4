parser grammar VariantAttributeParser;

@header {
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.*;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.IType.MessageEncoding_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.*;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute.ExtensionAttribute_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function.EncodingPrototype_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PrintingType.PrintingTypeEnum;
import org.eclipse.titan.designer.AST.TTCN3.types.*;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type.CharCoding;
}

/*
******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/
@members{
  private IFile actualFile = null;
  private int line = 0;
  private int offset = 0;

  private RawAST rawstruct;
  private int lengthMultiplier;
  private boolean raw_f = false;

  public void setActualFile(final IFile file) {
  	actualFile = file;
  }

  public void setLine(final int line) {
  	this.line = line;
  }

  public void setOffset(final int offset) {
  	this.offset = offset;
  }

  private Location getLocation(final Token token) {
  	return new Location(actualFile, line + token.getLine() - 1, offset + token.getStartIndex(), offset + token.getStopIndex() + 1);
  }

  private Location getLocation(final Token startToken, final Token endToken) {
  	if (endToken == null) {
  		return getLocation(startToken);
  	}

  	return new Location(actualFile, line - 1 + startToken.getLine(), offset + startToken.getStartIndex(), offset + endToken.getStopIndex() + 1);
  }

  public void setRawAST(final RawAST par) {
    rawstruct = par;
    raw_f = true;
  }

  public void setLengthMultiplier(final int lengthMultiplier) {
    this.lengthMultiplier = lengthMultiplier;
  }

  public boolean getRawFound() {
    return raw_f;
  }
}

/*
 * @author Kristof Szabados
 *
 * FIXME no information is extracted yet
 * */
options {
tokenVocab=VariantAttributeLexer;
}

pr_AttribSpec:
(
| pr_XEncodingDefList
| pr_XERAttributes
| pr_XBERAttributes
)
EOF
;

pr_XBERAttributes:
( LENGTHKeyword
  ACCEPTKeyword
  ( SHORTKeyword
  | LONGKeyword
  | INDEFINITEKeyword
  | DEFINITEKeyword
  )

);

pr_XEncodingDefList:
( pr_XSingleEncodingDef
 ( COMMA pr_XSingleEncodingDef )*
);

pr_XSingleEncodingDef:
( pr_XSingleRAWEncodingDef
| pr_XSingleTEXTEncodingDef
| pr_XJsonDef
);

pr_XSingleRAWEncodingDef:
( padding = pr_XPaddingDef {rawstruct.padding = $padding.padding; raw_f = true;}
| prepadding = pr_XPrePaddingDef {rawstruct.prepadding = $prepadding.prepadding; raw_f = true;}
| pr_XPaddingPattern {raw_f = true;}
| pr_XPaddAll
| fieldorder = pr_XFieldOrderDef {rawstruct.fieldorder = $fieldorder.msbOrLsb; raw_f = true;}
| extensionBit = pr_XExtensionBitDef {rawstruct.extension_bit = $extensionBit.YNR; raw_f = true;}
| pr_XExtensionBitGroupDef
| pr_XLengthToDef
| pr_XPointerToDef
| unit = pr_XUnitDef {rawstruct.extension_bit = $unit.value; raw_f = true;}
| pr_XLengthIndexDef
| pr_XTagDef
| pr_XCrossTagDef
| pr_XPresenceDef
| fieldlength = pr_XFieldLengthDef {rawstruct.fieldlength = $fieldlength.multiplier * lengthMultiplier; raw_f = true;}
| pr_XPtrOffsetDef { raw_f = true;}
| align = pr_XAlignDef {rawstruct.align = $align.leftOrRight; raw_f = true;}
| comp = pr_XCompDef {rawstruct.comp = $comp.comp; raw_f = true;}
| byteOrder = pr_XByteOrderDef {rawstruct.byteorder = $byteOrder.firstOrLast; raw_f = true;}
| bitOrderInField = pr_XBitOrderInFieldDef {rawstruct.bitorderinfield = $bitOrderInField.msbOrLsb; raw_f = true;}
| bitOrderInOctet = pr_XBitOrderInOctetDef {rawstruct.bitorderinoctet = $bitOrderInOctet.msbOrLsb; raw_f = true;}
| hexOrder = pr_HexOrderDef {rawstruct.hexorder = $hexOrder.lowOrHigh; raw_f = true;}
| pr_RepeatableDef
| pr_XTopLevelDef {rawstruct.toplevelind = 1; raw_f = true;}
| IntXKeyword  {rawstruct.intX = true; raw_f = true;}
| pr_BitDef {raw_f = true;}
| pr_XUTFDef {raw_f = true;}
| pr_XIEEE754Def {raw_f = true;}
);

pr_XSingleTEXTEncodingDef:
( pr_XBeginDef
| pr_XEndDef
| pr_XSeparatorDef
| pr_XCodingDef
);

pr_RepeatableDef: REPEATABLEKeyword LPAREN pr_XYesOrNo RPAREN;

pr_XYesOrNo returns [int yesOrNo]:
( YES {$yesOrNo = RawAST.XDEFYES;}
| NO {$yesOrNo = RawAST.XDEFNO;}
)
;

pr_XBits returns [int value]:
( BIT
| BITS
)
{$value = 1;};

pr_XOctets returns [int value]:
( OCTET {$value = 8;}
| OCTETS {$value = 8;}
| NIBBLE {$value = 4;}
| WORD16 {$value = 16;}
| DWORD32 {$value = 32;}
| ELEMENTS {$value = -1;}
);

pr_XNumber returns [int value]:
( NUMBER {$value = Integer.valueOf($NUMBER.getText()).intValue();}
| VARIABLE {$value = 0;}
| NULL_TERMINATED {$value = -1;}
| IEEE754_duble {$value = 64;}
| IEEE754_float {$value = 32;}
);

pr_XBitsOctets returns [int value]:
( a = pr_XBits {$value = $a.value;}
| b = pr_XOctets {$value = $b.value;}
| c = pr_XNumber {$value = $c.value;}
);

pr_XPaddingDef returns [int padding]:
( PADDINGKeyword
  LPAREN
  ( a = pr_XYesOrNo {$padding = $a.yesOrNo == RawAST.XDEFYES ? 8:0;}
  | b = pr_XBitsOctets {$padding = $b.value;}
  )
  RPAREN
);

pr_XPrePaddingDef returns [int prepadding]:
( PREPADDINGKeyword
  LPAREN
  ( a = pr_XYesOrNo {$prepadding = $a.yesOrNo == RawAST.XDEFYES ? 8:0;}
  | b = pr_XBitsOctets {$prepadding = $b.value;}
  )
  RPAREN
);

pr_XPaddingPattern:
( PADDINGPATTERNKeyword
  LPAREN
  BSTRING
  RPAREN
)
{
  final String text = $BSTRING.text;
  if (text != null) {
    String realText = text.replaceAll("^\'|\'B$", "");
    realText = realText.replaceAll("\\s+","");
    int len = realText.length();
    rawstruct.padding_pattern = realText;
    rawstruct.padding_pattern_length = len;
    while ( rawstruct.padding_pattern_length % 8 != 0) {
      rawstruct.padding_pattern = rawstruct.padding_pattern + realText;
      rawstruct.padding_pattern_length += len;
    }
  }
};

pr_XPaddAll: PADDALLKeyword;

pr_XMsbOrLsb returns [int msbOrLsb]:
( MSB {$msbOrLsb = RawAST.XDEFMSB;}
| LSB {$msbOrLsb = RawAST.XDEFMSB;}
);

pr_XFieldOrderDef returns [int msbOrLsb]:
( FIELDORDERKeyword LPAREN v = pr_XMsbOrLsb RPAREN
)
{ $msbOrLsb = $v.msbOrLsb;};

pr_XYesOrNoOrReverse returns [int YNR]:
( YES {$YNR = RawAST.XDEFYES;}
| NO {$YNR = RawAST.XDEFNO;}
| REVERSE {$YNR = RawAST.XDEFREVERSE;}
);

pr_XExtensionBitDef returns [int YNR]:
( EXTENSION_BITKeyword LPAREN v = pr_XYesOrNoOrReverse RPAREN
)
{ $YNR = $v.YNR;};

pr_XExtensionBitGroupDef: EXTENSION_BIT_GROUPKeyword LPAREN pr_XYesOrNoOrReverse COMMA IDENTIFIER COMMA IDENTIFIER RPAREN;

pr_XLengthToDef:
( LENGTHTOKeyword LPAREN pr_XRecordFieldRefList RPAREN
| LENGTHTOKeyword LPAREN pr_XRecordFieldRefList PLUS NUMBER RPAREN
| LENGTHTOKeyword LPAREN pr_XRecordFieldRefList MINUS NUMBER RPAREN
);

pr_XPointerToDef: POINTERTOKeyword LPAREN pr_XRecordFieldRef RPAREN;

pr_XUnitDef returns [int value]:
( UNITKeyword LPAREN a = pr_XBitsOctets RPAREN {$value = $a.value;}
| PTRUNITKeyword LPAREN b = pr_XBitsOctets RPAREN {$value = $b.value;}
);

pr_XLengthIndexDef: LENGTHINDEXKeyword LPAREN pr_XStructFieldRef? RPAREN;

pr_XTagDef: TAGKeyword LPAREN pr_XAssocList RPAREN;

pr_XCrossTagDef: CROSSTAGKeyword LPAREN pr_XAssocList RPAREN;

pr_XAssocList:
( pr_XAssocElement
  ( SEMICOLON pr_XAssocElement)*
);

pr_XAssocElement:
( IDENTIFIER COMMA ( pr_XkeyIdOrIdList | OTHERWISE )
);

pr_XkeyIdOrIdList:
( pr_XKeyId
| pr_XKeyIdList
);

pr_XKeyIdList: BEGINCHAR pr_XMultiKeyId ENDCHAR;

pr_XMultiKeyId:
( pr_XKeyId
  ( COMMA pr_XKeyId )*
);

pr_XKeyId: pr_XStructFieldRef ASSIGN pr_XRValue;

pr_XPresenceDef:
( PRESENCEKeyword LPAREN ( pr_XKeyIdList | pr_XMultiKeyId ) SEMICOLON? RPAREN
);

pr_XFieldLengthDef returns [int multiplier]:
FIELDLENGTHKeyword LPAREN NUMBER RPAREN
{
	$multiplier = Integer.valueOf($NUMBER.getText()).intValue();
};

pr_XPtrOffsetDef:
( PTROFFSETKeyword LPAREN NUMBER RPAREN {rawstruct.ptroffset =Integer.valueOf($NUMBER.getText()).intValue();}
| PTROFFSETKeyword LPAREN IDENTIFIER RPAREN {final String text = $IDENTIFIER.text;
		if ( text != null) {
			rawstruct.ptrbase = new Identifier( Identifier_type.ID_TTCN, text, getLocation( $IDENTIFIER ) );
		};}
);


pr_XAlignDef returns [int leftOrRight]:
( ALIGNKeyword LPAREN v = pr_XLeftOrRight RPAREN
)
{ $leftOrRight = $v.leftOrRight;};

pr_XLeftOrRight returns [int leftOrRight]:
( LEFT {$leftOrRight = RawAST.XDEFLEFT;}
| RIGHT {$leftOrRight = RawAST.XDEFRIGHT;}
);

pr_XCompDef returns [int comp]:
( COMPKeyword LPAREN v = pr_XCompValues RPAREN
)
{$comp = $v.comp;};

pr_XCompValues returns [int comp]:
( UNSIGNEDKeyword {$comp = RawAST.XDEFUNSIGNED;}
| COMPL {$comp = RawAST.XDEFCOMPL;}
| SIGNBIT {$comp = RawAST.XDEFSIGNBIT;}
);

pr_XByteOrderDef returns [int firstOrLast]:
( BYTEORDERKeyword LPAREN v = pr_XfirstOrLast RPAREN
)
{$firstOrLast = $v.firstOrLast;};

pr_XfirstOrLast returns [int firstOrLast]:
( FIRST {$firstOrLast = RawAST.XDEFFIRST;}
| LAST {$firstOrLast = RawAST.XDEFLAST;}
);

pr_XBitOrderInFieldDef returns [int msbOrLsb]:
( BITORDERINFIELDKeyword LPAREN v = pr_XMsbOrLsb RPAREN
)
{$msbOrLsb = $v.msbOrLsb;};

pr_XBitOrderInOctetDef returns [int msbOrLsb]:
( BITORDERINOCTETKeyword LPAREN v = pr_XMsbOrLsb RPAREN {$msbOrLsb = $v.msbOrLsb;}
| BITORDERKeyword LPAREN v = pr_XMsbOrLsb RPAREN {$msbOrLsb = $v.msbOrLsb;}
);

pr_XTopLevelDef: TOPLEVELKeyword LPAREN pr_XTopDefList RPAREN;

pr_XTopDefList:
( pr_XTopDef
  (COMMA pr_XTopDef )*
);

pr_XTopDef:
( v = pr_XBitOrderDef
)
{rawstruct.toplevel.bitorder = $v.msbOrLsb; raw_f = true;};

pr_XBitOrderDef returns [int msbOrLsb]:
( BITORDERKeyword LPAREN v = pr_XMsbOrLsb RPAREN
)
{$msbOrLsb = $v.msbOrLsb;};

pr_HexOrderDef returns [int lowOrHigh]:
( HEXORDERKeyword LPAREN v = pr_XLowOrHigh RPAREN
)
{$lowOrHigh = $v.lowOrHigh;};

pr_XLowOrHigh returns [int lowOrHigh]:
( LOW {$lowOrHigh = RawAST.XDEFLOW;}
| HIGH {$lowOrHigh = RawAST.XDEFHIGH;}
);

pr_XRecordFieldRefList:
( pr_XRecordFieldRef
  ( COMMA pr_XRecordFieldRef )*
);

pr_XRecordFieldRef: IDENTIFIER;

pr_XStructFieldRef:
( IDENTIFIER
  ( DOT IDENTIFIER )*
);

pr_XRValue:
( IDENTIFIER
| BSTRING
| HSTRING
| OSTRING
| CSTRING
| FLOATVALUE
| NUMBER
| TRUE
| FALSE
| NONE
| PASS
| INCONC
| FAIL
| ERROR
| Null
| NULL
| OMIT
);

pr_BitDef:
( NUMBER BitKeyword {rawstruct.fieldlength = Integer.valueOf($NUMBER.getText()).intValue();
			rawstruct.comp = RawAST.XDEFSIGNBIT;
			rawstruct.byteorder = RawAST.XDEFLAST;}
| UNSIGNEDKeyword NUMBER BitKeyword {rawstruct.fieldlength = Integer.valueOf($NUMBER.getText()).intValue();
					rawstruct.comp = RawAST.XDEFUNSIGNED;
					rawstruct.byteorder = RawAST.XDEFLAST;}
);

pr_XUTFDef:
( UTF8Keyword {rawstruct.stringformat = CharCoding.UTF_8;}
| UTF16Keyword {rawstruct.stringformat = CharCoding.UTF16;}
);

pr_XIEEE754Def:
( IEEE754FLOATKeyword {rawstruct.fieldlength = 32;}
| IEEE754DOUBLEKeyword {rawstruct.fieldlength = 64;}
);

// TEXT encoding rules

pr_XBeginDef:
( BEGINKeyword LPAREN pr_XEncodeToken
  ( COMMA pr_XMatchDef
    ( COMMA pr_XModifierDef )?
  )?
  RPAREN
);

pr_XEndDef:
( ENDKeyword LPAREN pr_XEncodeToken
  ( COMMA pr_XMatchDef
    ( COMMA pr_XModifierDef )?
  )?
  RPAREN
);

pr_XSeparatorDef:
( SEPARATORKeyword LPAREN pr_XEncodeToken
  ( COMMA pr_XMatchDef
    ( COMMA pr_XModifierDef )?
  )?
  RPAREN
);

pr_XCodingDef:
( TEXT_CODINGKeyword LPAREN pr_XCodingRule
  ( COMMA pr_XDecodingRule
    ( COMMA pr_XMatchDef
      ( COMMA pr_XModifierDef )?
    )?
  )?
  RPAREN
);

pr_XCodingRule:
(
| pr_XAttrListEnc
| pr_XTokenDefList
);

pr_XDecodingRule:
(
| pr_XAttrList
| pr_XDecodingTokenDefList
);

pr_XAttrList:
( pr_XAttr
  ( SEMICOLON pr_XAttr )*
);

pr_XAttr:
( LENGTHToken ASSIGN ( NUMBER | NUMBER MINUS NUMBER )
| CONVERTToken ASSIGN ( LOWERToken | UPPERToken)
| JUSTToken ASSIGN ( LEFTToken | RIGHTToken | CENTERToken )
| LEADINGToken ASSIGN ( TRUEToken | FALSEToken )
| REPEATToken ASSIGN ( TRUEToken | FALSEToken )
);

pr_XAttrListEnc:
( pr_XAttrEnc
  ( SEMICOLON pr_XAttrEnc )*
);

pr_XAttrEnc:
( LENGTHToken ASSIGN ( NUMBER | NUMBER MINUS NUMBER )
| CONVERTToken ASSIGN ( LOWERToken | UPPERToken )
| JUSTToken ASSIGN ( LEFTToken | RIGHTToken | CENTERToken )
| LEADINGToken ASSIGN ( TRUEToken | FALSEToken )
| REPEATToken ASSIGN ( TRUEToken | FALSEToken )
);

pr_XTokenDefList:
( pr_XTokenDef
  ( SEMICOLON pr_XTokenDef )*
);

pr_XTokenDef:
( pr_XIdentifierOrReserved COLON pr_XEncodeToken
| TRUEToken COLON pr_XEncodeToken
| FALSEToken COLON pr_XEncodeToken
);

pr_XIdentifierOrReserved:
( IDENTIFIER
| pr_XTextReservedWord
);

pr_XTextReservedWord:
( LENGTHToken
| REPEATToken
| CONVERTToken
| LOWERToken
| UPPERToken
| JUSTToken
| LEFTToken
| RIGHTToken
| CENTERToken
| LEADINGToken
| SENSITIVEToken
| INSENSITIVEToken
);

pr_XDecodingTokenDefList:
( pr_XDecodingTokenDef
  ( SEMICOLON pr_XDecodingTokenDef )*
);

pr_XDecodingTokenDef:
( pr_XIdentifierOrReserved COLON pr_XDecodeToken
| TRUEToken COLON pr_XDecodeToken
| FALSEToken COLON pr_XDecodeToken
);

pr_XEncodeToken: XToken;

pr_XDecodeToken:
( XToken
| BEGINCHAR XToken ENDCHAR
| BEGINCHAR pr_XMatchDef COMMA pr_XModifierDef ENDCHAR
);

pr_XMatchDef:
( XToken
)?;

pr_XModifierDef:
(
| SENSITIVEToken
| INSENSITIVEToken
);

// XER encoding rules
pr_XERAttributes: pr_XERAttribute;

pr_XERAttribute:
( ABSTRACTKeyword
| pr_anyAttributes
| pr_anyElement
| ATTRIBUTEKeyword
| ATTRIBUTEFROMUALIFIED
| BLOCKKeyword
| pr_controlNameSpace
| pr_defaultForEmpty
| ELEMENTKeyword
| ELEMENTFROMQUALIFIEDKeyword
| EMBEDVALUESKeyword
| pr_from
| pr_fractionDigits
| LISTKeyword
| pr_name
| pr_namespace
| pr_text
| UNTAGGEDKeyword
| USENILKeyword
| USENUMBERKeyword
| USEORDERKeyword
| USEUNIONKeyword
| USETYPEKeyword
| pr_whitesapce
| XSDKeyword COLON pr_xsddata
);

pr_anyAttributes: ANYATTRIBUTESKeyword pr_optNameSpaceRestriction;

pr_anyElement: ANYELEMENTKeyword pr_optNameSpaceRestriction;

pr_optNameSpaceRestriction:
( 
| FROMKeyword pr_urilist
| EXCEPTKeyword pr_urilist
);

pr_urilist:
( pr_quotedURIOrAbsent
  ( COMMA pr_quotedURIOrAbsent )*
);

pr_quotedURIOrAbsent:
( XSTRING
| UNQUALIFIEDKeyword
);

pr_controlNameSpace: CONTROLNAMESPACEKeyword XSTRING;

pr_from: FROMKeyword ASKeyword ( UNQUALIFIEDKeyword | QUALIFIEDKeyword );

pr_name: NAMEKeyword ASKeyword pr_newNameOrKeyword;

pr_newNameOrKeyword:
( pr_keyword
| XSTRING
);

pr_keyword:
( CAPITALIZEDKeyword
| UNCAPITALIZEDKeyword
| LOWERCASEDKeyword
| UPPERCASEDKeyword
);

pr_namespace: NAMESPACEKeyword pr_namespaceSpefication;

pr_namespaceSpefication: ASKeyword XSTRING pr_optPrefix;

pr_optPrefix:
( 
| PREFIXKeyword XSTRING
);

pr_text:
( TEXTKeyword
  (
  | XSTRING ASKeyword pr_newNameOrKeyword
  | ALLKeyword ASKeyword pr_newNameOrKeyword
  )
);

pr_defaultForEmpty:
( DEFAULTFOREMPTYKeyword ASKeyword IDENTIFIER
  ( DOT IDENTIFIER )?
);

pr_fractionDigits:
( FRACTIONDIGITSKeyword NUMBER
);

pr_whitesapce:
( WHITESPACEKeyword
  ( PRESERVEKeyword
  | REPLACEKeyword
  | COLLAPSEKeyword
  )
);

pr_xsddata:
( XSDbase64Binary
| XSDdecimal
| XSDhexBinary
| XSDQName
| SHORTKeyword
| LONGKeyword
| XSDstring
| XSDnormalizedString
| XSDtoken
| XSDName
| XSDNMToken
| XSDNCName
| XSDID
| XSDIDREF
| XSDENTITY
| XSDanyURI
| XSDlanguage
| XSDinteger
| XSDpositiveInteger
| XSDnonPositiveInteger
| XSDnegativeInteger
| XSDnonNegativeInteger
| XSDunsignedLong
| XSDint
| XSDunsignedInt
| XSDunsignedShort
| XSDbyte
| XSDunsignedByte
| XSDfloat
| XSDdouble
| XSDduration
| XSDdateTime
| XSDtime
| XSDdate
| XSDgYearMonth
| XSDgYear
| XSDgMonthDay
| XSDgDay
| XSDgMonth
| XSDNMTOKENS
| XSDIDREFS
| XSDENTITIES
| XSDboolean
| XSDanySimpleType
| XSDanyType
);



// JSON encoding rules
pr_XJsonDef: JSONKeyword COLON pr_XJsonAttribute;

pr_XJsonAttribute:
( pr_XOmitAsNull
| pr_XNameAs
| pr_XAsValue
| pr_Default
| pr_Extend
| pr_XMetainfoForUnbound
| pr_XAsNumber
);

pr_XOmitAsNull: OMITKeyword ASKeyword NullKeyword;

pr_XNameAs: NAMEKeyword ASKeyword pr_JsonAlias;

pr_JsonAlias:
( AliasToken
| OMITKeyword
| ASKeyword
| NullKeyword
| NAMEKeyword
| VALUEKeyword
| DEFAULTKeyword
| EXTENDKeyword
| METAINFOKeyword
| FORKeyword
| UNBOUNDKeyword
| NUMBERKeyword
);

pr_XAsValue: ASKeyword VALUEKeyword;

pr_Default: DEFAULTKeyword pr_JsonValue;

pr_Extend: EXTENDKeyword pr_JsonValue COLON pr_JsonValue;

pr_JsonValue:
( JSONValueStart pr_JsonValueCore JSONValueEnd
| JSONValueStart JSONValueEnd
);

pr_JsonValueCore:
( JSONValueSegment
  ( JSONValueSegment )*
);

pr_XMetainfoForUnbound: METAINFOKeyword FORKeyword UNBOUNDKeyword;

pr_XAsNumber: ASKeyword NUMBERKeyword;
