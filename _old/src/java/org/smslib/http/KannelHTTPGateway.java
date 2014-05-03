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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;
import org.smslib.Message.MessageEncodings;
import org.smslib.OutboundMessage.FailureCauses;
import org.smslib.OutboundMessage.MessageStatuses;
import org.smslib.helper.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Gateway for Kannel (http://www.kannel.org) Outbound only.
 * 
 * @author Bassam Al-Sarori
 */
public class KannelHTTPGateway extends HTTPGateway
{
	String sendUrl;

	String username;

	String password;

	String adminUrl;

	String adminPassword;

	String statusPassword;

	boolean autoStartSmsc;

	boolean autoStopSmsc;

	Object SYNC_Commander;

	public enum KannelSMSCStatuses
	{
		/**
		 * SMSC is online.
		 */
		ONLINE,
		/**
		 * SMSC is dead.
		 */
		DEAD,
		/**
		 * Connecting to SMSC.
		 */
		CONNECTING,
		/**
		 * Reconnecting to SMSC.
		 */
		RECONNECTING,
		/**
		 * SMSC is not available in Kannel.
		 */
		UNAVAILABLE,
		/**
		 * SMSC is unkown.
		 */
		UNKNOWN
	}

	/**
	 * Constructs a new instance object of this class.
	 * 
	 * @param smscId
	 *            this gateway's Id. Should be the same as the id used Kannel's
	 *            SMSC (smsc-id).
	 * @param sendUrl
	 *            URL used to send SMS through Kannel.
	 * @param username
	 *            Kannel's send-sms Username.
	 * @param password
	 *            Kannel's send-sms Password.
	 */
	public KannelHTTPGateway(String smscId, String sendUrl, String username, String password)
	{
		super(smscId);
		this.sendUrl = sendUrl;
		this.username = username;
		this.password = password;
		this.SYNC_Commander = new Object();
		setAttributes(AGateway.GatewayAttributes.SEND | AGateway.GatewayAttributes.CUSTOMFROM | AGateway.GatewayAttributes.BIGMESSAGES | AGateway.GatewayAttributes.FLASHSMS | AGateway.GatewayAttributes.RECEIVE);
	}

	@Override
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		URL url;
		List<HttpHeader> request = new ArrayList<HttpHeader>();
		List<String> response;
		String encodingHeader;
		String text;
		String from = msg.getFrom();
		request.add(new HttpHeader("username", this.username, false));
		request.add(new HttpHeader("password", this.password, false));
		request.add(new HttpHeader("smsc", getGatewayId(), false));
		request.add(new HttpHeader("to", msg.getRecipient(), false));
		int priority = msg.getPriority();
		if (priority < 0)
		{
			priority = 0;
		}
		else if (priority > 3)
		{
			priority = 3;
		}
		request.add(new HttpHeader("priority", priority + "", false));
		if (msg.getValidityPeriod() >= 0) request.add(new HttpHeader("validity", (msg.getValidityPeriod() * 60) + "", false));
		if (from == null)
		{
			from = getFrom();
		}
		if (from != null)
		{
			request.add(new HttpHeader("from", from, false));
		}
		MessageEncodings encoding = msg.getEncoding();
		switch (encoding)
		{
			case ENC8BIT:
				encodingHeader = "1";
				text = URLEncoder.encode(msg.getText(), "utf-8");
				break;
			case ENCUCS2:
				encodingHeader = "2";
				text = URLEncoder.encode(msg.getText(), "utf-16BE");
				break;
			default:
				encodingHeader = "0";
				text = URLEncoder.encode(msg.getText(), "utf-8");
				break;
		}
		request.add(new HttpHeader("coding", encodingHeader, false));
		if (msg.getFlashSms())
		{
			request.add(new HttpHeader("mclass", "1", false));
		}
		request.add(new HttpHeader("text", text, false));
		String reqLine = ExpandHttpHeaders(request);
		url = new URL(this.sendUrl + "?" + reqLine);
		synchronized (this.SYNC_Commander)
		{
			response = HttpGet(url);
			if (!response.get(0).startsWith("202"))
			{
				Logger.getInstance().logError("Error sending message. Response: " + response.get(0) + " - " + response.get(1), null, getGatewayId());
				msg.setRefNo(null);
				msg.setDispatchDate(null);
				msg.setMessageStatus(MessageStatuses.FAILED);
				msg.setFailureCause(getFailureCause(response));
				return false;
			}
			else
			{
				Logger.getInstance().logInfo("Message sent. Response: " + response.get(0) + " - " + response.get(1), null, getGatewayId());
			}
		}
		//msg.setRefNo("");
		msg.setDispatchDate(new Date());
		msg.setGatewayId(getGatewayId());
		msg.setMessageStatus(MessageStatuses.SENT);
		incOutboundMessageCount();
		return true;
	}

	private FailureCauses getFailureCause(List<String> response)
	{
		if (response.get(0).startsWith("503")) { return FailureCauses.GATEWAY_FAILURE; }
		String errorMessage = response.get(0);
		if (errorMessage.startsWith("Authorization failed"))
		{
			return FailureCauses.GATEWAY_AUTH;
		}
		else if (errorMessage.startsWith("Missing receiver"))
		{
			return FailureCauses.BAD_NUMBER;
		}
		else if (errorMessage.startsWith("Not routable"))
		{
			return FailureCauses.NO_ROUTE;
		}
		else if (errorMessage.startsWith("Sender missing"))
		{
			return FailureCauses.BAD_FORMAT;
		}
		else
		{
			return FailureCauses.UNKNOWN;
		}
	}

	public String getSendUrl()
	{
		return sendUrl;
	}

	public void setSendUrl(String sendUrl)
	{
		this.sendUrl = sendUrl;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getAdminUrl()
	{
		return adminUrl;
	}

	public void setAdminUrl(String adminUrl)
	{
		this.adminUrl = adminUrl;
		if (!this.adminUrl.endsWith("/")) this.adminUrl += "/";
	}

	public String getAdminPassword()
	{
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword)
	{
		this.adminPassword = adminPassword;
	}

	@Override
	public void startGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (autoStartSmsc)
		{
			if (!startSmsc())
			{
				setStatus(GatewayStatuses.STARTING);
				super.stopGateway();
				setStatus(GatewayStatuses.RESTART);
				return;
			}
		}
		super.startGateway();
	}

	@Override
	public void stopGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (autoStopSmsc)
		{
			stopSmsc();
		}
		super.stopGateway();
	}

	/**
	 * Sends a start-smsc command to Kannel in order to start SMSC.
	 * 
	 * @return true if command was send successfully, false otherwise.
	 * @throws TimeoutException
	 * @throws GatewayException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean startSmsc() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		boolean started = true;
		KannelSMSCStatuses smscStatus = getKannelSMSCStatus();
		if (smscStatus == KannelSMSCStatuses.UNAVAILABLE || smscStatus == KannelSMSCStatuses.UNKNOWN)
		{
			started = false;
			Logger.getInstance().logError("SMSC was not found on Kannel.", null, getGatewayId());
		}
		else if (smscStatus == KannelSMSCStatuses.DEAD)
		{
			if (this.adminPassword == null)
			{
				Logger.getInstance().logWarn("Can't start Kannel SMSC, admin password not set.", null, getGatewayId());
			}
			else
			{
				List<HttpHeader> request = new ArrayList<HttpHeader>();
				request.add(new HttpHeader("smsc", getGatewayId(), false));
				request.add(new HttpHeader("password", this.adminPassword, false));
				String reqLine = ExpandHttpHeaders(request);
				URL url = new URL(this.adminUrl + "start-smsc.txt?" + reqLine);
				List<String> response;
				synchronized (this.SYNC_Commander)
				{
					response = HttpGet(url);
				}
				if (response.get(1).startsWith("Denied"))
				{
					Logger.getInstance().logError("Could not start SMSC." + response.get(0) + " - " + response.get(1), null, getGatewayId());
					started = false;
				}
				else
				{
					Logger.getInstance().logInfo("Sent command to start SMSC." + response.get(0) + " - " + response.get(1), null, getGatewayId());
				}
			}
		}
		return started;
	}

	/**
	 * Sends a stop-smsc command to Kannel in order to stop SMSC.
	 * 
	 * @return true if command was send successfully, false otherwise.
	 * @throws TimeoutException
	 * @throws GatewayException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean stopSmsc() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		boolean stopped = true;
		if (this.adminUrl == null || this.adminPassword == null)
		{
			Logger.getInstance().logError("Can't stop Kannel SMSC, admin Url and password need to be set.", null, getGatewayId());
			stopped = false;
		}
		else
		{
			List<HttpHeader> request = new ArrayList<HttpHeader>();
			request.add(new HttpHeader("smsc", getGatewayId(), false));
			request.add(new HttpHeader("password", this.adminPassword, false));
			String reqLine = ExpandHttpHeaders(request);
			URL url = new URL(this.adminUrl + "stop-smsc.txt?" + reqLine);
			List<String> response;
			synchronized (this.SYNC_Commander)
			{
				response = HttpGet(url);
			}
			if (response.get(1).startsWith("Denied"))
			{
				Logger.getInstance().logError("Could not stop SMSC." + response.get(0) + " - " + response.get(1), null, getGatewayId());
				stopped = false;
			}
			else
			{
				Logger.getInstance().logInfo("Sent command to stop SMSC." + response.get(0) + " - " + response.get(1), null, getGatewayId());
			}
		}
		return stopped;
	}

	/**
	 * Gets SMSC Status from Kannel.
	 * 
	 * @return KannelSMSCStatuses that represents SMSC status.
	 *         KannelSMSCStatuses.UNKOWN in case status is unknown.
	 */
	public KannelSMSCStatuses getKannelSMSCStatus()
	{
		if (this.adminUrl == null)
		{
			Logger.getInstance().logWarn("Can't check Kannel SMSC status, admin URL not set.", null, getGatewayId());
			return KannelSMSCStatuses.UNKNOWN;
		}
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc = null;
		try
		{
			docBuilder = docBuilderFactory.newDocumentBuilder();
			String url = adminUrl + "status.xml";
			if (statusPassword != null) url += "?password=" + statusPassword;
			doc = docBuilder.parse(url);
		}
		catch (SAXException e)
		{
			Logger.getInstance().logError("Error getting smsc status.", e, getGatewayId());
			return KannelSMSCStatuses.UNKNOWN;
			//throw new GatewayException("Unable to get Gateway's ("+getGatewayId()+") status from Kannel.");
		}
		catch (ParserConfigurationException e)
		{
			Logger.getInstance().logError("Error getting smsc status.", e, getGatewayId());
			return KannelSMSCStatuses.UNKNOWN;
			//throw new GatewayException("Unable to get Gateway's ("+getGatewayId()+") status from Kannel.");
		}
		catch (IOException e)
		{
			Logger.getInstance().logError("Error getting smsc status. Make sure Kannel is running.", e, getGatewayId());
			return KannelSMSCStatuses.UNKNOWN;
			//throw new GatewayException("Unable to get Gateway's ("+getGatewayId()+") status from Kannel.");
		}
		NodeList smscsList = doc.getElementsByTagName("smsc");
		int totalSmscs = smscsList.getLength();
		KannelSMSCStatuses smscStatus = KannelSMSCStatuses.UNAVAILABLE;
		for (int i = 0; i < totalSmscs; i++)
		{
			Node smscNode = smscsList.item(i);
			if (smscNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element smsc = (Element) smscNode;
				NodeList smscIdNodes = smsc.getElementsByTagName("id");
				String smscId = smscIdNodes.item(0).getTextContent();
				if (getGatewayId().equalsIgnoreCase(smscId))
				{
					NodeList smscStatusNodes = smsc.getElementsByTagName("status");
					String status = smscStatusNodes.item(0).getTextContent();
					if (status.startsWith("online"))
					{
						smscStatus = KannelSMSCStatuses.ONLINE;
					}
					else if (status.startsWith("connecting"))
					{
						smscStatus = KannelSMSCStatuses.CONNECTING;
					}
					else if (status.startsWith("re-connecting"))
					{
						smscStatus = KannelSMSCStatuses.RECONNECTING;
					}
					else if (status.startsWith("dead"))
					{
						smscStatus = KannelSMSCStatuses.DEAD;
					}
					return smscStatus;
				}
			}
		}
		return smscStatus;
	}

	@Override
	List<String> HttpGet(URL url) throws IOException
	{
		List<String> responseList = new ArrayList<String>();
		Logger.getInstance().logInfo("HTTP GET: " + url, null, getGatewayId());
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setConnectTimeout(20000);
		con.setAllowUserInteraction(false);
		// con.getErrorStream() will always return null if con.getResponseCode() is not called first
		responseList.add(con.getResponseCode() + " " + con.getResponseMessage());
		// call con.getInputStream() with error response codes such as 4xx or 5xx (which is used by Kannel when there an error)
		// causes IOException
		// we need to make sure first that there is nothing in error stream
		InputStream inputStream = con.getErrorStream();
		// call con.getInputStream() only if con.getErrorStream() returns null
		if (inputStream == null) inputStream = con.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			responseList.add(inputLine);
		in.close();
		return responseList;
	}

	public String getStatusPassword()
	{
		return statusPassword;
	}

	public void setStatusPassword(String statusPassword)
	{
		this.statusPassword = statusPassword;
	}

	public boolean isAutoStartSmsc()
	{
		return autoStartSmsc;
	}

	/**
	 * Set to true in order to start SMSC whenever startGateway is called.
	 */
	public void setAutoStartSmsc(boolean autoStartSmsc)
	{
		this.autoStartSmsc = autoStartSmsc;
	}

	public boolean isAutoStopSmsc()
	{
		return autoStopSmsc;
	}

	/**
	 * Set to true in order to stop SMSC whenever stopGateway is called.
	 */
	public void setAutoStopSmsc(boolean autoStopSmsc)
	{
		this.autoStopSmsc = autoStopSmsc;
	}
}
