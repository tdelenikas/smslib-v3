// SendMessage.java - Sample application.
//
// This application shows you the basic procedure for sending messages.
// You will find how to send synchronous and asynchronous messages.
//
// For asynchronous dispatch, the example application sets a callback
// notification, to see what's happened with messages.

package org.ajwcc.pduUtils.test.integration;

import org.smslib.*;

public class SendPortedConcatTextMessage extends AbstractTester
{
	@Override
	public void test() throws Exception
	{
		OutboundMessage msg;
		// long message
		msg = new OutboundMessage(MODEM_NUMBER, "3 8 Thank you for using this service.  Your transaction has been logged as TXN 7 abcdef 7 Thank you for using this service.  Your transaction has been logged as TXN 7 abcdefz");
		// port of j2me app
		msg.setSrcPort(0);
		msg.setDstPort(4501);
		Service.getInstance().sendMessage(msg);
		System.out.println(msg);
		System.out.println("Now Sleeping - Hit <enter> to terminate.");
		System.in.read();
		Service.getInstance().stopService();
	}

	public static void main(String args[])
	{
		SendPortedConcatTextMessage app = new SendPortedConcatTextMessage();
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
