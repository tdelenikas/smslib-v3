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

package org.smslib.smsserver.gateways;

import java.util.Properties;
import org.smslib.AGateway.Protocols;
import org.smslib.modem.ModemGateway;
import org.smslib.modem.ModemGateway.IPProtocols;

/**
 * <b>SMSServer Application Gateway.</b>
 */
public class IPModem extends AGateway
{
	public IPModem(String myGatewayId, Properties myProps, org.smslib.smsserver.SMSServer myServer)
	{
		super(myGatewayId, myProps, myServer);
		setDescription("Default IP Modem Gateway.");
	}

	@Override
	public void create() throws Exception
	{
		String propName;
		propName = getGatewayId() + ".";
		setGateway(new org.smslib.modem.IPModemGateway(getGatewayId(), getProperties().getProperty(propName + "ip"), Integer.parseInt(getProperties().getProperty(propName + "port")), getProperties().getProperty(propName + "manufacturer"), getProperties().getProperty(propName + "model")));
		if (getProperties().getProperty(propName + "protocol").equalsIgnoreCase("pdu")) getGateway().setProtocol(Protocols.PDU);
		else if (getProperties().getProperty(propName + "protocol").equalsIgnoreCase("text")) getGateway().setProtocol(Protocols.TEXT);
		else throw new Exception("Incorrect parameter: " + propName + "protocol");
		if (getProperties().getProperty(propName + "ipprotocol").equalsIgnoreCase("text")) ((ModemGateway) getGateway()).setIpProtocol(IPProtocols.TEXT);
		else if (getProperties().getProperty(propName + "ipprotocol").equalsIgnoreCase("binary"))  ((ModemGateway) getGateway()).setIpProtocol(IPProtocols.BINARY);
		else throw new Exception("Incorrect parameter: " + propName + "ipprotocol");
		((org.smslib.modem.IPModemGateway) getGateway()).setSimPin(getProperties().getProperty(propName + "pin"));
		if (getProperties().getProperty(propName + "inbound").equalsIgnoreCase("yes")) getGateway().setInbound(true);
		else if (getProperties().getProperty(propName + "inbound").equalsIgnoreCase("no")) getGateway().setInbound(false);
		else throw new Exception("Incorrect parameter: " + propName + "inbound");
		if (getProperties().getProperty(propName + "outbound").equalsIgnoreCase("yes")) getGateway().setOutbound(true);
		else if (getProperties().getProperty(propName + "outbound").equalsIgnoreCase("no")) getGateway().setOutbound(false);
		else throw new Exception("Incorrect parameter: " + propName + "outbound");
		((org.smslib.modem.ModemGateway) getGateway()).setCustomInitString(getProperties().getProperty(propName + "init_string", ""));
		if (getProperties().getProperty(propName + "smsc_number", "").length() > 0) ((org.smslib.modem.ModemGateway) getGateway()).setSmscNumber(getProperties().getProperty(propName + "smsc_number"));
	}
}
