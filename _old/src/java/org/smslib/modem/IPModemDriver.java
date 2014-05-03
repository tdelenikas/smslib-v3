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
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;
import javax.net.ssl.SSLContext;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SimpleOptionHandler;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.GatewayException;
import org.smslib.Service;
import org.smslib.helper.Logger;
import org.smslib.modem.ModemGateway.IPProtocols;

class IPModemDriver extends AModemDriver
{
	private String ipAddress;

	private int ipPort;

	private TelnetClient tc;

	private InputStream in;

	private OutputStream out;

	private Peeker peeker;

	private TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);

	private SimpleOptionHandler binaryopt = new SimpleOptionHandler(0, true, false, true, false);

	private EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);

	private SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

	protected IPModemDriver(ModemGateway myGateway, String deviceParms)
	{
		super(myGateway, deviceParms);
		StringTokenizer tokens = new StringTokenizer(deviceParms, ":");
		this.ipAddress = tokens.nextToken();
		this.ipPort = Integer.parseInt(tokens.nextToken());
		this.tc = null;
	}

	@Override
	protected void connectPort() throws GatewayException, IOException, InterruptedException
	{
		try
		{
			Logger.getInstance().logInfo("Opening: " + this.ipAddress + " @" + this.ipPort, null, getGateway().getGatewayId());
			this.tc = new TelnetClient();
			this.tc.addOptionHandler(this.ttopt);
			this.tc.addOptionHandler(this.echoopt);
			this.tc.addOptionHandler(this.gaopt);
			if (getGateway().getIpProtocol() == IPProtocols.BINARY) this.tc.addOptionHandler(this.binaryopt); // Make telnet session binary, so ^Z in ATHander.Sendmessage is send raw!
			if (getGateway().getIpEncryption())
			{
				try
				{
					this.tc.setSocketFactory(SSLContext.getInstance("Default").getSocketFactory());
				}
				catch (NoSuchAlgorithmException e)
				{
					Logger.getInstance().logError("Unable to find algorithm needed for using SSL", e, getGateway().getGatewayId());
					// TODO: although not supposed to happen, something should be done if it does
				}
			}
			this.tc.connect(this.ipAddress, this.ipPort);
			this.in = this.tc.getInputStream();
			this.out = this.tc.getOutputStream();
			this.peeker = new Peeker();
		}
		catch (InvalidTelnetOptionException e)
		{
			throw new GatewayException("Unsupported telnet option for the selected IP connection.");
		}
	}

	@Override
	protected void disconnectPort() throws IOException, InterruptedException
	{
		Logger.getInstance().logInfo("Closing: " + this.ipAddress + " @" + this.ipPort, null, getGateway().getGatewayId());
		synchronized (getSYNCReader())
		{
			if (this.tc != null) this.tc.disconnect();
			this.tc = null;
			if (this.peeker != null)
			{
				this.peeker.interrupt();
				this.peeker.join();
			}
			this.peeker = null;
		}
	}

	@Override
	protected void clear() throws IOException
	{
		while (portHasData())
			read();
	}

	@Override
	protected boolean portHasData() throws IOException
	{
		return (this.in.available() > 0);
	}

	@Override
	public void write(char c) throws IOException
	{
		this.out.write((short) c);
		this.out.flush();
	}

	@Override
	public void write(byte[] s) throws IOException
	{
		this.out.write(s);
		this.out.flush();
	}

	@Override
	protected int read() throws IOException
	{
		return this.in.read();
	}

	TelnetClient getTc()
	{
		return this.tc;
	}

	private class Peeker extends Thread
	{
		public Peeker()
		{
			setPriority(MIN_PRIORITY);
			start();
		}

		@Override
		public void run()
		{
			Logger.getInstance().logDebug("Peeker started.", null, getGateway().getGatewayId());
			while (true)
			{
				try
				{
					if (getTc() != null)
					{
						if (portHasData())
						{
							synchronized (getSYNCReader())
							{
								setDataReceived(true);
								getSYNCReader().notifyAll();
							}
						}
					}
					sleep(Service.getInstance().getSettings().SERIAL_POLLING_INTERVAL);
				}
				catch (InterruptedException e)
				{
					if (getTc() == null) break;
				}
				catch (IOException e)
				{
					getGateway().setStatus(GatewayStatuses.RESTART);
				}
			}
			Logger.getInstance().logDebug("Peeker stopped.", null, getGateway().getGatewayId());
		}
	}
}
