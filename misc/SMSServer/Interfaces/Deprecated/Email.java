// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2008, Thanasis Delenikas, Athens/GREECE.
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

package org.smslib.smsserver.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.Message.MessageEncodings;
import org.smslib.helper.ExtStringBuilder;
import org.smslib.smsserver.SMSServer;

/**
 * Interface for Email communication with SMSServer. <br />
 * Inbound messages are send via SMTP. Outbound messages are received via POP3.
 * 
 * @author Sebastian Just
 */
public class Email extends Interface<Void>
{
	private Session mailSession;

	private String messageSubject;

	private String messageBody;

	public Email(String myInterfaceId, Properties myProps, SMSServer myServer, InterfaceTypes myType)
	{
		super(myInterfaceId, myProps, myServer, myType);
		setDescription("Interface for Email communication.");
	}

	@Override
	public void MessagesReceived(Collection<InboundMessage> msgList) throws Exception
	{
		for (InboundMessage im : msgList)
		{
			Message msg = new MimeMessage(this.mailSession);
			msg.setFrom();
			msg.addRecipient(RecipientType.TO, new InternetAddress(getProperty("to")));
			msg.setSubject(updateTemplateString(this.messageSubject, im));
			if (this.messageBody != null)
			{
				msg.setText(updateTemplateString(this.messageBody, im));
			}
			else
			{
				msg.setText(im.toString());
			}
			msg.setSentDate(im.getDate());
			Transport.send(msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.smslib.smsserver.AInterface#getMessagesToSend()
	 */
	@Override
	public Collection<OutboundMessage> getMessagesToSend() throws Exception
	{
		List<OutboundMessage> retValue = new ArrayList<OutboundMessage>();
		Store s = this.mailSession.getStore();
		s.connect();
		Folder inbox = s.getFolder(getProperty("mailbox_name", "INBOX"));
		inbox.open(Folder.READ_WRITE);
		for (Message m : inbox.getMessages())
		{
			OutboundMessage om = new OutboundMessage(m.getSubject(), m.getContent().toString());
			om.setFrom(m.getFrom().toString());
			om.setDate(m.getReceivedDate());
			retValue.add(om);
			// Delete message from inbox
			m.setFlag(Flags.Flag.DELETED, true);
		}
		inbox.close(true);
		s.close();
		return retValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.smslib.smsserver.AInterface#start()
	 */
	@Override
	public void start() throws Exception
	{
		Properties mailProps = new Properties();
		mailProps.setProperty("mail.store.protocol", getProperty("mailbox_protocol"));
		if ("pop3".equals(getProperty("mailbox_protocol")))
		{
			mailProps.setProperty("mail.pop3.host", getProperty("mailbox_host"));
			mailProps.setProperty("mail.pop3.port", getProperty("mailbox_port"));
			mailProps.setProperty("mail.pop3.user", getProperty("mailbox_user"));
			mailProps.setProperty("mail.pop3.password", getProperty("mailbox_password"));
		}
		else if ("pop3s".equals(getProperty("mailbox_protocol")))
		{
			mailProps.setProperty("mail.pop3s.host", getProperty("mailbox_host"));
			mailProps.setProperty("mail.pop3s.port", getProperty("mailbox_port"));
			mailProps.setProperty("mail.pop3s.user", getProperty("mailbox_user"));
			mailProps.setProperty("mail.pop3s.password", getProperty("mailbox_password"));
		}
		else if ("imap".equals(getProperty("mailbox_protocol")))
		{
			mailProps.setProperty("mail.imap.host", getProperty("mailbox_host"));
			mailProps.setProperty("mail.imap.port", getProperty("mailbox_port"));
			mailProps.setProperty("mail.imap.user", getProperty("mailbox_user"));
			mailProps.setProperty("mail.imap.password", getProperty("mailbox_password"));
		}
		else if ("imaps".equals(getProperty("mailbox_protocol")))
		{
			mailProps.setProperty("mail.imaps.host", getProperty("mailbox_host"));
			mailProps.setProperty("mail.imaps.port", getProperty("mailbox_port"));
			mailProps.setProperty("mail.imaps.user", getProperty("mailbox_user"));
			mailProps.setProperty("mail.imaps.password", getProperty("mailbox_password"));
		}
		else
		{
			throw new IllegalArgumentException("mailbox_protocol have to be pop3(s) or imap(s)!");
		}
		mailProps.setProperty("mail.transport.protocol", "smtp");
		mailProps.setProperty("mail.from", getProperty("from"));
		mailProps.setProperty("mail.smtp.host", getProperty("smtp_host"));
		mailProps.setProperty("mail.smtp.port", getProperty("smtp_port"));
		mailProps.setProperty("mail.smtp.user", getProperty("smtp_user"));
		mailProps.setProperty("mail.smtp.password", getProperty("smtp_password"));
		mailProps.setProperty("mail.smtp.auth", "true");
		this.mailSession = Session.getInstance(mailProps, new javax.mail.Authenticator()
		{
			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(getProperty("mailbox_user"), getProperty("mailbox_password"));
			}
		});
		if (isOutbound())
		{
			prepareEmailTemplate();
		}
		super.start();
	}

	private String updateTemplateString(String template, InboundMessage msg)
	{
		ExtStringBuilder sb = new ExtStringBuilder(template);
		sb.replaceAll("%gatewayId%", msg.getGatewayId());
		sb.replaceAll("%encoding%", (msg.getEncoding() == MessageEncodings.ENC7BIT ? "7-bit" : (msg.getEncoding() == MessageEncodings.ENC8BIT ? "8-bit" : "UCS2 (Unicode)")));
		sb.replaceAll("%date%", msg.getDate().toString());
		sb.replaceAll("%text%", msg.getText());
		sb.replaceAll("%pduUserData%", msg.getPduUserData());
		sb.replaceAll("%originator%", msg.getOriginator());
		sb.replaceAll("%memIndex%", msg.getMemIndex());
		sb.replaceAll("%mpMemIndex%", msg.getMpMemIndex());
		return sb.toString();
	}

	private void prepareEmailTemplate()
	{
		this.messageSubject = getProperty("message_subject");
		if (this.messageSubject == null ||this. messageSubject.length() == 0)
		{
			getService().getLogger().logWarn("No message_subject found - Using default", null, null);
			this.messageSubject = "SMS from %ORIGINATOR%";
		}
		File f = new File(getProperty("message_body"));
		if (f.canRead())
		{
			try
			{
				Reader r = new FileReader(f);
				BufferedReader br = new BufferedReader(r);
				String line = null;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null)
				{
					sb.append(line);
				}
				this.messageBody = sb.toString();
			}
			catch (IOException e)
			{
				getService().getLogger().logError("I/O-Exception while reading message body template: " + e.getMessage(), null, null);
			}
		}
		if (this.messageBody == null || this.messageBody.length() == 0)
		{
			getService().getLogger().logWarn("message_body can't be read or is empty - Using default", null, null);
			this.messageBody = null;
		}
	}
}
