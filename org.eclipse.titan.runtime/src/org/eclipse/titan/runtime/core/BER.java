/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;


/**
 * BER encoding/decoding functions
 *
 * @author Kristof Szabados
 */
public class BER {
	private BER() {
		// Hide constructor
	}

	public static enum ASN_TagClass {
		ASN_TAG_UNDEF,
		ASN_TAG_UNIV, /** < UNIVERSAL */
		ASN_TAG_APPL, /** < APPLICATION */
		ASN_TAG_CONT, /** < context-specific */
		ASN_TAG_PRIV /** PRIVATE */
	}

	/**
	 * @brief ASN.1 identifier
	 *
	 * Contains two thirds of the T from a TLV
	 * */
	public static final class ASN_Tag {
		/** Tag class */
		public ASN_TagClass tagclass;

		/**
		 * Tag value.
		 * For UNIVERSAL, the values are predefined.
		 *
		 * Should be handle as unsigned int
		 **/
		public int tagnumber;

		/**
		 * Constructor.
		 *
		 * @param tagclass the class of the tag.
		 * @param tagnumber the number of the tag.
		 * */
		public ASN_Tag(final ASN_TagClass tagclass, final int tagnumber) {
			this.tagclass = tagclass;
			this.tagnumber = tagnumber;
		}
	}

	/**
	 * Descriptor for BER encoding/decoding during runtime
	 * Originally ASN_BERdescriptor_t
	 *
	 * the number _of_tags field might disappear later, if proven unnecessary in practice.
	 */
	public static final class ASN_BERdescriptor {
		/**
		 * Number of tags.
		 *
		 * For the UNIVERSAL classes, this is usually 1 (except for CHOICE and ANY)
		 * */
		public int number_of_tags;

		/**
		 * This array contains the tags.
		 * Index 0 is the innermost tag.
		 */
		public ASN_Tag[] tags;

		/**
		 * Constructor.
		 *
		 * @param number_of_tags the number of tags.
		 * @param tags the tags themselves.
		 * */
		public ASN_BERdescriptor(final int number_of_tags, final ASN_Tag[] tags) {
			this.number_of_tags = number_of_tags;
			this.tags = tags;
		}
	}

	private static final ASN_Tag TitanBoolean_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 1)};
	public static final ASN_BERdescriptor TitanBoolean_Ber_ = new ASN_BERdescriptor(1, TitanBoolean_tag_);

	private static final ASN_Tag TitanInteger_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 2)};
	public static final ASN_BERdescriptor TitanInteger_Ber_ = new ASN_BERdescriptor(1, TitanInteger_tag_);

	private static final ASN_Tag TitanBitString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 3)};
	public static final ASN_BERdescriptor TitanBitString_Ber_ = new ASN_BERdescriptor(1, TitanBitString_tag_);

	private static final ASN_Tag TitanOctetString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 4)};
	public static final ASN_BERdescriptor TitanOctetString_Ber_ = new ASN_BERdescriptor(1, TitanOctetString_tag_);

	private static final ASN_Tag TitanASN_Null_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 5)};
	public static final ASN_BERdescriptor TitanASN_Null_Ber_ = new ASN_BERdescriptor(1, TitanASN_Null_tag_);

	private static final ASN_Tag TitanObjectId_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 6)};
	public static final ASN_BERdescriptor TitanObjectId_Ber_ = new ASN_BERdescriptor(1, TitanObjectId_tag_);

	private static final ASN_Tag TitanObjectDescriptor_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 7)};
	public static final ASN_BERdescriptor TitanObjectDescriptor_Ber_ = new ASN_BERdescriptor(1, TitanObjectDescriptor_tag_);

	private static final ASN_Tag TitanExternal_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 8)};
	public static final ASN_BERdescriptor TitanExternal_Ber_ = new ASN_BERdescriptor(1, TitanExternal_tag_);

	private static final ASN_Tag TitanReal_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 9)};
	public static final ASN_BERdescriptor TitanFloat_Ber_ = new ASN_BERdescriptor(1, TitanReal_tag_);

	private static final ASN_Tag TitanEnumerated_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 10)};
	public static final ASN_BERdescriptor TitanEnumerated_Ber_ = new ASN_BERdescriptor(1, TitanEnumerated_tag_);

	private static final ASN_Tag TitanEmbedded_PDV_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 11)};
	public static final ASN_BERdescriptor TitanEmbedded_PDV_Ber_ = new ASN_BERdescriptor(1, TitanEmbedded_PDV_tag_);

	private static final ASN_Tag TitanUTF8String_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 12)};
	public static final ASN_BERdescriptor TitanUTF8String_Ber_ = new ASN_BERdescriptor(1, TitanUTF8String_tag_);

	private static final ASN_Tag TitanASN_Roid_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 13)};
	public static final ASN_BERdescriptor TitanASN_Roid_Ber_ = new ASN_BERdescriptor(1, TitanASN_Roid_tag_);

	private static final ASN_Tag Sequence_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 16)};
	public static final ASN_BERdescriptor Sequence_Ber_ = new ASN_BERdescriptor(1, Sequence_tag_);

	private static final ASN_Tag Set_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 17)};
	public static final ASN_BERdescriptor Set_Ber_ = new ASN_BERdescriptor(1, Set_tag_);

	public static final ASN_BERdescriptor Choice_Ber_ = new ASN_BERdescriptor(0, null);

	public static final ASN_BERdescriptor TitanASN_Any_Ber_ = new ASN_BERdescriptor(0, null);

	private static final ASN_Tag TitanNumericString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 18)};
	public static final ASN_BERdescriptor TitanNumericString_Ber_ = new ASN_BERdescriptor(1, TitanNumericString_tag_);

	private static final ASN_Tag TitanPrintableString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 19)};
	public static final ASN_BERdescriptor TitanPrintableString_Ber_ = new ASN_BERdescriptor(1, TitanPrintableString_tag_);

	private static final ASN_Tag TitanTeletexString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 20)};
	public static final ASN_BERdescriptor TitanTeletexString_Ber_ = new ASN_BERdescriptor(1, TitanTeletexString_tag_);

	private static final ASN_Tag TitanVideotexString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 21)};
	public static final ASN_BERdescriptor TitanVideotexString_Ber_ = new ASN_BERdescriptor(1, TitanVideotexString_tag_);

	private static final ASN_Tag TitanIA5String_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 22)};
	public static final ASN_BERdescriptor TitanIA5String_Ber_ = new ASN_BERdescriptor(1, TitanIA5String_tag_);

	private static final ASN_Tag TitanUTCTime_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 23)};
	public static final ASN_BERdescriptor TitanUTCTime_Ber_ = new ASN_BERdescriptor(1, TitanUTCTime_tag_);

	private static final ASN_Tag TitanGeneralizedTime_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 24)};
	public static final ASN_BERdescriptor TitanGeneralizedTime_Ber_ = new ASN_BERdescriptor(1, TitanGeneralizedTime_tag_);

	private static final ASN_Tag TitanGraphicString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 25)};
	public static final ASN_BERdescriptor TitanGraphicString_Ber_ = new ASN_BERdescriptor(1, TitanGraphicString_tag_);

	private static final ASN_Tag TitanVisibleString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 26)};
	public static final ASN_BERdescriptor TitanVisibleString_Ber_ = new ASN_BERdescriptor(1, TitanVisibleString_tag_);

	private static final ASN_Tag TitanGeneralString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 27)};
	public static final ASN_BERdescriptor TitanGeneralString_Ber_ = new ASN_BERdescriptor(1, TitanGeneralString_tag_);

	private static final ASN_Tag TitanUniversalString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 28)};
	public static final ASN_BERdescriptor TitanUniversalString_Ber_ = new ASN_BERdescriptor(1, TitanUniversalString_tag_);

	private static final ASN_Tag TitanCharacter_String_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 29)};
	public static final ASN_BERdescriptor TitanCharacter_String_Ber_ = new ASN_BERdescriptor(1, TitanCharacter_String_tag_);

	private static final ASN_Tag TitanBMPString_tag_[] = new ASN_Tag[] {new ASN_Tag(ASN_TagClass.ASN_TAG_UNIV, 30)};
	public static final ASN_BERdescriptor TitanBMPString_Ber_ = new ASN_BERdescriptor(1, TitanBMPString_tag_);
}
