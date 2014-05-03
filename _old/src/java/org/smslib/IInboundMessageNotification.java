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

package org.smslib;

import org.smslib.Message.MessageTypes;

/**
 * Interface of the callback class used by SMSLib. SMSLib will call this method
 * when it detects an inbound message.
 * 
 * @see Service#setInboundMessageNotification(IInboundMessageNotification)
 */
public interface IInboundMessageNotification
{
	/**
	 * This method will be called by SMSLib upon receiving an sms message.
	 * 
	 * @param gateway
	 *            The gateway which received the message.
	 * @param msgType
	 *            The message type.
	 * @param msg
	 *            The message received.
	 */
	void process(final AGateway gateway, final MessageTypes msgType, final InboundMessage msg);
}
