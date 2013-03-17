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

package org.smslib.modem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.smslib.GatewayException;
import org.smslib.InboundMessage;
import org.smslib.InvalidMessageException;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.USSDResponse;
import org.smslib.AGateway.AsyncEvents;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.AGateway.Protocols;
import org.smslib.InboundMessage.MessageClasses;
import org.smslib.helper.Logger;
import org.smslib.notify.CallNotification;
import org.smslib.notify.InboundMessageNotification;
import org.smslib.threading.AServiceThread;

/**
 * Abstract implementation of a generic GSM modem driver.
 */
public abstract class AModemDriver
{
	private static final String rxErrorWithCode = "\\s*[\\p{ASCII}]*\\s*\\+(CM[ES])\\s+ERROR: (\\d+)\\s";

	private static final String rxPlainError = "\\s*[\\p{ASCII}]*\\s*(ERROR|NO CARRIER|NO DIALTONE)\\s";

	private Object SYNC_Reader, SYNC_Commander, SYNC_InboundReader;

	private ModemGateway gateway;

	private boolean dataReceived;

	private volatile boolean connected;

	private CharQueue charQueue;

	private ModemReader modemReader;

	private KeepAlive keepAlive;

	private AsyncNotifier asyncNotifier;

	private AsyncMessageProcessor asyncMessageProcessor;

	private CNMIEmulatorProcessor cnmiEmulationProcessor;

	/**
	 * Code of last error
	 * 
	 * <pre>
	 *   -1 = empty or invalid response
	 *    0 = OK
	 * 5xxx = CME error xxx
	 * 6xxx = CMS error xxx
	 * 9000 = ERROR
	 * </pre>
	 */
	private int lastError;

	static int OK = 0;

	protected AModemDriver(ModemGateway myGateway, String deviceParms)
	{
		setSYNCReader(new Object());
		setSYNCCommander(new Object());
		setSYNCInboundReader(new Object());
		setGateway(myGateway);
		setConnected(false);
		setDataReceived(false);
		setCharQueue(new CharQueue());
	}

	protected abstract void connectPort() throws GatewayException, IOException, InterruptedException;

	protected abstract void disconnectPort() throws IOException, InterruptedException;

	protected abstract void clear() throws IOException;

	protected void connect() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		String response;
		synchronized (getSYNCCommander())
		{
			try
			{
				connectPort();
				setConnected(true);
				setKeepAlive(new KeepAlive("KeepAlive [" + getGateway().getGatewayId() + "]", Service.getInstance().getSettings().SERIAL_KEEPALIVE_INTERVAL * 1000));
				setCnmiEmulationProcessor(new CNMIEmulatorProcessor("CNMIEmulatorProcessor [" + getGateway().getGatewayId() + "]", Service.getInstance().getSettings().CNMI_EMULATOR_INTERVAL * 1000));
				setModemReader(new ModemReader());
				setAsyncNotifier(new AsyncNotifier());
				setAsyncMessageProcessor(new AsyncMessageProcessor());
				clearBuffer();
				getGateway().getATHandler().reset();
				getGateway().getATHandler().sync();
				getGateway().getATHandler().echoOff();
				if ((getGateway().getCustomInitString() != null) && (getGateway().getCustomInitString().length() > 0))
				{
					write(getGateway().getCustomInitString() + "\r");
					getGateway().getATHandler().echoOff();
				}
				while (true)
				{
					response = getGateway().getATHandler().getSimStatus();
					while (response.indexOf("BUSY") >= 0)
					{
						Logger.getInstance().logDebug("SIM found busy, waiting...", null, getGateway().getGatewayId());
						Thread.sleep(Service.getInstance().getSettings().AT_WAIT_SIMPIN);
						response = getGateway().getATHandler().getSimStatus();
					}
					if (response.indexOf("SIM PIN2") >= 0)
					{
						Logger.getInstance().logDebug("SIM requesting PIN2.", null, getGateway().getGatewayId());
						if ((getGateway().getSimPin2() == null) || (getGateway().getSimPin2().length() == 0)) throw new GatewayException("The GSM modem requires SIM PIN2 to operate.");
						if (!getGateway().getATHandler().enterPin(getGateway().getSimPin2())) throw new GatewayException("SIM PIN2 provided is not accepted by the GSM modem.");
						Thread.sleep(Service.getInstance().getSettings().AT_WAIT_SIMPIN);
						continue;
					}
					else if (response.indexOf("SIM PIN") >= 0)
					{
						Logger.getInstance().logDebug("SIM requesting PIN.", null, getGateway().getGatewayId());
						if ((getGateway().getSimPin() == null) || (getGateway().getSimPin().length() == 0)) throw new GatewayException("The GSM modem requires SIM PIN to operate.");
						if (!getGateway().getATHandler().enterPin(getGateway().getSimPin())) throw new GatewayException("SIM PIN provided is not accepted by the GSM modem.");
						Thread.sleep(Service.getInstance().getSettings().AT_WAIT_SIMPIN);
						continue;
					}
					else if (response.indexOf("READY") >= 0) break;
					else if (response.indexOf("OK") >= 0) break;
					else if (response.indexOf("ERROR") >= 0)
					{
						Logger.getInstance().logWarn("Erroneous CPIN response, proceeding with defaults.", null, getGateway().getGatewayId());
						break;
					}
					Logger.getInstance().logWarn("Cannot understand SIMPIN response: " + response + ", will wait for a while...", null, getGateway().getGatewayId());
					Thread.sleep(Service.getInstance().getSettings().AT_WAIT_SIMPIN);
				}
				getGateway().getATHandler().echoOff();
				getGateway().getATHandler().init();
				getGateway().getATHandler().echoOff();
				if (!waitForNetworkRegistration()) Logger.getInstance().logWarn("Network Registration failed, proceeding with defaults.", null, getGateway().getGatewayId());
				getGateway().getATHandler().setVerboseErrors();
				if (getGateway().getATHandler().getStorageLocations().length() == 0)
				{
					try
					{
						getGateway().getATHandler().readStorageLocations();
						Logger.getInstance().logInfo("MEM: Storage Locations Found: " + getGateway().getATHandler().getStorageLocations(), null, getGateway().getGatewayId());
					}
					catch (Exception e)
					{
						getGateway().getATHandler().setStorageLocations("--");
						Logger.getInstance().logWarn("Storage locations could *not* be retrieved, will proceed with defaults.", e, getGateway().getGatewayId());
					}
				}
				if (!getGateway().getATHandler().setIndications())
				{
					Logger.getInstance().logWarn("Callback indications were *not* set succesfully!", null, getGateway().getGatewayId());
					getCnmiEmulationProcessor().enable();
				}
				else
				{
					if (getGateway().getATHandler().getIndications().getMode().equals("0")) getCnmiEmulationProcessor().enable();
				}
				if (getGateway().getProtocol() == Protocols.PDU)
				{
					if (!getGateway().getATHandler().setPduProtocol()) throw new GatewayException("The GSM modem does not support the PDU protocol.");
				}
				else if (getGateway().getProtocol() == Protocols.TEXT)
				{
					if (!getGateway().getATHandler().setTextProtocol()) throw new GatewayException("The GSM modem does not support the TEXT protocol.");
				}
			}
			catch (TimeoutException t)
			{
				try
				{
					disconnect();
				}
				catch (Exception e)
				{
					// Swallow this.
				}
				throw t;
			}
			catch (GatewayException t)
			{
				try
				{
					disconnect();
				}
				catch (Exception e)
				{
					// Swallow this.
				}
				throw t;
			}
			catch (IOException t)
			{
				try
				{
					disconnect();
				}
				catch (Exception e)
				{
					// Swallow this.
				}
				throw t;
			}
			catch (InterruptedException t)
			{
				try
				{
					disconnect();
				}
				catch (Exception e)
				{
					// Swallow this.
				}
				throw t;
			}
		}
	}

	protected void disconnect() throws IOException, InterruptedException
	{
		setConnected(false);
		if (getKeepAlive() != null)
		{
			getKeepAlive().cancel();
			setKeepAlive(null);
		}
		if (getCnmiEmulationProcessor() != null)
		{
			getCnmiEmulationProcessor().cancel();
			setCnmiEmulationProcessor(null);
		}
		if (getAsyncNotifier() != null)
		{
			getAsyncNotifier().interrupt();
			getAsyncNotifier().join();
			setAsyncNotifier(null);
		}
		if (getAsyncMessageProcessor() != null)
		{
			getAsyncMessageProcessor().interrupt();
			getAsyncMessageProcessor().join();
			setAsyncMessageProcessor(null);
		}
		if (getModemReader() != null)
		{
			getModemReader().interrupt();
			getModemReader().join();
			setModemReader(null);
		}
		disconnectPort();
	}

	public abstract void write(char c) throws IOException;

	public abstract void write(byte[] s) throws IOException;

	protected abstract int read() throws IOException;

	protected abstract boolean portHasData() throws IOException;

	public boolean dataAvailable() throws InterruptedException
	{
		return (getCharQueue().peek() == -1 ? false : true);
	}

	public void write(String s) throws IOException
	{
		Logger.getInstance().logDebug("SEND :" + formatLog(s), null, getGateway().getGatewayId());
		write(s.getBytes());
	}

	public void addToQueue(String s)
	{
		for (int i = 0; i < s.length(); i++)
			getCharQueue().put((byte) s.charAt(i));
	}

	public String getResponse() throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		return getResponse(AsyncEvents.NOTHING);
	}

	/*
	* Version of getResponse that looks for a particular type of unsolicited response (e.g. USSDResponse) and returns 
	* response without triggering an event.  Useful if you want to get a USSD response synchronously rather
	* using the event mechanism.  eventResponse is the type of unsolicited response to look for, set it to AsyncEvents.NOTHING
	* to handle all unsolicited responses through the event mechanism.
	*/
	public String getResponse(AsyncEvents eventResponse) throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		String response;
		byte c;
		setLastError(-1);
		StringBuffer buffer = new StringBuffer(Service.getInstance().getSettings().SERIAL_BUFFER_SIZE);
		try
		{
			while (true)
			{
				while ((getCharQueue().peek() == 0x0a) || (getCharQueue().peek() == 0x0d))
				{
					getCharQueue().get();
				}
				while (true)
				{
					c = getCharQueue().get();
					if (System.getProperty("smslib.dumpqueues") != null) Logger.getInstance().logDebug("OUT READER QUEUE : " + (int) c + " / " + (char) c, null, getGateway().getGatewayId());
					if (c != 0x0a) buffer.append((char) c);
					else break;
				}
				if (buffer.charAt(buffer.length() - 1) != 0x0d) buffer.append((char) 0x0d);
				response = buffer.toString();
				if (getGateway().getATHandler().matchesTerminator(response))
				{
					break;
				}
			}
			Logger.getInstance().logDebug("BUFFER: " + buffer, null, getGateway().getGatewayId());
			if (getGateway().getATHandler().isUnsolicitedResponse(buffer.toString()))
			{
				AsyncEvents event = getGateway().getATHandler().processUnsolicitedEvents(buffer.toString());
				if (event == eventResponse && eventResponse != AsyncEvents.NOTHING) { return buffer.toString(); }
				if ((event == AsyncEvents.INBOUNDMESSAGE) || (event == AsyncEvents.INBOUNDSTATUSREPORTMESSAGE) || (event == AsyncEvents.INBOUNDCALL) || (event == AsyncEvents.USSDRESPONSE)) getAsyncNotifier().setEvent(event, buffer.toString());
				return getResponse();
			}
			// Try to interpret error code
			if (response.matches(rxErrorWithCode))
			{
				Pattern p = Pattern.compile(rxErrorWithCode);
				Matcher m = p.matcher(response);
				if (m.find())
				{
					try
					{
						if (m.group(1).equals("CME"))
						{
							int code = Integer.parseInt(m.group(2));
							setLastError(5000 + code);
						}
						else if (m.group(1).equals("CMS"))
						{
							int code = Integer.parseInt(m.group(2));
							setLastError(6000 + code);
						}
						else throw new GatewayException("Invalid error response: " + m.group(1));
					}
					catch (NumberFormatException e)
					{
						Logger.getInstance().logDebug("Error on number conversion while interpreting response: ", null, getGateway().getGatewayId());
						throw new GatewayException("Cannot convert error code number.");
					}
				}
				else throw new GatewayException("Cannot match error code. Should never happen!");
			}
			else if (response.matches(rxPlainError)) setLastError(9000);
			else if (response.indexOf("OK") >= 0) setLastError(0);
			else setLastError(10000);
			Logger.getInstance().logDebug("RECV :" + formatLog(buffer.toString()), null, getGateway().getGatewayId());
		}
		catch (InterruptedException e)
		{
			Logger.getInstance().logWarn("GetResponse() Interrupted.", e, getGateway().getGatewayId());
			throw e;
		}
		catch (TimeoutException e)
		{
			Logger.getInstance().logDebug("Buffer contents on timeout: " + buffer, null, getGateway().getGatewayId());
			throw e;
		}
		return buffer.toString();
	}

	public void clearBuffer() throws IOException, InterruptedException
	{
		synchronized (getSYNCCommander())
		{
			Logger.getInstance().logDebug("clearBuffer() called.", null, getGateway().getGatewayId());
			Thread.sleep(Service.getInstance().getSettings().SERIAL_CLEAR_WAIT);
			clear();
			getCharQueue().clear();
		}
	}

	protected boolean waitForNetworkRegistration() throws GatewayException, TimeoutException, IOException, InterruptedException
	{
		//TODO: Move the magic number "6" (network retries) to settings(?)
		StringTokenizer tokens;
		String response;
		int answer;
		int retries = 0;
		while (true)
		{
			response = getGateway().getATHandler().getNetworkRegistration();
			if (response.indexOf("ERROR") >= 0) return false;
			response = response.replaceAll("\\s+OK\\s+", "");
			response = response.replaceAll("\\s+", "");
			response = response.replaceAll("\\+CREG:", "");
			tokens = new StringTokenizer(response, ",");
			tokens.nextToken();
			try
			{
				answer = Integer.parseInt(tokens.nextToken());
			}
			catch (Exception e)
			{
				answer = -1;
			}
			switch (answer)
			{
				case 0:
					Logger.getInstance().logError("GSM: Auto-registration disabled!", null, getGateway().getGatewayId());
					throw new GatewayException("GSM Network Auto-Registration disabled!");
				case 1:
					Logger.getInstance().logInfo("GSM: Registered to home network.", null, getGateway().getGatewayId());
					return true;
				case 2:
					Logger.getInstance().logWarn("GSM: Not registered, searching for network...", null, getGateway().getGatewayId());
					if (++retries == 6) throw new GatewayException("GSM Network Registration failed, give up trying!");
					break;
				case 3:
					Logger.getInstance().logError("GSM: Network registration denied!", null, getGateway().getGatewayId());
					throw new GatewayException("GSM Network Registration denied!");
				case 4:
					Logger.getInstance().logError("GSM: Unknown registration error!", null, getGateway().getGatewayId());
					throw new GatewayException("GSM Network Registration error!");
				case 5:
					Logger.getInstance().logInfo("GSM: Registered to foreign network (roaming).", null, getGateway().getGatewayId());
					return true;
				case -1:
					Logger.getInstance().logInfo("GSM: Invalid CREG response.", null, getGateway().getGatewayId());
					throw new GatewayException("GSM: Invalid CREG response.");
			}
			Thread.sleep(Service.getInstance().getSettings().AT_WAIT_NETWORK);
		}
	}

	private String formatLog(String s)
	{
		StringBuffer response = new StringBuffer();
		int i;
		char c;
		for (i = 0; i < s.length(); i++)
		{
			c = s.charAt(i);
			switch (c)
			{
				case 13:
					response.append("(cr)");
					break;
				case 10:
					response.append("(lf)");
					break;
				case 9:
					response.append("(tab)");
					break;
				default:
					if ((c >= 32) && (c < 128))
					{
						response.append(c);
					}
					else
					{
						response.append("(" + (int) c + ")");
					}
					break;
			}
		}
		return response.toString();
	}

	private class CharQueue
	{
		byte[] buffer;

		int bufferStart, bufferEnd;

		public CharQueue()
		{
			this.buffer = null;
			this.bufferStart = 0;
			this.bufferEnd = 0;
		}

		public synchronized void put(byte c)
		{
			if (this.buffer == null) this.buffer = new byte[Service.getInstance().getSettings().SERIAL_BUFFER_SIZE];
			this.buffer[this.bufferEnd] = c;
			this.bufferEnd++;
			if (this.bufferEnd == Service.getInstance().getSettings().SERIAL_BUFFER_SIZE) this.bufferEnd = 0;
			if (System.getProperty("smslib.dumpqueues") != null) Logger.getInstance().logDebug("IN READER QUEUE : " + (int) c + " / " + (char) c, null, getGateway().getGatewayId());
			notifyAll();
		}

		public synchronized byte get() throws TimeoutException, InterruptedException
		{
			byte c;
			if (this.buffer == null) this.buffer = new byte[Service.getInstance().getSettings().SERIAL_BUFFER_SIZE];
			while (true)
			{
				try
				{
					if (this.bufferStart == this.bufferEnd) wait(Service.getInstance().getSettings().SERIAL_TIMEOUT);
					if (this.bufferStart == this.bufferEnd) throw new TimeoutException("No response from device.");
					c = this.buffer[this.bufferStart];
					this.bufferStart++;
					if (this.bufferStart == Service.getInstance().getSettings().SERIAL_BUFFER_SIZE) this.bufferStart = 0;
					return c;
				}
				catch (InterruptedException e)
				{
					if (getGateway().getStatus() == GatewayStatuses.STARTED) Logger.getInstance().logWarn("Ignoring InterruptedException in Queue.get().", null, getGateway().getGatewayId());
					else
					{
						Logger.getInstance().logWarn("Re-throwing InterruptedException in Queue.get() - should be during shutdown...", null, getGateway().getGatewayId());
						throw new InterruptedException();
					}
				}
			}
		}

		public synchronized byte peek() throws InterruptedException
		{
			if (this.buffer == null) this.buffer = new byte[Service.getInstance().getSettings().SERIAL_BUFFER_SIZE];
			while (true)
			{
				try
				{
					if (this.bufferStart == this.bufferEnd) wait(Service.getInstance().getSettings().SERIAL_TIMEOUT);
					if (this.bufferStart == this.bufferEnd) return -1;
					return this.buffer[this.bufferStart];
				}
				catch (InterruptedException e)
				{
					if (getGateway().getStatus() == GatewayStatuses.STARTED) Logger.getInstance().logWarn("Ignoring InterruptedException in Queue.peek().", e, getGateway().getGatewayId());
					else
					{
						Logger.getInstance().logWarn("Re-throwing InterruptedException in Queue.peek() - should be during shutdown...", e, getGateway().getGatewayId());
						throw new InterruptedException();
					}
				}
			}
		}

		public synchronized String peek(int sizeToRead)
		{
			int i, size;
			StringBuffer result;
			if (this.buffer == null) this.buffer = new byte[Service.getInstance().getSettings().SERIAL_BUFFER_SIZE];
			size = sizeToRead;
			if (this.bufferStart == this.bufferEnd) return "";
			result = new StringBuffer(size);
			i = this.bufferStart;
			while (size > 0)
			{
				if ((this.buffer[i] != 0x0a) && (this.buffer[i] != 0x0d))
				{
					result.append((char) this.buffer[i]);
					size--;
				}
				i++;
				if (i == Service.getInstance().getSettings().SERIAL_BUFFER_SIZE) i = 0;
				if (i == this.bufferEnd) break;
			}
			return result.toString();
		}

		public synchronized void clear()
		{
			this.bufferStart = 0;
			this.bufferEnd = 0;
		}
	}

	private class ModemReader extends Thread
	{
		public ModemReader()
		{
			setName("SMSlib-ModemReader-" + getGateway().getGatewayId());
			setDaemon(true);
			start();
			Logger.getInstance().logDebug("ModemReader thread started.", null, getGateway().getGatewayId());
		}

		@Override
		public void run()
		{
			int c;
			String data;
			while (isConnected())
			{
				try
				{
					synchronized (getSYNCReader())
					{
						if (!isDataReceived()) getSYNCReader().wait();
						if (!isConnected()) break;
						c = read();
						while (c != -1)
						{
							getCharQueue().put((byte) c);
							if (!portHasData()) break;
							c = read();
						}
						setDataReceived(false);
					}
					data = getCharQueue().peek(6);
					for (int i = 0; i < getGateway().getATHandler().getUnsolicitedResponses().length; i++)
					{
						if (data.indexOf(getGateway().getATHandler().getUnsolicitedResponse(i)) >= 0)
						{
							Thread.sleep(100);
							getKeepAlive().interrupt();
							break;
						}
					}
				}
				catch (InterruptedException e)
				{
					if (!isConnected()) break;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			Logger.getInstance().logDebug("ModemReader thread ended.", null, getGateway().getGatewayId());
		}
	}

	private class KeepAlive extends AServiceThread
	{
		public KeepAlive(String name, int delay)
		{
			super(name, delay, 0, true);
		}

		@Override
		public void process() throws Exception
		{
			try
			{
				if (!isConnected()) return;
				if (getGateway().getStatus() == GatewayStatuses.STARTED)
				{
					synchronized (getSYNCCommander())
					{
						if (!isConnected()) return;
						try
						{
							if (!getGateway().getATHandler().isAlive()) getGateway().setStatus(GatewayStatuses.RESTART);
							if (!getCnmiEmulationProcessor().isEnabled()) getAsyncMessageProcessor().setProcess();
						}
						catch (Exception e)
						{
							getGateway().setStatus(GatewayStatuses.RESTART);
						}
					}
				}
			}
			catch (Exception e)
			{
				Logger.getInstance().logError("ModemDriver: KeepAlive Error.", e, getGateway().getGatewayId());
				getGateway().setStatus(GatewayStatuses.RESTART);
			}
		}
	}

	private class AsyncNotifier extends Thread
	{
		class Event
		{
			AsyncEvents event;

			String response;

			public Event(AsyncEvents myEvent, String myResponse)
			{
				this.event = myEvent;
				this.response = myResponse;
			}

			@Override
			public String toString()
			{
				return "Event: " + this.event + " / Response: " + this.response;
			}
		}

		private BlockingQueue<Event> eventQueue;

		private Object SYNC;

		public AsyncNotifier()
		{
			this.SYNC = new Object();
			this.eventQueue = new LinkedBlockingQueue<Event>();
			setPriority(MIN_PRIORITY);
			setName("SMSLib-AsyncNotifier : " + getGateway().getGatewayId());
			setDaemon(true);
			start();
			Logger.getInstance().logDebug("AsyncNotifier thread started.", null, getGateway().getGatewayId());
		}

		protected void setEvent(AsyncEvents event, String response)
		{
			synchronized (this.SYNC)
			{
				Event ev = new Event(event, response);
				Logger.getInstance().logDebug("Storing AsyncEvent: " + ev, null, getGateway().getGatewayId());
				this.eventQueue.add(ev);
				this.SYNC.notify();
			}
		}

		protected String getOriginator(String indication)
		{
			Pattern p = Pattern.compile("\\+?\"\\S+\"");
			Matcher m = p.matcher(indication);
			if (m.find()) return indication.substring(m.start(), m.end()).replaceAll("\"", "");
			return "";
		}

		@Override
		public void run()
		{
			String response;
			Event event;
			while (isConnected())
			{
				try
				{
					event = this.eventQueue.take();
					Logger.getInstance().logDebug("Processing AsyncEvent: " + event, null, getGateway().getGatewayId());
					if (event.event == AsyncEvents.INBOUNDMESSAGE)
					{
						Logger.getInstance().logDebug("Inbound message detected!", null, getGateway().getGatewayId());
						event.event = AsyncEvents.NOTHING;
						response = event.response;
						getAsyncMessageProcessor().setProcess();
					}
					else if (event.event == AsyncEvents.INBOUNDSTATUSREPORTMESSAGE)
					{
						Logger.getInstance().logDebug("Inbound status report message detected!", null, getGateway().getGatewayId());
						event.event = AsyncEvents.NOTHING;
						response = event.response;
						getAsyncMessageProcessor().setProcess();
					}
					else if (event.event == AsyncEvents.INBOUNDCALL)
					{
						Logger.getInstance().logDebug("Inbound call detected!", null, getGateway().getGatewayId());
						event.event = AsyncEvents.NOTHING;
						synchronized (getSYNCCommander())
						{
							getGateway().getATHandler().switchToCmdMode();
							getGateway().getModemDriver().write("ATH\r");
							getGateway().getModemDriver().getResponse();
							response = event.response;
						}
						Service.getInstance().getNotifyQueueManager().getNotifyQueue().add(new CallNotification(getGateway(), getOriginator(response)));
						//if (Service.getInstance().getCallNotification() != null) Service.getInstance().getCallNotification().process(getGateway().getGatewayId(), getOriginator(response));
					}
					else if (event.event == AsyncEvents.USSDRESPONSE)
					{
						Logger.getInstance().logDebug("Inbound USSD response detected!", null, getGateway().getGatewayId());
						event.event = AsyncEvents.NOTHING;
						response = event.response;
						Logger.getInstance().logDebug("USSD response : " + formatLog(response), null, getGateway().getGatewayId());
						if (Service.getInstance().getUSSDNotification() != null)
						{
							USSDResponse ussdResponse = new USSDResponse(response, getGateway().getGatewayId());
							ussdResponse.setContent(getGateway().getATHandler().formatUSSDResponse(ussdResponse.getContent()));
							Service.getInstance().getUSSDNotification().process(getGateway(), ussdResponse);
						}
					}
				}
				catch (InterruptedException e)
				{
					if (!isConnected()) break;
				}
				catch (InvalidMessageException e)
				{
					Logger.getInstance().logInfo("Invalid Message received! Ignoring. ", e, getGateway().getGatewayId());
				}
				catch (GatewayException e)
				{
					getGateway().setStatus(GatewayStatuses.RESTART);
				}
				catch (IOException e)
				{
					getGateway().setStatus(GatewayStatuses.RESTART);
				}
				catch (TimeoutException e)
				{
					getGateway().setStatus(GatewayStatuses.RESTART);
				}
			}
			Logger.getInstance().logDebug("AsyncNotifier thread ended.", null, getGateway().getGatewayId());
		}
	}

	private class AsyncMessageProcessor extends Thread
	{
		private List<InboundMessage> msgList;

		private Object SYNC;

		private boolean process;

		public AsyncMessageProcessor()
		{
			this.msgList = new ArrayList<InboundMessage>();
			this.SYNC = new Object();
			this.process = false;
			setPriority(MAX_PRIORITY);
			setName("SMSLib-AsyncMessageProcessor : " + getGateway().getGatewayId());
			setDaemon(true);
			start();
			Logger.getInstance().logDebug("AsyncMessageProcessor thread started.", null, getGateway().getGatewayId());
		}

		public void setProcess()
		{
			synchronized (this.SYNC)
			{
				if (this.process) return;
				this.process = true;
				this.SYNC.notify();
			}
		}

		@Override
		public void run()
		{
			while (isConnected())
			{
				try
				{
					synchronized (this.SYNC)
					{
						if (!this.process)
						{
							this.SYNC.wait();
							if (!isConnected()) break;
						}
					}
					synchronized (getSYNCInboundReader())
					{
						getGateway().readMessages(this.msgList, MessageClasses.ALL);
						for (InboundMessage msg : this.msgList)
						{
							switch (msg.getType())
							{
								case INBOUND:
								case STATUSREPORT:
									Service.getInstance().getNotifyQueueManager().getNotifyQueue().add(new InboundMessageNotification(getGateway(), msg.getType(), msg));
									break;
								default:
									break;
							}
						}
					}
					this.msgList.clear();
					this.process = false;
				}
				catch (InterruptedException e)
				{
					if (!isConnected()) break;
				}
				catch (GatewayException e)
				{
					getGateway().setStatus(GatewayStatuses.RESTART);
				}
				catch (IOException e)
				{
					getGateway().setStatus(GatewayStatuses.RESTART);
				}
				catch (TimeoutException e)
				{
					getGateway().setStatus(GatewayStatuses.RESTART);
				}
			}
			Logger.getInstance().logDebug("AsyncMessageProcessor thread ended.", null, getGateway().getGatewayId());
		}
	}

	private class CNMIEmulatorProcessor extends AServiceThread
	{
		private List<InboundMessage> msgList;

		public CNMIEmulatorProcessor(String name, int delay)
		{
			super(name, delay, 0, false);
		}

		@Override
		public void process() throws Exception
		{
			if ((isConnected()) && (getGateway().getStatus() == GatewayStatuses.STARTED))
			{
				synchronized (getSYNCInboundReader())
				{
					this.msgList = new ArrayList<InboundMessage>();
					getGateway().readMessages(this.msgList, MessageClasses.ALL);
					for (InboundMessage msg : this.msgList)
					{
						switch (msg.getType())
						{
							case INBOUND:
							case STATUSREPORT:
								Service.getInstance().getNotifyQueueManager().getNotifyQueue().add(new InboundMessageNotification(getGateway(), msg.getType(), msg));
								break;
						}
					}
					this.msgList.clear();
				}
			}
		}
	}

	void setLastError(int myLastError)
	{
		this.lastError = myLastError;
	}

	public int getLastError()
	{
		return this.lastError;
	}

	public String getLastErrorText()
	{
		if (getLastError() == 0) return "OK";
		else if (getLastError() == -1) return "Invalid or empty response";
		else if ((getLastError() / 1000) == 5) return "CME Error " + (getLastError() % 1000);
		else if ((getLastError() / 1000) == 6) return "CMS Error " + (getLastError() % 1000);
		else return "Error: unknown";
	}

	public boolean isOk()
	{
		return (getLastError() == OK);
	}

	protected ModemGateway getGateway()
	{
		return this.gateway;
	}

	protected void setGateway(ModemGateway myGateway)
	{
		this.gateway = myGateway;
	}

	protected boolean isConnected()
	{
		return this.connected;
	}

	protected void setConnected(boolean myConnected)
	{
		this.connected = myConnected;
	}

	protected boolean isDataReceived()
	{
		return this.dataReceived;
	}

	protected void setDataReceived(boolean myDataReceived)
	{
		this.dataReceived = myDataReceived;
	}

	protected CharQueue getCharQueue()
	{
		return this.charQueue;
	}

	protected void setCharQueue(CharQueue myCharQueue)
	{
		this.charQueue = myCharQueue;
	}

	protected Object getSYNCReader()
	{
		return this.SYNC_Reader;
	}

	protected void setSYNCReader(Object reader)
	{
		this.SYNC_Reader = reader;
	}

	protected Object getSYNCCommander()
	{
		return this.SYNC_Commander;
	}

	protected void setSYNCCommander(Object commander)
	{
		this.SYNC_Commander = commander;
	}

	protected Object getSYNCInboundReader()
	{
		return this.SYNC_InboundReader;
	}

	protected void setSYNCInboundReader(Object inbMessage)
	{
		this.SYNC_InboundReader = inbMessage;
	}

	protected KeepAlive getKeepAlive()
	{
		return this.keepAlive;
	}

	protected void setKeepAlive(KeepAlive myKeepAlive)
	{
		this.keepAlive = myKeepAlive;
	}

	protected AsyncNotifier getAsyncNotifier()
	{
		return this.asyncNotifier;
	}

	protected void setAsyncNotifier(AsyncNotifier myAsyncNotifier)
	{
		this.asyncNotifier = myAsyncNotifier;
	}

	protected AsyncMessageProcessor getAsyncMessageProcessor()
	{
		return this.asyncMessageProcessor;
	}

	protected void setAsyncMessageProcessor(AsyncMessageProcessor myAsyncMessageProcessor)
	{
		this.asyncMessageProcessor = myAsyncMessageProcessor;
	}

	protected CNMIEmulatorProcessor getCnmiEmulationProcessor()
	{
		return this.cnmiEmulationProcessor;
	}

	protected void setCnmiEmulationProcessor(CNMIEmulatorProcessor myCnmiEmulationProcessor)
	{
		this.cnmiEmulationProcessor = myCnmiEmulationProcessor;
	}

	protected ModemReader getModemReader()
	{
		return this.modemReader;
	}

	protected void setModemReader(ModemReader myModemReader)
	{
		this.modemReader = myModemReader;
	}
}
