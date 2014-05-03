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

import java.io.Serializable;

public abstract class USSDDatagram implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String gtwId;

	private String content;

	private USSDDcs dcs;

	public String getGatewayId()
	{
		return gtwId;
	}

	public void setGatewayId(String aGtwId)
	{
		gtwId = aGtwId;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String aContent)
	{
		content = aContent;
	}

	public USSDDcs getDcs()
	{
		return dcs;
	}

	public void setDcs(USSDDcs aDcs)
	{
		dcs = aDcs;
	}
}
