// SendMessage.java - Sample application.
//
// (c) 2011, Velvetech, LLC (http://www.velvetech.com)
//
// This application shows you the basic procedure for sending messages.
// You will find how to send synchronous and asynchronous messages.
//
// For asynchronous dispatch, the example application sets a callback
// notification, to see what's happened with messages.
//
// Bulk Operator used: EzTexting (http://www.eztexting.com)
// Please look the EzTextingHTTPGateway documentation for details.

package examples.eztexting;

import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.http.EzTextingHTTPGateway;

public class SendMessage 
{	
	public void doIt() throws Exception
	{
		try
		{
			OutboundMessage msg;
			System.out.println("Example: Send message from ExTexting HTTP Interface.");
			System.out.println(Library.getLibraryDescription());
			System.out.println("Version: " + Library.getLibraryVersion());
			EzTextingHTTPGateway gateway = new EzTextingHTTPGateway("eztexting.http.1", "username", "password");
			gateway.setOutbound(true);
			Service.getInstance().addGateway(gateway);
			Service.getInstance().startService();											
			// Query the service to find out our credit balance.
			System.out.println("Remaining credit: " + gateway.queryBalance());
			// Send a message synchronously.
			msg = new OutboundMessage("0123456789", "Hello from SMSLib (EzTexting handler)");							
			Service.getInstance().sendMessage(msg);			
			System.out.println(msg);
			System.out.println("Now Sleeping - Hit <enter> to terminate.");			
			System.in.read();		
			Service.getInstance().stopService();
		}
		catch (Exception e)
		{
			System.out.print(e);
			e.printStackTrace();
		}
	}
	
	public static void main(String args[])
	{
		SendMessage app = new SendMessage();
		try
		{
			app.doIt();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}		
}
