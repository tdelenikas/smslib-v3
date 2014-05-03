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

/**
 * Interface of the callback class used by SMSLib. SMSLib will call this method
 * when it sends (or fails to send) an outbound message from its queue.
 * 
 * @see Service#setOutboundMessageNotification(IOutboundMessageNotification)
 */
public interface IOutboundMessageNotification
{
	/**
	 * This method will be called by SMSLib upon sending or when it failed to
	 * send a message. Please note that this method is only called when you send
	 * messages via SMSLib queue manager.
	 * 
	 * @param gateway
	 *            The gateway from which the message was sent.
	 * @param msg
	 *            The actual outbound message. This messages has its fields
	 *            updated according to whether SMSLib has manage to send it or
	 *            failed to send it.
	 */
	void process(final AGateway gateway, final OutboundMessage msg);
}
