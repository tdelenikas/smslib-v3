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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Class representing a GSM Unstructured Supplemental Service Data (USSD)
 * network response.
 */
public class USSDResponse extends USSDDatagram
{
	private static final long serialVersionUID = 1L;

	private static final Pattern MSG_PATTERN = Pattern.compile("^\\+CUSD:\\s+(\\d)(?:,\\s*\"([^\"]*))?(?:\",\\s*(\\d+)\\s*)?\"?\r?$");

	private static final int STATUS_INDEX = 1;

	private static final int CONTENT_INDEX = 2;

	private static final int ENCODING_INDEX = 3;

	/**
	 * Class representing GSM USSD network response.
	 */
	private String rawResponse;

	private USSDSessionStatus sessionStatus;

	/**
	 * Default constructor
	 */
	public USSDResponse()
	{
		super();
		rawResponse = null;
		sessionStatus = null;
	}

	/**
	 * Constructor that takes a raw response from a modem
	 */
	public USSDResponse(String rawResp, String gtwId) throws InvalidMessageException
	{
		Matcher matcher = MSG_PATTERN.matcher(rawResp);
		if (!matcher.matches()) throw new InvalidMessageException("Not a well-formed +CUSD response: |" + rawResp + "|");
		try
		{
			setGatewayId(gtwId);
			rawResponse = rawResp;
			sessionStatus = USSDSessionStatus.getByNumeric(Integer.valueOf(matcher.group(STATUS_INDEX)));
			if (matcher.groupCount() >= CONTENT_INDEX && matcher.group(CONTENT_INDEX) != null)
			{
				setContent(matcher.group(CONTENT_INDEX));
			}
			if (matcher.groupCount() >= ENCODING_INDEX && matcher.group(ENCODING_INDEX) != null)
			{
				setDcs(USSDDcs.getByNumeric(Integer.valueOf(matcher.group(ENCODING_INDEX))));
			}
		}
		catch (Exception e)
		{
			throw new InvalidMessageException("Session status: " + matcher.group(STATUS_INDEX) + "; DCS: " + matcher.group(ENCODING_INDEX));
		}
	}

	public String getRawResponse()
	{
		return rawResponse;
	}

	public void setRawResponse(String aRawResponse)
	{
		rawResponse = aRawResponse;
	}

	public USSDSessionStatus getSessionStatus()
	{
		return sessionStatus;
	}

	public void setUSSDSessionStatus(USSDSessionStatus aUSSDSessionStatus)
	{
		sessionStatus = aUSSDSessionStatus;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer("Gateway: ");
		buf.append(getGatewayId());
		buf.append("\n");
		buf.append("Session status: ");
		buf.append(sessionStatus);
		buf.append("\n");
		buf.append("Data coding scheme: ");
		buf.append(getDcs() != null ? getDcs() : "Unspecified");
		buf.append("\n");
		buf.append("Content: ");
		buf.append(getContent() != null ? getContent() : "(EMPTY)");
		return buf.toString();
	}
}
