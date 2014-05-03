/**
 * 
 */
package org.smslib.balancing;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.smslib.AGateway;
import org.smslib.OutboundMessage;
import org.smslib.mocks.GatewayMock;

/**
 * @author balsarori
 *
 */
public class RoundRobinLoadBalancerTest {

	/**
	 * Test method for {@link org.smslib.balancing.RoundRobinLoadBalancer#balance(org.smslib.OutboundMessage, java.util.Collection)}.
	 */
	@Test
	public void testBalance() {
		List<AGateway> gateways=new ArrayList<AGateway>();
		
		gateways.add(new GatewayMock("G1"));	
		gateways.add(new GatewayMock("G2"));
		gateways.add(new GatewayMock("G3"));
		
		RoundRobinLoadBalancer balancer = new RoundRobinLoadBalancer();
		OutboundMessage message=new OutboundMessage("77374847", "test");
		
		Assert.assertEquals("Wrong gateway returned by balancer. G1 was expected.", balancer.balance(message, gateways),gateways.get(0));
		Assert.assertEquals("Wrong gateway returned by balancer. G2 was expected.", balancer.balance(message, gateways),gateways.get(1));
		Assert.assertEquals("Wrong gateway returned by balancer. G3 was expected.", balancer.balance(message, gateways),gateways.get(2));
		
		Assert.assertEquals("Wrong gateway returned by balancer after Round 1. G1 was expected.", balancer.balance(message, gateways),gateways.get(0));
		
	}

}
