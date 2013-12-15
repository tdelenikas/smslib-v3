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

import org.ajwcc.pduUtils.gsm3040.*;

/**
 * Class representing an outbound binary (forced 8bit encoded) sms message.
 */
public class OutboundBinaryMessage extends OutboundMessage
{
	private static final long serialVersionUID = 2L;

	private byte[] dataBytes;

	public OutboundBinaryMessage()
	{
		super();
		setEncoding(MessageEncodings.ENC8BIT);
	}

	/**
	 * Outbound message constructor.
	 * 
	 * @param myRecipient
	 *            The recipient of the message.
	 * @param bytes
	 *            The bytes (binary data) of the message.
	 */
	public OutboundBinaryMessage(String myRecipient, byte[] bytes)
	{
		super();
		this.recipient = myRecipient;
		setEncoding(MessageEncodings.ENC8BIT);
		setDataBytes(bytes);
	}

	@Override
	public String getText()
	{
		throw new RuntimeException("getText() not supported");
	}

	@Override
	public void setText(String s)
	{
		throw new RuntimeException("setText() not supported");
	}

	@Override
	public void addText(String s)
	{
		throw new RuntimeException("addText() not supported");
	}

	public byte[] getDataBytes()
	{
		return this.dataBytes;
	}

	public void setDataBytes(byte[] myDataBytes)
	{
		this.dataBytes = myDataBytes;
		setEncoding(MessageEncodings.ENC8BIT);
	}

	public void addDataBytes(byte[] myDataBytes)
	{
		if (this.dataBytes != null)
		{
			byte[] newArray = new byte[this.dataBytes.length + myDataBytes.length];
			System.arraycopy(this.dataBytes, 0, newArray, 0, this.dataBytes.length);
			System.arraycopy(myDataBytes, 0, newArray, this.dataBytes.length, this.dataBytes.length);
			setDataBytes(newArray);
		}
		else
		{
			setDataBytes(myDataBytes);
		}
	}

	@Override
	protected void setPduPayload(SmsSubmitPdu pdu)
	{
		pdu.setDataBytes(this.dataBytes);
	}
}
