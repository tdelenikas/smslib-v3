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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.PriorityBlockingQueue;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.helper.Logger;

/**
 * @author Bassam Al-Sarori
 * @since 3.5
 */
public class DefaultQueueManager extends AbstractQueueManager
{
	public static final String MESSAGE_FILE_EXT = ".msg";

	private Map<String, PriorityBlockingQueue<OutboundMessage>> queueMap;

	private DelayQueue<ScheduledOutboundMessage> delayQueue;

	private String queueDirectory;

	private File pendingMessageDir;

	private File delayedMessageDir;

	public DefaultQueueManager()
	{
		super();
	}

	public DefaultQueueManager(String queueDirectory)
	{
		super();
		this.queueDirectory = queueDirectory;
	}

	public DefaultQueueManager(int delay)
	{
		super(delay);
	}

	public DefaultQueueManager(int delay, String queueDirectory)
	{
		super(delay);
		this.queueDirectory = queueDirectory;
	}

	@Override
	protected void init()
	{
		super.init();
		queueMap = new HashMap<String, PriorityBlockingQueue<OutboundMessage>>();
		delayQueue = new DelayQueue<ScheduledOutboundMessage>();
		if (queueDirectory == null)
		{
			queueDirectory = Service.getInstance().getSettings().QUEUE_DIRECTORY;
			if (queueDirectory == null)
			{
			Logger.getInstance().logInfo("Queue directory not defined. Queued messages will not be saved to filesystem.", null, null);
			return;
			}
		}
		File baseDir = new File(queueDirectory, "queue");
		pendingMessageDir = new File(baseDir, "pending");
		
		if (!pendingMessageDir.exists())
		{
			if (!pendingMessageDir.mkdirs())
			{
				Logger.getInstance().logError("Could not create directory for pending messages queue at "+pendingMessageDir.getPath(), null, null);
			}
		}
		else
		{
			Logger.getInstance().logDebug("loading pending messages..", null, null);
			loadPendingMessages();
		}
		delayedMessageDir = new File(baseDir, "delayed");
		if (!delayedMessageDir.exists())
		{
			if (!delayedMessageDir.mkdirs())
			{
				Logger.getInstance().logError("Could not create directory for delayed messages queue at "+delayedMessageDir.getPath(), null, null);
			}
		}
		else
		{
			Logger.getInstance().logDebug("loading delayed messages..", null, null);
			loadDelayedMessages();
		}
	}

	/* (non-Javadoc)
	 * @see org.smslib.queues.AbstractQueueManager#queueMessage(org.smslib.OutboundMessage)
	 */
	@Override
	public boolean queueMessage(OutboundMessage message)
	{
		if (message.getDeliveryDelay() > 0)
		{
			return addToDelayedQueue(message, true);
		}
		else
		{
			return addToGatewayQueue(message, true);
		}
	}

	/* (non-Javadoc)
	 * @see org.smslib.queues.AbstractQueueManager#removePendingMessage(org.smslib.OutboundMessage)
	 */
	@Override
	public boolean removePendingMessage(OutboundMessage message)
	{
		for (PriorityBlockingQueue<OutboundMessage> q : queueMap.values())
		{
			if (q.remove(message))
			{
				deletePendingMessage(message.getGatewayId(), message.getUuid());
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.smslib.queues.AbstractQueueManager#removePendingMessage(java.lang.String)
	 */
	@Override
	public boolean removePendingMessage(String messageUUID)
	{
		for (PriorityBlockingQueue<OutboundMessage> q : queueMap.values())
		{
			for (OutboundMessage m : q)
			{
				if (m.getId().equalsIgnoreCase(messageUUID))
				{
					if (q.remove(m))
					{
						deletePendingMessage(m.getGatewayId(), messageUUID);
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean addToGatewayQueue(OutboundMessage message, boolean store)
	{
		PriorityBlockingQueue<OutboundMessage> queue = queueMap.get(message.getGatewayId());
		if (queue == null)
		{
			queue = new PriorityBlockingQueue<OutboundMessage>(50, new PriorityComparator());
			queueMap.put(message.getGatewayId(), queue);
		}
		boolean queued = queue.add(message);
		if (store && queued) storePendingMessage(message);
		return queued;
	}

	private boolean addToDelayedQueue(OutboundMessage message, boolean store)
	{
		boolean queued = delayQueue.add(new ScheduledOutboundMessage(message));
		if (store && queued) storeDelayedMessage(message);
		return queued;
	}

	private boolean storePendingMessage(OutboundMessage message)
	{
		if (queueDirectory == null){
			return true;
		}
		
		File gatewayDir = new File(pendingMessageDir, message.getGatewayId().replace("/", "."));
		if (!gatewayDir.exists())
		{
			if (!gatewayDir.mkdir())
			{
				Logger.getInstance().logError("Queue directory could be created for gateway "+message.getGatewayId()+". Could not create directory .."+gatewayDir.getPath(), null, null);
				return false;
			}
		}
		return serializeMessage(message, new File(gatewayDir, message.getUuid() + MESSAGE_FILE_EXT));
	}

	private boolean deletePendingMessage(String gatewayId, String messageUUID)
	{
		if (queueDirectory == null){
			return true;
		}
		return new File(new File(pendingMessageDir, gatewayId), messageUUID + MESSAGE_FILE_EXT).delete();
	}
	
	private boolean deletePendingMessages(String gatewayId)
	{
		if (queueDirectory == null){
			return true;
		}
		if(gatewayId==null)
			return emptyDirectory(pendingMessageDir,false);
		else
			return emptyDirectory(new File(pendingMessageDir, gatewayId),true);
	}

	private boolean storeDelayedMessage(OutboundMessage message)
	{
		if (queueDirectory == null){
			return true;
		}
		return serializeMessage(message, new File(delayedMessageDir, message.getUuid() + MESSAGE_FILE_EXT));
	}

	private boolean deleteDelayedMessage(String messageUUID)
	{
		if (queueDirectory == null){
			return true;
		}
		return new File(delayedMessageDir, messageUUID + MESSAGE_FILE_EXT).delete();
	}

	@Override
	public OutboundMessage pollDelayedMessage()
	{
		try
		{
			OutboundMessage message = delayQueue.take().getMessage();
			deleteDelayedMessage(message.getUuid());
			return message;
		}
		catch (InterruptedException e)
		{
			//ignored
		}
		return null;
	}

	@Override
	public OutboundMessage pollPendingMessage(String gatewayId)
	{
		PriorityBlockingQueue<OutboundMessage> queue = queueMap.get(gatewayId);
		if (queue == null) return null;
		OutboundMessage message = queue.poll();
		if (message != null) deletePendingMessage(gatewayId, message.getUuid());
		return message;
	}

	private boolean serializeMessage(OutboundMessage message, File toFile)
	{
		if (queueDirectory == null){
			return true;
		}
		
		if (toFile.exists())
		{
			Logger.getInstance().logError("Cannot save Message "+message.getUuid()+" File already exist.", null, null);
			return false;
		}
		ObjectOutputStream out = null;
		try
		{
			out = new ObjectOutputStream(new FileOutputStream(toFile));
			out.writeObject(message);
			out.close();
		}
		catch (IOException e)
		{
			Logger.getInstance().logError("Cannot save Message "+message.getUuid(), e, null);
			return false;
		}
		return true;
	}

	private OutboundMessage deserializeMessage(File fromFile)
	{
		
		if (!fromFile.exists())
		{
			Logger.getInstance().logError("File of queued message doesn't exist "+fromFile.getPath(), null, null);
			return null;
		}
		ObjectInputStream in = null;
		OutboundMessage message = null;
		try
		{
			in = new ObjectInputStream(new FileInputStream(fromFile));
			message = (OutboundMessage) in.readObject();
			in.close();
		}
		catch (IOException e)
		{
			Logger.getInstance().logError("Could not read queued message from file "+fromFile.getPath(), e, null);
			return null;
		}
		catch (ClassNotFoundException e)
		{
			Logger.getInstance().logError("Could not read queued message from file "+fromFile.getPath(), e, null);
			return null;
		}
		return message;
	}

	private void loadPendingMessages()
	{
		File[] pendingDirs = pendingMessageDir.listFiles();
		for (File pendingDir : pendingDirs)
		{
			if (pendingDir.isDirectory())
			{
				File[] pendingFiles = pendingDir.listFiles();
				for (File pendingFile : pendingFiles)
				{
					if (pendingFile.getName().endsWith(MESSAGE_FILE_EXT))
					{
						addToGatewayQueue(deserializeMessage(pendingFile), false);
					}
				}
			}
		}
	}

	private void loadDelayedMessages()
	{
		File[] delayedFiles = delayedMessageDir.listFiles();
		for (File delayedFile : delayedFiles)
		{
			if (delayedFile.getName().endsWith(MESSAGE_FILE_EXT))
			{
				OutboundMessage message = deserializeMessage(delayedFile);
				if (message.getDeliveryDelay() > 0) addToDelayedQueue(message, false);
				else
				{
					addToGatewayQueue(message, true);
					deleteDelayedMessage(message.getUuid());
				}
			}
		}
	}

	class PriorityComparator implements Comparator<OutboundMessage>
	{
		@Override
		public int compare(OutboundMessage x, OutboundMessage y)
		{
			int comp = y.getPriority() - x.getPriority();
			if (comp == 0) comp = x.getDate().compareTo(y.getDate());
			return comp;
		}
	}

	@Override
	public int delayedQueueSize(String gatewayId)
	{
		return delayQueue.size();
	}

	@Override
	public Collection<OutboundMessage> getDelayedMessages()
	{
		List<OutboundMessage> messages = new ArrayList<OutboundMessage>();
		for (ScheduledOutboundMessage scheduledOutboundMessage : delayQueue)
		{
			messages.add(scheduledOutboundMessage.getMessage());
		}
		return messages;
	}

	@Override
	public Collection<OutboundMessage> getPendingMessages(String gatewayId)
	{
		PriorityBlockingQueue<OutboundMessage> queue = queueMap.get(gatewayId);
		if (queue == null) return new ArrayList<OutboundMessage>();
		return new ArrayList<OutboundMessage>(queue);
	}

	@Override
	public int pendingQueueSize(String gatewayId)
	{
		PriorityBlockingQueue<OutboundMessage> queue = queueMap.get(gatewayId);
		if (queue == null) return 0;
		return queue.size();
	}

	@Override
	public boolean removeDelayedMessage(OutboundMessage message)
	{
		for (ScheduledOutboundMessage scheduledOutboundMessage : delayQueue)
		{
			if (message.equals(scheduledOutboundMessage.getMessage()))
			{
				if (delayQueue.remove(scheduledOutboundMessage))
				{
					deleteDelayedMessage(message.getUuid());
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean removeDelayedMessage(String messageUUID)
	{
		for (ScheduledOutboundMessage scheduledOutboundMessage : delayQueue)
		{
			if (messageUUID.equals(scheduledOutboundMessage.getMessage().getUuid()))
			{
				if (delayQueue.remove(scheduledOutboundMessage))
				{
					deleteDelayedMessage(messageUUID);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean removeAllDelayedMessages() {
		delayQueue.clear();
		if (queueDirectory == null){
			return true;
		}
		return emptyDirectory(delayedMessageDir,false);
	}

	@Override
	public boolean removeAllPendingMessages(String gatewayId) {
		PriorityBlockingQueue<OutboundMessage> queue = queueMap.get(gatewayId);
		if(queue!=null){			
			queue.clear();
			queueMap.remove(queue);
			deletePendingMessages(gatewayId);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAllPendingMessages() {
		queueMap.clear();
		deletePendingMessages(null);
		return false;
	}
	
	private boolean emptyDirectory(File dir,boolean removeDir){
		File[] pendingDirs = dir.listFiles();
		for (File file : pendingDirs)
		{
			if(file.isDirectory()){
				emptyDirectory(file,true);
			}else
			if(!file.delete()){
				return false;
			}
		}
		if(removeDir){
			return dir.delete();
		}
		return true;
	}
}
