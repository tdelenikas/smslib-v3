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

/**
 * This handler supports Ubinecits GDC201 GSM PCMCIA cards. Ubineitcs is
 * insolvent, but so they are cheap. <br />
 * The GSM AT Set is available from the Ubinetics vendor Expansys:
 * {http://i.expansys.com/i/drv/ubinetics-at-command-set.pdf}
 * 
 * @author Sebastian Just
 */
public class ATHandler_Ubinetics_GDC201 extends ATHandler
{
	public ATHandler_Ubinetics_GDC201(ModemGateway myGateway)
	{
		super(myGateway);
		/** "SM", SIM is the only supported storage type. */
		setStorageLocations("SM");
	}

	/**
	 * Sets the storage location to the Ubinetics-required
	 * "SM","SM","SM"-location.
	 * 
	 * @param mem
	 *            takes the value of storageLocations
	 * @return returns the result from
	 *         {@link org.smslib.modem.AModemDriver#isOk()}
	 */
	@Override
	public boolean switchStorageLocation(String mem) throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		/*
		 * Command: +CPMS? Response: +CPMS: <mem1>,<used1>,<total1>,<mem2>,<used2>,<total2>,<mem3>,<used3>,<total3>
		 * Command: AT+CPMS=<mem1>,<mem2>,<mem3> Response: +CPMS: <used1>,<total1>,<used2>,<total2>,<used3>,<total3>
		 */
		getModemDriver().write("AT+CPMS=\"" + mem + "\",\"" + mem + "\",\"" + mem + "\"\r");
		getModemDriver().getResponse();
		return (getModemDriver().isOk());
	}
}
