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
import java.util.List;
import org.ajwcc.pduUtils.gsm3040.Pdu;
import org.ajwcc.pduUtils.gsm3040.PduFactory;
import org.ajwcc.pduUtils.gsm3040.PduGenerator;
import org.ajwcc.pduUtils.gsm3040.PduParser;
import org.ajwcc.pduUtils.gsm3040.PduUtils;
import org.ajwcc.pduUtils.gsm3040.SmsSubmitPdu;
import org.ajwcc.pduUtils.gsm3040.ie.InformationElementFactory;

/**
 * Class representing an outbound sms message.
 */
public class OutboundMessage extends Message
{
	private static final long serialVersionUID = 2L;

	/**
	 * Enumeration representing the failure reasons of a failed outbound
	 * message.
	 */
	public enum FailureCauses
	{
		/**
		 * No error, everything OK.
		 */
		NO_ERROR,
		/**
		 * Bad destination number - fatal error.
		 */
		BAD_NUMBER,
		/**
		 * Bad message format - fatal error.
		 */
		BAD_FORMAT,
		/**
		 * Generic gateway failure - transient error, retry later.
		 */
		GATEWAY_FAILURE,
		/**
		 * No credit left - fatal error.
		 */
		NO_CREDIT,
		/**
		 * Authentication problem (pin, passwords, etc) - fatal error.
		 */
		GATEWAY_AUTH,
		/**
		 * Unable to route message - transient error.
		 */
		NO_ROUTE,
		/**
		 * EzTexting specific: Local opt out (the recipient/number is on your opt-out list.)
		 */
		LOCAL_OPTOUT,
		/**
		 * EzTexting specific: Globally opted out phone number (the phone number has been opted out from all messages sent from our short code)
		 */
		GLOBAL_OPTOUT,
		/**
		 * Unknown generic problems encountered.
		 */
		UNKNOWN
	}

	/**
	 * Class representing the status of an outbound message.
	 */
	public enum MessageStatuses
	{
		/**
		 * A not-yet-sent outbound message.
		 */
		UNSENT,
		/**
		 * An already-sent outbound message.
		 */
		SENT,
		/**
		 * A sent-but-failed outbound message.
		 */
		FAILED
	}

	protected String recipient;

	private Date dispatchDate;

	private int validityPeriod;

	private boolean statusReport;

	private String from;

	private MessageStatuses messageStatus;

	private FailureCauses failureCause;

	private int retryCount;

	private int priority;

	private String refNo;

	private String errorMessage;
	
	//private long deliveryDelay;
	
	private Date scheduledDeliveryDate;

	/**
	 * Outbound message constructor. This parameterless constructor creates an
	 * empty outbound message.
	 * 
	 * @see #OutboundMessage(String, String)
	 */
	public OutboundMessage()
	{
		super(MessageTypes.OUTBOUND, null, null);
		setRecipient("");
		setValidityPeriod(-1);
		setStatusReport(false);
		setDCSMessageClass(MessageClasses.MSGCLASS_NONE);
		setFrom("");
		setDispatchDate(null);
		setDate(new Date());
		setEncoding(MessageEncodings.ENC7BIT);
		setMessageStatus(MessageStatuses.UNSENT);
		setFailureCause(FailureCauses.NO_ERROR);
		setPriority(0);
		setRefNo("");
		setGatewayId("*");
		setRetryCount(0);
	}

	/**
	 * Outbound message constructor.
	 * 
	 * @param myRecipient
	 *            The recipient of the message.
	 * @param text
	 *            The text of the message.
	 */
	public OutboundMessage(String myRecipient, String text)
	{
		super(MessageTypes.OUTBOUND, new Date(), text);
		setRecipient(myRecipient);
		setValidityPeriod(-1);
		setStatusReport(false);
		setDCSMessageClass(MessageClasses.MSGCLASS_NONE);
		setFrom("");
		setDispatchDate(null);
		setDate(new Date());
		setEncoding(MessageEncodings.ENC7BIT);
		setMessageStatus(MessageStatuses.UNSENT);
		setFailureCause(FailureCauses.NO_ERROR);
		setPriority(0);
		setRefNo("");
		setGatewayId("*");
		setRetryCount(0);
	}

	/**
	 * Returns the recipient of this outbound message.
	 * 
	 * @return The recipient of the message.
	 * @see #setRecipient(String)
	 */
	public String getRecipient()
	{
		return this.recipient;
	}

	/**
	 * Set the recipient of the message.
	 * 
	 * @param myRecipient
	 *            The recipient of the message.
	 * @see #getRecipient()
	 */
	public void setRecipient(String myRecipient)
	{
		this.recipient = myRecipient;
	}

	/**
	 * Returns the dispatch date of this message. If the message has not been
	 * sent yet, the dispatch date is null.
	 * 
	 * @return The message dispatch date.
	 */
	public Date getDispatchDate()
	{
		if (this.dispatchDate != null) return new java.util.Date(this.dispatchDate.getTime());
		return null;
	}

	public void setDispatchDate(Date myDispatchDate)
	{
		this.dispatchDate = myDispatchDate;
	}

	/**
	 * Returns true if this message is to be sent out as a flash SMS. Otherwise,
	 * it returns false.
	 * 
	 * @return True for a Flash message.
	 * @see #setFlashSms(boolean)
	 */
	public boolean getFlashSms()
	{
		if (getDCSMessageClass() == MessageClasses.MSGCLASS_FLASH) return true;
		return false;
	}

	/**
	 * Set the flash message indication. Set this to true for this message to be
	 * sent as a flash message. Flash messages appear directly on the handset,
	 * so use this feature with care, because it may be a bit annoying.
	 * Furthermore, keep in mind that flash messaging is not supported on all
	 * phones.
	 * <p>
	 * The default is non-flash (false).
	 * 
	 * @param flashSms
	 *            True for a flash sms.
	 */
	public void setFlashSms(boolean flashSms)
	{
		if (flashSms) setDCSMessageClass(MessageClasses.MSGCLASS_FLASH);
		else setDCSMessageClass(MessageClasses.MSGCLASS_NONE);
	}

	/**
	 * Returns true if a status/delivery report will be asked for this message.
	 * 
	 * @return True if a status report will be generated.
	 */
	public boolean getStatusReport()
	{
		return this.statusReport;
	}

	/**
	 * Sets the status report request. If you set it to true, a status report
	 * message will be generated, otherwise no status report message will be
	 * generated.
	 * <p>
	 * The default is (false).
	 * 
	 * @param myStatusReport
	 *            The status report request status.
	 */
	public void setStatusReport(boolean myStatusReport)
	{
		this.statusReport = myStatusReport;
	}

	/**
	 * Returns the message validity period (in hours).
	 * 
	 * @return The message validity period.
	 * @see #setValidityPeriod(int)
	 */
	public int getValidityPeriod()
	{
		return this.validityPeriod;
	}

	/**
	 * Sets the message validity period.
	 * 
	 * @param myValidityPeriod
	 *            The message validity period in hours.
	 * @see #getValidityPeriod()
	 */
	public void setValidityPeriod(int myValidityPeriod)
	{
		this.validityPeriod = myValidityPeriod;
	}

	/**
	 * Receives the custom originator string. Set it to empty string to leave
	 * the default behavior.
	 * 
	 * @return The custom originator string.
	 * @see #setFrom(String)
	 */
	public String getFrom()
	{
		return this.from;
	}

	/**
	 * Sets the custom originator string. Some gateways allow you to define a
	 * custom string as the originator. When the message arrives at the
	 * recipient, the latter will not see your number but this string.
	 * <p>
	 * Note that this functionality is not supported on GSM modems / phones. It
	 * is supported on most bulk sms operators.
	 * 
	 * @param myFrom
	 *            The custom originator string.
	 * @see #getFrom()
	 */
	public void setFrom(String myFrom)
	{
		this.from = myFrom;
	}

	/**
	 * Returns the message status.
	 * 
	 * @return The message status.
	 * @see MessageStatuses
	 */
	public MessageStatuses getMessageStatus()
	{
		return this.messageStatus;
	}

	public void setMessageStatus(MessageStatuses myMessageStatus)
	{
		this.messageStatus = myMessageStatus;
	}

	public FailureCauses getFailureCause()
	{
		return this.failureCause;
	}

	/**
	 * Mark message as failed and set cause of failure.
	 * 
	 * @param myFailureCause
	 *            Cause of failure
	 */
	public void setFailureCause(FailureCauses myFailureCause)
	{
		if (myFailureCause != FailureCauses.NO_ERROR) this.messageStatus = MessageStatuses.FAILED;
		this.failureCause = myFailureCause;
	}

	/**
	 * Return value of internal sending retry counter.
	 * 
	 * @return Number of sending message retries
	 */
	public int getRetryCount()
	{
		return this.retryCount;
	}

	public void setRetryCount(int myRetryCount)
	{
		this.retryCount = myRetryCount;
	}

	void incrementRetryCount()
	{
		this.retryCount++;
	}

	/**
	 * Returns the priority of the message.
	 * 
	 * @return The priority of the message.
	 */
	public int getPriority()
	{
		return this.priority;
	}

	/**
	 * Sets the priority of the message.
	 * 
	 * @param myPriority
	 *            The new priority.
	 */
	public void setPriority(int myPriority)
	{
		this.priority = myPriority;
	}

	/**
	 * Returns the message Reference Number. The Reference Number comes into
	 * existence when the message is sent. Its format depends on the gateway:
	 * For modems, its a number. For bulk sms operators, this is a hex string.
	 * If the message has not been sent yet, the Reference number is blank.
	 * 
	 * @return The message reference number.
	 */
	public String getRefNo()
	{
		return this.refNo;
	}

	public void setRefNo(String myRefNo)
	{
		this.refNo = myRefNo;
	}

	/**
	 * Returns the error message associated with the failure of this
	 * outbound message to be sent out.
	 * 
	 * @return The error message.
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString()
	{
		String str = "\n";
		str += "===============================================================================";
		str += "\n";
		str += "<< " + getClass().getSimpleName() + " >>";
		str += "\n";
		str += "-------------------------------------------------------------------------------";
		str += "\n";
		str += " Gateway Id: " + getGatewayId();
		str += "\n";
		str += " Message Id: " + getMessageId();
		str += "\n";
		str += " Message UUID: " + getUuid();
		str += "\n";
		str += " Encoding: " + (getEncoding() == MessageEncodings.ENC7BIT ? "7-bit" : (getEncoding() == MessageEncodings.ENC8BIT ? "8-bit" : "UCS2 (Unicode)"));
		str += "\n";
		str += " Date: " + getDate();
		str += "\n";
		str += " SMSC Ref No: " + getRefNo();
		str += "\n";
		str += " Recipient: " + getRecipient();
		str += "\n";
		str += " Dispatch Date: " + getDispatchDate();
		str += "\n";
		str += " Message Status: " + getMessageStatus();
		str += "\n";
		str += " Failure Cause: " + getFailureCause();
		str += "\n";
		str += " Validity Period (Hours): " + getValidityPeriod();
		str += "\n";
		str += " Status Report: " + getStatusReport();
		str += "\n";
		str += " Source / Destination Ports: " + getSrcPort() + " / " + getDstPort();
		str += "\n";
		str += " Flash SMS: " + getFlashSms();
		str += "\n";
		if (this instanceof OutboundBinaryMessage)
		{
			OutboundBinaryMessage binaryMessage = (OutboundBinaryMessage) this;
			if (binaryMessage.getDataBytes() != null)
			{
				String binaryString = PduUtils.bytesToPdu((binaryMessage).getDataBytes());
				str += " Binary: " + binaryString;
				str += "\n";
			}
			else
			{
				str += " Binary: null";
				str += "\n";
			}
		}
		else
		{
			str += " Text: " + getText();
			str += "\n";
			try
			{
				str += " PDU data: " + getPduUserData();
				str += "\n";
			}
			catch (Exception e)
			{
				str += " PDU data: <cannot extract properly, udh present>";
				str += "\n";
			}
		}
		str += " Scheduled Delivery: "+scheduledDeliveryDate;
		str += "\n";
		str += "===============================================================================";
		str += "\n";
		return str;
	}

	public List<String> getPdus(String smscNumber, int mpRefNo)
	{
		PduGenerator pduGenerator = new PduGenerator();
		SmsSubmitPdu pdu = createPduObject();
		initPduObject(pdu, smscNumber);
		return pduGenerator.generatePduList(pdu, mpRefNo);
	}

	protected SmsSubmitPdu createPduObject()
	{
		// if you want to be able to change some other parts of the first octet
		// do it here
		SmsSubmitPdu pdu;
		if (this.statusReport)
		{
			pdu = PduFactory.newSmsSubmitPdu(PduUtils.TP_SRR_REPORT | PduUtils.TP_VPF_INTEGER);
		}
		else
		{
			pdu = PduFactory.newSmsSubmitPdu();
		}
		return pdu;
	}

	protected void initPduObject(SmsSubmitPdu pdu, String smscNumber)
	{
		if ((getDstPort() > -1) && (getSrcPort() > -1))
		{
			// port info
			pdu.addInformationElement(InformationElementFactory.generatePortInfo(getDstPort(), getSrcPort()));
		}
		// smscInfo
		// address type field + #octets for smscNumber
		// NOTE: make sure the + is not present when computing the smscInfoLength
		String smscNumberForLengthCheck = smscNumber;
		if (smscNumber.startsWith("+"))
		{
			smscNumberForLengthCheck = smscNumber.substring(1);
		}
		pdu.setSmscInfoLength(1 + (smscNumberForLengthCheck.length() / 2) + ((smscNumberForLengthCheck.length() % 2 == 1) ? 1 : 0));
		// set address
		pdu.setSmscAddress(smscNumber);
		// set address type using the address (+ needs to be passed with it, if present)
		pdu.setSmscAddressType(PduUtils.getAddressTypeFor(smscNumber));
		// message reference
		// just use 0 since this is not tracked by the ModemGateway
		pdu.setMessageReference(0);
		// destination address info
		pdu.setAddress(getRecipient());
		pdu.setAddressType(PduUtils.getAddressTypeFor(getRecipient()));
		// protocol id
		pdu.setProtocolIdentifier(0);
		// data coding scheme
		if (!pdu.isBinary())
		{
			int dcs = 0;
			if (getEncoding() == MessageEncodings.ENC7BIT)
			{
				dcs = PduUtils.DCS_ENCODING_7BIT;
			}
			else if (getEncoding() == MessageEncodings.ENC8BIT)
			{
				dcs = PduUtils.DCS_ENCODING_8BIT;
			}
			else if (getEncoding() == MessageEncodings.ENCUCS2)
			{
				dcs = PduUtils.DCS_ENCODING_UCS2;
			}
			else if (getEncoding() == MessageEncodings.ENCCUSTOM)
			{
				// just use this
				dcs = PduUtils.DCS_ENCODING_7BIT;
			}
			if (getDCSMessageClass() == MessageClasses.MSGCLASS_FLASH)
			{
				dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_FLASH;
			}
			else if (getDCSMessageClass() == MessageClasses.MSGCLASS_ME)
			{
				dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_ME;
			}
			else if (getDCSMessageClass() == MessageClasses.MSGCLASS_SIM)
			{
				dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_SIM;
			}
			else if (getDCSMessageClass() == MessageClasses.MSGCLASS_TE)
			{
				dcs = dcs | PduUtils.DCS_MESSAGE_CLASS_TE;
			}
			pdu.setDataCodingScheme(dcs);
		}
		// validity period
		pdu.setValidityPeriod(this.validityPeriod);
		// add payload
		setPduPayload(pdu);
	}

	protected void setPduPayload(SmsSubmitPdu pdu)
	{
		pdu.setDecodedText(getText());
	}

	@Override
	public String getPduUserData()
	{
		// generate
		PduGenerator pduGenerator = new PduGenerator();
		SmsSubmitPdu pdu = createPduObject();
		initPduObject(pdu, "");
		// NOTE: - the mpRefNo is arbitrarily set to 1
		// - this won't matter since we aren't looking at the UDH in this method
		// - this method is not allowed for 7-bit messages with UDH
		// since it is probable that the returned value will not be
		// correct due to the encoding's dependence on the UDH
		// - if the user wishes to extract the UD per part, he would need to get all pduStrings
		// using getPdus(String smscNumber, int mpRefNo), use a
		// PduParser on each pduString in the returned list, then access the UD via the Pdu object
		List<String> pdus = pduGenerator.generatePduList(pdu, 1);
		// my this point, pdu will be updated with concat info (in udhi), if present
		if ((pdu.hasTpUdhi()) && (getEncoding() == MessageEncodings.ENC7BIT)) { throw new RuntimeException("getPduUserData() not supported for 7-bit messages with UDH"); }
		// sum up the ud parts
		StringBuffer ud = new StringBuffer();
		for (String pduString : pdus)
		{
			Pdu newPdu = new PduParser().parsePdu(pduString);
			ud.append(PduUtils.bytesToPdu(newPdu.getUserDataAsBytes()));
		}
		return ud.toString();
	}

	@Override
	public String getPduUserDataHeader()
	{
		// generate
		PduGenerator pduGenerator = new PduGenerator();
		SmsSubmitPdu pdu = createPduObject();
		initPduObject(pdu, "");
		// NOTE: - the mpRefNo is arbitrarily set to 1
		// - if the user wishes to extract the UDH per part, he would need to get all pduStrings
		// using getPdus(String smscNumber, int mpRefNo), use a
		// PduParser on each pduString in the returned list, then access the UDH via the Pdu object
		List<String> pdus = pduGenerator.generatePduList(pdu, 1);
		Pdu newPdu = new PduParser().parsePdu(pdus.get(0));
		byte[] udh = newPdu.getUDHData();
		if (udh != null) return PduUtils.bytesToPdu(udh);
		return null;
	}

	@Override
	public void setEncoding(MessageEncodings encoding)
	{
		if (encoding == MessageEncodings.ENC8BIT)
		{
			if (this instanceof OutboundBinaryMessage) super.setEncoding(encoding);
			else throw new RuntimeException("Cannot use 8-bit encoding with OutgoingMessage, use OutgoingBinaryMessage instead");
		}
		else
		{
			// 7-bit / ucs2
			super.setEncoding(encoding);
		}
	}

	protected void copyTo(OutboundMessage msg)
	{
		super.copyTo(msg);
		msg.setRecipient(getRecipient());
		msg.setDispatchDate(getDispatchDate());
		msg.setValidityPeriod(getValidityPeriod());
		msg.setStatusReport(getStatusReport());
		msg.setFlashSms(getFlashSms());
		msg.setFrom(getFrom());
		msg.setMessageStatus(getMessageStatus());
		msg.setFailureCause(getFailureCause());
		msg.retryCount = getRetryCount();
		msg.setPriority(getPriority());
		msg.setRefNo(getRefNo());
	}

	public void setScheduledDeliveryDate(Date scheduledDeliveryDate) {
		this.scheduledDeliveryDate=scheduledDeliveryDate;
	} 
	
	public Date getScheduledDeliveryDate() {
		return scheduledDeliveryDate;
	} 
	
	public long getDeliveryDelay() {
		return (scheduledDeliveryDate==null)? 0: scheduledDeliveryDate.getTime()-System.currentTimeMillis();
	}

	public void setDeliveryDelay(long deliveryDelay) {
		scheduledDeliveryDate=new Date(System.currentTimeMillis()+deliveryDelay);
		
	}
}
