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

package org.smslib.modem.athandler;

import java.io.IOException;
import org.smslib.GatewayException;
import org.smslib.TimeoutException;
import org.smslib.modem.ModemGateway;

public class ATHandler_SonyEricsson extends ATHandler
{
	public ATHandler_SonyEricsson(ModemGateway myGateway)
	{
		super(myGateway);
	}

	/**
	 * Many SonyEricssons return different responses to CMGF command. Instead of
	 * a plain OK, they return "+CMGF=0\rOK\r", independently of the ECHO
	 * setting. This code bypasses the standard SMSLib checking routines and
	 * performs the check itself.
	 */
	@Override
	public boolean setPduProtocol() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("AT+CMGF=0\r");
		return (getModemDriver().getResponse().indexOf("OK") >= 0);
	}

	/**
	 * Many (all?) SonyEricssons require a DCS identifier with the CUSD command.
	 * Only a value of 15 (7-bit alphabet, language unspecified) seems to work
	 * with these phones.
	 */
	@Override
	protected String formatUSSDCommand(String presentation, String ussdCommand, String dcs)
	{
		return super.formatUSSDCommand(presentation, ussdCommand, "15");
	}
}
