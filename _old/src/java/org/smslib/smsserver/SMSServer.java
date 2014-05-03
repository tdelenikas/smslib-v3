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

package org.smslib.smsserver;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.smslib.ICallNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.IOrphanedMessageNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.IQueueSendingNotification;
import org.smslib.InboundMessage;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.Library;
import org.smslib.Message.MessageTypes;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.helper.Logger;
import org.smslib.smsserver.gateways.AGateway;
import org.smslib.smsserver.interfaces.Interface;
import org.smslib.smsserver.interfaces.Interface.InterfaceTypes;

/**
 * SMSServer Application.
 */
public class SMSServer
{
	Properties props;

	boolean shutdown = false;

	List<Interface<? extends Object>> infList;

	InboundNotification inboundNotification;

	OutboundNotification outboundNotification;

	CallNotification callNotification;

	QueueSendingNotification queueSendingNotification;

	OrphanedMessageNotification orphanedMessageNotification;

	InboundPollingThread inboundPollingThread;

	OutboundPollingThread outboundPollingThread;

	boolean optRunOnce = false;

	public SMSServer()
	{
		this.infList = new ArrayList<Interface<? extends Object>>();
		Runtime.getRuntime().addShutdownHook(new Shutdown());
		this.inboundNotification = new InboundNotification();
		this.outboundNotification = new OutboundNotification();
		this.callNotification = new CallNotification();
		this.queueSendingNotification = new QueueSendingNotification();
		orphanedMessageNotification = new OrphanedMessageNotification();
		this.inboundPollingThread = null;
		this.outboundPollingThread = null;
		//Service.getInstance().setInboundMessageNotification(this.inboundNotification);
		Service.getInstance().setOutboundMessageNotification(this.outboundNotification);
		Service.getInstance().setCallNotification(this.callNotification);
		Service.getInstance().setQueueSendingNotification(this.queueSendingNotification);
		Service.getInstance().setOrphanedMessageNotification(this.orphanedMessageNotification);
	}

	public List<Interface<? extends Object>> getInfList()
	{
		return this.infList;
	}

	public boolean getShutdown()
	{
		return shutdown;
	}

	public Properties getProperties()
	{
		return this.props;
	}

	class Shutdown extends Thread
	{
		@Override
		public void run()
		{
			Logger.getInstance().logInfo("SMSServer shutting down, please wait...", null, null);
			SMSServer.this.shutdown = true;
			try
			{
				stopInterfaces();
				if (Service.getInstance().getQueueManager() != null) Service.getInstance().getQueueManager().removeAllPendingMessages();
				if (Service.getInstance().getQueueManager() != null) Service.getInstance().getQueueManager().removeAllDelayedMessages();
				Service.getInstance().stopService();
				if (SMSServer.this.inboundPollingThread != null)
				{
					SMSServer.this.inboundPollingThread.interrupt();
					SMSServer.this.inboundPollingThread.join();
				}
				if (SMSServer.this.outboundPollingThread != null)
				{
					SMSServer.this.outboundPollingThread.interrupt();
					SMSServer.this.outboundPollingThread.join();
				}
			}
			catch (Exception e)
			{
				Logger.getInstance().logError("Shutdown hook error.", e, null);
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadConfiguration() throws Exception
	{
		FileInputStream f = null;
		try
		{
			this.props = new Properties();
			if (System.getProperty("smsserver.configdir") != null) f = new FileInputStream(System.getProperty("smsserver.configdir") + "SMSServer.conf");
			else if (System.getProperty("smsserver.configfile") != null) f = new FileInputStream(System.getProperty("smsserver.configfile"));
			else f = new FileInputStream("SMSServer.conf");
			getProperties().load(f);
			if (getProperties().getProperty("smsserver.balancer", "").length() > 0)
			{
				try
				{
					Class<?> c = Class.forName((getProperties().getProperty("smsserver.balancer", "").indexOf('.') == -1 ? "org.smslib.balancing." : "") + getProperties().getProperty("smsserver.balancer", ""));
					Constructor<?> constructor = c.getConstructor();
					org.smslib.balancing.LoadBalancer balancer = (org.smslib.balancing.LoadBalancer) constructor.newInstance();
					Service.getInstance().setLoadBalancer(balancer);
					Logger.getInstance().logInfo("SMSServer: set balancer to: " + getProperties().getProperty("smsserver.balancer", ""), null, null);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Logger.getInstance().logError("SMSServer: error setting custom balancer!", null, null);
				}
			}
			if (getProperties().getProperty("smsserver.router", "").length() > 0)
			{
				try
				{
					Class<?> c = Class.forName((getProperties().getProperty("smsserver.router", "").indexOf('.') == -1 ? "org.smslib.routing." : "") + getProperties().getProperty("smsserver.router", ""));
					Constructor<?> constructor = c.getConstructor();
					org.smslib.routing.Router router = (org.smslib.routing.Router) constructor.newInstance();
					Service.getInstance().setRouter(router);
					Logger.getInstance().logInfo("SMSServer: set router to: " + getProperties().getProperty("smsserver.router", ""), null, null);
				}
				catch (Exception e)
				{
					Logger.getInstance().logError("SMSServer: error setting custom balancer!", null, null);
				}
			}
			for (int i = 0; i < Integer.MAX_VALUE; i++)
			{
				try
				{
					String propName = "gateway." + i;
					String propValue = getProperties().getProperty(propName, "");
					if (propValue.length() == 0) break;
					StringTokenizer tokens = new StringTokenizer(propValue, ",");
					String gtwId = tokens.nextToken().trim();
					String gtwClass = tokens.nextToken().trim();
					Object[] args = new Object[] { gtwId, getProperties(), this };
					Class<?>[] argsClass = new Class[] { String.class, Properties.class, SMSServer.class };
					Class<?> c = Class.forName((gtwClass.indexOf('.') == -1 ? "org.smslib.smsserver.gateways." : "") + gtwClass);
					Constructor<?> constructor = c.getConstructor(argsClass);
					AGateway gtw = (AGateway) constructor.newInstance(args);
					gtw.create();
					Service.getInstance().addGateway(gtw.getGateway());
					Logger.getInstance().logInfo("SMSServer: added gateway " + gtwId + " / " + gtw.getDescription(), null, null);
				}
				catch (Exception e)
				{
					Logger.getInstance().logError("SMSServer: Unknown Gateway in configuration file!", null, null);
				}
			}
			for (int i = 0; i < Integer.MAX_VALUE; i++)
			{
				try
				{
					String propName = "interface." + i;
					String propValue = getProperties().getProperty(propName, "");
					if (propValue.length() == 0) break;
					StringTokenizer tokens = new StringTokenizer(propValue, ",");
					String infId = tokens.nextToken().trim();
					String infClass = tokens.nextToken().trim();
					InterfaceTypes infType = null;
					String sinfType = tokens.hasMoreTokens() ? tokens.nextToken() : "inoutbound";
					sinfType = sinfType.trim();
					if ("inbound".equalsIgnoreCase(sinfType))
					{
						infType = InterfaceTypes.INBOUND;
					}
					else if ("outbound".equalsIgnoreCase(sinfType))
					{
						infType = InterfaceTypes.OUTBOUND;
					}
					else
					{ // NULL or other crap
						infType = InterfaceTypes.INOUTBOUND;
					}
					Object[] args = new Object[] { infId, getProperties(), this, infType };
					Class<?>[] argsClass = new Class[] { String.class, Properties.class, SMSServer.class, InterfaceTypes.class };
					Class<?> c = Class.forName((infClass.indexOf('.') == -1 ? "org.smslib.smsserver.interfaces." : "") + infClass);
					Constructor<?> constructor = c.getConstructor(argsClass);
					Interface<? extends Object> inf = (Interface<? extends Object>) constructor.newInstance(args);
					getInfList().add(inf);
					Logger.getInstance().logInfo("SMSServer: added interface " + infId + " / " + inf.getDescription() + " / " + inf.getType(), null, null);
				}
				/* Check for exceptions thrown by the constructor itself */
				catch (InvocationTargetException e)
				{
					Logger.getInstance().logError("SMSServer: Illegal Interface configuration: " + e.getCause().getMessage(), null, null);
				}
				catch (Exception e)
				{
					Logger.getInstance().logError("SMSServer: Unknown Interface in configuration file!", null, null);
				}
			}
		}
		finally
		{
			if (f != null) f.close();
		}
	}

	class InboundPollingThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while (!SMSServer.this.shutdown)
				{
					Logger.getInstance().logDebug("InboundPollingThread() run.", null, null);
					readMessages();
					if (SMSServer.this.optRunOnce)
					{
						SMSServer.this.shutdown = true;
						new Shutdown().start();
						break;
					}
					Thread.sleep(Integer.parseInt(getProperties().getProperty("settings.inbound_interval", "60")) * 1000);
				}
			}
			catch (InterruptedException e)
			{
				Logger.getInstance().logDebug("InboundPollingThread() interrupted.", null, null);
			}
			catch (Exception e)
			{
				Logger.getInstance().logDebug("Error in InboundPollingThread()", e, null);
			}
		}
	}

	class OutboundPollingThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while (!SMSServer.this.shutdown)
				{
					Logger.getInstance().logDebug("OutboundPollingThread() run.", null, null);
					sendMessages();
					if (SMSServer.this.optRunOnce) break;
					Thread.sleep(Integer.parseInt(getProperties().getProperty("settings.outbound_interval", "60")) * 1000);
				}
			}
			catch (InterruptedException e)
			{
				Logger.getInstance().logDebug("OutboundPollingThread() interrupted.", null, null);
			}
			catch (Exception e)
			{
				Logger.getInstance().logDebug("Error in OutboundPollingThread()", e, null);
			}
		}
	}

	private void process() throws Exception
	{
		this.inboundPollingThread = new InboundPollingThread();
		this.inboundPollingThread.setName("SMSServer - InboundPollingThread");
		this.inboundPollingThread.start();
		this.outboundPollingThread = new OutboundPollingThread();
		this.outboundPollingThread.setName("SMSServer - OutboundPollingThread");
		this.outboundPollingThread.start();
		while (!this.shutdown)
			Thread.sleep(1000);
	}

	void startInterfaces() throws Exception
	{
		for (Interface<? extends Object> inf : getInfList())
			inf.start();
	}

	void stopInterfaces() throws Exception
	{
		for (Interface<? extends Object> inf : getInfList())
			inf.stop();
	}

	private void run() throws Exception
	{
		loadConfiguration();
		try
		{
			startInterfaces();
			Service.getInstance().startService();
			process();
		}
		catch (Exception e)
		{
			Logger.getInstance().logError("SMSServer error!", e, null);
			stopInterfaces();
			Service.getInstance().stopService();
			if (this.inboundPollingThread != null)
			{
				this.inboundPollingThread.interrupt();
				this.inboundPollingThread.join();
			}
			if (this.outboundPollingThread != null)
			{
				this.outboundPollingThread.interrupt();
				this.outboundPollingThread.join();
			}
		}
	}

	void readMessages()
	{
		List<InboundMessage> msgList = new ArrayList<InboundMessage>();
		try
		{
			Service.getInstance().readMessages(msgList, MessageClasses.ALL);
			if (msgList.size() > 0)
			{
				for (Interface<? extends Object> inf : getInfList())
					if (inf.isInbound()) inf.messagesReceived(msgList);
				if (getProperties().getProperty("settings.delete_after_processing", "no").equalsIgnoreCase("yes"))
					for (InboundMessage msg : msgList) Service.getInstance().deleteMessage(msg);
			}
		}
		catch (Exception e)
		{
			Logger.getInstance().logError("SMSServer: reading messages exception!", e, null);
		}
	}

	void sendMessages()
	{
		boolean foundOutboundGateway = false;
		for (org.smslib.AGateway gtw : Service.getInstance().getGateways())
			if (gtw.isOutbound())
			{
				foundOutboundGateway = true;
				break;
			}
		if (foundOutboundGateway)
		{
			List<OutboundMessage> msgList = new ArrayList<OutboundMessage>();
			try
			{
				for (Interface<? extends Object> inf : getInfList())
					if (inf.isOutbound()) msgList.addAll(inf.getMessagesToSend());
			}
			catch (Exception e)
			{
				Logger.getInstance().logError("SMSServer: sending messages exception!", e, null);
			}
			if (getProperties().getProperty("settings.send_mode", "sync").equalsIgnoreCase(("sync")))
			{
				Logger.getInstance().logInfo("SMSServer: sending synchronously...", null, null);
				for (OutboundMessage msg : msgList)
				{
					try
					{
						Service.getInstance().sendMessage(msg);
						for (Interface<? extends Object> inf : getInfList())
							if (inf.isOutbound()) inf.markMessage(msg);
					}
					catch (Exception e)
					{
						Logger.getInstance().logError("SMSServer: sending messages exception!", e, null);
						try
						{
							for (Interface<? extends Object> inf : getInfList())
								if (inf.isOutbound()) inf.markMessage(msg);
						}
						catch (Exception e1)
						{
							Logger.getInstance().logError("SMSServer: sending messages exception!", e1, null);
						}
					}
				}
			}
			else
			{
				Logger.getInstance().logInfo("SMSServer: sending asynchronously... [" + msgList.size() + "]", null, null);
				for (OutboundMessage msg : msgList)
				{
					if (!Service.getInstance().queueMessage(msg))
					{
						try
						{
							for (Interface<? extends Object> inf : getInfList())
								if (inf.isOutbound()) inf.markMessage(msg);
						}
						catch (Exception e)
						{
							Logger.getInstance().logError("SMSServer: sending messages exception!", e, null);
						}
					}
				}
			}
		}
	}

	class InboundNotification implements IInboundMessageNotification
	{
		@Override
		public void process(org.smslib.AGateway gateway, MessageTypes msgType, InboundMessage msg)
		{
			List<InboundMessage> msgList = new ArrayList<InboundMessage>();
			msgList.add(msg);
			for (Interface<? extends Object> inf : getInfList())
				if (inf.isInbound())
				{
					try
					{
						inf.messagesReceived(msgList);
					}
					catch (Exception e)
					{
						Logger.getInstance().logError("Error receiving message!", e, null);
					}
				}
			if (getProperties().getProperty("settings.delete_after_processing", "no").equalsIgnoreCase("yes"))
			{
				try
				{
					Service.getInstance().deleteMessage(msg);
				}
				catch (Exception e)
				{
					Logger.getInstance().logError("Error deleting received message!", e, null);
				}
			}
			msgList.clear();
		}
	}

	class OutboundNotification implements IOutboundMessageNotification
	{
		@Override
		public void process(org.smslib.AGateway gateway, org.smslib.OutboundMessage msg)
		{
			try
			{
				for (Interface<? extends Object> inf : getInfList())
					if (inf.isOutbound()) inf.markMessage(msg);
			}
			catch (Exception e)
			{
				Logger.getInstance().logError("IOutboundMessageNotification error.", e, null);
			}
		}
	}

	class CallNotification implements ICallNotification
	{
		@Override
		public void process(org.smslib.AGateway gateway, String callerId)
		{
			try
			{
				for (Interface<? extends Object> inf : getInfList())
					inf.callReceived(gateway.getGatewayId(), callerId);
			}
			catch (Exception e)
			{
				Logger.getInstance().logError("ICallNotification error.", e, null);
			}
		}
	}

	class QueueSendingNotification implements IQueueSendingNotification
	{
		@Override
		public void process(org.smslib.AGateway gateway, OutboundMessage msg)
		{
			Logger.getInstance().logInfo("**** >>>> Now Sending: " + msg.getRecipient(), null, gateway.getGatewayId());
		}
	}

	class OrphanedMessageNotification implements IOrphanedMessageNotification
	{
		@Override
		public boolean process(org.smslib.AGateway gateway, InboundMessage msg)
		{
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&& ORPHANED INFO &&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println(msg);
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&& ORPHANED INFO &&&&&&&&&&&&&&&&&");
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
			// Return FALSE to leave orphaned parts in memory.
			return false;
		}
	}

	public boolean checkPriorityTimeFrame(int priority)
	{
		String timeFrame;
		String from, to, current;
		Calendar cal = Calendar.getInstance();
		if (priority < 0) timeFrame = getProperties().getProperty("settings.timeframe.low", "0000-2359");
		else if (priority == 0) timeFrame = getProperties().getProperty("settings.timeframe.normal", "0000-2359");
		else if (priority >= 0) timeFrame = getProperties().getProperty("settings.timeframe.high", "0000-2359");
		else timeFrame = "0000-2359";
		from = timeFrame.substring(0, 4);
		to = timeFrame.substring(5, 9);
		cal.setTime(new java.util.Date());
		current = cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + cal.get(Calendar.HOUR_OF_DAY) : "" + cal.get(Calendar.HOUR_OF_DAY);
		current += cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE) : "" + cal.get(Calendar.MINUTE);
		if ((Integer.parseInt(current) >= Integer.parseInt(from)) && (Integer.parseInt(current) < Integer.parseInt(to))) return true;
		return false;
	}

	public static void main(String[] args)
	{
		System.out.println(Library.getLibraryDescription());
		System.out.println("\nSMSLib API version: " + Library.getLibraryVersion());
		System.out.println("SMSServer version: " + Library.getLibraryVersion());
		SMSServer app = new SMSServer();
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equalsIgnoreCase("-runonce")) app.optRunOnce = true;
			else System.out.println("Invalid argument: " + args[i]);
		}
		try
		{
			app.run();
			Logger.getInstance().logInfo("SMSServer exiting normally.", null, null);
		}
		catch (Exception e)
		{
			Logger.getInstance().logError("SMSServer Error: ", e, null);
			try
			{
				Service.getInstance().stopService();
			}
			catch (Exception e1)
			{
				Logger.getInstance().logError("SMSServer error while shutting down: ", e1, null);
			}
		}
	}
}
