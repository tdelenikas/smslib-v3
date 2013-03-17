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

package org.smslib.smsserver.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.helper.Logger;
import org.smslib.smsserver.SMSServer;

/**
 * The base class of all implemented SMSServer interfaces.
 * <p>
 * An SMSServer interface can be thought of as a message producer or a message
 * consumer.
 * <p>
 * SMSServer comes with a couple of ready-made interfaces. If you wish to extend
 * SMSServer with new interface functionality, create your own interface by
 * implementing the current class.
 */
public class Interface<T>
{
	/**
	 * Class representing SMSServer interface types.
	 */
	public enum InterfaceTypes
	{
		/**
		 * Representing an inbound-only interface.
		 */
		INBOUND,
		/**
		 * Representing an outbound-only interface.
		 */
		OUTBOUND,
		/**
		 * Representing a dual (inbound + outbound) interface.
		 */
		INOUTBOUND
	}

	private String infId;

	private Properties props;

	private SMSServer server;

	private InterfaceTypes type;

	private String description;

	/**
	 * Store to save messageId with interface-specific message identifications
	 * like primary keys or filenames
	 */
	private Map<Long, T> messageIdCache;

	public Interface(String myInfId, Properties myProps, SMSServer myServer, InterfaceTypes myType)
	{
		this.infId = myInfId;
		this.props = myProps;
		this.server = myServer;
		this.type = myType;
		this.messageIdCache = new HashMap<Long, T>();
	}

	public final SMSServer getServer()
	{
		return this.server;
	}

	public String getId()
	{
		return this.infId;
	}

	/**
	 * This method is called by SMSServer every time an inbound call is
	 * received. SMSServer calls this method for all available/active
	 * interfaces.
	 * <p>
	 * <b>Override this call if you wish to implement your own
	 * functionality</b>.
	 * 
	 * @param gtwId
	 *            The Id of the gateway which received the call.
	 * @param callerId
	 *            The caller id.
	 */
	public void callReceived(String gtwId, String callerId) throws Exception
	{
		// Should be overridden if necessary.
	}

	/**
	 * Returns the interface description.
	 * 
	 * @return The interface description.
	 */
	public final String getDescription()
	{
		return this.description;
	}

	/**
	 * Sets the interface description.
	 * 
	 * @param myDescription
	 *            The interface description.
	 */
	public final void setDescription(String myDescription)
	{
		this.description = myDescription;
	}

	public final Map<Long, T> getMessageCache()
	{
		return this.messageIdCache;
	}

	/**
	 * SMSServer calls this method in order to query the interface for messages
	 * that need to be send out.
	 * <p>
	 * <b>Override this call if you wish to implement your own
	 * functionality</b>.
	 * 
	 * @return A list of Outbound messages to be sent. Return an empty list if
	 *         the interface has no messages for dispatch.
	 * @throws Exception
	 */
	public Collection<OutboundMessage> getMessagesToSend() throws Exception
	{
		// Should be overridden if necessary.
		return new ArrayList<OutboundMessage>();
	}

	/**
	 * This method returns the number of outbound queued messages identified by
	 * this interface. Should return (-1) if this method is not implemented or
	 * if the number cannot be determined.
	 * 
	 * @return The number of pending messages to be sent via this interface.
	 * @throws Exception
	 */
	public int getPendingMessagesToSend() throws Exception
	{
		return -1;
	}

	/**
	 * Reads the property key of this interface.
	 * 
	 * @param key
	 *            The key of the property to read.
	 * @return The value of the property or null if not set
	 */
	public final String getProperty(String key)
	{
		return getProperty(key, null);
	}

	/**
	 * Reads the property key of this interface. <br /> The defaultValue is
	 * returned if the key is not defined in the properties.
	 * 
	 * @param key
	 *            The key of the property to read.
	 * @param defaultValue
	 *            The defaultValue if key is not defined.
	 * @return The value of the property or defaultValue if not set.
	 */
	public final String getProperty(String key, String defaultValue)
	{
		String value = this.props.getProperty(this.infId + "." + key, defaultValue);
		if ((value == null) || (value.length() == 0)) value = defaultValue;
		return value;
	}

	/**
	 * Returns the interface type.
	 * 
	 * @return The interface type.
	 * @see InterfaceTypes
	 */
	public final InterfaceTypes getType()
	{
		return this.type;
	}

	/**
	 * Returns true if the interface is for inbound messaging.
	 * 
	 * @return True if the interface is for inbound messaging.
	 */
	public final boolean isInbound()
	{
		if (InterfaceTypes.INBOUND == this.type || InterfaceTypes.INOUTBOUND == this.type) return true;
		return false;
	}

	/**
	 * Returns true if the interface is for outbound messaging.
	 * 
	 * @return True if the interface is for outbound messaging.
	 */
	public final boolean isOutbound()
	{
		if (InterfaceTypes.OUTBOUND == this.type || InterfaceTypes.INOUTBOUND == this.type) return true;
		return false;
	}

	/**
	 * After a successful or unsuccessful attempt to send a message, SMSServer
	 * calls this method. The interface can then decide what to do with the
	 * message. Note that the message status and errors member fields are
	 * updated, so you should examine them in order to determine whether the
	 * message has been sent out, etc.
	 * <p>
	 * <b>Override this call if you wish to implement your own
	 * functionality</b>.
	 * 
	 * @param msg
	 *            The Outbound message.
	 * @throws Exception
	 */
	public void markMessage(OutboundMessage msg) throws Exception
	{
		// Should be overridden if necessary.
	}

	public void markMessages(Collection<OutboundMessage> msgList) throws Exception
	{
		for (OutboundMessage msg : msgList)
			markMessage(msg);
	}

	/**
	 * This method is called by SMSServer every time a message (or more
	 * messages) is received. SMSServer calls this method for all
	 * available/active interfaces.
	 * <p>
	 * <b>Override this call if you wish to implement your own
	 * functionality</b>.
	 * 
	 * @param msgList
	 *            A message list of all received messages.
	 * @throws Exception
	 */
	public void messagesReceived(Collection<InboundMessage> msgList) throws Exception
	{
		// Should be overridden if necessary.
	}

	/**
	 * Called once before SMSServer starts its operation. Use this method for
	 * initialization.
	 * <p>
	 * <b>Override this call if you wish to implement your own
	 * functionality</b>.
	 * 
	 * @throws Exception
	 *             An exception thrown will stop SMSServer from starting its
	 *             processing.
	 */
	public void start() throws Exception
	{
		Logger.getInstance().logInfo("SMSServer: interface: " + this.getClass().getName() + " started.", null, null);
	}

	/**
	 * Called once after SMSServer has finished. Use this method for cleaning up
	 * your interface.
	 * <p>
	 * <b>Override this call if you wish to implement your own
	 * functionality</b>.
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception
	{
		Logger.getInstance().logInfo("SMSServer: interface: " + this.getClass().getName() + " stopped.", null, null);
	}
}
