// SendMessage.java - Sample application.
//
// This application shows you the basic procedure for sending messages.
// You will find how to send synchronous and asynchronous messages.
//
// For asynchronous dispatch, the example application sets a callback
// notification, to see what's happened with messages.

package org.ajwcc.pduUtils.test.integration;

import org.smslib.*;

public class SendBinaryMessage extends AbstractTester
{
	@Override
	public void test() throws Exception
	{
		// basic message
		OutboundBinaryMessage msg = new OutboundBinaryMessage(MODEM_NUMBER, "Hello from SMSLib!".getBytes());
		msg.setStatusReport(true);
		Service.getInstance().sendMessage(msg);
		System.out.println(msg);
		System.out.println("Now Sleeping - Hit <enter> to terminate.");
		System.in.read();
		Service.getInstance().stopService();
	}

	public static void main(String args[])
	{
		SendBinaryMessage app = new SendBinaryMessage();
		try
		{
			app.initModem();
			app.test();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
