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

package org.smslib.notify;

import java.util.concurrent.LinkedBlockingQueue;
import org.smslib.Service;
import org.smslib.helper.Logger;
import org.smslib.threading.AServiceThread;

public class NotifyQueueManager
{
	NotificationQueueManager notificationQueueManager;

	LinkedBlockingQueue<Notification> notifyQueue;

	public NotifyQueueManager()
	{
		setNotifyQueue(new LinkedBlockingQueue<Notification>());
	}

	public void start()
	{
		setNotifyQueueManager(new NotificationQueueManager("NotifyQueueManager", 100));
	}

	public void cancel()
	{
		int counter = 0;
		int prevSize = 0;
		while (getNotifyQueue().size() != 0)
		{
			if (prevSize != getNotifyQueue().size())
			{
				prevSize = getNotifyQueue().size();
				counter = 0;
			}
			else if ((prevSize == getNotifyQueue().size()) && (counter == 25)) break;
			try
			{
				Thread.sleep(200);
			}
			catch (Exception e)
			{
				// Swallow this... its an artificial delay to drain queue of events...
			}
			counter++;
		}
		getNotifyQueueManager().cancel();
	}

	public LinkedBlockingQueue<Notification> getNotifyQueue()
	{
		return this.notifyQueue;
	}

	public void setNotifyQueue(LinkedBlockingQueue<Notification> notifyQueue)
	{
		this.notifyQueue = notifyQueue;
	}

	protected NotificationQueueManager getNotifyQueueManager()
	{
		return this.notificationQueueManager;
	}

	protected void setNotifyQueueManager(NotificationQueueManager notifyQueueManager)
	{
		this.notificationQueueManager = notifyQueueManager;
	}

	class NotificationQueueManager extends AServiceThread
	{
		public NotificationQueueManager(String name, int delay)
		{
			super(name, delay, 0, true);
		}

		@Override
		public void process() throws Exception
		{
			Logger.getInstance().logDebug("NotifyQueueManager running...", null, null);
			Notification notification = getNotifyQueue().take();
			if (notification instanceof GatewayStatusNotification)
			{
				if (Service.getInstance().getGatewayStatusNotification() != null)
				{
					GatewayStatusNotification n = (GatewayStatusNotification) notification;
					Service.getInstance().getGatewayStatusNotification().process(n.getGateway(), n.getOldStatus(), n.getNewStatus());
				}
			}
			else if (notification instanceof CallNotification)
			{
				if (Service.getInstance().getCallNotification() != null)
				{
					CallNotification n = (CallNotification) notification;
					Service.getInstance().getCallNotification().process(n.getGateway(), n.getCallerId());
				}
			}
			else if (notification instanceof InboundMessageNotification)
			{
				if (Service.getInstance().getInboundMessageNotification() != null)
				{
					InboundMessageNotification n = (InboundMessageNotification) notification;
					Service.getInstance().getInboundMessageNotification().process(n.getGateway(), n.getMsgType(), n.getMsg());
				}
			}
			else if (notification instanceof OutboundMessageNotification)
			{
				if (Service.getInstance().getOutboundMessageNotification() != null)
				{
					OutboundMessageNotification n = (OutboundMessageNotification) notification;
					Service.getInstance().getOutboundMessageNotification().process(n.getGateway(), n.getMsg());
				}
			}
			Logger.getInstance().logDebug("NotifyQueueManager end...", null, null);
		}
	}
}
