// SMSLib for Java v4
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

package org.smslib.queues;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.smslib.OutboundMessage;

/**
 * @author Bassam Al-Sarori
 * @since 3.5
 */
public class ScheduledOutboundMessage implements Delayed
{
	private OutboundMessage message;

	public ScheduledOutboundMessage(OutboundMessage message)
	{
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Delayed#getDelay(java.util.concurrent.TimeUnit)
	 */
	@Override
	public long getDelay(TimeUnit unit)
	{
		return unit.convert(message.getDeliveryDelay(), TimeUnit.MILLISECONDS);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Delayed o)
	{
		if (message.getDeliveryDelay() < ((ScheduledOutboundMessage) o).message.getDeliveryDelay()) return -1;
		if (message.getDeliveryDelay() > ((ScheduledOutboundMessage) o).message.getDeliveryDelay()) return 1;
		return 0;
	}

	public OutboundMessage getMessage()
	{
		return message;
	}
}
