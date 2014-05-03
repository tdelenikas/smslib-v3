// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2012, Thanasis Delenikas, Athens/GREECE.
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.smslib.balancing;

import java.util.ArrayList;
import java.util.Collection;
import org.smslib.AGateway;
import org.smslib.OutboundMessage;

/**
 * RoundRobinLoadBalancer is forwarding messages via each gateway in turns. This
 * is the default SMSLib load balancer.
 */
public final class RoundRobinLoadBalancer extends LoadBalancer
{
	private int currentGateway;

	public RoundRobinLoadBalancer()
	{
		this.currentGateway = 0;
	}

	/**
	 * This Load Balancing implementation returns every other available gateway
	 * on each invocation.
	 */
	@Override
	public AGateway balance(OutboundMessage msg, Collection<AGateway> candidates)
	{
		int currentIndex;
		ArrayList<AGateway> c = new ArrayList<AGateway>(candidates);
		synchronized (this) {
			if (this.currentGateway >= c.size()) this.currentGateway = 0;
			currentIndex = this.currentGateway++;
		}
		
		return (c.get(currentIndex));
	}
}
