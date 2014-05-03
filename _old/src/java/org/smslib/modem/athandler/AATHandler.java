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
import java.lang.reflect.Constructor;
import org.smslib.GatewayException;
import org.smslib.TimeoutException;
import org.smslib.AGateway.AsyncEvents;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.modem.CNMIDetector;
import org.smslib.modem.ModemGateway;

public abstract class AATHandler
{
	private ModemGateway gateway;

	private String storageLocations;

	private String description;

	String[] terminators;

	String[] unsolicitedResponses;

	public AATHandler(ModemGateway myGateway)
	{
		this.gateway = myGateway;
		this.storageLocations = "";
	}

	public ModemGateway getGateway()
	{
		return this.gateway;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getStorageLocations()
	{
		return this.storageLocations;
	}

	public void setStorageLocations(String myStorageLocations)
	{
		this.storageLocations = myStorageLocations;
	}

	public void addStorageLocation(String myStorageLocation)
	{
		this.storageLocations += myStorageLocation;
	}

	public String[] getTerminators()
	{
		return this.terminators;
	}

	public String[] getUnsolicitedResponses()
	{
		return this.unsolicitedResponses;
	}

	public String getUnsolicitedResponse(int index)
	{
		return this.unsolicitedResponses[index];
	}

	public abstract void sync() throws IOException, InterruptedException;

	public abstract void reset() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract void echoOff() throws IOException, InterruptedException;

	public abstract void init() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract void done() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean isAlive() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getSimStatus() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean enterPin(String pin) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean setVerboseErrors() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean setPduProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean setTextProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean setIndications() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract CNMIDetector getIndications();

	public abstract String getManufacturer() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getModel() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getSerialNo() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getImsi() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getSwVersion() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getBatteryLevel() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getSignalLevel() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getNetworkOperator()  throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean switchStorageLocation(String mem) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract void switchToCmdMode() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract void keepLinkOpen() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract int sendMessage(int size, String pdu, String phone, String text) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String listMessages(MessageClasses messageClass) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getMessageByIndex(int msgIndex) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract boolean deleteMessage(int memIndex, String memLocation) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getGprsStatus() throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String send(String s) throws TimeoutException, GatewayException, IOException, InterruptedException;

	public abstract String getNetworkRegistration() throws GatewayException, TimeoutException, IOException, InterruptedException;

	public abstract void readStorageLocations() throws Exception;

	public abstract String sendCustomATCommand(String atCommand) throws GatewayException, TimeoutException, IOException, InterruptedException;

	public abstract String sendUSSDCommand(String ussdCommand) throws GatewayException, TimeoutException, IOException, InterruptedException;

	public abstract String sendUSSDCommand(String ussdCommand, boolean interactive) throws GatewayException, TimeoutException, IOException, InterruptedException;

	public abstract boolean sendUSSDRequest(String presentation, String content, String dcs) throws GatewayException, TimeoutException, IOException, InterruptedException;

	public abstract String formatUSSDResponse(String ussdResponse);

	public abstract String readPhonebookLocations() throws GatewayException, TimeoutException, IOException, InterruptedException;

	public abstract String readPhonebook(String location) throws GatewayException, TimeoutException, IOException;

	public abstract AsyncEvents processUnsolicitedEvents(String response) throws IOException;

	public static AATHandler load(ModemGateway gateway, String gsmManuf, String gsmModel) throws RuntimeException
	{
		String BASE_HANDLER = ATHandler.class.getName();
		String[] handlerClassNames = { null, null, BASE_HANDLER };
		String[] handlerDescriptions = { null, null, "Generic" };
		StringBuffer handlerClassName = new StringBuffer(BASE_HANDLER);
		if (gsmManuf != null && gsmManuf.length() != 0)
		{
			handlerClassName.append("_").append(gsmManuf);
			handlerClassNames[1] = handlerClassName.toString();
			handlerDescriptions[1] = gsmManuf + " (Generic)";
			if (gsmModel != null && gsmModel.length() != 0)
			{
				handlerClassName.append("_").append(gsmModel);
				handlerClassNames[0] = handlerClassName.toString();
				handlerDescriptions[0] = gsmManuf + " " + gsmModel;
			}
		}
		AATHandler atHandler = null;
		for (int i = 0; i < 3; ++i)
		{
			try
			{
				if (handlerClassNames[i] != null)
				{
					Class<?> handlerClass = Class.forName(handlerClassNames[i]);
					Constructor<?> handlerConstructor = handlerClass.getConstructor(new Class[] { ModemGateway.class });
					atHandler = (AATHandler) handlerConstructor.newInstance(new Object[] { gateway });
					atHandler.description = handlerDescriptions[i];
					break;
				}
			}
			catch (Exception ex)
			{
				if (i == 2)
				{
					ex.printStackTrace();
					throw new RuntimeException("Class AATHandler: Cannot initialize handler!");
				}
			}
		}
		return atHandler;
	}

	/**
	 * Look up a terminating string for a response from a buffer
	 * 
	 * @param response
	 * @return negative = no match, otherwise match
	 */
	public int findMatchingTerminator(String response)
	{
		for (int i = 0; i < terminators.length; i++)
		{
			if (response.matches(terminators[i])) { return i; }
		}
		return -1;
	}

	/**
	 * Determine whether a terminator index returned by
	 * {@link #findMatchingTerminator(String)} is indicative of an unsolicited
	 * response from the modem
	 * 
	 * @param terminatorIndex
	 *            Index returned by {@link #findMatchingTerminator(String)}
	 * @return True if terminatorIndex indicates an unsolicited response
	 */
	public boolean isUnsolicitedResponse(int terminatorIndex)
	{
		return terminatorIndex >= this.terminators.length - 5;
	}

	/**
	 * Convenience method to avoid having to call
	 * {@link #findMatchingTerminator(String)} and
	 * {@link #isUnsolicitedResponse(int)}
	 * 
	 * @param response
	 * @return True if response is an unsolicited response
	 */
	public boolean isUnsolicitedResponse(String response)
	{
		int i = findMatchingTerminator(response);
		return isUnsolicitedResponse(i);
	}

	/**
	 * Convenience method to determine if a response is a terminating response
	 * 
	 * @param response
	 * @return True if a terminating response
	 */
	public boolean matchesTerminator(String response)
	{
		return findMatchingTerminator(response) >= 0;
	}
}
