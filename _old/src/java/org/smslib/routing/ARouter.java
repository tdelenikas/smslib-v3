/**
 * 
 */
package org.smslib.routing;

import java.util.ArrayList;
import java.util.Collection;

import org.smslib.AGateway;
import org.smslib.OutboundMessage;
import org.smslib.AGateway.GatewayStatuses;

/**
 * Base class for all possible Router implementations.
 * 
 * @author Bassam Al-Sarori
 *
 */
public abstract class ARouter {

	/**
	 * Performs basic routing. Selects gateways that are able to outbound messages and are started. If gatewayId is 
	 * specified in <code>msg</code> then only gateways with matching ids are selected. Before returning, the message
	 * and selected gateways are passed to <code>customRoute</code> method. 
	 * 
	 * @param msg message to be routed
	 * @param gateways a collection of gateways that will used for selecting appropriate gateways for routing.
	 * @return a collection of gateways that this <code>msg</code> should be routed through
	 */
	public Collection<AGateway> route(OutboundMessage msg, Collection<AGateway> gateways){
		ArrayList<AGateway> candidates = new ArrayList<AGateway>();
		for (AGateway gtw : gateways)
			if ((gtw.isOutbound()) && (gtw.getStatus() == GatewayStatuses.STARTED))
			{
				if (msg.getGatewayId().equalsIgnoreCase("*")) candidates.add(gtw);
				else if (msg.getGatewayId().equalsIgnoreCase(gtw.getGatewayId())) candidates.add(gtw);
			}
		return customRoute(msg, candidates);
	}
	
	/**
	 * Performs custom routing.
	 * 
	 * @param msg message to be routed
	 * @param gateways a collection of gateways that will used for selecting appropriate gateways for routing.
	 * @return a collection of gateways that this <code>msg</code> should be routed through
	 */
	public abstract Collection<AGateway> customRoute(OutboundMessage msg, Collection<AGateway> gateways);
}
