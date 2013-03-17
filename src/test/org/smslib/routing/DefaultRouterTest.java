package org.smslib.routing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;
import org.smslib.mocks.GatewayMock;

public class DefaultRouterTest {

	@Test
	public void testNoRoute() throws TimeoutException, GatewayException, IOException, InterruptedException {
		List<AGateway> gateways=new ArrayList<AGateway>();
		AGateway gateway=new GatewayMock("G1");
		gateways.add(gateway);
		gateway=new GatewayMock("G2");
		gateways.add(gateway);
		gateway=new GatewayMock("G3");
		gateways.add(gateway);
		
		ARouter router=new DefaultRouter();
		OutboundMessage message=new OutboundMessage("77374847", "test");
		
		Collection<AGateway> candidates= router.route(message, gateways);
		Assert.assertEquals("No route expected. No started gateways and no outbound gateways.", 0, candidates.size());
		
		gateway.setAttributes(AGateway.GatewayAttributes.SEND);
		gateway.setOutbound(true);
		message.setGatewayId("G3");
		
		gateway=gateways.get(0);
		gateway.startGateway();
			
		candidates= router.route(message, gateways);
		Assert.assertEquals("No route expected. No outbound gateways started.", 0, candidates.size());
		
	}
	
	@Test
	public void testOneRoute() throws TimeoutException, GatewayException, IOException, InterruptedException {
		List<AGateway> gateways=new ArrayList<AGateway>();
		AGateway gateway=new GatewayMock("G1");
		gateway.setAttributes(AGateway.GatewayAttributes.SEND);
		gateway.setOutbound(true);
		gateway.startGateway();
		
		gateways.add(gateway);
		
		gateway=new GatewayMock("G2");
		gateway.setAttributes(AGateway.GatewayAttributes.SEND);
		gateway.setOutbound(true);
		gateway.startGateway();
		
		gateways.add(gateway);
		
		gateway=new GatewayMock("G3");
		gateway.setAttributes(AGateway.GatewayAttributes.SEND);
		gateway.setOutbound(true);
		gateway.startGateway();
		
		gateways.add(gateway);
		
		ARouter router=new DefaultRouter();
		OutboundMessage message=new OutboundMessage("77374847", "test");
		message.setGatewayId("G1");
		
		Collection<AGateway> candidates= router.route(message, gateways);
		Assert.assertEquals("One gateway for message routing is expected.", 1, candidates.size());
		
		Assert.assertEquals("Message should be routed through G1 gateway.",candidates.contains(gateways.get(0)),true);
		
	}
	
	@Test
	public void testMultiRoute() throws TimeoutException, GatewayException, IOException, InterruptedException {
		List<AGateway> gateways=new ArrayList<AGateway>();
		AGateway gateway=new GatewayMock("G1");
		gateway.setAttributes(AGateway.GatewayAttributes.SEND);
		gateway.setOutbound(true);
		gateway.startGateway();
		
		gateways.add(gateway);
		
		gateway=new GatewayMock("G2");
		gateway.setAttributes(AGateway.GatewayAttributes.SEND);
		gateway.setOutbound(true);
		gateway.startGateway();
		
		gateways.add(gateway);
		
		gateway=new GatewayMock("G3");
		gateway.setAttributes(AGateway.GatewayAttributes.SEND);
		gateway.setOutbound(true);
		
		//this gateway is not started, therefore should not be returned by Router
		//gateway.startGateway();
		
		gateways.add(gateway);
		
		ARouter router=new DefaultRouter();
		OutboundMessage message=new OutboundMessage("77374847", "test");
		
		
		Collection<AGateway> candidates= router.route(message, gateways);
		Assert.assertEquals("Two gateways for message routing are expected.", 2, candidates.size());
		
		Assert.assertEquals("Message should not be routed through G3 gateway.",candidates.contains(gateways.get(2)),false);
		
	}

}
