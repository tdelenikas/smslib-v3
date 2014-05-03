// SendMessage.java - Sample application.
//
// Gateway used: Kannel (http://www.kannel.org)
// Please look the KannelHTTPGateway documentation for details.

package examples.kannel;

import org.smslib.AGateway;
import org.smslib.IGatewayStatusNotification;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.http.KannelHTTPGateway;

/**
 * @author Bassam Al-Sarori
 */
public class SendMessage
{
	public void doIt() throws Exception
	{
		GatewayStatusNotification statusNotification = new GatewayStatusNotification();
		OutboundMessage msg;
		System.out.println("Example: Send message through Kannel HTTP Interface.");
		System.out.println(Library.getLibraryDescription());
		System.out.println("Version: " + Library.getLibraryVersion());
		KannelHTTPGateway gateway = new KannelHTTPGateway("mysmsc", "http://localhost:13013/cgi-bin/sendsms", "simple", "elpmis");
		// Uncomment in order gateway to start and stop SMSC automatically on Kannel
		//gateway.setAutoStartSmsc(true);
		//gateway.setAutoStopSmsc(true);
		// Set Kannel's Admin URL and password to be used starting, stopping and checking SMSC status   
		gateway.setAdminUrl("http://localhost:13000");
		gateway.setAdminPassword("bar");
		gateway.setOutbound(true);
		Service.getInstance().addGateway(gateway);
		Service.getInstance().setGatewayStatusNotification(statusNotification);
		Service.getInstance().startService();
		// Send a message.
		msg = new OutboundMessage("+967712831950", "Hello from SMSLib (Kannel handler)");
		//msg.setEncoding(MessageEncodings.ENCUCS2);
		Service.getInstance().sendMessage(msg);
		System.out.println(msg);
		System.out.println("Now Sleeping - Hit <enter> to terminate.");
		System.in.read();
		Service.getInstance().stopService();
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

	public class GatewayStatusNotification implements IGatewayStatusNotification
	{
		public void process(AGateway gateway, GatewayStatuses oldStatus, GatewayStatuses newStatus)
		{
			System.out.println(">>> Gateway Status change for " + gateway.getGatewayId() + ", OLD: " + oldStatus + " -> NEW: " + newStatus);
		}
	}
}
