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
( pr_XPaddingDef
| pr_XPrePaddingDef
| pr_XPaddingPattern
| pr_XPaddAll
| pr_XFieldOrderDef
| pr_XExtensionBitDef
| pr_XExtensionBitGroupDef
| pr_XLengthToDef
| pr_XPointerToDef
| pr_XUnitDef
| pr_XLengthIndexDef
| pr_XTagDef
| pr_XCrossTagDef
| pr_XPresenceDef
| pr_XFieldLengthDef
| pr_XAlignDef
| pr_XCompDef
| pr_XByteOrderDef
| pr_XBitOrderInFieldDef
| pr_XBitOrderInOctetDef
| pr_HexOrderDef
| pr_RepeatableDef
| pr_XTopLevelDef
| IntXKeyword
| pr_BitDef
| pr_XUTFDef
| pr_XIEEE754Def
);

pr_XSingleTEXTEncodingDef:
( pr_XBeginDef
| pr_XEndDef
| pr_XSeparatorDef
| pr_XCodingDef
);

pr_RepeatableDef: REPEATABLEKeyword LPAREN pr_XYesOrNo RPAREN;

pr_XYesOrNo: (YES | NO);

pr_XBits: (BIT | BITS);

pr_XOctets: (OCTET | OCTETS | NIBBLE | WORD16 | DWORD32 | ELEMENTS);

pr_XNumber: (NUMBER | VARIABLE | NULL_TERMINATED | IEEE754_duble | IEEE754_float);

pr_XBitsOctets: (pr_XBits | pr_XOctets | pr_XNumber);

pr_XPaddingDef:
( PADDINGKeyword LPAREN ( pr_XYesOrNo | pr_XBitsOctets ) RPAREN
);

pr_XPrePaddingDef:
( PREPADDINGKeyword LPAREN ( pr_XYesOrNo | pr_XBitsOctets ) RPAREN
);

pr_XPaddingPattern: PADDINGPATTERNKeyword LPAREN BSTRING RPAREN;

pr_XPaddAll: PADDALLKeyword;

pr_XMsbOrLsb: (MSB | LSB);

pr_XFieldOrderDef: FIELDORDERKeyword LPAREN pr_XMsbOrLsb RPAREN;

pr_XYesOrNoOrRevers: (YES | NO | REVERSE);

pr_XExtensionBitDef: EXTENSION_BITKeyword LPAREN pr_XYesOrNoOrRevers RPAREN;

pr_XExtensionBitGroupDef: EXTENSION_BIT_GROUPKeyword LPAREN pr_XYesOrNoOrRevers COMMA IDENTIFIER COMMA IDENTIFIER RPAREN;

pr_XLengthToDef:
( LENGTHTOKeyword LPAREN pr_XRecordFieldRefList RPAREN
| LENGTHTOKeyword LPAREN pr_XRecordFieldRefList PLUS NUMBER RPAREN
| LENGTHTOKeyword LPAREN pr_XRecordFieldRefList MINUS NUMBER RPAREN
);

pr_XPointerToDef: POINTERTOKeyword LPAREN pr_XRecordFieldRef RPAREN;

pr_XUnitDef:
( UNITKeyword LPAREN pr_XBitsOctets RPAREN
| PTRUNITKeyword LPAREN pr_XBitsOctets RPAREN
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

pr_XFieldLengthDef:
FIELDLENGTHKeyword LPAREN NUMBER RPAREN;

pr_XAlignDef: ALIGNKeyword LPAREN pr_XLeftOrRight RPAREN;

pr_XLeftOrRight: LEFT | RIGHT;

pr_XCompDef: COMPKeyword LPAREN pr_XCompValues RPAREN;

pr_XCompValues: (UNSIGNEDKeyword | COMPL | SIGNBIT);

pr_XByteOrderDef: BYTEORDERKeyword LPAREN pr_XfirstOrLast RPAREN;

pr_XfirstOrLast: (FIRST | LAST);

pr_XBitOrderInFieldDef: BITORDERINFIELDKeyword LPAREN pr_XMsbOrLsb RPAREN;

pr_XBitOrderInOctetDef:
( BITORDERINOCTETKeyword LPAREN pr_XMsbOrLsb RPAREN
| BITORDERKeyword LPAREN pr_XMsbOrLsb RPAREN
);

pr_XTopLevelDef: TOPLEVELKeyword LPAREN pr_XTopDefList RPAREN;

pr_XTopDefList:
( pr_XTopDef
  (COMMA pr_XTopDef )*
);

pr_XTopDef: pr_XBitOrderDef;

pr_XBitOrderDef: BITORDERKeyword LPAREN pr_XMsbOrLsb RPAREN;

pr_HexOrderDef: HEXORDERKeyword LPAREN pr_XLowOrHigh RPAREN;

pr_XLowOrHigh: (LOW | HIGH);

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
( NUMBER BitKeyword
| UNSIGNEDKeyword NUMBER BitKeyword
);

pr_XUTFDef:
( UTF8Keyword
| UTF16Keyword
);

pr_XIEEE754Def:
( IEEE754FLOATKeyword
| IEEE754DOUBLEKeyword
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
