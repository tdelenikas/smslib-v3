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
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.helper.Logger;
import org.smslib.modem.ModemGateway;

public class ATHandler_Siemens_MC35i extends ATHandler
{
	public static final int RETRIES = 5;
	public static final int WAIT = 1500;

	public ATHandler_Siemens_MC35i(ModemGateway myGateway)
	{
		super(myGateway);
	}

	@Override
	public void init() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		for (int i = 0; i < RETRIES; i++)
		{
			getModemDriver().write("AT+CLIP=1\r");
			getModemDriver().getResponse();
			if (getModemDriver().isOk()) break;
			Logger.getInstance().logDebug("Modem didn't respond correctly on AT+CLIP. Retrying...", null, getGateway().getGatewayId());
			Thread.sleep(WAIT);
		}
		if (!getModemDriver().isOk())
			Logger.getInstance().logDebug("Modem didn't respond correctly on AT+CLIP correctly on 5 attemts. Giving up.", null, getGateway().getGatewayId());
		getModemDriver().write("AT+COPS=0\r");
		getModemDriver().getResponse();
	}

	@Override
	public void echoOff() throws IOException, InterruptedException
	{
		getModemDriver().write("ATV1\r");
		getModemDriver().write("ATQ0\r");
		getModemDriver().write("ATE0\r");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().clearBuffer();
	}
}
