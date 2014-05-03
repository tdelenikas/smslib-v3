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

import java.util.Date;
import org.ajwcc.pduUtils.gsm3040.SmsStatusReportPdu;

/**
 * Class representing an delivery/status report message.
 */
public class StatusReportMessage extends InboundMessage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Enumeration representing delivery (status report) message status.
	 */
	public enum DeliveryStatuses
	{
		/**
		 * Unknown delivery status.
		 */
		UNKNOWN,
		/**
		 * Delivery in progress
		 */
		INPROGRESS,
		/**
		 * Message has been delivered.
		 */
		DELIVERED,
		/**
		 * Message has not been delivered yet - will keep trying.
		 */
		KEEPTRYING,
		/**
		 * Message has failed to be delivered.
		 */
		ABORTED
	}

	private String recipient;

	private Date sent;

	private Date received;

	private DeliveryStatuses status;

	private String refNo;

	public StatusReportMessage(SmsStatusReportPdu pdu, int memIndex, String memLocation)
	{
		super(MessageTypes.STATUSREPORT, memIndex, memLocation);
		this.refNo = String.valueOf(pdu.getMessageReference());
		this.recipient = pdu.getAddress();
		this.sent = pdu.getTimestamp();
		this.received = pdu.getDischargeTime();
		int i = pdu.getStatus();
		if ((i & 0x60) == 0)
		{
			setText("00 - Succesful Delivery.");
			this.status = DeliveryStatuses.DELIVERED;
		}
		if ((i & 0x20) == 0x20)
		{
			setText("01 - Errors, will retry dispatch.");
			this.status = DeliveryStatuses.KEEPTRYING;
		}
		if ((i & 0x40) == 0x40)
		{
			setText("02 - Errors, stopped retrying dispatch.");
			this.status = DeliveryStatuses.ABORTED;
		}
		if ((i & 0x60) == 0x60)
		{
			setText("03 - Errors, stopped retrying dispatch.");
			this.status = DeliveryStatuses.ABORTED;
		}
		setDate(null);
	}

	public StatusReportMessage(String myRefNo, int memIndex, String memLocation, Date dateOriginal, Date dateReceived)
	{
		super(MessageTypes.STATUSREPORT, memIndex, memLocation);
		this.refNo = myRefNo;
		this.sent = dateOriginal;
		this.received = dateReceived;
		setText("");
		this.status = DeliveryStatuses.UNKNOWN;
		setDate(null);
	}
	
	public StatusReportMessage(String myRefNo,String srcAddress,String destAddress, String text, Date dateOriginal, Date dateReceived)
	{
		super(MessageTypes.STATUSREPORT, -1, "");
		this.refNo = myRefNo;
		this.sent = dateOriginal;
		this.received = dateReceived;
		this.recipient = destAddress;
		setOriginator(srcAddress);
		setText(text);
		this.status = DeliveryStatuses.UNKNOWN;
		setDate(null);
	}

	/**
	 * Returns the recipient of the original outbound message that created this
	 * status report.
	 * 
	 * @return The recipient of the original outbound message.
	 */
	public String getRecipient()
	{
		return this.recipient;
	}

	/**
	 * Returns the date that the recipient received the original outbound
	 * message.
	 * 
	 * @return The receive date.
	 * @see #getSent()
	 */
	public Date getReceived()
	{
		return new java.util.Date(this.received.getTime());
	}

	public void setReceived(Date myReceived)
	{
		this.received = myReceived;
	}

	/**
	 * Returns the date when the original outbound message was sent.
	 * 
	 * @return The sent date.
	 * @see #getReceived()
	 */
	public Date getSent()
	{
		return new java.util.Date(this.sent.getTime());
	}

	public void setSent(Date mySent)
	{
		this.sent = mySent;
	}

	/**
	 * The status of the original outbound message. Use this field to see what
	 * happened with the original message.
	 * 
	 * @return The status of the outbound message;
	 * @see DeliveryStatuses
	 */
	public DeliveryStatuses getStatus()
	{
		return this.status;
	}

	public void setStatus(DeliveryStatuses myStatus)
	{
		this.status = myStatus;
	}

	/**
	 * Returns the Reference Number of the original outbound message that this
	 * status report refers to.
	 * 
	 * @return The Reference Number of the original outbound message.
	 */
	public String getRefNo()
	{
		return this.refNo;
	}
}
