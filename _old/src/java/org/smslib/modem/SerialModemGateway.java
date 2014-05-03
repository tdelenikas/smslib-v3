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

/**
 * Gateway representing a GSM Modem or Phone connected via a serial port.
 */
public class SerialModemGateway extends ModemGateway
{
	/**
	 * Constructor for a serially connected gsm modem or phone.
	 * 
	 * @param id
	 *            Your own ID for addressing this gateway.
	 * @param comPort
	 *            The comm port to which this modem is connected. For example,
	 *            COM1 or /dev/ttyS1.
	 * @param baudRate
	 *            The baud rate of the serial connection.
	 * @param manufacturer
	 *            The manufacturer, for example "Nokia".
	 * @param model
	 *            The model, for example "6130"
	 */
	public SerialModemGateway(String id, String comPort, int baudRate, String manufacturer, String model)
	{
		super(ModemTypes.SERIAL, id, comPort, baudRate, manufacturer, model);
	}
}
