/**
 * 
 */
package org.smslib.mocks;

import java.io.IOException;

import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.TimeoutException;

/**
 * A mock sub class of AGateway used for testing.
 * 
 * @author Bassam Al-Sarori
 *
 */
public class GatewayMock extends AGateway {

	public GatewayMock(String id) {
		super(id);
	}
	
	

	/* (non-Javadoc)
	 * @see org.smslib.AGateway#getQueueSchedulingInterval()
	 */
	@Override
	public int getQueueSchedulingInterval() {
		return 0;
	}



	@Override
	public void setStatus(GatewayStatuses myStatus) {
		this.status = myStatus;
	}



	@Override
	public void startGateway() throws TimeoutException, GatewayException,
			IOException, InterruptedException {
		setStatus(GatewayStatuses.STARTING);
		this.restartCount++;
		setStatus(GatewayStatuses.STARTED);
	}



	@Override
	public void stopGateway() throws TimeoutException, GatewayException,
			IOException, InterruptedException {
		setStatus(GatewayStatuses.STOPPING);
		setStatus(GatewayStatuses.STOPPED);
	}

}
