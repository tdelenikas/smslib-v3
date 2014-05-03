// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// (c) 2011, Velvetech, LLC (http://www.velvetech.com)
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
import org.smslib.EzTextingOutboundMessage;
import org.smslib.GatewayException;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;
import org.smslib.OutboundMessage.FailureCauses;
import org.smslib.OutboundMessage.MessageStatuses;
import org.smslib.helper.Logger;

public class EzTextingHTTPGateway  extends HTTPGateway 
{
	Object SYNC_Commander;
	final String providerUrl = "https://app.eztexting.com";
	String username, password;
	
	public EzTextingHTTPGateway (String id, String myUsername, String myPassword)
	{
		super(id);
		this.username = myUsername;
		this.password = myPassword;				
		this.SYNC_Commander = new Object();
		setAttributes(AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.CUSTOMFROM);
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
	
	/**
	 * @return	See details here: http://www.eztexting.com/developers/sms-api-documentation.html#CheckCredits
	 */		
	@Override
	public float queryBalance() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url = null;
		List<HttpHeader> request = new ArrayList<HttpHeader>();
		List<String> response;
								
		request.add(new HttpHeader("user", this.username, false));
		request.add(new HttpHeader("pass", this.password, false));
			
		url = new URL(this.providerUrl + "/api/credits/check/");
		synchronized (this.SYNC_Commander)
		{
			response = HttpPost(url, request);			
		}								
		return Float.parseFloat(response.get(0));
	}
		
	@Override
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url = null;
		List<HttpHeader> request = new ArrayList<HttpHeader>();
		List<String> response;					
		boolean ok = false;

		boolean isExpress = true;
		String subject = "";
		
		request.add(new HttpHeader("user", this.username, false));
		request.add(new HttpHeader("pass", this.password, false));			
		request.add(new HttpHeader("message", msg.getText(), false));		
		request.add(new HttpHeader("phonenumber", msg.getRecipient(), false));

		if (msg instanceof EzTextingOutboundMessage)
		{
			if (!((EzTextingOutboundMessage) msg).isExpress()) {
				isExpress = false;
			}
			subject = ((EzTextingOutboundMessage) msg).getSubject();
		} 
		request.add(new HttpHeader("express", (isExpress?"1":"0"), false));
		request.add(new HttpHeader("subject", subject, false));
						
		url = new URL(this.providerUrl + "/api/sending/");
		synchronized (this.SYNC_Commander)
		{
			response = HttpPost(url, request);					
		}

		if (response.get(0).length() == 1 && response.get(0).charAt(0) == '1')
		{								
			msg.setDispatchDate(new Date());
			msg.setGatewayId(getGatewayId());
			msg.setMessageStatus(MessageStatuses.SENT);
			incOutboundMessageCount();
			ok = true;		
		}		
		else
		{			
			Logger.getInstance().logError("Error sending message. Response: " + response.get(0), null, getGatewayId());
			
			switch (Integer.parseInt(response.get(0)))
			{
				case -1:
					msg.setFailureCause(FailureCauses.GATEWAY_AUTH);
					break;
				case -2:
					msg.setFailureCause(FailureCauses.NO_CREDIT);
					break;
				case -5:
					msg.setFailureCause(FailureCauses.LOCAL_OPTOUT);
					break;					
				case -7:
					msg.setFailureCause(FailureCauses.BAD_FORMAT);
					break;
				case -104:
					msg.setFailureCause(FailureCauses.GLOBAL_OPTOUT);
					break;					
				case -106:
					msg.setFailureCause(FailureCauses.BAD_NUMBER);
					break;										
				case -10:
					msg.setFailureCause(FailureCauses.UNKNOWN);
					break;
				default:
					msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
					break;
			}
			msg.setRefNo(null);
			msg.setDispatchDate(null);
			msg.setMessageStatus(MessageStatuses.FAILED);
			ok = false;			
			
		}
		return ok;
	}
	
}
