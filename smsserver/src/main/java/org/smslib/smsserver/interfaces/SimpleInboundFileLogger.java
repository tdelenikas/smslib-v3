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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Properties;
import org.smslib.InboundMessage;
import org.smslib.smsserver.SMSServer;

public class SimpleInboundFileLogger extends Interface<Void>
{
	private BufferedWriter out;

	public SimpleInboundFileLogger(String myInterfaceId, Properties myProps, SMSServer myServer, InterfaceTypes myType)
	{
		super(myInterfaceId, myProps, myServer, myType);
		setDescription("Simple file logger for inbound messages.");
	}

	@Override
	public void start() throws Exception
	{
		this.out = new BufferedWriter(new FileWriter(getProperty("filename", ""), true));
		super.start();
	}

	@Override
	public void stop() throws Exception
	{
		if (this.out != null) this.out.close();
		super.stop();
	}

	@Override
	public void messagesReceived(Collection<InboundMessage> msgList) throws Exception
	{
		for (InboundMessage msg : msgList)
		{
			this.out.write(msg.getDate() + "|" + msg.getOriginator() + "|" + msg.getText());
			this.out.flush();
		}
	}
}
