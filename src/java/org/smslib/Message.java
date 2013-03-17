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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

/**
 * The parent of all message-related classes. Most of common fields and
 * attributes of both inbound and outbound messages are placed in this class.
 */
public abstract class Message implements Serializable
{
	private static final long serialVersionUID = 2L;

	private static long messageIdSeed = 0;

	private final long messageId;

	/**
	 * Enumeration representing available message encodings.
	 */
	public enum MessageEncodings
	{
		/**
		 * 7 bit encoding - standard GSM alphabet.
		 */
		ENC7BIT,
		/**
		 * 8 bit encoding.
		 */
		ENC8BIT,
		/**
		 * UCS2 (Unicode) encoding.
		 */
		ENCUCS2,
		/**
		 * Custom encoding. Currently just defaults to 7-bit.
		 */
		ENCCUSTOM
	}

	/**
	 * Enumeration representing the different message classes.
	 */
	public enum MessageClasses
	{
		/**
		 * Default option to set no message class meaning.
		 */
		MSGCLASS_NONE,
		/**
		 * Class 0 - Immediate display (alert).
		 */
		MSGCLASS_FLASH,
		/**
		 * Class 1 - ME specific
		 */
		MSGCLASS_ME,
		/**
		 * Classs 2 - SIM specific
		 */
		MSGCLASS_SIM,
		/**
		 * Class 3 - TE specific
		 */
		MSGCLASS_TE
	}

	/**
	 * Enumeration representing the different types of messages.
	 */
	public enum MessageTypes
	{
		/**
		 * Inbound message.
		 */
		INBOUND,
		/**
		 * Outbound message.
		 */
		OUTBOUND,
		/**
		 * Status (delivery) report message
		 */
		STATUSREPORT,
		/**
		 * Outbound WAP SI message.
		 */
		WAPSI,
		/**
		 * Unknown message.
		 */
		UNKNOWN
	}

	private String gtwId;

	private MessageTypes type;

	private Date date;

	private String id;

	private String text;
	
	private String uuid;

	private MessageEncodings encoding;

	private MessageClasses messageClass;

	protected int dstPort;

	protected int srcPort;

	protected int messageCharCount;

	public Message(MessageTypes myType, Date myDate, String myText)
	{
		this.messageId = messageIdSeed++;
		this.uuid=UUID.randomUUID().toString();
		setGatewayId("");
		setType(myType);
		setId("");
		setDate(myDate);
		if (myText != null) setText(myText);
		setEncoding(MessageEncodings.ENC7BIT);
		setSrcPort(-1);
		setDstPort(-1);
		this.messageCharCount = 0;
	}

	/**
	 * Returns the creation date. For outbound messages, this is the object's
	 * creation date. For inbound messages, this is the date when the originator
	 * has sent the message.
	 * 
	 * @return the creation date.
	 * @see #setDate(Date)
	 */
	public Date getDate()
	{
		return this.date == null ? null : new java.util.Date(this.date.getTime());
	}

	/**
	 * Sets the creation date to a specific date.
	 * 
	 * @param myDate
	 *            A custom date.
	 * @see #getDate()
	 */
	public void setDate(Date myDate)
	{
		this.date = (myDate != null ? new java.util.Date(myDate.getTime()) : null);
	}

	/**
	 * Returns the message encoding.
	 * 
	 * @return The message encoding.
	 * @see #setEncoding(MessageEncodings)
	 * @see MessageEncodings
	 */
	public MessageEncodings getEncoding()
	{
		return this.encoding;
	}

	/**
	 * Returns the message Class.
	 * 
	 * @return The message class.
	 * @see #setDCSMessageClass
	 * @see MessageClasses
	 */
	public MessageClasses getDCSMessageClass()
	{
		return this.messageClass;
	}

	/**
	 * Sets the message Class to the specified one.
	 * 
	 * @param messageClass
	 *            The message Class.
	 * @see #getDCSMessageClass()
	 * @see MessageClasses
	 */
	public void setDCSMessageClass(MessageClasses messageClass)
	{
		this.messageClass = messageClass;
	}

	/**
	 * Returns the ID of the gateway which the message was received from (for
	 * inbound messages) or the message was dispatched from (outbound messages).
	 * 
	 * @return The Gateway ID.
	 * @see #setGatewayId(String)
	 */
	public String getGatewayId()
	{
		return this.gtwId;
	}

	/**
	 * Sets the message's Gateway ID to a specific value.
	 * 
	 * @param myGtwId
	 *            The Gateway ID.
	 * @see #getGatewayId()
	 */
	public void setGatewayId(String myGtwId)
	{
		this.gtwId = myGtwId;
	}

	/**
	 * Returns the message ID. This field can be used for your own purposes.
	 * 
	 * @return The message ID.
	 * @see #setId(String)
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * Sets the message ID to a specific value.
	 * 
	 * @param myId
	 *            The new message ID.
	 * @see #getId()
	 */
	public void setId(String myId)
	{
		this.id = myId;
	}

	/**
	 * Returns the auto-generated, internal message ID.
	 * 
	 * @return The message ID.
	 */
	public long getMessageId()
	{
		return this.messageId;
	}

	/**
	 * Returns the message text.
	 * 
	 * @return The message text.
	 * @see #setText(String)
	 */
	public String getText()
	{
		return this.text;
	}

	/**
	 * Sets the message text.
	 * 
	 * @param myText
	 *            The message text.
	 * @see #getText()
	 */
	public void setText(String myText)
	{
		this.text = myText;
	}

	public void addText(String addText) throws UnsupportedEncodingException
	{
		if (this.text == null) setText(addText);
		else this.text += addText;
	}

	/**
	 * Returns the message type.
	 * 
	 * @return The message type.
	 * @see MessageTypes
	 * @see #setType(MessageTypes)
	 */
	public MessageTypes getType()
	{
		return this.type;
	}

	void setType(MessageTypes myType)
	{
		this.type = myType;
	}

	/**
	 * Returns the destination port of the message. Source and Destination ports
	 * are used when messages are targeting a midlet application. For standard
	 * SMS messages, the Source and Destination ports should <b>both</b> be set
	 * to -1 (which is their default value anyway).
	 * 
	 * @return The destination port.
	 * @see #getDstPort()
	 * @see #setSrcPort(int)
	 * @see #getSrcPort()
	 */
	public int getDstPort()
	{
		return this.dstPort;
	}

	/**
	 * Sets the destination port of the message. Source and Destination ports
	 * are used when messages are targeting a midlet application. For standard
	 * SMS messages, the Source and Destination ports should <b>both</b> be set
	 * to -1 (which is their default value anyway).
	 * <p>
	 * The default is (-1).
	 * 
	 * @param myDstPort
	 *            The destination port.
	 * @see #setDstPort(int)
	 * @see #setSrcPort(int)
	 * @see #getSrcPort()
	 */
	public void setDstPort(int myDstPort)
	{
		this.dstPort = myDstPort;
	}

	/**
	 * Returns the source port of the message. Source and Destination ports are
	 * used when messages are targeting a midlet application. For standard SMS
	 * messages, the Source and Destination ports should <b>both</b> be set to
	 * -1 (which is their default value anyway).
	 * 
	 * @return The source port.
	 * @see #setSrcPort(int)
	 * @see #setDstPort(int)
	 * @see #getDstPort()
	 */
	public int getSrcPort()
	{
		return this.srcPort;
	}

	/**
	 * Sets the source port of the message. Source and Destination ports are
	 * used when messages are targeting a midlet application. For standard SMS
	 * messages, the Source and Destination ports should <b>both</b> be set to
	 * -1 (which is their default value anyway).
	 * <p>
	 * The default is (-1).
	 * 
	 * @param mySrcPort
	 *            The source port.
	 * @see #setDstPort(int)
	 * @see #setSrcPort(int)
	 * @see #getSrcPort()
	 */
	public void setSrcPort(int mySrcPort)
	{
		this.srcPort = mySrcPort;
	}

	/**
	 * Sets the message encoding to the specified one.
	 * 
	 * @param myEncoding
	 *            The message encoding.
	 * @see #getEncoding()
	 * @see MessageEncodings
	 */
	public void setEncoding(MessageEncodings myEncoding)
	{
		this.encoding = myEncoding;
	}

	public abstract String getPduUserData();

	public abstract String getPduUserDataHeader();

	protected void copyTo(Message msg)
	{
		msg.setDate(getDate());
		msg.setEncoding(getEncoding());
		msg.setDCSMessageClass(getDCSMessageClass());
		msg.setId(getId());
		msg.setGatewayId(getGatewayId());
		msg.setSrcPort(getSrcPort());
		msg.setDstPort(getDstPort());
		msg.setType(getType());
		msg.setText(getText());
		msg.messageCharCount = this.messageCharCount;
	}

	public String getUuid() {
		return uuid;
	}
}
