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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.smslib.GatewayException;
import org.smslib.TimeoutException;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.modem.ModemGateway;

/**
 * AT Handler for Wavecom Q2438F CDMA modems. Rewritten from Wavecom
 * WISMOQCDMA CDMA modem
 *
 * @author Hendra Cengala
 */
public class ATHandler_Wavecom_Q2438F extends ATHandler_Wavecom
{
	public ATHandler_Wavecom_Q2438F(ModemGateway myGateway)
	{
		super(myGateway);
		setStorageLocations("MT");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.smslib.modem.athandler.ATHandler#listMessages(org.smslib.InboundMessage
	 * .MessageClasses) GSM modem
	 * +CMGL:2,"REC READ","+8613520073322",,"08/07/02,15:10:49+32" CDMA modem
	 * +CMGL:23,"REC READ","02607707075",0,2,18
	 * +CMGR:"REC READ","02270731045","08/09/03,22 :07 :13",0,2,0,19
	 */
	@Override
	public String listMessages(MessageClasses messageClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		BufferedReader reader;
		String listMsgRespons, line = null;
		StringBuffer msgList = new StringBuffer();
		listMsgRespons = super.listMessages(messageClass);
		reader = new BufferedReader(new StringReader(listMsgRespons));
		while ((line = reader.readLine()) != null)
		{
			if (line.matches("^\\+CMGL:\\s*\\d+,.*"))
			{
				int memIndex = 0;
				int i = line.indexOf(':');
				int j = line.indexOf(',');
				memIndex = Integer.parseInt(line.substring(i + 1, j).trim());
				String oneMessage = getGateway().getMessageByIndex(memIndex);
				BufferedReader oneMessageReader = new BufferedReader(new StringReader(oneMessage));
				// get first line which has message sender ,time fields
				// make the header can be compatible GSM header information
                //+CMGR:"REC READ","9009","10/06/18,12 :16 :48",0,2,0,"9009",156
				String header = oneMessageReader.readLine().trim();
				//+CMGR:"REC READ","02313383079","08/09/02,18 :41 :03",0,2,0,159
				String headerRegex = "(^\\+CMGR:)(\\\"[^\\\"]+\\\",)(\\\"[^\\\"]+\\\",)(\\\"[^,]+,)(\\d+)\\s+(:\\d+)\\s+(:\\d+\\\")(,\\d,)(\\d)";
				Pattern headerPattern = Pattern.compile(headerRegex);
				Matcher headerMatcher = headerPattern.matcher(header);
				header = headerMatcher.replaceAll("$1 " + memIndex + ",$2$3,$4$5$6$7$8$9");
				char encoding = headerMatcher.replaceAll("$9").charAt(0);
				// read message content
				StringBuffer msgContent = new StringBuffer();
				String bodyLine = "";
				while ((bodyLine = oneMessageReader.readLine()) != null)
				{
					bodyLine = bodyLine.trim();
					if (bodyLine.length() <= 0 || bodyLine.equalsIgnoreCase("OK"))
					{
						continue;
					}
					msgContent.append(bodyLine);
				}
				String msgContentStr = msgContent.toString();
				if (encoding == '4')
				{
					char[] unicodeText = msgContentStr.toCharArray();
					msgContentStr = new String(unicodeText);
				}
				// recomposite the header and message content
				String msgText = header + "\r" + msgContentStr + "\r";
				msgList.append(msgText);
			}
		}
		reader.close();
		// add OK at the end
		//msgList.append("OK\r");
		return msgList.toString();
	}

	@Override
	public String getManufacturer() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+WGMI\r");
		return (getModemDriver().getResponse().replaceAll("\\+WGMI: ", ""));
	}

	@Override
	public String getModel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+WGMM\r");
		return (getModemDriver().getResponse().replaceAll("\\+WGMM: ", ""));
	}

	@Override
	public String getSimStatus() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+WPIN?\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public boolean enterPin(String pin) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+WPIN=3,_1_\r".replaceAll("_1_", pin));
		getModemDriver().getResponse();
		return (getModemDriver().isOk());
	}

	@Override
	public String getBatteryLevel() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CBC?\r");
		return (getModemDriver().getResponse());
	}

	@Override
	public String getSerialNo() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CGSN\r");
		return (getModemDriver().getResponse().replaceAll("\\+CGSN: ", ""));
	}

	@Override
	public boolean setTextProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
			getModemDriver().write("AT+CSCS=\"CDMA\"\r");
			getModemDriver().getResponse();
			return (getModemDriver().isOk());
	}
}