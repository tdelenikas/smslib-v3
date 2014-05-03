// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// (c) 2011, Velvetech, LLC (http://www.velvetech.com)
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

public class EzTextingOutboundMessage extends OutboundMessage 
{
	private static final long serialVersionUID = 1L;

	private boolean express = true;
	private String subject = "";  

	/**
	 * EzTexting Outbound message constructor. This parameterless constructor creates an
	 * empty outbound message.
	 * 
	 * @see #EzTextingOutboundMessage(String, String)
	 * @see #EzTextingOutboundMessage(String, String, String, boolean)
	 */	
	public EzTextingOutboundMessage()
	{
		super();
	}

	/**
	 * EzTexting Outbound message constructor.
	 * 
	 * @param myRecipient
	 *            The recipient of the message.
	 * @param text
	 *            The text of the message.
	 */	
	public EzTextingOutboundMessage(String myRecipient, String text)
	{
		super(myRecipient, text);	
	}

	/**
	 * EzTexting Outbound message constructor.
	 * 
	 * @param myRecipient
	 *            The recipient of the message.
	 * @param text
	 *            The text of the message.
	 * @param subject
	 *            The subject of the message.
	 * @param express
	 *            Express delivery method of the message.           
	 */		
	public EzTextingOutboundMessage(String myRecipient, String text, String subject, boolean express)
	{
		super(myRecipient, text);
		setSubject(subject);
		setExpress(express);
	}	
	
	/**
	 * Set Express delivery method of the message.
	 * 
	 * @param express
	 *            Express delivery method of the message.
	 * @see #isExpress()
	 */		
	public void setExpress(boolean express) {
		this.express = express;
	}
	
	/**
	 * Returns true if Use Express delivery method of the message.
	 * 
	 * @return True if Use Express delivery method of the message.
	 */
	public boolean isExpress() {
		return express;
	}

	/**
	 * Set the subject of the message.
	 * 
	 * @param subject
	 *            The subject of the message.
	 * @see #getSubject()
	 */		
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Return the subject of the message.
	 * 
	 * @return the subject of the message.
	 */
	public String getSubject() {
		return subject;
	}
	
	@Override
	public String toString()
	{
		String str;
		str = super.toString();
		str += "\n";
		str += " Express: " + isExpress();		
		str += "\n";
		str += " Subject: " + getSubject();
		str += "\n";
		str += "===============================================================================";
		str += "\n";		
		return str;
	}	
	
}
