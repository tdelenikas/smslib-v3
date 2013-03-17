// SendMessage.java - Sample application.
//
// This application shows you the basic procedure for sending messages.
// You will find how to send synchronous and asynchronous messages.
//
// For asynchronous dispatch, the example application sets a callback
// notification, to see what's happened with messages.

package org.ajwcc.pduUtils.test.integration;

import java.net.*;
import org.smslib.*;

public class SendWapSIConcatMessage extends AbstractTester
{
	@Override
	public void test() throws Exception
	{
		// send out a WAP SI message.
		OutboundWapSIMessage wapMsg = new OutboundWapSIMessage(MODEM_NUMBER, new URL("https://mail.google.com/"), "1 Visit GMail now! 2 now! now! now! 3 now! now! now! 4 now! now! now! 5 now! now! now! 6 now! now! now! 7 now! now! now! 8 now! now! now! 9 now! now! now!");
		//OutboundWapSIMessage wapMsg = new OutboundWapSIMessage(MODEM_NUMBER,  new URL("https://mail.google.com/"), "1 Visit GMail now! 2 now! now! now! 3 now! now! now! 4 now! now! now! 5 now! now! now! 6 now! now! now! 7 now! now! now! 8 now! now! now! 9 now! now! now!");
		Service.getInstance().sendMessage(wapMsg);
		System.out.println(wapMsg);
		System.out.println("Now Sleeping - Hit <enter> to terminate.");
		System.in.read();
		Service.getInstance().stopService();
	}

	public static void main(String args[])
	{
		SendWapSIConcatMessage app = new SendWapSIConcatMessage();
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
