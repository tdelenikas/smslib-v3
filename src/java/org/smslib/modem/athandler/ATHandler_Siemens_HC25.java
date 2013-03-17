// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
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
//
//
//  Copyright (C) 2009, Mobileview GmbH, Hamburg/Germany.
//

package org.smslib.modem.athandler;

import java.io.IOException;
import org.smslib.GatewayException;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.modem.ModemGateway;

/**
 * 
 * A custom AT handler for the Siemens/Cinterion HC25 circumventing the problem
 * of the modem losing its SMSC address after issuing 'ATZ' or 'AT&F' by
 * reloading the address from the SIM card.
 * 
 */
public class ATHandler_Siemens_HC25 extends ATHandler {

	/**
	 * Construct a HC25 handler
	 * 
	 * @param myGateway
	 *            the gateway to use
	 */
	public ATHandler_Siemens_HC25(ModemGateway myGateway) {

		super(myGateway);

	}

	@Override
	public void sync() throws IOException, InterruptedException {

		getModemDriver().write("ATZ\r");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		// AT+CSCA? will reload the SMSC from SIM
		getModemDriver().write("AT+CSCA?\r");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);

	}

	@Override
	public void reset() throws TimeoutException, GatewayException, IOException, InterruptedException {

		getModemDriver().write("\u001b");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().write("+++");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().write("ATZ");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().write("AT+CSCA?\r");
		Thread.sleep(Service.getInstance().getSettings().AT_WAIT);
		getModemDriver().clearBuffer();

	}

}
