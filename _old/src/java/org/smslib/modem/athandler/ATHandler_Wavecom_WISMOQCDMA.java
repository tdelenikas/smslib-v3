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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.smslib.GatewayException;
import org.smslib.TimeoutException;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.modem.ModemGateway;

/**
 * AT Handler for Wavecom WISMOQ CDMA modems. Tested with WISMOQ WQ2.16R.
 * 
 * @author MDengFeng
 */

public class ATHandler_Wavecom_WISMOQCDMA extends ATHandler_Wavecom
{
	public ATHandler_Wavecom_WISMOQCDMA(ModemGateway myGateway)
	{
		super(myGateway);
		this.terminators[1] = "\\s*([\\p{ASCII}]|[^\\x00-\\xff])*\\s+OK\\s";
		setStorageLocations("MT");
	}

	@Override
	/**
	 * This override for covering both GSM and CDMA modem message format
	 *
	 * For GSM modem, send time field can be found in AT+CMGL. But it can't be
	 * found for CDMA modem
	 *
	 * GSM modem +CMGL: 2,"REC READ","+8613520073322",,"08/07/02,15:10:49+32"
	 * GSM Hello,World
	 *
	 *
	 * For CDMA modem, The message format like below. +CMGL:6,"REC
	 * UNREAD","13911577644",0,2,23 Just for test send time
	 *
	 * when the message read by the index, the time is displayed, like this:
	 * AT+CMGR=6 +CMGR:"REC UNREAD","13911577644","08/07/09,11 :41 :49",0,2,3,23
	 * Just for test send time OK
	 *
	 * So overwrite this method to cover this case
	 */
	public String listMessages(MessageClasses messageClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String oneMessage;
		String line, header, msgText, msgContentStr;
		char encoding;
		char[] unicodeText;
		StringBuffer msgList = new StringBuffer();
		String listMsgRespons = super.listMessages(messageClass);
		ArrayList<Integer> memIndexs = this.getMsgIndexs(listMsgRespons);
		for (int memIndex : memIndexs)
		{
			oneMessage = "";
			header = "";
			msgText = "";
			oneMessage = getGateway().getMessageByIndex(memIndex);
			BufferedReader reader = new BufferedReader(new StringReader(oneMessage));
			// get first line which has message sender ,time fields
			// make the header can be compatible GSM header information
			header = reader.readLine().trim();
			String regx = "(^\\+CMGR:)(\\\"[^\\\"]+\\\",)(\\\"[^\\\"]+\\\",)(\\\"[^,]+,)(\\d+)\\s+(:\\d+)\\s+(:\\d+\\\")(,\\d,)(\\d)";
			Pattern pat = Pattern.compile(regx);
			Matcher matcher = pat.matcher(header);
			header = matcher.replaceAll("$1 " + memIndex + ",$2$3,$4$5$6$7$8$9");
			encoding = matcher.replaceAll("$9").charAt(0);
			// read message content
			StringBuffer msgContent = new StringBuffer();
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.length() <= 0 || line.equalsIgnoreCase("OK")) continue;
				msgContent.append(line);
			}
			msgContentStr = msgContent.toString();
			if (encoding == '4')
			{
				unicodeText = msgContentStr.toCharArray();
				msgContentStr = new String(unicodeText);
			}
			// recomposite the header and message content
			msgText = header + "\r" + msgContentStr + "\r";
			msgList.append(msgText);
		}
		// add OK at the end
		msgList.append("OK\r");
		//System.out.println("All messages:\n" + msgList.toString());
		return (msgList.toString());
	}

	private ArrayList<Integer> getMsgIndexs(String respons) throws IOException
	{
		ArrayList<Integer> msgIndexs = new ArrayList<Integer>();
		int memIndex, i, j;
		String listMsgRespons, line;
		BufferedReader reader;
		listMsgRespons = respons;
		reader = new BufferedReader(new StringReader(listMsgRespons));
		for (;;)
		{
			line = reader.readLine().trim();
			if (line == null) break;
			line = line.trim();
			if (line.length() > 0) break;
		}
		while (true)
		{
			if (line == null) break;
			line = line.trim();
			//System.out.println(line);
			if (line.length() <= 0 || line.equalsIgnoreCase("OK")) break;
			if (line.matches("^\\+CMGL:\\s*\\d+,.*"))
			{
				i = line.indexOf(':');
				j = line.indexOf(',');
				memIndex = Integer.parseInt(line.substring(i + 1, j).trim());
				msgIndexs.add(Integer.valueOf(memIndex));
			}
			line = reader.readLine().trim();
			while (line.length() == 0)
				line = reader.readLine().trim();
		}
		reader.close();
		return msgIndexs;
	}

	@Override
	public boolean setTextProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CMGF=1\r");
		getModemDriver().getResponse();
		if (getModemDriver().isOk())
		{
			getModemDriver().write("AT+CSCS=\"CDMA\"\r");
			getModemDriver().getResponse();
			if (getModemDriver().isOk())
			{
				getModemDriver().write("AT+WSCL=1,2\r");
				echoOff();
				return true;
			}
			return false;
		}
		return false;
	}
}
