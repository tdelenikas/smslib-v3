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

package org.smslib.smsserver.interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageEncodings;
import org.smslib.Message.MessageTypes;
import org.smslib.OutboundMessage;
import org.smslib.helper.ExtStringBuilder;
import org.smslib.helper.Logger;
import org.smslib.smsserver.SMSServer;

/**
 * Interface for http communication with SMSServer. <br />
 * 
 * @author Ernas M. Jamil
 * @author Sebastian Just - Added POST suppport (Issue 133)
 */
public class Http extends Interface<Integer>
{
	public Http(String myInterfaceId, Properties myProps, SMSServer myServer, InterfaceTypes myType)
	{
		super(myInterfaceId, myProps, myServer, myType);
		setDescription("Default HTTP interface.");
	}

	@Override
	public void messagesReceived(Collection<InboundMessage> msgList) throws Exception
	{
		String template = getProperty("get_url");
		Logger.getInstance().logInfo("SMSServer: Interface HTTP: " + template, null, null);
		for (InboundMessage im : msgList)
		{
			if (template != null && ((im.getType() == MessageTypes.INBOUND) || (im.getType() == MessageTypes.STATUSREPORT)))
			{
				String getURL = updateInboundTemplateString(template, im);
				Logger.getInstance().logInfo("SMSServer: Interface HTTP: " + getURL, null, null);
				URL url = new URL(getURL);
				HttpDo(url);
			}
		}
	}

	@Override
	public void markMessage(OutboundMessage om) throws Exception
	{
		String template = getProperty("dlr_url");
		if (template != null)
		{
			String dlrURL = updateOutboundTemplateString(template, om);
			URL url = new URL(dlrURL);
			HttpDo(url);
		}
		getMessageCache().remove(om.getId());
	}

	List<String> HttpDo(URL url) throws IOException
	{
		if ("POST".equalsIgnoreCase(getProperty("method"))) return HttpPost(url);
		return HttpGet(url);
	}

	List<String> HttpPost(URL url) throws IOException
	{
		List<String> responseList = new ArrayList<String>();
		URL cleanUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath());
		Logger.getInstance().logInfo("HTTP POST: " + cleanUrl, null, null);
		URLConnection con = cleanUrl.openConnection();
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(url.getQuery());
		out.flush();
		con.setAllowUserInteraction(false);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			responseList.add(inputLine);
		out.close();
		in.close();
		return responseList;
	}

	List<String> HttpGet(URL url) throws IOException
	{
		List<String> responseList = new ArrayList<String>();
		Logger.getInstance().logInfo("HTTP GET: " + url, null, null);
		URLConnection con = url.openConnection();
		con.setAllowUserInteraction(false);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			responseList.add(inputLine);
		in.close();
		return responseList;
	}

	private String updateInboundTemplateString(String template, InboundMessage msg) throws UnsupportedEncodingException
	{
		ExtStringBuilder sb = new ExtStringBuilder(template);
		sb.replaceAll("%gatewayId%", URLEncoder.encode(msg.getGatewayId(), getProperty("encoding", "ISO-8859-1")));
		sb.replaceAll("%encoding%", (msg.getEncoding() == MessageEncodings.ENC7BIT ? "7-bit" : (msg.getEncoding() == MessageEncodings.ENC8BIT ? "8-bit" : "UCS2 (Unicode)")));
		if(msg.getDate() != null) sb.replaceAll("%date%", URLEncoder.encode(msg.getDate().toString(), getProperty("encoding", "ISO-8859-1")));
		sb.replaceAll("%text%", URLEncoder.encode(msg.getText(), getProperty("encoding", "ISO-8859-1")));
		sb.replaceAll("%originator%", msg.getOriginator());
		sb.replaceAll("%memIndex%", msg.getMemIndex());
		sb.replaceAll("%mpMemIndex%", msg.getMpMemIndex());
		return sb.toString();
	}

	private String updateOutboundTemplateString(String template, OutboundMessage msg) throws UnsupportedEncodingException
	{
		ExtStringBuilder sb = new ExtStringBuilder(template);
		sb.replaceAll("%gatewayId%", URLEncoder.encode(msg.getGatewayId(), getProperty("encoding", "ISO-8859-1")));
		sb.replaceAll("%encoding%", (msg.getEncoding() == MessageEncodings.ENC7BIT ? "7-bit" : (msg.getEncoding() == MessageEncodings.ENC8BIT ? "8-bit" : "UCS2 (Unicode)")));
		if(msg.getDate() != null) sb.replaceAll("%date%", URLEncoder.encode(msg.getDate().toString(), getProperty("encoding", "ISO-8859-1")));
		sb.replaceAll("%text%", URLEncoder.encode(msg.getText(), getProperty("encoding", "ISO-8859-1")));
		sb.replaceAll("%refNo%", URLEncoder.encode(msg.getRefNo(), getProperty("encoding", "ISO-8859-1")));
		sb.replaceAll("%recipient%", msg.getRecipient());
		sb.replaceAll("%from%", msg.getFrom());
		sb.replaceAll("%failureCause%", URLEncoder.encode(msg.getFailureCause().toString(), getProperty("encoding", "ISO-8859-1")));
		sb.replaceAll("%messageStatus%", msg.getMessageStatus().toString());
		return sb.toString();
	}	}
