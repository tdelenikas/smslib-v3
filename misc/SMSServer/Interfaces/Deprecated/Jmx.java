// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2008, Thanasis Delenikas, Athens/GREECE.
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

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.smsserver.gateways.AGateway;
import org.smslib.smsserver.SMSServer;

/**
 * This interface uses JMX for inbound/outbound communication. <br />
 * The other end of the jmx communication can add new outbound messages and ask
 * for inbound messages. Additional, there are some statistic methods for
 * service inspection.
 * 
 * @author Sebastian Just
 */
public class Jmx extends Interface<Void>
{
	/** Interface for JMX connection */
	public interface SMSServerMethodsMBean
	{
		/**
		 * Spools the given message and sends it on the next run.
		 * 
		 * @param msg
		 *            The message to send
		 */
		public void addOutboundMessage(OutboundMessage msg);

		/**
		 * Spools the given messages and send them on the next run.
		 * 
		 * @param msgs
		 *            The messages to send
		 */
		public void addOutboundMessage(Collection<OutboundMessage> msgs);

		/**
		 * Get all inbound messages since the last call of this method. <br />
		 * The inbound message spool is deleted after this call!
		 * 
		 * @return All new inbound messages
		 */
		public Collection<InboundMessage> getInboundMessages();

		/**
		 * Get all marked outbound messages since the last call of this method.
		 * <br />
		 * The message spool is deleted after this call!
		 * 
		 * @return List of outbound messaages
		 */
		public Collection<OutboundMessage> getMarkedOutboundMessages();

		/**
		 * Returns the inbound message count.
		 * 
		 * @see Service#getInboundMessageCount()
		 * @return The number of received messages.
		 */
		public int getInboundMessageCount();

		/**
		 * Returns the outbound message count.
		 * 
		 * @see Service#getOutboundMessageCount()
		 * @return The number of sent messages.
		 */
		public int getOutboundMessageCount();
	}

	/** Logic for JMX */
	public class SMSServerMethods implements SMSServerMethodsMBean
	{
		/* (non-Javadoc)
		 * @see org.smslib.smsserver.interfaces.Jmx.SMSServerMethodsMBean#addOutboundMessage(org.smslib.OutboundMessage)
		 */
		public void addOutboundMessage(OutboundMessage msg)
		{
			synchronized (Jmx.this.outboundMessages)
			{
				Jmx.this.outboundMessages.add(msg);
			}
		}

		/* (non-Javadoc)
		 * @see org.smslib.smsserver.interfaces.Jmx.SMSServerMethodsMBean#addOutboundMessage(java.util.List)
		 */
		public void addOutboundMessage(Collection<OutboundMessage> msgs)
		{
			synchronized (Jmx.this.outboundMessages)
			{
				Jmx.this.outboundMessages.addAll(msgs);
			}
		}

		/* (non-Javadoc)
		 * @see org.smslib.smsserver.interfaces.Jmx.SMSServerMethodsMBean#getInboundMessages()
		 */
		public Collection<InboundMessage> getInboundMessages()
		{
			synchronized (Jmx.this.inboundMessages)
			{
				Collection<InboundMessage> retValue = new Vector<InboundMessage>();
				retValue.addAll(Jmx.this.inboundMessages);
				Jmx.this.inboundMessages.clear();
				return retValue;
			}
		}

		/* (non-Javadoc)
		 * @see org.smslib.smsserver.interfaces.Jmx.SMSServerMethodsMBean#getMarkedOutboundMessages()
		 */
		public Collection<OutboundMessage> getMarkedOutboundMessages()
		{
			synchronized (Jmx.this.markedOutboundMessages)
			{
				Collection<OutboundMessage> retValue = new Vector<OutboundMessage>();
				retValue.addAll(Jmx.this.markedOutboundMessages);
				Jmx.this.markedOutboundMessages.clear();
				return retValue;
			}
		}

		/* (non-Javadoc)
		 * @see org.smslib.smsserver.interfaces.Jmx.SMSServerMethodsMBean#getGatewayQueueLoad()
		 */
		public int getQueueLoad()
		{
			return getService().getQueueLoad();
		}

		/* (non-Javadoc)
		 * @see org.smslib.smsserver.interfaces.Jmx.SMSServerMethodsMBean#getInboundMessageCount()
		 */
		public int getInboundMessageCount()
		{
			return getService().getInboundMessageCount();
		}

		/* (non-Javadoc)
		 * @see org.smslib.smsserver.interfaces.Jmx.SMSServerMethodsMBean#getOutboundMessageCount()
		 */
		public int getOutboundMessageCount()
		{
			return getService().getOutboundMessageCount();
		}
	}

	/** Local JMX server */
	private MBeanServer mbs;

	/** JMX endpoint */
	private SMSServerMethods ssm;

	/** Glue between JMX and the managed bean */
	private ObjectName obn;

	/** Outbound message spool */
	List<OutboundMessage> outboundMessages;

	/** Inbound message spool */
	List<InboundMessage> inboundMessages;

	/** Spool for marked outbound messages */
	List<OutboundMessage> markedOutboundMessages;

	/**
	 * Creates this interface and initalize the message spools.
	 * 
	 * @see AGateway#AGateway(String, Properties, SMSServer)
	 */
	public Jmx(String myInterfaceId, Properties myProps, SMSServer myServer, InterfaceTypes myType)
	{
		super(myInterfaceId, myProps, myServer, myType);
		setDescription("JMX based interface.");
		this.ssm = new SMSServerMethods();
		this.outboundMessages = new Vector<OutboundMessage>();
		this.inboundMessages = new Vector<InboundMessage>();
		this.markedOutboundMessages = new Vector<OutboundMessage>();
	}

	/* (non-Javadoc)
	 * @see org.smslib.smsserver.AInterface#MessagesReceived(java.util.Collection)
	 */
	@Override
	public void MessagesReceived(Collection<InboundMessage> msgList) throws Exception
	{
		synchronized (this.inboundMessages)
		{
			this.inboundMessages.addAll(msgList);
		}
	}

	/* (non-Javadoc)
	 * @see org.smslib.smsserver.AInterface#getMessagesToSend()
	 */
	@Override
	public Collection<OutboundMessage> getMessagesToSend() throws Exception
	{
		synchronized (this.outboundMessages)
		{
			Collection<OutboundMessage> retValue = new Vector<OutboundMessage>();
			retValue.addAll(this.outboundMessages);
			this.outboundMessages.clear();
			return retValue;
		}
	}

	/* (non-Javadoc)
	 * @see org.smslib.smsserver.AInterface#markMessage(org.smslib.OutboundMessage)
	 */
	@Override
	public void markMessage(OutboundMessage msg) throws Exception
	{
		synchronized (this.markedOutboundMessages)
		{
			this.markedOutboundMessages.add(msg);
		}
	}

	/* (non-Javadoc)
	 * @see org.smslib.smsserver.AInterface#start()
	 */
	@Override
	public void start() throws Exception
	{
		LocateRegistry.createRegistry(Integer.parseInt(getProperty("registry_port")));
		// Get the platform MBeanServer
		this.mbs = ManagementFactory.getPlatformMBeanServer();
		JMXServiceURL url = new JMXServiceURL(getProperty("url"));
		JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, this.mbs);
		connectorServer.start();
		this.obn = new ObjectName(getProperty("object_name"));
		// Uniquely identify the MBeans and register them with the platform
		this.mbs.registerMBean(this.ssm, this.obn);
		getService().getLogger().logInfo("Bound JMX to " + url, null, null);
		super.start();
	}

	/* (non-Javadoc)
	 * @see org.smslib.smsserver.AInterface#stop()
	 */
	@Override
	public void stop() throws Exception
	{
		if (this.mbs != null)
		{
			this.mbs.unregisterMBean(this.obn);
		}
		super.stop();
	}
}
