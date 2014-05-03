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

import java.util.Collection;
import org.smslib.OutboundMessage;
import org.smslib.helper.Logger;
import org.smslib.threading.AServiceThread;

/**
 * Base class for all Queue Management implementations. Queue Managers are used
 * to save <code>OutboundMessage</code> that are scheduled for delivery in the
 * future. Gateways also use Queue Managers in order to send pending
 * <code>OutboundMessage</code>.
 * 
 * @author Bassam Al-Sarori
 * @since 3.5
 */
public abstract class AbstractQueueManager
{
	protected int queueDelay;

	private DelayQueueManager delayQueueManager;

	public AbstractQueueManager()
	{
		this(200);
	}

	public AbstractQueueManager(int queueDelay)
	{
		this.queueDelay = queueDelay;
		init();
	}

	/**
	 * Called after construction. Sub classes can override this method in order
	 * to do any initialization that may be required.
	 */
	protected void init()
	{
	}

	/**
	 * Queues the <code>message</code>. If
	 * <code>message.getDeliveryDelay > 0</code> then it is queued for later
	 * delivery else it is added to the gateway's pending queue specified by
	 * <code>message.getGatewayId</code>.
	 * 
	 * @param message
	 * @return if the <code>message</code> was queued, false otherwise.
	 */
	public abstract boolean queueMessage(OutboundMessage message);

	public abstract boolean removePendingMessage(OutboundMessage message);

	public abstract boolean removePendingMessage(String messageUUID);

	public abstract boolean removeDelayedMessage(OutboundMessage message);

	public abstract boolean removeDelayedMessage(String messageUUID);
	
	public abstract boolean removeAllPendingMessages(String gatewayId);
	
	public abstract boolean removeAllPendingMessages();
	public abstract boolean removeAllDelayedMessages();

	public abstract OutboundMessage pollDelayedMessage();

	public abstract OutboundMessage pollPendingMessage(String gatewayId);

	public abstract Collection<OutboundMessage> getPendingMessages(String gatewayId);

	public abstract int pendingQueueSize(String gatewayId);

	public abstract Collection<OutboundMessage> getDelayedMessages();

	public abstract int delayedQueueSize(String gatewayId);

	public int getQueueDelay()
	{
		return queueDelay;
	}

	public void setQueueDelay(int queueDelay)
	{
		this.queueDelay = queueDelay;
	}

	public void start()
	{
		if (delayQueueManager == null || delayQueueManager.isCanceled())
		{
			delayQueueManager = new DelayQueueManager("DelayQueueManager", queueDelay);
		}
	}

	public void stop()
	{
		if (delayQueueManager != null)
		{
			delayQueueManager.cancel();
		}
	}

	public void pause()
	{
		if (delayQueueManager != null)
		{
			delayQueueManager.disable();
		}
	}

	public void resume()
	{
		if (delayQueueManager != null)
		{
			delayQueueManager.enable();
		}
	}

	//TODO getStatus
	class DelayQueueManager extends AServiceThread
	{
		public DelayQueueManager(String name, int delay)
		{
			super(name, delay, 0, true);
		}

		@Override
		public void process() throws Exception
		{
			Logger.getInstance().logDebug("DelayQueueManager running...", null, null);
			OutboundMessage message = pollDelayedMessage();
			if (message != null) queueMessage(message);
			//queueMessage(getDelayQueue().take().getMsg());
			Logger.getInstance().logDebug("DelayQueueManager end...", null, null);
		}
	}
}
