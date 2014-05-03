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

package org.smslib.http;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;
import org.smslib.OutboundMessage.FailureCauses;
import org.smslib.OutboundMessage.MessageStatuses;
import org.smslib.helper.Logger;

/**
 * Gateway for BulkSMS bulk operator (http://www.bulksms.com) Outbound only -
 * implements HTTP interface.
 */
public class SkypeHTTPGateway extends HTTPGateway
{
	String password, reply;

	String providerHost;

	Object SYNC_Commander;

	public SkypeHTTPGateway(String id, String myProviderHost, String myPassword, String myReply)
	{
		super(id);
		this.providerHost = myProviderHost;
		this.password = myPassword;
		this.reply = myReply;
		this.SYNC_Commander = new Object();
		setAttributes(AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.CUSTOMFROM | AGateway.GatewayAttributes.BIGMESSAGES | AGateway.GatewayAttributes.FLASHSMS);
	}

	@Override
	public void startGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		Logger.getInstance().logInfo("Starting gateway.", null, getGatewayId());
		super.startGateway();
	}

	@Override
	public void stopGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		Logger.getInstance().logInfo("Stopping gateway.", null, getGatewayId());
		super.stopGateway();
	}

	@Override
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		try{
		URL url = null;
		List<HttpHeader> request = new ArrayList<HttpHeader>();
		List<String> response;
		String reqLine;
		boolean ok = false;
		request.add(new HttpHeader("password", this.password, false));
		request.add(new HttpHeader("text", msg.getText(), false));
		request.add(new HttpHeader("to", msg.getRecipient(), false));
		request.add(new HttpHeader("reply", this.reply, false));
		reqLine = ExpandHttpHeaders(request);
		url = new URL(this.providerHost + "/send" + "?" + reqLine);
System.out.println(">>>>>>>>>>>> " + url.toString());
		synchronized (this.SYNC_Commander)
		{
			response = HttpGet(url);
		}
System.out.println(">>>>>>>>>>>>>>>>>>>>>  " + response.get(0));
		switch (Integer.parseInt(response.get(0)))
		{
			case 0:
				msg.setRefNo("N/A");
				msg.setDispatchDate(new Date());
				msg.setGatewayId(getGatewayId());
				msg.setMessageStatus(MessageStatuses.SENT);
				incOutboundMessageCount();
				ok = true;
				break;
			default:
				msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
				msg.setRefNo(null);
				msg.setDispatchDate(null);
				msg.setMessageStatus(MessageStatuses.FAILED);
				ok = false;
				break;
		}
		return ok;
	} catch (Exception e) { e.printStackTrace(); return false;}
	}
}
