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

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a GSM Unstructured Supplemental Service Data (USSD)
 * session between a MT and the network
 */
public class USSDSession
{
	/**
	 * Class representing GSM USSD session.
	 */
	private List<USSDDatagram> datagrams;

	private USSDSessionStatus sessionStatus;

	private String gtwId;

	/**
	 * Default constructor
	 */
	public USSDSession()
	{
		datagrams = new ArrayList<USSDDatagram>();
		gtwId = null;
	}

	/**
	 * Full constructor
	 */
	public USSDSession(USSDDatagram initialDatagram, String aGtwId) throws IllegalArgumentException
	{
		datagrams = new ArrayList<USSDDatagram>();
		if (!isGatewaySet(initialDatagram))
		{
			initialDatagram.setGatewayId(aGtwId);
		}
		datagrams.add(initialDatagram);
	}

	/**
	 * Initial-datagram-only constructor
	 */
	public USSDSession(USSDDatagram initialDatagram) throws IllegalArgumentException
	{
		datagrams = new ArrayList<USSDDatagram>();
		if (isGatewaySet(initialDatagram))
		{
			gtwId = initialDatagram.getGatewayId();
		}
		else
		{
			gtwId = null;
		}
		datagrams.add(initialDatagram);
	}

	/**
	 * Gateway-only constructor
	 */
	public USSDSession(String aGtwId)
	{
		datagrams = new ArrayList<USSDDatagram>();
		gtwId = aGtwId;
	}

	public USSDResponse sendRequest(USSDRequest aRequest)
	{
		return null;
	}

	public String getGatewayId()
	{
		return gtwId;
	}

	public void setGatewayId(String aGtwId)
	{
		gtwId = aGtwId;
	}

	public List<USSDDatagram> getDatagrams()
	{
		return datagrams;
	}

	public void setDatagrams(List<USSDDatagram> aDatagramList)
	{
		datagrams = aDatagramList;
	}

	public USSDSessionStatus getSessionStatus()
	{
		return sessionStatus;
	}

	public void setSessionStatus(USSDSessionStatus aStatus)
	{
		sessionStatus = aStatus;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer("Session status: ");
		buf.append(sessionStatus).append("\n");
		buf.append("Datagrams:\n");
		for (USSDDatagram dgram : datagrams)
		{
			buf.append("\t").append(dgram.toString());
			buf.append("\n");
		}
		return buf.toString();
	}

	private boolean isGatewaySet(USSDDatagram dgram)
	{
		if ("".equals(dgram.getGatewayId()) || "*".equals(dgram.getGatewayId()))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
