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
import org.smslib.smsserver.SMSServer;

/**
 * The AGateway abstract class is an abstraction layer between SMSServer and
 * SMSLib gateways. You should not tamper with these files unless you know what
 * you are doing!
 */
public abstract class AGateway
{
	private String gatewayId;

	private Properties props;

	private SMSServer server;

	private org.smslib.AGateway gateway;

	private String description;

	public AGateway(String myGatewayId, Properties myProps, SMSServer myServer)
	{
		this.gatewayId = myGatewayId;
		this.props = myProps;
		this.server = myServer;
	}

	public final String getGatewayId()
	{
		return this.gatewayId;
	}

	public final Properties getProperties()
	{
		return this.props;
	}

	public final SMSServer getServer()
	{
		return this.server;
	}

	public final org.smslib.AGateway getGateway()
	{
		return this.gateway;
	}

	public final void setGateway(org.smslib.AGateway myGateway)
	{
		this.gateway = myGateway;
	}

	public final String getDescription()
	{
		return this.description;
	}

	public final void setDescription(String myDescription)
	{
		this.description = myDescription;
	}

	public abstract void create() throws Exception;
}
