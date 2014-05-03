
package org.smslib.helper;

import java.util.EventListener;

/**
 * Propagates serial port events.
 * <p>
 * <b>Please note: </b>This is a wrapper around
 * <code>javax.comm.SerialPortEventListener</code> (and so
 * <code>gnu.io.SerialPortEventListener</code>). The API definition is taken
 * from Sun. So honor them!
 * </p>
 * 
 * @author Jagane Sundar
 */
public interface SerialPortEventListener extends EventListener
{
	/**
	 * Propagates a <code>SerialPortEvent</code> event.
	 * 
	 * @param ev
	 */
	public void serialEvent(SerialPortEvent ev);
}
