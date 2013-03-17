// SendMessage.java - Sample application.
//
// SMPP Gateway used: JSMPP (http://code.google.com/p/jsmpp/)

package examples.smpp.jsmpp;

import org.smslib.AGateway;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.smpp.BindAttributes;
import org.smslib.smpp.BindAttributes.BindType;
import org.smslib.smpp.jsmpp.JSMPPGateway;

/**
 * @author Bassam Al-Sarori
 */
public class SendMessage
{
	public void doIt() throws Exception
	{
		GatewayStatusNotification statusNotification = new GatewayStatusNotification();
		OutboundNotification outboundNotification = new OutboundNotification();
		OutboundMessage msg;
		System.out.println("Example: Send messages through SMPP using JSMPP.");
		System.out.println(Library.getLibraryDescription());
		System.out.println("Version: " + Library.getLibraryVersion());
		JSMPPGateway gateway = new JSMPPGateway("smppcon", "localhost", 2715, new BindAttributes("smppclient1", "password", "cp", BindType.TRANSMITTER));
		Service.getInstance().addGateway(gateway);
		Service.getInstance().setGatewayStatusNotification(statusNotification);
		Service.getInstance().setOutboundMessageNotification(outboundNotification);
		Service.getInstance().startService();
		// Send a message.
		msg = new OutboundMessage("+967712831950", "Hello from SMSLib and JSMPP");
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

	public class OutboundNotification implements IOutboundMessageNotification
	{
		public void process(AGateway gateway, OutboundMessage msg)
		{
			System.out.println("Outbound handler called from Gateway: " + gateway.getGatewayId());
			System.out.println(msg);
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
