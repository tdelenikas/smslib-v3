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
 * Class representing a GSM Unstructured Supplemental Service Data (USSD)
 * network response.
 */
public class USSDRequest extends USSDDatagram
{
	/**
	 * Class representing GSM USSD mobile request.
	 */
	private static final long serialVersionUID = 1L;

	private USSDResultPresentation presentation;

	/**
	 * Default constructor
	 */
	public USSDRequest()
	{
		super();
		presentation = null;
	}

	/**
	 * Full constructor
	 */
	public USSDRequest(USSDResultPresentation aPresentation, String aContent, USSDDcs aDcs, String aGatewayId) throws IllegalArgumentException
	{
		super();
		presentation = aPresentation;
		setContent(aContent);
		setDcs(aDcs);
		setGatewayId(aGatewayId);
	}

	/**
	 * Content-only constructor
	 */
	public USSDRequest(String aContent)
	{
		super();
		presentation = USSDResultPresentation.PRESENTATION_ENABLED;
		setContent(aContent);
		setDcs(USSDDcs.UNSPECIFIED_7BIT);
	}

	public String getRawRequest()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("AT+CUSD=");
		buf.append(presentation.getNumeric());
		buf.append(",");
		buf.append("\"");
		buf.append(getContent());
		buf.append("\"");
		buf.append(",");
		buf.append(getDcs().getNumeric());
		buf.append("\r");
		return buf.toString();
	}

	public USSDResultPresentation getResultPresentation()
	{
		return presentation;
	}

	public void setUSSDResultPresentation(USSDResultPresentation aResultPresentation)
	{
		presentation = aResultPresentation;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer("Gateway: ");
		buf.append(getGatewayId());
		buf.append("\n");
		buf.append("Result presentation: ");
		buf.append(presentation);
		buf.append("\n");
		buf.append("Data coding scheme: ");
		buf.append(getDcs());
		buf.append("\n");
		buf.append("Content: ");
		buf.append(getContent() != null ? getContent() : "(EMPTY)");
		return buf.toString();
	}
}
