parser grammar VariantAttributeParser;

@header {
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.titan.designer.AST.*;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.IType.MessageEncoding_type;
import org.eclipse.titan.designer.AST.ASN1.values.ASN1_Null_Value;
import org.eclipse.titan.designer.AST.TTCN3.attributes.*;
import org.eclipse.titan.designer.AST.TTCN3.attributes.ExtensionAttribute.ExtensionAttribute_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function.EncodingPrototype_type;
import org.eclipse.titan.designer.AST.TTCN3.attributes.PrintingType.PrintingTypeEnum;
import org.eclipse.titan.designer.AST.TTCN3.types.*;
import org.eclipse.titan.designer.AST.TTCN3.types.CharString_Type.CharCoding;
import org.eclipse.titan.designer.AST.TTCN3.values.*;
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
 * TODO only RAW attributes are extracted for now.
 * */
options {
tokenVocab=VariantAttributeLexer;
}

pr_AttribSpec:
(	/* empty */
|	pr_XEncodingDefList
|	pr_XERAttributes
|	pr_XBERAttributes
)
EOF
;

pr_XBERAttributes:
(	LENGTHKeyword
	ACCEPTKeyword
	(	SHORTKeyword
	|	LONGKeyword
	|	INDEFINITEKeyword
	|	DEFINITEKeyword
	)

);

pr_XEncodingDefList:
(	pr_XSingleEncodingDef
	(	COMMA
		pr_XSingleEncodingDef
	)*
);

pr_XSingleEncodingDef:
(	pr_XSingleRAWEncodingDef
|	pr_XSingleTEXTEncodingDef
|	pr_XJsonDef
);

pr_XSingleRAWEncodingDef:
(	padding = pr_XPaddingDef		{rawstruct.padding = $padding.padding; raw_f = true;}
|	prepadding = pr_XPrePaddingDef		{rawstruct.prepadding = $prepadding.prepadding; raw_f = true;}
|	pr_XPaddingPattern			{raw_f = true;}
|	pr_XPaddAll				{rawstruct.paddall = RawAST.XDEFYES; raw_f = true;}
|	fieldorder = pr_XFieldOrderDef		{rawstruct.fieldorder = $fieldorder.msbOrLsb; raw_f = true;}
|	extensionBit = pr_XExtensionBitDef	{rawstruct.extension_bit = $extensionBit.YNR; raw_f = true;}
|	pr_XExtensionBitGroupDef		{ raw_f = true;}
|	pr_XLengthToDef				{ raw_f = true;}
|	pointerto = pr_XPointerToDef		{rawstruct.pointerto = $pointerto.identifier; raw_f = true;}
|	unit = pr_XUnitDef			{rawstruct.extension_bit = $unit.value; raw_f = true;}
|	lengthindex = pr_XLengthIndexDef	{rawstruct.lengthindex = new RawAST.rawAST_field_list();
							rawstruct.lengthindex.names = $lengthindex.values; raw_f = true;}
|	pr_XTagDef				{raw_f = true;}
|	pr_XCrossTagDef				{raw_f = true;}
|	pr_XPresenceDef				{raw_f = true;}
|	fieldlength = pr_XFieldLengthDef	{rawstruct.fieldlength = $fieldlength.multiplier * lengthMultiplier; raw_f = true;}
|	pr_XPtrOffsetDef			{ raw_f = true;}
|	align = pr_XAlignDef			{rawstruct.align = $align.leftOrRight; raw_f = true;}
|	comp = pr_XCompDef			{rawstruct.comp = $comp.comp; raw_f = true;}
|	byteOrder = pr_XByteOrderDef		{rawstruct.byteorder = $byteOrder.firstOrLast; raw_f = true;}
|	bitOrderInField = pr_XBitOrderInFieldDef {rawstruct.bitorderinfield = $bitOrderInField.msbOrLsb; raw_f = true;}
|	bitOrderInOctet = pr_XBitOrderInOctetDef {rawstruct.bitorderinoctet = $bitOrderInOctet.msbOrLsb; raw_f = true;}
|	hexOrder = pr_HexOrderDef		{rawstruct.hexorder = $hexOrder.lowOrHigh; raw_f = true;}
|	repeatable = pr_RepeatableDef		{rawstruct.repeatable = $repeatable.yesOrNo; raw_f = true;}
|	pr_XTopLevelDef				{rawstruct.toplevelind = 1; raw_f = true;}
|	IntXKeyword				{rawstruct.intX = true; raw_f = true;}
|	pr_BitDef				{raw_f = true;}
|	pr_XUTFDef				{raw_f = true;}
|	pr_XIEEE754Def				{raw_f = true;}
);

pr_XSingleTEXTEncodingDef:
(	pr_XBeginDef
|	pr_XEndDef
|	pr_XSeparatorDef
|	pr_XCodingDef
);

pr_RepeatableDef returns [int yesOrNo]:
(	REPEATABLEKeyword
	LPAREN
	v = pr_XYesOrNo
	RPAREN
)
{ $yesOrNo = $v.yesOrNo;};

pr_XYesOrNo returns [int yesOrNo]:
(	YES	{$yesOrNo = RawAST.XDEFYES;}
|	NO	{$yesOrNo = RawAST.XDEFNO;}
)
;

pr_XBits returns [int value]:
(	BIT
|	BITS
)
{$value = 1;};

pr_XOctets returns [int value]:
(	OCTET		{$value = 8;}
|	OCTETS		{$value = 8;}
|	NIBBLE		{$value = 4;}
|	WORD16		{$value = 16;}
|	DWORD32		{$value = 32;}
|	ELEMENTS	{$value = -1;}
);

pr_XNumber returns [int value]:
(	NUMBER		{$value = Integer.valueOf($NUMBER.getText()).intValue();}
|	VARIABLE	{$value = 0;}
|	NULL_TERMINATED	{$value = -1;}
|	IEEE754_duble	{$value = 64;}
|	IEEE754_float	{$value = 32;}
);

pr_XBitsOctets returns [int value]:
(	a = pr_XBits	{$value = $a.value;}
|	b = pr_XOctets	{$value = $b.value;}
|	c = pr_XNumber	{$value = $c.value;}
);

pr_XPaddingDef returns [int padding]:
(	PADDINGKeyword
	LPAREN
	(	a = pr_XYesOrNo		{$padding = $a.yesOrNo == RawAST.XDEFYES ? 8:0;}
	|	b = pr_XBitsOctets	{$padding = $b.value;}
	)
	RPAREN
);

pr_XPrePaddingDef returns [int prepadding]:
(	PREPADDINGKeyword
	LPAREN
	(	a = pr_XYesOrNo		{$prepadding = $a.yesOrNo == RawAST.XDEFYES ? 8:0;}
	|	b = pr_XBitsOctets	{$prepadding = $b.value;}
	)
	RPAREN
);

pr_XPaddingPattern:
(	PADDINGPATTERNKeyword
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
(	MSB	{$msbOrLsb = RawAST.XDEFMSB;}
|	LSB	{$msbOrLsb = RawAST.XDEFLSB;}
);

pr_XFieldOrderDef returns [int msbOrLsb]:
(	FIELDORDERKeyword
	LPAREN
	v = pr_XMsbOrLsb
	RPAREN
)
{ $msbOrLsb = $v.msbOrLsb;};

pr_XYesOrNoOrReverse returns [int YNR]:
(	YES	{$YNR = RawAST.XDEFYES;}
|	NO	{$YNR = RawAST.XDEFNO;}
|	REVERSE	{$YNR = RawAST.XDEFREVERSE;}
);

pr_XExtensionBitDef returns [int YNR]:
(	EXTENSION_BITKeyword
	LPAREN
	v = pr_XYesOrNoOrReverse
	RPAREN
)
{ $YNR = $v.YNR;};

pr_XExtensionBitGroupDef:
(	EXTENSION_BIT_GROUPKeyword
	LPAREN
	bit = pr_XYesOrNoOrReverse
	COMMA
	from = IDENTIFIER
	COMMA
	to = IDENTIFIER
	RPAREN
)
{
	if (rawstruct.ext_bit_groups == null) {
		rawstruct.ext_bit_groups = new ArrayList<RawAST.rawAST_ext_bit_group>();
	}
	RawAST.rawAST_ext_bit_group temp = new RawAST.rawAST_ext_bit_group();
	temp.ext_bit = $bit.YNR;
	final String fromtext = $from.text;
	if ( fromtext != null) {
		temp.from = new Identifier( Identifier_type.ID_TTCN, fromtext, getLocation( $from ) );
	};
	final String totext = $to.text;
	if ( totext != null) {
		temp.to = new Identifier( Identifier_type.ID_TTCN, totext, getLocation( $to ) );
	};
	rawstruct.ext_bit_groups.add(temp);
};

pr_XLengthToDef:
(	LENGTHTOKeyword
	LPAREN
	pr_XRecordFieldRefList
	(
	|	PLUS n1 = NUMBER	{rawstruct.lengthto_offset = Integer.valueOf($n1.getText()).intValue();}
	|	MINUS n2 = NUMBER	{rawstruct.lengthto_offset = (-1) * Integer.valueOf($n2.getText()).intValue();}
	)
	RPAREN
);

pr_XPointerToDef returns [Identifier identifier]:
(	POINTERTOKeyword
	LPAREN
	id = pr_XRecordFieldRef
	RPAREN
)
{ $identifier = $id.identifier;
};

pr_XUnitDef returns [int value]:
(	UNITKeyword LPAREN a = pr_XBitsOctets RPAREN	{$value = $a.value;}
|	PTRUNITKeyword LPAREN b = pr_XBitsOctets RPAREN	{$value = $b.value;}
);

pr_XLengthIndexDef returns [ArrayList<Identifier> values = null]:
(	LENGTHINDEXKeyword
	LPAREN
	(	fields = pr_XStructFieldRef	{$values = $fields.values;}
	)?
	RPAREN
);

pr_XTagDef:
(	TAGKeyword
	LPAREN
	taglist = pr_XAssocList	{rawstruct.taglist = $taglist.taglist;}
	SEMICOLON?
	RPAREN
);

pr_XCrossTagDef:
(	CROSSTAGKeyword
	LPAREN
	taglist = pr_XAssocList	{rawstruct.crosstaglist = $taglist.taglist;}
	SEMICOLON?
	RPAREN
);

pr_XAssocList returns [ArrayList<RawAST.rawAST_single_tag> taglist]:
(	element1 = pr_XAssocElement	{$taglist = new ArrayList<RawAST.rawAST_single_tag>();
					$taglist.add($element1.singleTag);}
	(	SEMICOLON
		element2 = pr_XAssocElement {//FIXME check for duplication
					$taglist.add($element2.singleTag);}
	)*
);

pr_XAssocElement returns [RawAST.rawAST_single_tag singleTag]:
(	id = IDENTIFIER
	COMMA
	(	list = pr_XkeyIdOrIdList { $singleTag = new RawAST.rawAST_single_tag();
					$singleTag.fieldName = new Identifier( Identifier_type.ID_TTCN, $id.text, getLocation( $id ) );
					$singleTag.keyList = $list.singleTag.keyList;}
	|	OTHERWISE		{ $singleTag = new RawAST.rawAST_single_tag();
					$singleTag.fieldName = new Identifier( Identifier_type.ID_TTCN, $id.text, getLocation( $id ) );
					$singleTag.keyList = null;}
	)
);

pr_XkeyIdOrIdList returns [RawAST.rawAST_single_tag singleTag]:
(	key = pr_XKeyId		{ $singleTag = new RawAST.rawAST_single_tag();
				$singleTag.fieldName = null;
				$singleTag.keyList = new ArrayList<RawAST.rawAST_tag_field_value>();
				$singleTag.keyList.add($key.tagFieldValue);}
|	keys = pr_XKeyIdList	{$singleTag = $keys.singleTag;}
);

pr_XKeyIdList returns [RawAST.rawAST_single_tag singleTag]:
(	BEGINCHAR
	keys = pr_XMultiKeyId
	ENDCHAR
) {$singleTag = $keys.singleTag;};

pr_XMultiKeyId returns [RawAST.rawAST_single_tag singleTag]:
(	key1 = pr_XKeyId	{ if ($key1.tagFieldValue != null) {
				$singleTag = new RawAST.rawAST_single_tag();
				$singleTag.fieldName = null;
				$singleTag.keyList = new ArrayList<RawAST.rawAST_tag_field_value>();
				$singleTag.keyList.add($key1.tagFieldValue);
				}}
	(	COMMA
		key2 = pr_XKeyId	{ if ($key2.tagFieldValue != null) {$singleTag.keyList.add($key2.tagFieldValue);}}
	)*
);

pr_XKeyId returns [RawAST.rawAST_tag_field_value tagFieldValue]:
(	fieldref = pr_XStructFieldRef
	ASSIGN
	value = pr_XRValue
) {
	$tagFieldValue = new RawAST.rawAST_tag_field_value();
	$tagFieldValue.keyField = new RawAST.rawAST_field_list();
	$tagFieldValue.keyField.names = $fieldref.values;
	$tagFieldValue.value = $value.value;
	$tagFieldValue.v_value = $value.v_value;
};

pr_XPresenceDef:
(	PRESENCEKeyword
	LPAREN
	(	key = pr_XKeyIdList	{rawstruct.presence = $key.singleTag;}
	|	keys = pr_XMultiKeyId	{rawstruct.presence = $keys.singleTag;}
	)
	SEMICOLON?
	RPAREN
);

pr_XFieldLengthDef returns [int multiplier]:
(	FIELDLENGTHKeyword
	LPAREN
	NUMBER
	RPAREN
) {
	$multiplier = Integer.valueOf($NUMBER.getText()).intValue();
};

pr_XPtrOffsetDef:
(	PTROFFSETKeyword
	LPAREN
	(	NUMBER		{rawstruct.ptroffset = Integer.valueOf($NUMBER.getText()).intValue();}
	|	IDENTIFIER	{final String text = $IDENTIFIER.text;
				if ( text != null) {
					rawstruct.ptrbase = new Identifier( Identifier_type.ID_TTCN, text, getLocation( $IDENTIFIER ) );
				};}
	)
	RPAREN
);


pr_XAlignDef returns [int leftOrRight]:
(	ALIGNKeyword
	LPAREN
	v = pr_XLeftOrRight
	RPAREN
)
{ $leftOrRight = $v.leftOrRight;};

pr_XLeftOrRight returns [int leftOrRight]:
(	LEFT	{$leftOrRight = RawAST.XDEFLEFT;}
|	RIGHT	{$leftOrRight = RawAST.XDEFRIGHT;}
);

pr_XCompDef returns [int comp]:
(	COMPKeyword
	LPAREN
	v = pr_XCompValues
	RPAREN
)
{$comp = $v.comp;};

pr_XCompValues returns [int comp]:
(	UNSIGNEDKeyword	{$comp = RawAST.XDEFUNSIGNED;}
|	COMPL		{$comp = RawAST.XDEFCOMPL;}
|	SIGNBIT		{$comp = RawAST.XDEFSIGNBIT;}
);

pr_XByteOrderDef returns [int firstOrLast]:
(	BYTEORDERKeyword
	LPAREN
	v = pr_XfirstOrLast
	RPAREN
)
{$firstOrLast = $v.firstOrLast;};

pr_XfirstOrLast returns [int firstOrLast]:
(	FIRST	{$firstOrLast = RawAST.XDEFFIRST;}
|	LAST	{$firstOrLast = RawAST.XDEFLAST;}
);

pr_XBitOrderInFieldDef returns [int msbOrLsb]:
(	BITORDERINFIELDKeyword
	LPAREN
	v = pr_XMsbOrLsb
	RPAREN
)
{$msbOrLsb = $v.msbOrLsb;};

pr_XBitOrderInOctetDef returns [int msbOrLsb]:
(	BITORDERINOCTETKeyword LPAREN v = pr_XMsbOrLsb RPAREN	{$msbOrLsb = $v.msbOrLsb;}
|	BITORDERKeyword LPAREN v = pr_XMsbOrLsb RPAREN		{$msbOrLsb = $v.msbOrLsb;}
);

pr_XTopLevelDef: TOPLEVELKeyword LPAREN pr_XTopDefList RPAREN;

pr_XTopDefList:
(	pr_XTopDef
	(	COMMA
		pr_XTopDef
	)*
);

pr_XTopDef:
(	v = pr_XBitOrderDef
)
{rawstruct.toplevel.bitorder = $v.msbOrLsb; raw_f = true;};

pr_XBitOrderDef returns [int msbOrLsb]:
(	BITORDERKeyword
	LPAREN
	v = pr_XMsbOrLsb
	RPAREN
)
{$msbOrLsb = $v.msbOrLsb;};

pr_HexOrderDef returns [int lowOrHigh]:
(	HEXORDERKeyword
	LPAREN
	v = pr_XLowOrHigh
	RPAREN
)
{$lowOrHigh = $v.lowOrHigh;};

pr_XLowOrHigh returns [int lowOrHigh]:
(	LOW	{$lowOrHigh = RawAST.XDEFLOW;}
|	HIGH	{$lowOrHigh = RawAST.XDEFHIGH;}
);

pr_XRecordFieldRefList:
(	a = pr_XRecordFieldRef		{ rawstruct.lengthto = new ArrayList<Identifier>();
					if ($a.identifier != null) {rawstruct.lengthto.add($a.identifier);}}
	(	COMMA
		b = pr_XRecordFieldRef	{ if ($b.identifier != null) {rawstruct.lengthto.add($b.identifier);}}
	)*
);

pr_XRecordFieldRef returns [Identifier identifier]:
(	IDENTIFIER
)
{	final String text = $IDENTIFIER.text;
	if ( text != null) {
		$identifier = new Identifier( Identifier_type.ID_TTCN, text, getLocation( $IDENTIFIER ) );
	};
};

pr_XStructFieldRef returns [ArrayList<Identifier> values]:
(	id1 = IDENTIFIER		{$values = new ArrayList<Identifier>();
					if ($id1.text != null) {
						$values.add(new Identifier( Identifier_type.ID_TTCN, $id1.text, getLocation( $id1 ) ));
					}}
	(	DOT
		id2 = IDENTIFIER	{if ($id2.text != null) {
						$values.add(new Identifier( Identifier_type.ID_TTCN, $id2.text, getLocation( $id2 ) ));
					}}
	)*
);

pr_XRValue returns [String value, Value v_value]:
(	IDENTIFIER	{
			$value = $IDENTIFIER.text;
			$v_value = new Undefined_LowerIdentifier_Value(new Identifier( Identifier_type.ID_TTCN, $IDENTIFIER.text, getLocation( $IDENTIFIER ) ));
			$v_value.setLocation(getLocation( $IDENTIFIER ));
			}
|	BSTRING		{final String text = $BSTRING.text;
			if (text != null) {
				String realText = text.replaceAll("^\'|\'B$", "");
				realText = realText.replaceAll("\\s+","");
				$value = realText;
				$v_value = new Bitstring_Value(realText);
				$v_value.setLocation(getLocation( $BSTRING ));
			}}
|	HSTRING		{final String text = $HSTRING.text;
			if (text != null) {
				String realText = text.replaceAll("^\'|\'H$", "");
				realText = realText.replaceAll("\\s+","");
				$value = realText;
				$v_value = new Hexstring_Value(realText);
				$v_value.setLocation(getLocation( $HSTRING ));
			}}
|	OSTRING		{final String text = $OSTRING.text;
			if (text != null) {
				String realText = text.replaceAll("^\'|\'O$", "");
				realText = realText.replaceAll("\\s+","");
				$value = realText;
				$v_value = new Octetstring_Value(realText);
				$v_value.setLocation(getLocation( $OSTRING ));
			}}
|	CSTRING		{final String text = $CSTRING.text;
			if (text != null) {
				$value = text;
				$v_value = new Charstring_Value(text);
				$v_value.setLocation(getLocation( $CSTRING ));
			}}
|	FLOATVALUE	{final String text = $FLOATVALUE.text;
			if (text != null) {
				$value = text;
				$v_value = new Real_Value( Double.parseDouble( text ) );
				$v_value.setLocation(getLocation( $FLOATVALUE ));
			}}
|	NUMBER		{final String text = $NUMBER.text;
			if (text != null) {
				$value = text;
				$v_value = new Integer_Value( text );
				$v_value.setLocation(getLocation( $NUMBER ));
			}}
|	TRUE		{$value = "TRUE";
			$v_value = new Boolean_Value( true );
			$v_value.setLocation(getLocation( $TRUE ));
			}
|	FALSE		{
			$value = "FALSE";
			$v_value = new Boolean_Value( false );
			$v_value.setLocation(getLocation( $FALSE ));
			}
|	NONE		{
			$value = "none";
			$v_value = new Verdict_Value( Verdict_Value.Verdict_type.NONE );
			$v_value.setLocation(getLocation( $NONE ));
			}
|	PASS		{
			$value = "pass";
			$v_value = new Verdict_Value( Verdict_Value.Verdict_type.PASS );
			$v_value.setLocation(getLocation( $PASS ));
			}
|	INCONC		{
			$value = "inconc";
			$v_value = new Verdict_Value( Verdict_Value.Verdict_type.INCONC );
			$v_value.setLocation(getLocation( $INCONC ));
			}
|	FAIL		{
			$value = "fail";
			$v_value = new Verdict_Value( Verdict_Value.Verdict_type.FAIL );
			$v_value.setLocation(getLocation( $FAIL ));
			}
|	ERROR		{
			$value = "error";
			$v_value = new Verdict_Value( Verdict_Value.Verdict_type.ERROR );
			$v_value.setLocation(getLocation( $ERROR ));
			}
|	Null		{
			$value = "NULL_COMPREF";
			$v_value = new TTCN3_Null_Value( );
			$v_value.setLocation(getLocation( $Null ));
			}
|	NULL		{
			$value = "ASN_NULL_VALUE";
			$v_value = new ASN1_Null_Value( );
			$v_value.setLocation(getLocation( $NULL ));
			}
|	OMIT		{
			$value = "OMIT";
			$v_value = new Omit_Value( );
			$v_value.setLocation(getLocation( $OMIT ));
			}
);

pr_BitDef:
(	NUMBER (BitKeyword|BitsKeyword)			{rawstruct.fieldlength = Integer.valueOf($NUMBER.getText()).intValue();
							rawstruct.comp = RawAST.XDEFSIGNBIT;
							rawstruct.byteorder = RawAST.XDEFLAST;}
|	UNSIGNEDKeyword NUMBER (BitKeyword|BitsKeyword)	{rawstruct.fieldlength = Integer.valueOf($NUMBER.getText()).intValue();
							rawstruct.comp = RawAST.XDEFUNSIGNED;
							rawstruct.byteorder = RawAST.XDEFLAST;}
);

pr_XUTFDef:
(	UTF8Keyword	{rawstruct.stringformat = CharCoding.UTF_8;}
|	UTF16Keyword	{rawstruct.stringformat = CharCoding.UTF16;}
);

pr_XIEEE754Def:
(	IEEE754FLOATKeyword	{rawstruct.fieldlength = 32;}
|	IEEE754DOUBLEKeyword	{rawstruct.fieldlength = 64;}
);

// TEXT encoding rules

pr_XBeginDef:
(	BEGINKeyword
	LPAREN
	pr_XEncodeToken
	(	COMMA
		pr_XMatchDef
		(	COMMA
			pr_XModifierDef
		)?
	)?
	RPAREN
);

pr_XEndDef:
(	ENDKeyword
	LPAREN
	pr_XEncodeToken
	(	COMMA
		pr_XMatchDef
		(	COMMA
			pr_XModifierDef
		)?
	)?
	RPAREN
);

pr_XSeparatorDef:
(	SEPARATORKeyword
	LPAREN
	pr_XEncodeToken
	(	COMMA
		pr_XMatchDef
		(	COMMA
			pr_XModifierDef
		)?
	)?
	RPAREN
);

pr_XCodingDef:
(	TEXT_CODINGKeyword
	LPAREN
	pr_XCodingRule
	(	COMMA
		pr_XDecodingRule
		(	COMMA
			pr_XMatchDef
			(	COMMA
				pr_XModifierDef
			)?
		)?
	)?
	RPAREN
);

pr_XCodingRule:
(	/* empty */
|	pr_XAttrListEnc
|	pr_XTokenDefList
);

pr_XDecodingRule:
(	/* empty */
|	pr_XAttrList
|	pr_XDecodingTokenDefList
);

pr_XAttrList:
(	pr_XAttr
	(	SEMICOLON
		pr_XAttr
	)*
);

pr_XAttr:
(	LENGTHToken ASSIGN ( NUMBER | NUMBER MINUS NUMBER )
|	CONVERTToken ASSIGN ( LOWERToken | UPPERToken)
|	JUSTToken ASSIGN ( LEFTToken | RIGHTToken | CENTERToken )
|	LEADINGToken ASSIGN ( TRUEToken | FALSEToken )
|	REPEATToken ASSIGN ( TRUEToken | FALSEToken )
);

pr_XAttrListEnc:
(	pr_XAttrEnc
	(	SEMICOLON
		pr_XAttrEnc
	)*
);

pr_XAttrEnc:
(	LENGTHToken ASSIGN ( NUMBER | NUMBER MINUS NUMBER )
|	CONVERTToken ASSIGN ( LOWERToken | UPPERToken )
|	JUSTToken ASSIGN ( LEFTToken | RIGHTToken | CENTERToken )
|	LEADINGToken ASSIGN ( TRUEToken | FALSEToken )
|	REPEATToken ASSIGN ( TRUEToken | FALSEToken )
);

pr_XTokenDefList:
(	pr_XTokenDef
	(	SEMICOLON
		pr_XTokenDef
	)*
);

pr_XTokenDef:
(	pr_XIdentifierOrReserved COLON pr_XEncodeToken
|	TRUEToken COLON pr_XEncodeToken
|	FALSEToken COLON pr_XEncodeToken
);

pr_XIdentifierOrReserved:
(	IDENTIFIER
|	pr_XTextReservedWord
);

pr_XTextReservedWord:
(	LENGTHToken
|	REPEATToken
|	CONVERTToken
|	LOWERToken
|	UPPERToken
|	JUSTToken
|	LEFTToken
|	RIGHTToken
|	CENTERToken
|	LEADINGToken
|	SENSITIVEToken
|	INSENSITIVEToken
);

pr_XDecodingTokenDefList:
(	pr_XDecodingTokenDef
	(	SEMICOLON
		pr_XDecodingTokenDef
	)*
);

pr_XDecodingTokenDef:
(	pr_XIdentifierOrReserved COLON pr_XDecodeToken
|	TRUEToken COLON pr_XDecodeToken
|	FALSEToken COLON pr_XDecodeToken
);

pr_XEncodeToken: XToken;

pr_XDecodeToken:
(	XToken
|	BEGINCHAR XToken ENDCHAR
|	BEGINCHAR pr_XMatchDef COMMA pr_XModifierDef ENDCHAR
);

pr_XMatchDef:
(	XToken
)?;

pr_XModifierDef:
(	/* empty */
|	SENSITIVEToken
|	INSENSITIVEToken
);

// XER encoding rules
pr_XERAttributes: pr_XERAttribute;

pr_XERAttribute:
(	ABSTRACTKeyword
|	pr_anyAttributes
|	pr_anyElement
|	ATTRIBUTEKeyword
|	ATTRIBUTEFROMUALIFIED
|	BLOCKKeyword
|	pr_controlNameSpace
|	pr_defaultForEmpty
|	ELEMENTKeyword
|	ELEMENTFROMQUALIFIEDKeyword
|	EMBEDVALUESKeyword
|	pr_from
|	pr_fractionDigits
|	LISTKeyword
|	pr_name
|	pr_namespace
|	pr_text
|	UNTAGGEDKeyword
|	USENILKeyword
|	USENUMBERKeyword
|	USEORDERKeyword
|	USEUNIONKeyword
|	USETYPEKeyword
|	pr_whitesapce
|	XSDKeyword COLON pr_xsddata
);

pr_anyAttributes: ANYATTRIBUTESKeyword pr_optNameSpaceRestriction;

pr_anyElement: ANYELEMENTKeyword pr_optNameSpaceRestriction;

pr_optNameSpaceRestriction:
(	/* empty */
|	FROMKeyword pr_urilist
|	EXCEPTKeyword pr_urilist
);

pr_urilist:
(	pr_quotedURIOrAbsent
	(	COMMA
		pr_quotedURIOrAbsent
	)*
);

pr_quotedURIOrAbsent:
(	XSTRING
|	UNQUALIFIEDKeyword
);

pr_controlNameSpace: CONTROLNAMESPACEKeyword XSTRING PREFIXKeyword XSTRING;

pr_from: FROMKeyword ASKeyword ( UNQUALIFIEDKeyword | QUALIFIEDKeyword );

pr_name: NAMEKeyword ASKeyword pr_newNameOrKeyword;

pr_newNameOrKeyword:
(	pr_keyword
|	XSTRING
);

pr_keyword:
(	CAPITALIZEDKeyword
|	UNCAPITALIZEDKeyword
|	LOWERCASEDKeyword
|	UPPERCASEDKeyword
);

pr_namespace: NAMESPACEKeyword pr_namespaceSpefication;

pr_namespaceSpefication: ASKeyword XSTRING pr_optPrefix;

pr_optPrefix:
(	/* empty */
|	PREFIXKeyword XSTRING
);

pr_text:
(	TEXTKeyword
	(	/* empty */
	|	XSTRING ASKeyword pr_newNameOrKeyword
	|	ALLKeyword ASKeyword pr_newNameOrKeyword
	)
);

pr_defaultForEmpty:
(	DEFAULTFOREMPTYKeyword
	ASKeyword
	(	IDENTIFIER
		(	DOT
			IDENTIFIER
		)?
	|	XSTRING
	)
);

pr_fractionDigits:
(	FRACTIONDIGITSKeyword NUMBER
);

pr_whitesapce:
(	WHITESPACEKeyword
	(	PRESERVEKeyword
	|	REPLACEKeyword
	|	COLLAPSEKeyword
	)
);

pr_xsddata:
(	XSDbase64Binary
|	XSDdecimal
|	XSDhexBinary
|	XSDQName
|	SHORTKeyword
|	LONGKeyword
|	XSDstring
|	XSDnormalizedString
|	XSDtoken
|	XSDName
|	XSDNMToken
|	XSDNCName
|	XSDID
|	XSDIDREF
|	XSDENTITY
|	XSDanyURI
|	XSDlanguage
|	XSDinteger
|	XSDpositiveInteger
|	XSDnonPositiveInteger
|	XSDnegativeInteger
|	XSDnonNegativeInteger
|	XSDunsignedLong
|	XSDint
|	XSDunsignedInt
|	XSDunsignedShort
|	XSDbyte
|	XSDunsignedByte
|	XSDfloat
|	XSDdouble
|	XSDduration
|	XSDdateTime
|	XSDtime
|	XSDdate
|	XSDgYearMonth
|	XSDgYear
|	XSDgMonthDay
|	XSDgDay
|	XSDgMonth
|	XSDNMTOKENS
|	XSDIDREFS
|	XSDENTITIES
|	XSDboolean
|	XSDanySimpleType
|	XSDanyType
);



// JSON encoding rules
pr_XJsonDef: JSONKeyword COLON pr_XJsonAttribute;

pr_XJsonAttribute:
(	pr_XOmitAsNull
|	pr_XNameAs
|	pr_XAsValue
|	pr_Default
|	pr_Extend
|	pr_XMetainfoForUnbound
|	pr_XAsNumber
);

pr_XOmitAsNull: OMITKeyword ASKeyword NullKeyword;

pr_XNameAs: NAMEKeyword ASKeyword pr_JsonAlias;

pr_JsonAlias:
(	AliasToken
|	OMITKeyword
|	ASKeyword
|	NullKeyword
|	NAMEKeyword
|	VALUEKeyword
|	DEFAULTKeyword
|	EXTENDKeyword
|	METAINFOKeyword
|	FORKeyword
|	UNBOUNDKeyword
|	NUMBERKeyword
);

pr_XAsValue: ASKeyword VALUEKeyword;

pr_Default: DEFAULTKeyword pr_JsonValue;

pr_Extend: EXTENDKeyword pr_JsonValue COLON pr_JsonValue;

pr_JsonValue:
(	JSONValueStart
	(	pr_JsonValueCore
	)?
	JSONValueEnd
);

pr_JsonValueCore:
(	JSONValueSegment
	(	JSONValueSegment
	)*
);

pr_XMetainfoForUnbound: METAINFOKeyword FORKeyword UNBOUNDKeyword;

pr_XAsNumber: ASKeyword NUMBERKeyword;
