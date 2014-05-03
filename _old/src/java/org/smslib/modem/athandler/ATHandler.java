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

package org.smslib.modem.athandler;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.smslib.GatewayException;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.AGateway.AsyncEvents;
import org.smslib.AGateway.Protocols;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.helper.Logger;
import org.smslib.modem.AModemDriver;
import org.smslib.modem.CNMIDetector;
import org.smslib.modem.ModemGateway;

/**
 * Generic modem AT handler.
 */
public class ATHandler extends AATHandler
{
	protected AModemDriver modemDriver;

	protected CNMIDetector cnmiDetector;

	/**
	 * Cell Broadcast Data Coding Scheme (CBS DCS) value of 15 indicates GSM
	 * default 7-bit alphabet (high nybble 0000), language unspecified (low
	 * nybble 1111)
	 **/
	protected static final int DEFAULT_USSD_DCS_NUM = 0x0f;

	public AModemDriver getModemDriver()
	{
		return this.modemDriver;
	}

	public ATHandler(ModemGateway myGateway)
	{
		super(myGateway);
		this.modemDriver = myGateway.getModemDriver();
		this.cnmiDetector = null;
		this.terminators = new String[14];
		this.terminators[0] = "OK\\s";
		this.terminators[1] = "\\s*[\\p{ASCII}]*\\s+OK\\s";
		this.terminators[2] = "(ERROR|NO CARRIER|NO DIALTONE)\\s";
		this.terminators[3] = "ERROR:\\s*\\d+\\s";
		this.terminators[4] = "\\+CM[ES]\\s+ERROR:\\s*\\d+\\s";
		this.terminators[5] = "\\+CPIN:\\s*READY\\s";
		this.terminators[6] = "\\+CPIN:\\s*SIM\\s*BUSY\\s";
		this.terminators[7] = "\\+CPIN:\\s*SIM\\s*PIN\\s";
		this.terminators[8] = "\\+CPIN:\\s*SIM\\s*PIN2\\s";
		this.terminators[9] = "\\+CUSD:\\s.*\\s";
		this.terminators[10] = "\\+CMTI:\\s*\\p{Punct}[\\p{ASCII}]+\\p{Punct}\\p{Punct}\\s*\\d+\\s";
		this.terminators[11] = "\\+CDSI:\\s*\\p{Punct}[\\p{ASCII}]+\\p{Punct}\\p{Punct}\\s*\\d+\\s";
		this.terminators[12] = "RING\\s";
		this.terminators[13] = "\\+CLIP:\\s*\\p{Punct}[\\p{ASCII}]*\\p{Punct}\\p{Punct}\\s*\\d+[\\p{ASCII}]*\\s";
		this.unsolicitedResponses = new String[5];
		this.unsolicitedResponses[0] = "+CMTI";
		this.unsolicitedResponses[1] = "+CDSI";
		this.unsolicitedResponses[2] = "RING";
		this.unsolicitedResponses[3] = "+CLIP";
		this.unsolicitedResponses[4] = "+CUSD";
	}

	@Override
	public void sync() throws IOException, InterruptedException
	{
		getModemDriver().write("ATZ\r");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
	}

	@Override
	public void reset() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("\u001b");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().write("+++");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().write("ATZ\r");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().clearBuffer();
	}

	@Override
	public void echoOff() throws IOException, InterruptedException
	{
		getModemDriver().write("ATE0\r");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().clearBuffer();
	}

	@Override
	public void init() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CLIP=1\r");
		getModemDriver().getResponse();
		if (!Service.getInstance().getSettings().DISABLE_COPS)
		{
			getModemDriver().write("AT+COPS=0\r");
			getModemDriver().getResponse();
		}
	}

	@Override
	public void done() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		// No default behavior.
	}

	@Override
	public boolean isAlive() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT\r");
		String response = getModemDriver().getResponse();
		if (response.indexOf("AT\r") == -1) { return getModemDriver().isOk(); }
		return false;
	}

	@Override
	public String getSimStatus() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CPIN?\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public boolean enterPin(String pin) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CPIN=\"_1_\"\r".replaceAll("_1_", pin));
		getModemDriver().getResponse();
		return (getModemDriver().isOk());
	}

	@Override
	public boolean setVerboseErrors() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CMEE=1\r");
		getModemDriver().getResponse();
		return (getModemDriver().isOk());
	}

	@Override
	public boolean setPduProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CMGF=0\r");
		getModemDriver().getResponse();
		return (getModemDriver().isOk());
	}

	@Override
	public boolean setTextProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CMGF=1\r");
		getModemDriver().getResponse();
		if (getModemDriver().isOk())
		{
			getModemDriver().write("AT+CSCS=\"8859-1\"\r");
			getModemDriver().getResponse();
			return (getModemDriver().isOk());
		}
		return false;
	}

	@Override
	public boolean setIndications() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int RETRIES = 3;
		int count = 0;
		while (count < RETRIES)
		{
			getModemDriver().write("AT+CNMI=?\r");
			try
			{
				this.cnmiDetector = new CNMIDetector(getModemDriver().getResponse());
				getModemDriver().write(this.cnmiDetector.getATCommand());
				getModemDriver().getResponse();
				return (getModemDriver().isOk());
			}
			catch (Exception e)
			{
				count++;
				Logger.getInstance().logWarn("Retrying the detection of CNMI, modem busy?", null, getGateway().getGatewayId());
				Thread.sleep(Service.getInstance().getSettings().AT_WAIT_CNMI);
			}
		}
		Logger.getInstance().logWarn("CNMI detection failed, proceeding with defaults.", null, getGateway().getGatewayId());
		return false;
	}

	@Override
	public CNMIDetector getIndications()
	{
		return this.cnmiDetector;
	}

	@Override
	public String getManufacturer() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CGMI\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public String getModel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CGMM\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public String getSerialNo() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CGSN\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public String getImsi() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CIMI\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public String getSwVersion() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CGMR\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public String getBatteryLevel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CBC\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public String getSignalLevel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CSQ\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public String getNetworkOperator() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+COPS?\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public boolean switchStorageLocation(String mem) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (mem.equalsIgnoreCase("--")) return true;
		getModemDriver().write("AT+CPMS=\"" + mem + "\"\r");
		getModemDriver().getResponse();
		return (getModemDriver().isOk());
	}

	@Override
	public void switchToCmdMode() throws IOException, InterruptedException
	{
		getModemDriver().write("+++");
		java.util.Date start = new java.util.Date();
		while (new java.util.Date().getTime() - start.getTime() <= Service.getInstance().getSettings().AT_WAIT_CMD)
			Thread.sleep(100);
	}

	@Override
	public void keepLinkOpen() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (!Service.getInstance().getSettings().DISABLE_CMMS)
		{
			getModemDriver().write("AT+CMMS=2\r");
			getModemDriver().getResponse();
		}
	}

	@Override
	public int sendMessage(int size, String pdu, String phone, String text) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int responseRetries, errorRetries;
		String response;
		int refNo = -1;
		if (getGateway().getProtocol() == Protocols.PDU)
		{
			errorRetries = 0;
			while (true)
			{
				responseRetries = 0;
				getModemDriver().write("AT+CMGS=\"_1_\"\r".replaceAll("\"_1_\"", "" + size));
				Thread.sleep(Service.getInstance().getSettings().AT_WAIT_CGMS);
				while (!getModemDriver().dataAvailable())
				{
					responseRetries++;
					if (responseRetries == Service.getInstance().getSettings().OUTBOUND_RETRIES) throw new GatewayException("Gateway is not responding, max number of retries reached.");
					Logger.getInstance().logWarn("ATHandler().SendMessage(): Still waiting for response (I) (" + responseRetries + ")...", null, getGateway().getGatewayId());
					Thread.sleep(Service.getInstance().getSettings().OUTBOUND_RETRY_WAIT);
				}
				responseRetries = 0;
				getModemDriver().clearBuffer();
				getModemDriver().write(pdu);
				getModemDriver().write((char) 26);
				response = getModemDriver().getResponse();
				while (response.length() == 0)
				{
					responseRetries++;
					if (responseRetries == Service.getInstance().getSettings().OUTBOUND_RETRIES) throw new GatewayException("Gateway is not responding, max number of retries reached.");
					Logger.getInstance().logWarn("ATHandler().SendMessage(): Still waiting for response (II) (" + responseRetries + ")...", null, getGateway().getGatewayId());
					Thread.sleep(Service.getInstance().getSettings().OUTBOUND_RETRY_WAIT);
					response = getModemDriver().getResponse();
				}
				if (getModemDriver().getLastError() == 0)
				{
					Matcher m = Pattern.compile("\\s*\\+CMGS: *(\\d+)").matcher(response);
					if (m.find())
					{
						refNo = Integer.parseInt(m.group(1));
					}
					else
					{
						// Message-Reference ID not returned
						refNo = -1;
					}
					break;
				}
				else if (getModemDriver().getLastError() > 0)
				{
					// CMS or CME error could happen here
					errorRetries++;
					if (errorRetries == Service.getInstance().getSettings().OUTBOUND_RETRIES)
					{
						Logger.getInstance().logError(getModemDriver().getLastErrorText() + ": Quit retrying, message lost...", null, getGateway().getGatewayId());
						refNo = -1;
						break;
					}
					Logger.getInstance().logWarn(getModemDriver().getLastErrorText() + ": Retrying...", null, getGateway().getGatewayId());
					Thread.sleep(Service.getInstance().getSettings().OUTBOUND_RETRY_WAIT);
				}
				else refNo = -1;
			}
		}
		else if (getGateway().getProtocol() == Protocols.TEXT)
		{
			getModemDriver().write("AT+CMGS=\"_1_\"\r".replaceAll("_1_", phone));
			getModemDriver().clearBuffer();
			getModemDriver().write(text);
			Thread.sleep(Service.getInstance().getSettings().AT_WAIT_CGMS);
			getModemDriver().write((char) 26);
			response = getModemDriver().getResponse();
			if (response.indexOf("OK\r") >= 0)
			{
				int i;
				StringBuilder tmp = new StringBuilder();
				i = response.indexOf(":");
				while (!Character.isDigit(response.charAt(i)))
					i++;
				while (Character.isDigit(response.charAt(i)))
				{
					tmp.append(response.charAt(i));
					i++;
				}
				refNo = Integer.parseInt(tmp.toString());
			}
			else refNo = -1;
		}
		return refNo;
	}

	@Override
	public String listMessages(MessageClasses messageClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getGateway().getProtocol() == Protocols.PDU)
		{
			if (messageClass == MessageClasses.ALL) getModemDriver().write("AT+CMGL=4\r");
			else if (messageClass == MessageClasses.UNREAD) getModemDriver().write("AT+CMGL=0\r");
			else if (messageClass == MessageClasses.READ) getModemDriver().write("AT+CMGL=1\r");
		}
		else if (getGateway().getProtocol() == Protocols.TEXT)
		{
			if (messageClass == MessageClasses.ALL) getModemDriver().write("AT+CMGL=\"ALL\"\r");
			else if (messageClass == MessageClasses.UNREAD) getModemDriver().write("AT+CMGL=\"REC UNREAD\"\r");
			else if (messageClass == MessageClasses.READ) getModemDriver().write("AT+CMGL=\"REC READ\"\r");
		}
		return getModemDriver().getResponse();
	}

	@Override
	public String getMessageByIndex(int msgIndex) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CMGR=" + msgIndex + "\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public boolean deleteMessage(int memIndex, String memLocation) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (!switchStorageLocation(memLocation)) return false;
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().write("AT+CMGD=_1_\r".replaceAll("_1_", "" + memIndex));
		getModemDriver().getResponse();
		return (getModemDriver().isOk());
	}

	@Override
	public String getGprsStatus() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CGATT?\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public String send(String s) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write(s);
		return (getModemDriver().getResponse());
	}

	@Override
	public String getNetworkRegistration() throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CREG=1\r");
		getModemDriver().getResponse();
		getModemDriver().write("AT+CREG?\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public void readStorageLocations() throws Exception
	{
		String response, loc;
		StringTokenizer tokens;
		getModemDriver().write("AT+CPMS=?\r");
		response = getModemDriver().getResponse();
		try
		{
			if (response.indexOf("+CPMS:") >= 0)
			{
				int i, j;
				i = response.indexOf('(');
				while (response.charAt(i) == '(')
					i++;
				j = i;
				while (response.charAt(j) != ')')
					j++;
				response = response.substring(i, j);
				tokens = new StringTokenizer(response, ",");
				while (tokens.hasMoreTokens())
				{
					loc = tokens.nextToken().replaceAll("\"", "");
					if ((!loc.equalsIgnoreCase("MT")) && ((getStorageLocations().indexOf(loc) < 0))) addStorageLocation(loc);
				}
			}
			else
			{
				addStorageLocation("SM");
				Logger.getInstance().logWarn("CPMS detection failed, proceeding with default storage 'SM'.", null, getGateway().getGatewayId());
			}
		}
		catch (Exception e)
		{
			addStorageLocation("SM");
			Logger.getInstance().logWarn("CPMS detection failed, proceeding with default storage 'SM'.", null, getGateway().getGatewayId());
		}
	}

	@Override
	public String readPhonebookLocations() throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		String response;
		getModemDriver().write("AT+CPBS=?\r");
		response = getModemDriver().getResponse();
		if (response.indexOf("+CPBS:") >= 0)
		{
			response = response.replaceAll("\\s*\\+CPBS:\\s*", "");
			response = response.replaceAll("[()]", "");
			return response;
		}
		return "";
	}

	@Override
	public String readPhonebook(String loc) throws GatewayException, TimeoutException, IOException
	{
		String response;
		int minIndex, maxIndex;
		try
		{
			getModemDriver().write("AT+CPBS=\"" + loc + "\"\r");
			getModemDriver().getResponse();
			getModemDriver().write("AT+CPBR=?\r");
			response = getModemDriver().getResponse();
			response = response.replaceAll("\\s*\\+CPBR:\\s*", "");
			response = response.replaceAll("[()]", "");
			StringTokenizer tokens1 = new StringTokenizer(response, ",");
			StringTokenizer tokens2 = new StringTokenizer(tokens1.nextToken(), "-");
			minIndex = Integer.parseInt(tokens2.nextToken());
			maxIndex = Integer.parseInt(tokens2.nextToken());
			getModemDriver().write("AT+CPBR=" + minIndex + "," + maxIndex + "\r");
			response = getModemDriver().getResponse();
			return response;
		}
		catch (Exception e)
		{
			Logger.getInstance().logWarn("Phonebook detection failed.", null, getGateway().getGatewayId());
			return "";
		}
	}

	@Override
	public String sendCustomATCommand(String atCommand) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		getModemDriver().write(atCommand);
		return getModemDriver().getResponse();
	}

	@Override
	public String sendUSSDCommand(String ussdCommand) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		return sendUSSDCommand(ussdCommand, false);
	}

	@Override
	public String sendUSSDCommand(String ussdCommand, boolean interactive) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		String command = formatUSSDCommand(ussdCommand);
		// get the immediate result code
		String ussdResponse = sendCustomATCommand(command);
		if (!ussdResponse.contains("OK"))
		{
			Logger.getInstance().logError("+CUSD command returned non-OK result: " + ussdResponse, null, getGateway().getGatewayId());
			return null;
		}
		// get the unsolicted result
		ussdResponse = getModemDriver().getResponse(AsyncEvents.USSDRESPONSE);
		if (!interactive)
		{
			/*
			 * This is a workaround for interactive USSD sessions which expect
			 * further user input. We preemptively send an escape character which
			 * will terminate such a session.
			 */
			char esc = 0x1b;
			command = "" + esc + '\r';
			getModemDriver().write(command);
			getModemDriver().clearBuffer();
		}
		String response;
		String regex = "\"(.*)\"";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(ussdResponse);
		if (matcher.find())
		{
			response = formatUSSDResponse(matcher.group(1));
		}
		else
		{
			response = ussdResponse;
		}
		return response;
	}

	@Override
	public boolean sendUSSDRequest(String presentation, String content, String dcs) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		String rawRequest = formatUSSDCommand(presentation, content, dcs);
		String ussdResponse = sendCustomATCommand(rawRequest);
		if (!ussdResponse.contains("OK"))
		{
			Logger.getInstance().logError("+CUSD command returned non-OK result: " + ussdResponse, null, getGateway().getGatewayId());
			return false;
		}
		return true;
	}

	protected String formatUSSDCommand(String ussdCommand)
	{
		return formatUSSDCommand("1", ussdCommand, null);
	}

	protected String formatUSSDCommand(String presentation, String content, String dcs)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("AT+CUSD=");
		buf.append(presentation);
		buf.append(",");
		buf.append("\"");
		buf.append(content);
		buf.append("\"");
		if (dcs != null && dcs.length() > 0)
		{
			buf.append(",");
			buf.append(dcs);
		}
		buf.append("\r");
		return buf.toString();
	}

	public String formatUSSDResponse(String ussdResponse)
	{
		// noop for most modems but some may require additional processing e.g. pdudecode
		return ussdResponse;
	}

	@Override
	public AsyncEvents processUnsolicitedEvents(String response) throws IOException
	{
		AsyncEvents event = AsyncEvents.NOTHING;
		if (response.indexOf(getUnsolicitedResponse(0)) >= 0) event = AsyncEvents.INBOUNDMESSAGE;
		else if (response.indexOf(getUnsolicitedResponse(1)) >= 0) event = AsyncEvents.INBOUNDSTATUSREPORTMESSAGE;
		else if (response.indexOf(getUnsolicitedResponse(2)) >= 0) event = AsyncEvents.NOTHING;
		else if (response.indexOf(getUnsolicitedResponse(3)) >= 0) event = AsyncEvents.INBOUNDCALL;
		else if (response.indexOf(getUnsolicitedResponse(4)) >= 0) event = AsyncEvents.USSDRESPONSE;
		return event;
	}
}
