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

/**
 * Class representing an inbound sms message of unknown type. These messages are
 * not handled by SMSLib, however you are given the chance to delete them.
 */
public class UnknownMessage extends InboundMessage
{
	private static final long serialVersionUID = 2L;

	private String pdu;

	public UnknownMessage(String myPdu, int memIndex, String memLocation)
	{
		super(MessageTypes.UNKNOWN, memIndex, memLocation);
		this.pdu = myPdu;
	}

	/**
	 * Returns the PDU data block.
	 * 
	 * @return The PDU data block.
	 */
	public String getPDU()
	{
		return this.pdu;
	}

	@Override
	public String toString()
	{
		String str = "";
		str += "===============================================================================";
		str += "\n";
		str += "<< UNKNOWN MESSAGE DUMP >>";
		str += "\n";
		str += "-------------------------------------------------------------------------------";
		str += "\n";
		str += " Gateway Id: " + getGatewayId();
		str += "\n";
		str += " Memory Index: " + getMemIndex();
		str += "\n";
		str += " Memory Location: " + getMemLocation();
		str += "\n";
		str += "===============================================================================";
		str += "\n";
		return str;
	}
}
