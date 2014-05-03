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
import org.smslib.modem.ModemGateway;

/**
 * AT Handler for ZTE MF622 (Thanks to Dustin Cheng!).
 */
public class ATHandler_ZTE_MF622 extends ATHandler
{
	public ATHandler_ZTE_MF622(ModemGateway myGateway)
	{
		super(myGateway);
		setStorageLocations("SMME");
	}

	@Override
	public void reset() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		super.reset();
		getModemDriver().write("AT+CFUN=1\r");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT_AFTER_RESET);
		getModemDriver().clearBuffer();
	}
}
