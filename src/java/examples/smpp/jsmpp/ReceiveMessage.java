// ReceiveMessage.java - Sample application.
//
// SMPP Gateway used: JSMPP (http://code.google.com/p/jsmpp/)

package examples.smpp.jsmpp;

import org.smslib.AGateway;
import org.smslib.IGatewayStatusNotification;
import org.smslib.IInboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Library;
import org.smslib.Service;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.Message.MessageTypes;
import org.smslib.smpp.BindAttributes;
import org.smslib.smpp.BindAttributes.BindType;
import org.smslib.smpp.jsmpp.JSMPPGateway;

/**
 * @author Bassam Al-Sarori
 */
public class ReceiveMessage
{
	public void doIt() throws Exception
	{
		System.out.println("Example: Receive messages through SMPP using JSMPP.");
		System.out.println(Library.getLibraryDescription());
		System.out.println("Version: " + Library.getLibraryVersion());
		JSMPPGateway gateway = new JSMPPGateway("smppcon", "localhost", 2715, new BindAttributes("smppclient1", "password", "cp", BindType.RECEIVER));
		Service.getInstance().setInboundMessageNotification(new InboundNotification());
		Service.getInstance().addGateway(gateway);
		Service.getInstance().setGatewayStatusNotification(new GatewayStatusNotification());
		Service.getInstance().startService();
		System.out.println("Now Sleeping - Hit <enter> to terminate.");
		System.in.read();
		Service.getInstance().stopService();
	}

	public static void main(String args[])
	{
		ReceiveMessage app = new ReceiveMessage();
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

	public class InboundNotification implements IInboundMessageNotification
	{
		public void process(AGateway gateway, MessageTypes msgType, InboundMessage msg)
		{
			if (msgType == MessageTypes.INBOUND) System.out.println(">>> New Inbound message detected from Gateway: " + gateway.getGatewayId());
			else if (msgType == MessageTypes.STATUSREPORT) System.out.println(">>> New Inbound Status Report message detected from Gateway: " + gateway.getGatewayId());
			System.out.println(msg);
		}
	}
}
