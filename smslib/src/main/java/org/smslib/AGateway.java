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

import java.io.IOException;
import java.util.Collection;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.OutboundMessage.FailureCauses;
import org.smslib.OutboundMessage.MessageStatuses;
import org.smslib.StatusReportMessage.DeliveryStatuses;
import org.smslib.helper.Logger;
import org.smslib.notify.GatewayStatusNotification;
import org.smslib.notify.OutboundMessageNotification;
import org.smslib.threading.AServiceThread;

/**
 * Abstract class representing a Gateway, i.e. an interface capable of sending
 * and/or receiving SMS messages.
 */
public abstract class AGateway
{
	/**
	 * Enumeration representing the operation protocols of a GSM modem.
	 */
	public enum Protocols
	{
		/**
		 * PDU protocol.
		 */
		PDU,
		/**
		 * TEXT protocol. <b>Warning</b>: the TEXT protocol is not yet fully
		 * supported.
		 */
		TEXT
	}

	public enum GatewayStatuses
	{
		STOPPED, STOPPING, STARTING, STARTED, FAILURE, RESTART
	}

	public enum AsyncEvents
	{
		DELETE, NOTHING, INBOUNDMESSAGE, INBOUNDSTATUSREPORTMESSAGE, INBOUNDCALL, USSDRESPONSE
	}

	public static class GatewayAttributes
	{
		public static final int SEND = 0x0001;

		public static final int RECEIVE = 0x0002;

		public static final int CUSTOMFROM = 0x0004;

		public static final int BIGMESSAGES = 0x0008;

		public static final int WAPSI = 0x0010;

		public static final int PORTADDRESSING = 0x0020;

		public static final int FLASHSMS = 0x0040;

		public static final int DELIVERYREPORTS = 0x0080;
	}

	private String gatewayId;

	private int attributes;

	private boolean inbound;

	private boolean outbound;

	private Protocols protocol;

	private Statistics statistics;

	private String from;

	private int deliveryErrorCode;

	protected GatewayStatuses status;

	protected int restartCount;

	private QueueManager queueManager;

	public AGateway(String id)
	{
		this.gatewayId = id;
		this.inbound = false;
		this.outbound = false;
		this.attributes = 0;
		this.protocol = Protocols.PDU;
		this.from = "";
		this.statistics = new Statistics();
		this.from = "";
		this.deliveryErrorCode = -1;
		this.status = GatewayStatuses.STOPPED;
		this.restartCount = 0;
	}

	public void setAttributes(int myAttributes)
	{
		this.attributes = myAttributes;
	}

	public int getAttributes()
	{
		return this.attributes;
	}

	public AGateway getMyself()
	{
		return this;
	}

	/**
	 * Returns true if the the gateway is set for inbound messaging.
	 * 
	 * @return True if this gateway is set for inbound messaging.
	 */
	public boolean isInbound()
	{
		return this.inbound;
	}

	/**
	 * Enables or disables the gateway for inbound messaging. The command is
	 * accepted only if the gateway supports inbound messaging.
	 * 
	 * @param value
	 *            True to enable the gateway for inbound messaging.
	 */
	public void setInbound(boolean value)
	{
		if ((this.attributes & GatewayAttributes.RECEIVE) != 0) this.inbound = value;
	}

	/**
	 * Returns true if the the gateway is set for outbound messaging.
	 * 
	 * @return True if this gateway is set for outbound messaging.
	 */
	public boolean isOutbound()
	{
		return this.outbound;
	}

	/**
	 * Enables or disables the gateway for outbound messaging. The command is
	 * accepted only if the gateway supports outbound messaging.
	 * 
	 * @param value
	 *            True to enable the gateway for outbound messaging.
	 */
	public void setOutbound(boolean value)
	{
		if ((this.attributes & GatewayAttributes.SEND) != 0) this.outbound = value;
	}

	/**
	 * Sets the communication protocol of the gateway. The call is applicable
	 * only for modem gateways, in other cases it is ignored.
	 * 
	 * @param myProtocoll
	 *            The protocol to be used.
	 * @see Protocols
	 * @see #getProtocol
	 */
	public void setProtocol(Protocols myProtocoll)
	{
		this.protocol = myProtocoll;
	}

	/**
	 * Returns the communication protocol current in use by the gateway.
	 * 
	 * @return The communication protocol.
	 * @see Protocols
	 * @see #setProtocol(Protocols)
	 */
	public Protocols getProtocol()
	{
		return this.protocol;
	}

	/**
	 * Returns the gateway id assigned to this gateway during initialization.
	 * 
	 * @return The gateway id.
	 */
	public String getGatewayId()
	{
		return this.gatewayId;
	}

	/**
	 * Returns the gateway status.
	 * 
	 * @return The gateway status
	 * @see GatewayStatuses
	 */
	public GatewayStatuses getStatus()
	{
		return this.status;
	}

	/**
	 * Sets the gateway status to a new value.
	 * 
	 * @param myStatus
	 *            The new gateway status.
	 * @see GatewayStatuses
	 */
	public void setStatus(GatewayStatuses myStatus)
	{
		Service.getInstance().getNotifyQueueManager().getNotifyQueue().add(new GatewayStatusNotification(getMyself(), getStatus(), myStatus));
		this.status = myStatus;
	}

	/**
	 * Returns the total number of messages received by this gateway.
	 * 
	 * @return The number of received messages.
	 */
	public int getInboundMessageCount()
	{
		return this.statistics.inbound;
	}

	public void incInboundMessageCount()
	{
		this.statistics.inbound++;
	}

	/**
	 * Returns the total number of messages sent via this gateway.
	 * 
	 * @return The number of sent messages.
	 */
	public int getOutboundMessageCount()
	{
		return this.statistics.outbound;
	}

	public void incOutboundMessageCount()
	{
		this.statistics.outbound++;
	}

	/**
	 * Returns the string that will appear on recipient's phone as the
	 * originator. Not all gateways support this.
	 * 
	 * @return The originator string.
	 * @see #setFrom(String)
	 */
	public String getFrom()
	{
		return this.from;
	}

	/**
	 * Sets the string that will appear on recipient's phone as the originator.
	 * Not all gateways support this.
	 * 
	 * @param myFrom
	 *            The originator string.
	 * @see #getFrom()
	 */
	public void setFrom(String myFrom)
	{
		this.from = myFrom;
	}

	public void startGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		setStatus(GatewayStatuses.STARTING);
		this.queueManager = new QueueManager("QueueManager [" + this.gatewayId + "]", getQueueSchedulingInterval());
		this.restartCount++;
		setStatus(GatewayStatuses.STARTED);
	}

	public void stopGateway() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		setStatus(GatewayStatuses.STOPPING);
		if (this.queueManager != null)
		{
			this.queueManager.cancel();
			this.queueManager = null;
		}
		setStatus(GatewayStatuses.STOPPED);
	}

	public void readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	public InboundMessage readMessage(String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	public int sendMessages(Collection<OutboundMessage> msgList) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int cnt = 0;
		for (OutboundMessage msg : msgList)
			if (sendMessage(msg)) cnt++;
		return cnt;
	}

	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	/**
	 * Queries the gateway for remaining credit.
	 * 
	 * @return Remaining credit.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public float queryBalance() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	/**
	 * Queries the gateway to see if a specific message and its recipient are
	 * covered. The given message is not sent out - it is just tested.
	 * 
	 * @param msg
	 *            The message to test.
	 * @return True is the recipient is covered by the network.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public boolean queryCoverage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	/**
	 * Query the gateway for message delivery status.
	 * 
	 * @param msg
	 *            The OutboundMessage object to be checked.
	 * @return The delivery status. This is interpreted and mapped to the
	 *         standard SMSLib status codes. For detailed information, check
	 *         method getDeliveryErrorCode().
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see DeliveryStatuses
	 * @see #getDeliveryErrorCode()
	 */
	public DeliveryStatuses queryMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		return queryMessage(msg.getRefNo());
	}

	/**
	 * Query the gateway for message delivery status.
	 * 
	 * @param refNo
	 *            The reference number of a previously sent message to be
	 *            checked.
	 * @return The delivery status. This is interpreted and mapped to the
	 *         standard SMSLib status codes. For detailed information, check
	 *         method getDeliveryErrorCode().
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see DeliveryStatuses
	 * @see #getDeliveryErrorCode()
	 */
	public DeliveryStatuses queryMessage(String refNo) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	public int readPhonebook(Phonebook phonebook) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported.");
	}

	/**
	 * Returns the gateway-specific error code from the last queryMessage()
	 * call. Note that each call to queryMessage() resets this error.
	 * 
	 * @return The error code - actual values depend on gateway used.
	 * @see #queryMessage(OutboundMessage)
	 */
	public int getDeliveryErrorCode()
	{
		return this.deliveryErrorCode;
	}

	public void setDeliveryErrorCode(int error)
	{
		this.deliveryErrorCode = error;
	}

	boolean isCapableOf(int att)
	{
		return ((att & this.attributes) == att);
	}

	boolean conformsTo(int attrib, boolean required)
	{
		if (required && !isCapableOf(attrib)) return false;
		return true;
	}

	static class Statistics
	{
		public int inbound;

		public int outbound;

		public Statistics()
		{
			this.inbound = 0;
			this.outbound = 0;
		}
	}

	public int getRestartCount()
	{
		return this.restartCount;
	}

	private class QueueManager extends AServiceThread
	{
		public QueueManager(String name, int delay)
		{
			super(name, delay, 0, true);
		}

		@Override
		public void process() throws Exception
		{
			OutboundMessage msg = null;
			try
			{
				if (getStatus() == GatewayStatuses.STARTED)
				{
					msg = Service.getInstance().getQueueManager().pollPendingMessage(getGatewayId());
					if (msg != null)
					{
						if (Service.getInstance().getQueueSendingNotification() != null) Service.getInstance().getQueueSendingNotification().process(getMyself(), msg);
						try
						{
							if (!sendMessage(msg))
							{
								if (msg.getRetryCount() < Service.getInstance().getSettings().QUEUE_RETRIES)
								{
									Logger.getInstance().logInfo("Reinserting message to queue.", null, getGatewayId());
									msg.incrementRetryCount();
									Service.getInstance().getQueueManager().queueMessage(msg);
								}
								else
								{
									Logger.getInstance().logWarn("Maximum number of queue retries exceeded, message lost.", null, getGatewayId());
									msg.setFailureCause(FailureCauses.UNKNOWN);
									Service.getInstance().getNotifyQueueManager().getNotifyQueue().add(new OutboundMessageNotification(getMyself(), msg));
								}
							}
							else
							{
								Service.getInstance().getNotifyQueueManager().getNotifyQueue().add(new OutboundMessageNotification(getMyself(), msg));
							}
						}
						catch (TimeoutException e)
						{
							Service.getInstance().getQueueManager().queueMessage(msg);
							throw e;
						}
					}
				}
			}
			catch (InterruptedException e)
			{
				if ((msg != null) && (msg.getMessageStatus() != MessageStatuses.SENT)) Service.getInstance().getQueueManager().queueMessage(msg);
				Logger.getInstance().logInfo("QueueManager interrupted.", e, getGatewayId());
			}
			catch (Exception e)
			{
				Logger.getInstance().logWarn("Queue exception, marking gateway for reset.", e, getGatewayId());
				setStatus(GatewayStatuses.RESTART);
				Service.getInstance().getNotifyQueueManager().getNotifyQueue().add(new OutboundMessageNotification(getMyself(), msg));
			}
		}
	}

	/**
	 * Returns the Gateway Queue sending internal (in milliseconds). Should be
	 * defined in every actual Gateway implementation.
	 * 
	 * @return The scheduling interval (in milliseconds).
	 */
	public abstract int getQueueSchedulingInterval();

	public String sendUSSDCommand(String ussdCommand) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported");
	}

	public String sendUSSDCommand(String ussdCommand, boolean interactive) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported");
	}

	public boolean sendUSSDRequest(USSDRequest request) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		throw new GatewayException("Feature not supported");
	}
}
