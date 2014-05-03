// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2012, Thanasis Delenikas, Athens/GREECE.
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.smslib;

/**
 * Enum representing the status of a GSM Unstructured Supplemental Service Data
 * (USSD) data coding schemd (DCS).
 */
public enum USSDDcs
{
	/**
	 * Bits 7..4 equal to zero: GSM 7 bit default alphabet Bits 3..0 indicate
	 * the language
	 */
	GERMAN_7BIT(0x00), ENGLISH_7BIT(0x01), ITALIAN_7BIT(0x02), FRENCH_7BIT(0x03), SPANISH_7BIT(0x04), DUTCH_7BIT(0x05), SWEDISH_7BIT(0x06), DANISH_7BIT(0x07), PORTUGUESE_7BIT(0x08), FINNISH_7BIT(0x09), NORWEGIAN_7BIT(0x0a), GREEK_7BIT(0x0b), TURKISH_7BIT(0x0c), HUNGARIAN_7BIT(0x0d), POLISH_7BIT(0x0e), UNSPECIFIED_7BIT(0x0f),
	/**
	 * Bits 7..4 = 0001 --
	 */
	/**
	 * Bits 3..0 0000: GSM 7 bit default alphabet; message preceded by language
	 * indication. The first 3 characters of the message are a two-character
	 * representation of the language encoded according to ISO 639 [12],
	 * followed by a CR character. The CR character is then followed by 90
	 * characters of text.
	 */
	LANGUAGE_IN_PREFIX_7BIT(0x10),
	/**
	 * UCS2; message preceded by language indication The message starts with a
	 * two GSM 7-bit default alphabet character representation of the language
	 * encoded according to ISO 639 [12]. This is padded to the octet boundary
	 * with two bits set to 0 and then followed by 40 characters of UCS2-encoded
	 * message. An MS not supporting UCS2 coding will present the two character
	 * language identifier followed by improperly interpreted user data.
	 */
	LANGUAGE_IN_PREFIX_UCS2(0x11),
	/**
	 * Bits 7..4 = 0010 --
	 */
	CZECH(0x20), HEBREW(0x21), ARABIC(0x22), RUSSIAN(0x23), ICELANDIC(0x24);
	/* Further encodings not implemented */
	private final int numeric;

	USSDDcs(int aNumeric)
	{
		numeric = aNumeric;
	}

	public int getNumeric()
	{
		return numeric;
	}

	@Override
	public String toString()
	{
		return super.toString() + " (" + numeric + ")";
	}

	public static USSDDcs getByNumeric(int aNumeric)
	{
		for (USSDDcs dcs : USSDDcs.values())
		{
			if (aNumeric == dcs.getNumeric()) return dcs;
		}
		return null;
	}
}
