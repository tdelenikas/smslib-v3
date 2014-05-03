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

/**
 * <b>SMSServer Application Gateway.</b>
 */
public class BulkSmsHttp extends AGateway
{
	public BulkSmsHttp(String myGatewayId, Properties myProps, org.smslib.smsserver.SMSServer myServer)
	{
		super(myGatewayId, myProps, myServer);
		setDescription("Default BulkSms Gateway.");
	}

	@Override
	public void create() throws Exception
	{
		String propName;
		propName = getGatewayId() + ".";
		if (getProperties().getProperty(propName + "region") == null)
		{
			setGateway(new org.smslib.http.BulkSmsHTTPGateway(getGatewayId(), getProperties().getProperty(propName + "username"), getProperties().getProperty(propName + "password")));
		}
		else
		{
			org.smslib.http.BulkSmsHTTPGateway.Regions Region = org.smslib.http.BulkSmsHTTPGateway.Regions.INTERNATIONAL;
			if (getProperties().getProperty(propName + "region").equalsIgnoreCase("UNITEDKINGDOM")) Region = org.smslib.http.BulkSmsHTTPGateway.Regions.UNITEDKINGDOM;
			else if (getProperties().getProperty(propName + "region").equalsIgnoreCase("SOUTHAFRICA")) Region = org.smslib.http.BulkSmsHTTPGateway.Regions.SOUTHAFRICA;
			else if (getProperties().getProperty(propName + "region").equalsIgnoreCase("SPAIN")) Region = org.smslib.http.BulkSmsHTTPGateway.Regions.SPAIN;
			else if (getProperties().getProperty(propName + "region").equalsIgnoreCase("USA")) Region = org.smslib.http.BulkSmsHTTPGateway.Regions.USA;
			else if (getProperties().getProperty(propName + "region").equalsIgnoreCase("GERMANY")) Region = org.smslib.http.BulkSmsHTTPGateway.Regions.GERMANY;
			setGateway(new org.smslib.http.BulkSmsHTTPGateway(getGatewayId(), getProperties().getProperty(propName + "username"), getProperties().getProperty(propName + "password"), Region));
		}
		if (getProperties().getProperty(propName + "outbound").equalsIgnoreCase("yes")) getGateway().setOutbound(true);
		else if (getProperties().getProperty(propName + "outbound").equalsIgnoreCase("no")) getGateway().setOutbound(false);
		else throw new Exception("Incorrect parameter: " + propName + "outbound");
	}
}
