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
 * Library / Version constants.
 */
public class Library
{
	private static final String LIB_INFOTEXT = "SMSLib: A Java API library for sending and receiving SMS via a GSM modem or other supported gateways.\nThis software is distributed under the terms of the Apache v2.0 License.\nWeb Site: http://smslib.org";

	private static final int LIB_VERSION = 3;

	private static final int LIB_RELEASE = 5;

	private static final int LIB_SUBRELEASE = 4;

	private static final String LIB_STATUS = "";

	public static final String getLibraryDescription()
	{
		return LIB_INFOTEXT;
	}

	public static final String getLibraryVersion()
	{
		String text;
		text = LIB_VERSION + "." + LIB_RELEASE + "." + LIB_SUBRELEASE;
		if (LIB_STATUS.length() != 0) text = text + "-" + LIB_STATUS;
		return text;
	}
}
