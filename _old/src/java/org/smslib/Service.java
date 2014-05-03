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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.OutboundMessage.FailureCauses;
import org.smslib.OutboundMessage.MessageStatuses;
import org.smslib.balancing.LoadBalancer;
import org.smslib.balancing.RoundRobinLoadBalancer;
import org.smslib.crypto.KeyManager;
import org.smslib.helper.Logger;
import org.smslib.notify.NotifyQueueManager;
import org.smslib.queues.AbstractQueueManager;
import org.smslib.queues.DefaultQueueManager;
import org.smslib.routing.Router;
import org.smslib.threading.AServiceThread;

/**
 * This is main library class. Your primary interface with SMSLib is via methods
 * defined in this class.
 */
public class Service
{
	public enum ServiceStatus
	{
		STARTING, STARTED, STOPPING, STOPPED
	}

	private List<AGateway> gatewayList;

	private Router router;

	private LoadBalancer loadBalancer;

	private WatchDog watchDog;

	private IInboundMessageNotification inboundMessageNotification;

	private IOutboundMessageNotification outboundMessageNotification;

	private ICallNotification callNotification;

	private IGatewayStatusNotification gatewayStatusNotification;

	private IQueueSendingNotification queueSendingNotification;

	private IOrphanedMessageNotification orphanedMessageNotification;

	protected AbstractQueueManager queueManager;

	protected NotifyQueueManager notifyQueueManager;

	private long startMillis;

	private Collection<Group> groups;

	private volatile ServiceStatus serviceStatus;

	private KeyManager keyManager;

	public Settings S;

	private IUSSDNotification ussdNotification;

	private static final Service service = new Service();

	private Service()
	{
		setSettings(new Settings());
		initializeService();
	}

	protected void initializeService()
	{
		this.startMillis = System.currentTimeMillis();
		setServiceStatus(ServiceStatus.STOPPED);
		this.groups = new ArrayList<Group>();
		listSystemInformation();
		this.gatewayList = new ArrayList<AGateway>();
		this.keyManager = KeyManager.getInstance();
		setRouter(new Router());
		setLoadBalancer(new RoundRobinLoadBalancer());
		setNotifyQueueManager(new NotifyQueueManager());
	}

	private void listSystemInformation()
	{
		Logger.getInstance().logInfo(Library.getLibraryDescription(), null, null);
		Logger.getInstance().logInfo("Version: " + Library.getLibraryVersion(), null, null);
		Logger.getInstance().logInfo("JRE Version: " + System.getProperty("java.version"), null, null);
		Logger.getInstance().logInfo("JRE Impl Version: " + System.getProperty("java.vm.version"), null, null);
		Logger.getInstance().logInfo("O/S: " + System.getProperty("os.name") + " / " + System.getProperty("os.arch") + " / " + System.getProperty("os.version"), null, null);
	}

	public static Service getInstance()
	{
		return Service.service;
	}

	/**
	 * Adds a gateway to the list of gateways managed by the Service class. The
	 * Service should be stopped in order to add gateways.
	 * 
	 * @param gateway
	 *            The gateway to be added.
	 * @throws GatewayException
	 *             You tried to add a gateway while the Service is started.
	 * @see #removeGateway(AGateway)
	 * @see #getGateways()
	 */
	public void addGateway(AGateway gateway) throws GatewayException
	{
		if (getServiceStatus() != ServiceStatus.STOPPED) throw new GatewayException("Cannot add gateways while Service is running!");
		getGateways().add(gateway);
	}

	/**
	 * Removes a gateway from the list of gateways managed by the Service class.
	 * The Service should be stopped in order to remove a gateway.
	 * 
	 * @param gateway
	 *            The gateway to be removed.
	 * @return True if the requested gateway has been removed successfully.
	 * @throws GatewayException
	 *             You tried to remove a gateway while the Service is started.
	 * @see #addGateway(AGateway)
	 * @see #getGateways()
	 */
	public boolean removeGateway(AGateway gateway) throws GatewayException
	{
		if (getServiceStatus() != ServiceStatus.STOPPED) throw new GatewayException("Cannot remove gateways while Service is running!");
		return getGateways().remove(gateway);
	}

	/**
	 * Returns the gateway object based on the provided id. Returns null if no
	 * gateway matches the provided id.
	 * 
	 * @param gatewayId
	 *            The gateway id to search for.
	 * @return The gateway which matches the provided gateway id, or null.
	 */
	public AGateway getGateway(String gatewayId)
	{
		for (AGateway gateway : getGateways())
		{
			if (gateway.getGatewayId().equalsIgnoreCase(gatewayId)) return gateway;
		}
		return null;
	}

	/**
	 * Initializes and starts the SMSLib service. This should be the first call
	 * before you use the Service class for sending/receiving messages. The call
	 * will try to start all defined gateways.
	 * 
	 * @throws SMSLibException
	 *             No Gateways are defined.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #stopService()
	 */
	public synchronized void startService() throws SMSLibException, TimeoutException, GatewayException, IOException, InterruptedException
	{
		startService(true);
	}

	/**
	 * Initializes and starts the SMSLib service. Depending on the parameter,
	 * all defined gateways can be requested to start or some may be allowed to
	 * fail. This should be the first call before you use the Service class for
	 * sending/receiving messages.
	 * 
	 * @param startAllGateways
	 *            True if all gateways should be started, False if some gateways
	 *            are allowed to fail.
	 * @throws SMSLibException
	 *             No Gateways are defined.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #stopService()
	 */
	public synchronized void startService(boolean startAllGateways) throws SMSLibException, TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (queueManager == null) queueManager = new DefaultQueueManager();
		try
		{
			getNotifyQueueManager().start();
			getQueueManager().start();
			setServiceStatus(ServiceStatus.STARTING);
			setWatchDog(new WatchDog("WatchDog", (getSettings().WATCHDOG_INTERVAL * 1000)));
			startService_Internal(startAllGateways);
			setServiceStatus(ServiceStatus.STARTED);
		}
		catch (SMSLibException e)
		{
			stopService();
			throw e;
		}
		catch (IOException e)
		{
			stopService();
			throw e;
		}
		catch (InterruptedException e)
		{
			stopService();
			throw e;
		}
	}

	private void startService_Internal(boolean startAll) throws SMSLibException, TimeoutException, GatewayException, IOException, InterruptedException
	{
		class ExceptionPair
		{
			public Throwable e;

			public AGateway src;
		}
		class Starter extends Thread
		{
			AGateway gateway;

			List<ExceptionPair> eList;

			List<AGateway> gList;

			public Starter(AGateway gateway, List<ExceptionPair> eList, List<AGateway> gList)
			{
				this.gateway = gateway;
				this.eList = eList;
				this.gList = gList;
			}

			@Override
			public void run()
			{
				try
				{
					this.gateway.startGateway();
					this.gList.add(this.gateway);
				}
				catch (Exception e)
				{
					ExceptionPair exc = new ExceptionPair();
					exc.e = e;
					exc.src = this.gateway;
					this.eList.add(exc);
				}
			}
		}
		if (getSettings().CONCURRENT_GATEWAY_START)
		{
			List<ExceptionPair> eList;
			List<AGateway> gList;
			eList = new LinkedList<ExceptionPair>();
			gList = new LinkedList<AGateway>();
			if (getGateways().size() == 0) throw new SMSLibException("No gateways are defined.");
			for (AGateway gateway : getGateways())
				new Starter(gateway, eList, gList).start();
			while ((gList.size() != getGateways().size()) && (eList.size() == 0))
				Thread.sleep(1000);
			for (ExceptionPair e : eList)
				e.src.setStatus(GatewayStatuses.RESTART);
			if (startAll)
			{
				if (eList.size() != 0)
				{
					ExceptionPair e = eList.get(0);
					if (e.e instanceof TimeoutException) throw (TimeoutException) e.e;
					if (e.e instanceof GatewayException) throw (GatewayException) e.e;
					if (e.e instanceof SMSLibException) throw (SMSLibException) e.e;
					if (e.e instanceof IOException) throw (IOException) e.e;
					if (e.e instanceof InterruptedException) throw (InterruptedException) e.e;
				}
				else
				{
					eList.clear();
					gList.clear();
				}
			}
		}
		else
		{
			if (getGateways().size() == 0) throw new SMSLibException("No gateways are defined.");
			for (AGateway gateway : getGateways())
				gateway.startGateway();
		}
	}

	/**
	 * Stops all gateways - does not remove them from Service's internal list.
	 * Once stopped, all SMSLib operations will fail. You need to start the
	 * gateways again before proceeding.
	 * 
	 * @throws SMSLibException
	 *             No Gateways are defined.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #startService()
	 */
	public synchronized void stopService() throws SMSLibException, TimeoutException, GatewayException, IOException, InterruptedException
	{
		setServiceStatus(ServiceStatus.STOPPING);
		if (getQueueManager() != null) getQueueManager().stop();
		if (getWatchDog() != null)
		{
			getWatchDog().cancel();
			setWatchDog(null);
		}
		for (AGateway gateway : getGateways())
			gateway.stopGateway();
		getNotifyQueueManager().cancel();
		setServiceStatus(ServiceStatus.STOPPED);
	}

	/**
	 * Reads inbound messages from ALL gateways with the Inbound attribute set.
	 * When successful, the message list will contain all messages read.
	 * 
	 * @param msgList
	 *            A (probably empty) list that will be populated with Inbound
	 *            messages read.
	 * @param msgClass
	 *            Filtering: Class of messages that need to be read.
	 * @return The number of messages read.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see MessageClasses
	 */
	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		for (AGateway gateway : getGateways())
		{
			if (gateway.isInbound())
			{
				try
				{
					readMessages(msgList, msgClass, gateway);
				}
				catch (TimeoutException e)
				{
					Logger.getInstance().logWarn("readMessages(): Gateway " + gateway.getGatewayId() + " does not respond, marking for restart.", null, null);
					gateway.setStatus(GatewayStatuses.RESTART);
				}
				catch (IOException e)
				{
					Logger.getInstance().logWarn("readMessages(): Gateway " + gateway.getGatewayId() + " throws IO errors, marking for restart.", null, null);
					gateway.setStatus(GatewayStatuses.RESTART);
				}
			}
		}
		return msgList.size();
	}

	/**
	 * .NET bridge method.
	 */
	public InboundMessage[] readMessages(MessageClasses msgClass) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		ArrayList<InboundMessage> messageList = new ArrayList<InboundMessage>();
		readMessages(messageList, msgClass);
		return messageList.toArray(new InboundMessage[0]);
	}

	/**
	 * Reads inbound messages from the SPECIFIC gateway. When successful, the
	 * message list will contain all messages read.
	 * 
	 * @param msgList
	 *            A (probably empty) list that will be populated with Inbound
	 *            messages read.
	 * @param msgClass
	 *            Filtering: Class of messages that need to be read.
	 * @param gatewayId
	 *            The identifier of the gateway from which to read messages.
	 * @return The number of messages read.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see MessageClasses
	 * @see AGateway
	 */
	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		AGateway gateway = findGateway(gatewayId);
		if ((gateway != null) && (gateway.isInbound()))
		{
			try
			{
				readMessages(msgList, msgClass, gateway);
			}
			catch (TimeoutException e)
			{
				Logger.getInstance().logWarn("readMessages(): Gateway " + gateway.getGatewayId() + " does not respond, marking for restart.", null, null);
				gateway.setStatus(GatewayStatuses.RESTART);
			}
			catch (IOException e)
			{
				Logger.getInstance().logWarn("readMessages(): Gateway " + gateway.getGatewayId() + " throws IO errors, marking for restart.", null, null);
				gateway.setStatus(GatewayStatuses.RESTART);
			}
		}
		return msgList.size();
	}

	/**
	 * .NET bridge method.
	 */
	public InboundMessage[] readMessages(MessageClasses msgClass, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		ArrayList<InboundMessage> messageList = new ArrayList<InboundMessage>();
		readMessages(messageList, msgClass, gatewayId);
		return messageList.toArray(new InboundMessage[0]);
	}

	/**
	 * Reads inbound messages from the SPECIFIC gateway. When successful, the
	 * message list will contain all messages read.
	 * 
	 * @param msgList
	 *            A (probably empty) list that will be populated with inbound
	 *            messages read.
	 * @param msgClass
	 *            Filtering: Class of messages that need to be read.
	 * @param gateway
	 *            The gateway object from which to read messages.
	 * @return The number of messages read.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see MessageClasses
	 * @see AGateway
	 */
	public int readMessages(Collection<InboundMessage> msgList, MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		try
		{
			gateway.readMessages(msgList, msgClass);
		}
		catch (TimeoutException e)
		{
			Logger.getInstance().logWarn("readMessages(): Gateway " + gateway.getGatewayId() + " does not respond, marking for restart.", null, null);
			gateway.setStatus(GatewayStatuses.RESTART);
		}
		catch (IOException e)
		{
			Logger.getInstance().logWarn("readMessages(): Gateway " + gateway.getGatewayId() + " throws IO errors, marking for restart.", null, null);
			gateway.setStatus(GatewayStatuses.RESTART);
		}
		return msgList.size();
	}

	/**
	 * .NET bridge method.
	 */
	public InboundMessage[] readMessages(MessageClasses msgClass, AGateway gateway) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		ArrayList<InboundMessage> messageList = new ArrayList<InboundMessage>();
		readMessages(messageList, msgClass, gateway);
		return messageList.toArray(new InboundMessage[0]);
	}

	/**
	 * Reads a specific gateway for a message matching the given Memory Location
	 * and Memory Index.
	 * <p>
	 * This is a "dummy" approach. It does not implement the CGMR command,
	 * rather it reads all messages and searches for a match.
	 * 
	 * @param gatewayId
	 *            The Gateway ID of the gateway to read from.
	 * @param memLoc
	 *            The memory location string.
	 * @param memIndex
	 *            The memory index.
	 * @return The message read. Null if no relevant message is found or if the
	 *         Gateway ID given is invalid.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public InboundMessage readMessage(String gatewayId, String memLoc, int memIndex) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return null;
		InboundMessage msg = null;
		AGateway gateway = findGateway(gatewayId);
		if ((gateway != null) && (gateway.isInbound()))
		{
			try
			{
				msg = gateway.readMessage(memLoc, memIndex);
			}
			catch (TimeoutException e)
			{
				Logger.getInstance().logWarn("readMessages(): Gateway " + gateway.getGatewayId() + " does not respond, marking for restart.", null, null);
				gateway.setStatus(GatewayStatuses.RESTART);
			}
			catch (IOException e)
			{
				Logger.getInstance().logWarn("readMessages(): Gateway " + gateway.getGatewayId() + " throws IO errors, marking for restart.", null, null);
				gateway.setStatus(GatewayStatuses.RESTART);
			}
		}
		return msg;
	}

	/**
	 * Sends a single message. The following logic is applied in order for
	 * SMSLib to decide from which gateway it will send the message:<br>
	 * 1. If the message holds gateway information (member field "gatewayId"),
	 * SMSLib will try to send it from that gateway.<br>
	 * 2. If the message does not hold gateway information (member field
	 * "gatewayId" is empty or "*") then if router and load balancer is defined,
	 * then message is processed by these classes.<br>
	 * 3. Otherwise the method selects the first outbound-capable gateway
	 * defined and sends the message from it.<br>
	 * The method blocks until the message is actually sent (synchronous
	 * operation).
	 * 
	 * @param msg
	 *            An OutboundMessage object.
	 * @return True if the message is sent.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #queueMessage(OutboundMessage)
	 */
	public boolean sendMessage(OutboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return false;
		AGateway gateway = routeMessage(msg);
		if (gateway != null)
		{
			try
			{
				List<String> recipients = expandGroup(msg.getRecipient());
				if (recipients.size() == 0) return gateway.sendMessage(msg);
				List<OutboundMessage> groupMessages = new ArrayList<OutboundMessage>();
				for (String to : recipients)
				{
					OutboundMessage newMessage = new OutboundMessage();
					msg.copyTo(newMessage);
					newMessage.setRecipient(to);
					groupMessages.add(newMessage);
				}
				sendMessages(groupMessages);
				return true;
			}
			catch (TimeoutException e)
			{
				Logger.getInstance().logWarn("sendMessage(): Gateway " + gateway.getGatewayId() + " does not respond, marking for restart.", null, null);
				gateway.setStatus(GatewayStatuses.RESTART);
				msg.setMessageStatus(MessageStatuses.FAILED);
				msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
				return false;
			}
			catch (IOException e)
			{
				Logger.getInstance().logWarn("sendMessage(): Gateway " + gateway.getGatewayId() + " throws IO errors, marking for restart.", null, null);
				gateway.setStatus(GatewayStatuses.RESTART);
				msg.setMessageStatus(MessageStatuses.FAILED);
				msg.setFailureCause(FailureCauses.GATEWAY_FAILURE);
				return false;
			}
		}
		return false;
	}

	/**
	 * Sends a single message from the specified gateway.
	 * 
	 * @param msg
	 *            An OutboundMessage object.
	 * @param gatewayId
	 *            The id of the gateway that will be used for sending.
	 * @return True if the message is sent.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #sendMessage(OutboundMessage)
	 */
	public boolean sendMessage(OutboundMessage msg, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return false;
		msg.setGatewayId(gatewayId);
		return sendMessage(msg);
	}

	/**
	 * Sends a list of messages.
	 * 
	 * @param msgList
	 *            A list of OutboundMessage objects.
	 * @return The number of messages sent.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #sendMessage(OutboundMessage)
	 */
	public int sendMessages(Collection<OutboundMessage> msgList) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		int counter = 0;
		for (OutboundMessage msg : msgList)
			if (sendMessage(msg)) counter++;
		return counter;
	}

	/**
	 * .NET bridge method.
	 */
	public int sendMessages(OutboundMessage[] msgArray) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int counter = 0;
		for (int i = 0; i < msgArray.length; i++)
			if (sendMessage(msgArray[i])) counter++;
		return counter;
	}

	/**
	 * Sends a list of messages from the specified gateway.
	 * 
	 * @param msgList
	 *            A list of OutboundMessage objects.
	 * @param gatewayId
	 *            The id of the gateway that will be used for sending.
	 * @return The number of messages sent.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 * @see #sendMessage(OutboundMessage)
	 */
	public int sendMessages(Collection<OutboundMessage> msgList, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		int counter = 0;
		for (OutboundMessage msg : msgList)
		{
			msg.setGatewayId(gatewayId);
			if (sendMessage(msg)) counter++;
		}
		return counter;
	}

	/**
	 * .NET bridge method.
	 */
	public int sendMessages(OutboundMessage[] msgArray, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		int counter = 0;
		for (int i = 0; i < msgArray.length; i++)
		{
			msgArray[i].setGatewayId(gatewayId);
			if (sendMessage(msgArray[i])) counter++;
		}
		return counter;
	}

	/**
	 * Queues a message for sending. The gateway selection logic is the same as
	 * for sendMessage(). The method does not block - returns immediately. If
	 * you wish to be alerted about the fate of the message, you may implement a
	 * IOutboundMessageNotification listener.
	 * 
	 * @param msg
	 *            Message to be sent
	 * @return True if the message is accepted in the Queue.
	 * @see #queueMessageAt(OutboundMessage, Date)
	 * @see #sendMessage(OutboundMessage)
	 * @see IOutboundMessageNotification
	 */
	public boolean queueMessage(OutboundMessage msg)
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return false;
		List<String> recipients = expandGroup(msg.getRecipient());
		if (recipients.size() == 0)
		{
			if ("*".equals(msg.getGatewayId()))
			{
				AGateway gateway = routeMessage(msg);
				if (gateway == null) { return false; }
				msg.setGatewayId(gateway.getGatewayId());
			}
			return getQueueManager().queueMessage(msg);
		}
		for (String to : recipients)
		{
			OutboundMessage newMessage = new OutboundMessage();
			msg.copyTo(newMessage);
			newMessage.setRecipient(to);
			if ("*".equals(msg.getGatewayId()))
			{
				AGateway gateway = routeMessage(msg);
				if (gateway == null) { return false; }
				msg.setGatewayId(gateway.getGatewayId());
			}
			getQueueManager().queueMessage(newMessage);
		}
		return true;
	}

	/**
	 * Queues a message for sending from the specific gateway.
	 * 
	 * @param msg
	 *            A OutboundMessage object.
	 * @param gatewayId
	 *            The id of the gateway that will be used for sending.
	 * @return True if the message is accepted in the Queue.
	 * @see #queueMessage(OutboundMessage)
	 */
	public boolean queueMessage(OutboundMessage msg, String gatewayId)
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return false;
		msg.setGatewayId(gatewayId);
		return queueMessage(msg);
	}

	/**
	 * Queues a list of messages for sending.
	 * 
	 * @param msgList
	 *            A list of OutboundMessage objects.
	 * @return The number of messages accepted in the Queue.
	 * @see #queueMessage(OutboundMessage)
	 */
	public int queueMessages(Collection<OutboundMessage> msgList)
	{
		int counter = 0;
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		for (OutboundMessage msg : msgList)
			if (queueMessage(msg)) counter++;
		return counter;
	}

	/**
	 * .NET bridge method.
	 */
	public int queueMessages(OutboundMessage[] msgArray)
	{
		int counter = 0;
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		for (int i = 0; i < msgArray.length; i++)
			if (queueMessage(msgArray[i])) counter++;
		return counter;
	}

	/**
	 * Queues a list of messages for sending from the specific gateway.
	 * 
	 * @param msgList
	 *            A list of OutboundMessage objects.
	 * @param gatewayId
	 *            The id of the gateway to be used for sending.
	 * @return The number of messages accepted in the Queue.
	 * @see #queueMessage(OutboundMessage)
	 */
	public int queueMessages(Collection<OutboundMessage> msgList, String gatewayId)
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		int counter = 0;
		for (OutboundMessage msg : msgList)
		{
			msg.setGatewayId(gatewayId);
			if (queueMessage(msg)) counter++;
		}
		return counter;
	}

	/**
	 * .NET bridge method.
	 */
	public int queueMessages(OutboundMessage[] msgArray, String gatewayId)
	{
		int counter = 0;
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		for (int i = 0; i < msgArray.length; i++)
		{
			msgArray[i].setGatewayId(gatewayId);
			if (queueMessage(msgArray[i])) counter++;
		}
		return counter;
	}

	/**
	 * Queues a message for sending at a specific time. The gateway selection
	 * logic is the same as for sendMessage(). The method does not block -
	 * returns immediately. If you wish to be alerted about the fate of the
	 * message, you may implement a IOutboundMessageNotification listener.
	 * 
	 * @param msg
	 *            The message to be sent.
	 * @param at
	 *            The date/time that the message should be sent at.
	 * @return True if the message is accepted in the Queue.
	 * @see #queueMessageAt(OutboundMessage, long)
	 * @see #queueMessage(OutboundMessage)
	 */
	public boolean queueMessageAt(OutboundMessage msg, Date at)
	{
		msg.setScheduledDeliveryDate(at);
		return queueMessage(msg);
	}

	/**
	 * Queues a message for sending with a specific delay. The gateway selection
	 * logic is the same as for sendMessage(). The method does not block -
	 * returns immediately. If you wish to be alerted about the fate of the
	 * message, you may implement a IOutboundMessageNotification listener.
	 * 
	 * @param msg
	 *            The message to be sent.
	 * @param delayMillis
	 *            The delay (ms) that the message should be sent after.
	 * @return True if the message is accepted in the Queue.
	 * @see #queueMessageAt(OutboundMessage, Date)
	 * @see #queueMessage(OutboundMessage)
	 */
	public boolean queueMessageAt(OutboundMessage msg, long delayMillis)
	{
		msg.setDeliveryDelay(delayMillis);
		return queueMessage(msg);
	}

	/**
	 * Attempts to remove the specified message from the background sending
	 * queue. Can only be used for messages previously queued up with the
	 * queueMessage() calls.
	 * 
	 * @param msg
	 *            The outbound message to be removed.
	 * @return true if the message was removed from the queue.
	 */
	public boolean removeMessage(OutboundMessage msg)
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return false;
		return getQueueManager().removePendingMessage(msg);
	}

	/**
	 * Attempts to remove the message with the specified ID from the background
	 * queue. Can only be used for messages previously queued with the
	 * queueMessage() call.
	 * 
	 * @param messageId
	 *            The ID of the message to be removed.
	 * @return true if the message was removed from the queue.
	 */
	public boolean removeMessage(String messageId)
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return false;
		return getQueueManager().removePendingMessage(messageId);
	}

	/**
	 * Deletes the specified message. The operation is not supported by all
	 * gateways.
	 * 
	 * @param msg
	 *            The message to be deleted. It must be a valid InboundMessage
	 *            object. <b>DO NOT PASS invalid objects to the method!</b>
	 * @return True if the message is deleted.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public boolean deleteMessage(InboundMessage msg) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return false;
		AGateway gateway = findGateway(msg.getGatewayId());
		if (gateway != null)
		{
			try
			{
				return gateway.deleteMessage(msg);
			}
			catch (TimeoutException e)
			{
				Logger.getInstance().logWarn("deleteMessage(): Gateway " + gateway.getGatewayId() + " does not respond, marking for restart.", null, null);
				gateway.setStatus(GatewayStatuses.RESTART);
				return false;
			}
			catch (IOException e)
			{
				Logger.getInstance().logWarn("deleteMessage(): Gateway " + gateway.getGatewayId() + " throws IO errors, marking for restart.", null, null);
				gateway.setStatus(GatewayStatuses.RESTART);
				return false;
			}
		}
		return false;
	}

	public boolean sendUSSDRequest(USSDRequest request, String gatewayId) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) { throw new GatewayException("Service is not started"); }
		AGateway gateway = findGateway(gatewayId);
		if (request == null) { throw new IllegalArgumentException("Cannot use a null request object"); }
		if (gateway == null) { throw new GatewayException("Cannot use a null gateway"); }
		return gateway.sendUSSDRequest(request);
	}

	/**
	 * Loads the phonebook from the specified gateway into a Phonebook class.
	 * 
	 * @param phonebook
	 *            An already instantiated, empty Phonebook class.
	 * @param gatewayId
	 *            The gateway id for which the phonebook should be loaded
	 * @return The number of phonebook entries read.
	 * @throws TimeoutException
	 *             The gateway did not respond in a timely manner.
	 * @throws GatewayException
	 *             A Gateway error occurred.
	 * @throws IOException
	 *             An IO error occurred.
	 * @throws InterruptedException
	 *             The call was interrupted.
	 */
	public int readPhonebook(Phonebook phonebook, String gatewayId) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		if (getServiceStatus() != ServiceStatus.STARTED) return 0;
		AGateway gateway = findGateway(gatewayId);
		if (gateway != null) { return gateway.readPhonebook(phonebook); }
		return 0;
	}

	/**
	 * Returns the total number of messages received by the specified gateway.
	 * 
	 * @param gatewayId
	 *            The id of the gateway to query.
	 * @return The number of received messages or -1 on error.
	 */
	public int getInboundMessageCount(String gatewayId)
	{
		return getInboundMessageCount(findGateway(gatewayId));
	}

	/**
	 * Returns the total number of messages received by the specified gateway.
	 * 
	 * @param gateway
	 *            The AGateway object to query.
	 * @return The number of received messages or -1 on error.
	 */
	public int getInboundMessageCount(AGateway gateway)
	{
		return (gateway != null ? gateway.getInboundMessageCount() : -1);
	}

	/**
	 * Returns the total number of messages sent via the specified gateway.
	 * 
	 * @param gatewayId
	 *            The id of the gateway to query.
	 * @return The number of sent messages or -1 on error.
	 */
	public int getOutboundMessageCount(String gatewayId)
	{
		return getOutboundMessageCount(findGateway(gatewayId));
	}

	/**
	 * Returns the total number of messages sent via the specified gateway.
	 * 
	 * @param gateway
	 *            The AGateway object to query.
	 * @return The number of sent messages or -1 on error.
	 */
	public int getOutboundMessageCount(AGateway gateway)
	{
		return (gateway != null ? gateway.getOutboundMessageCount() : -1);
	}

	/**
	 * Returns the total number of messages received by all gateways.
	 * 
	 * @return The number of received messages.
	 */
	public int getInboundMessageCount()
	{
		int total = 0;
		for (AGateway gateway : getGateways())
			total += gateway.getInboundMessageCount();
		return total;
	}

	/**
	 * Returns the total number of messages sent via all gateways.
	 * 
	 * @return The number of sent messages.
	 */
	public int getOutboundMessageCount()
	{
		int total = 0;
		for (AGateway gateway : getGateways())
			total += gateway.getOutboundMessageCount();
		return total;
	}

	/**
	 * Find and return a gateway by its ID.
	 * 
	 * @param gatewayId
	 *            The ID of gateway to find.
	 * @return Gateway object bearing given name, or NULL if not found.
	 */
	public AGateway findGateway(String gatewayId)
	{
		for (AGateway gateway : getGateways())
			if (gateway.getGatewayId().equals(gatewayId)) return gateway;
		return null;
	}

	/**
	 * Returns the list of defined gateways.
	 * 
	 * @return The list of gateways.
	 */
	public Collection<AGateway> getGateways()
	{
		return this.gatewayList;
	}

	/**
	 * .NET bridge method.
	 */
	public AGateway[] getGatewaysNET()
	{
		return getGateways().toArray(new AGateway[0]);
	}

	/**
	 * Returns the active Load Balancer class.
	 * 
	 * @return The active LoadBalancer class.
	 * @see LoadBalancer
	 */
	public LoadBalancer getLoadBalancer()
	{
		return this.loadBalancer;
	}

	/**
	 * Sets a new Load Balancer.
	 * 
	 * @param loadBalancer
	 *            The Load Balancer that will take effect.
	 * @see LoadBalancer
	 */
	public void setLoadBalancer(LoadBalancer loadBalancer)
	{
		this.loadBalancer = loadBalancer;
	}

	/**
	 * Returns the active Router class.
	 * 
	 * @return The active Router class.
	 * @see Router
	 */
	public Router getRouter()
	{
		return this.router;
	}

	/**
	 * Sets a new Router.
	 * 
	 * @param router
	 *            The Router that will take effect.
	 * @see Router
	 */
	public void setRouter(Router router)
	{
		this.router = router;
	}

	/**
	 * Find best suitable gateway to send specific message through Router and
	 * Load Balancer.
	 * 
	 * @param msg
	 *            Message to be routed
	 * @return Reference to gateway or <code>null</code> if no suitable gateway
	 *         is found.
	 */
	public synchronized AGateway routeMessage(OutboundMessage msg)
	{
		Collection<AGateway> gateways;
		gateways = getRouter().route(msg, getGateways());
		if (gateways.size() > 0) return getLoadBalancer().balance(msg, gateways);
		else
		{
			msg.setMessageStatus(MessageStatuses.FAILED);
			msg.setFailureCause(FailureCauses.NO_ROUTE);
			return null;
		}
	}

	/**
	 * Returns the notification method set for inbound messages. Returns null if
	 * no such method is set.
	 * 
	 * @return The notification method.
	 * @see #setInboundMessageNotification(IInboundMessageNotification)
	 */
	public IInboundMessageNotification getInboundMessageNotification()
	{
		return this.inboundMessageNotification;
	}

	/**
	 * Sets the inbound message notification method. The method must adhere to
	 * the IInboundMessageNotification interface. If set, SMSLib will call this
	 * method upon arrival of a new inbound message.
	 * 
	 * @param inboundNotification
	 *            The method to be called.
	 * @see #getInboundMessageNotification()
	 * @see IInboundMessageNotification
	 */
	public void setInboundMessageNotification(IInboundMessageNotification inboundNotification)
	{
		this.inboundMessageNotification = inboundNotification;
	}

	/**
	 * Returns the notification method set for outbound messages. Returns null
	 * if no such method is set.
	 * 
	 * @return The notification method.
	 * @see #setOutboundMessageNotification(IOutboundMessageNotification)
	 */
	public IOutboundMessageNotification getOutboundMessageNotification()
	{
		return this.outboundMessageNotification;
	}

	/**
	 * Sets the outbound notification method. The method must adhere to the
	 * IOutboundMessageNotification interface. If set, SMSLib will call this
	 * method upon dispatch of a message through the queueing (asyncronous)
	 * calls.
	 * 
	 * @param outboundNotification
	 * @see #getOutboundMessageNotification()
	 * @see IOutboundMessageNotification
	 */
	public void setOutboundMessageNotification(IOutboundMessageNotification outboundNotification)
	{
		this.outboundMessageNotification = outboundNotification;
	}

	/**
	 * Returns the call notification method. Returns null if no such method is
	 * set.
	 * 
	 * @return The notification method.
	 * @see #setCallNotification(ICallNotification)
	 */
	public ICallNotification getCallNotification()
	{
		return this.callNotification;
	}

	/**
	 * Sets the call notification method. The method must adhere to the
	 * ICallNotification interface. If set, SMSLib will call this method upon
	 * detection of an inbound call.
	 * 
	 * @param callNotification
	 * @see #getCallNotification()
	 * @see ICallNotification
	 */
	public void setCallNotification(ICallNotification callNotification)
	{
		this.callNotification = callNotification;
	}

	/**
	 * Returns the USSD notification method. Returns null if no such method is
	 * set.
	 * 
	 * @return The USSD notification method.
	 * @see #setUSSDNotification(IUSSDNotification)
	 */
	public IUSSDNotification getUSSDNotification()
	{
		return this.ussdNotification;
	}

	/**
	 * Sets the USSD notification method. The method must adhere to the
	 * IUSSDNotification interface. If set, SMSLib will call this method upon
	 * detection of an inbound USSD datagram.
	 * 
	 * @param ussdNotification
	 * @see #getUSSDNotification()
	 * @see IUSSDNotification
	 */
	public void setUSSDNotification(IUSSDNotification ussdNotification)
	{
		this.ussdNotification = ussdNotification;
	}

	/**
	 * Returns the gateway status notification method. Returns null if no such
	 * method has been set.
	 * 
	 * @return The notification method.
	 * @see #setGatewayStatusNotification(IGatewayStatusNotification)
	 */
	public IGatewayStatusNotification getGatewayStatusNotification()
	{
		return this.gatewayStatusNotification;
	}

	/**
	 * Sets the gateway status notification method. The method must adhere to
	 * the IGatewayStatusNotification interface. If set, SMSLib will call this
	 * method upon every gateway status change.
	 * 
	 * @param gatewayStatusNotification
	 * @see #getGatewayStatusNotification()
	 * @see IGatewayStatusNotification
	 */
	public void setGatewayStatusNotification(IGatewayStatusNotification gatewayStatusNotification)
	{
		this.gatewayStatusNotification = gatewayStatusNotification;
	}

	/**
	 * Returns the notification method set for Queue sending operation. Returns
	 * null if no such method is set.
	 * 
	 * @return The notification method.
	 * @see #setQueueSendingNotification(IQueueSendingNotification)
	 */
	public IQueueSendingNotification getQueueSendingNotification()
	{
		return this.queueSendingNotification;
	}

	/**
	 * Sets the Queue sending notification method. The method must adhere to the
	 * IQueueSendingNotification interface. If set, SMSLib will call this method
	 * upon dispatch of a message through the queueing (asyncronous) calls.
	 * 
	 * @param queueSendingNotification
	 * @see #getQueueSendingNotification()
	 * @see IQueueSendingNotification
	 */
	public void setQueueSendingNotification(IQueueSendingNotification queueSendingNotification)
	{
		this.queueSendingNotification = queueSendingNotification;
	}

	/**
	 * Returns the orphaned message notification method, or null if no-one is
	 * set.
	 * 
	 * @return The orphaned notification method.
	 * @see #setOrphanedMessageNotification(IOrphanedMessageNotification)
	 */
	public IOrphanedMessageNotification getOrphanedMessageNotification()
	{
		return this.orphanedMessageNotification;
	}

	/**
	 * Sets the orphaned message notification. The method must implement the
	 * IOrphanedMessageNotification interface.
	 * 
	 * @param orphanedMessageNotification
	 * @see #getOrphanedMessageNotification()
	 */
	public void setOrphanedMessageNotification(IOrphanedMessageNotification orphanedMessageNotification)
	{
		this.orphanedMessageNotification = orphanedMessageNotification;
	}

	public long getStartMillis()
	{
		return this.startMillis;
	}

	void setSettings(Settings settings)
	{
		this.S = settings;
	}

	public ServiceStatus getServiceStatus()
	{
		return this.serviceStatus;
	}

	void setServiceStatus(ServiceStatus myServiceStatus)
	{
		this.serviceStatus = myServiceStatus;
	}

	/**
	 * Returns the Settings object, holding SMSLib run-time values.
	 * 
	 * @return The Settings object.
	 * @see Settings
	 */
	public Settings getSettings()
	{
		return this.S;
	}

	/**
	 * Creates a destination group. A group can hold an unlimited number of
	 * recipients. Sending a message to a predefined group expands and sends the
	 * message to all numbers defined by the group. A group is valid for as long
	 * as this instance of SMSLib is active.
	 * 
	 * @param groupName
	 *            The group name.
	 * @return True if the group creation succeded.
	 * @see #removeGroup(String)
	 */
	public boolean createGroup(String groupName)
	{
		this.groups.add(new Group(groupName));
		return true;
	}

	/**
	 * Removes a group.
	 * 
	 * @param groupName
	 *            The name of the group to be removed.
	 * @return True if the removal was a success.
	 * @see #createGroup(String)
	 */
	public boolean removeGroup(String groupName)
	{
		for (Group a : this.groups)
		{
			if (a.getName().equalsIgnoreCase(groupName))
			{
				a.clear();
				this.groups.remove(a);
				return true;
			}
		}
		return false;
	}

	/**
	 * Expands a group to its recipient numbers.
	 * 
	 * @param groupName
	 *            The group name to be expanded.
	 * @return A list of the numbers that this group represents. If the group is
	 *         not defined, an empty list is returned.
	 * @see #addToGroup(String, String)
	 * @see #removeFromGroup(String, String)
	 */
	public ArrayList<String> expandGroup(String groupName)
	{
		for (Group a : this.groups)
		{
			if (a.getName().equalsIgnoreCase(groupName)) { return new ArrayList<String>(a.getNumbers()); }
		}
		return new ArrayList<String>();
	}

	/**
	 * Adds a number to the specified group.
	 * 
	 * @param groupName
	 *            The group to which the number is to be added.
	 * @param number
	 *            The number to add.
	 * @return True if the number is added. False if the group is not found.
	 * @see #createGroup(String)
	 * @see #removeFromGroup(String, String)
	 */
	public boolean addToGroup(String groupName, String number)
	{
		for (Group a : this.groups)
		{
			if (a.getName().equalsIgnoreCase(groupName))
			{
				a.addNumber(number);
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes a number from the specified group.
	 * 
	 * @param groupName
	 *            The group from which the number is to be removed.
	 * @param number
	 *            The number to remove.
	 * @return True if the number was removed. False if the group or the number
	 *         is not found.
	 * @see #removeGroup(String)
	 * @see #addToGroup(String, String)
	 */
	public boolean removeFromGroup(String groupName, String number)
	{
		for (Group a : this.groups)
		{
			if (a.getName().equalsIgnoreCase(groupName)) { return a.removeNumber(number); }
		}
		return false;
	}

	/**
	 * Checks if a number belongs to a group.
	 * 
	 * @param groupName
	 *            The group in which the number is to be searched.
	 * @param number
	 *            The number to search.
	 * @return True if the number is found.
	 */
	public boolean checkInGroup(String groupName, String number)
	{
		for (Group a : this.groups)
		{
			if (a.getName().equalsIgnoreCase(groupName))
			{
				Collection<String> numbers = a.getNumbers();
				return numbers.contains(number);
			}
		}
		return false;
	}

	public boolean setQueueManager(AbstractQueueManager myQueueManager)
	{
		if (getServiceStatus() != ServiceStatus.STOPPED) return false;
		this.queueManager = myQueueManager;
		return true;
	}

	public AbstractQueueManager getQueueManager()
	{
		return this.queueManager;
	}

	void setNotifyQueueManager(NotifyQueueManager myNotifyQueueManager)
	{
		this.notifyQueueManager = myNotifyQueueManager;
	}

	public NotifyQueueManager getNotifyQueueManager()
	{
		return this.notifyQueueManager;
	}

	public KeyManager getKeyManager()
	{
		return this.keyManager;
	}

	WatchDog getWatchDog()
	{
		return this.watchDog;
	}

	void setWatchDog(WatchDog myWatchDog)
	{
		this.watchDog = myWatchDog;
	}

	private class WatchDog extends AServiceThread
	{
		public WatchDog(String name, int delay)
		{
			super(name, delay, 0, true);
		}

		@Override
		public void process() throws Exception
		{
			if (getServiceStatus() != ServiceStatus.STARTED) return;
			for (AGateway gateway : getGateways())
			{
				if (gateway.getStatus() == GatewayStatuses.RESTART)
				{
					Logger.getInstance().logWarn("Gateway: " + gateway.getGatewayId() + " restarting.", null, null);
					try
					{
						gateway.stopGateway();
						gateway.setStatus(GatewayStatuses.RESTART);
					}
					catch (Exception e)
					{
						gateway.setStatus(GatewayStatuses.RESTART);
						Logger.getInstance().logWarn("Error while shutting down Gateway: " + gateway.getGatewayId(), e, null);
					}
					try
					{
						gateway.startGateway();
					}
					catch (Exception e)
					{
						gateway.setStatus(GatewayStatuses.RESTART);
						Logger.getInstance().logError("Error while starting Gateway: " + gateway.getGatewayId(), e, null);
					}
				}
			}
		}
	}

	public static void main(String[] args)
	{
		System.out.println(Library.getLibraryDescription());
		System.out.println("\nSMSLib API Version: " + Library.getLibraryVersion());
	}
}
