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

public class ATHandler_Siemens_MC35i_FD extends ATHandler_Siemens_MC35i
{
	public ATHandler_Siemens_MC35i_FD(ModemGateway myGateway)
	{
		super(myGateway);
	}

	@Override
	public void sync() throws IOException, InterruptedException
	{
		getModemDriver().write("AT&F\r");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
	}

	@Override
	public void reset() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		getModemDriver().write("\u001b");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().write("+++");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().write("AT&F");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().clearBuffer();
	}
}
