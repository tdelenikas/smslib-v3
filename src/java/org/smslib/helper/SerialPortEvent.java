
package org.smslib.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A serial port event.
 * <p>
 * <b>Please note: </b>This is a wrapper around
 * <code>javax.comm.SerialPortEvent</code> (and so
 * <code>gnu.io.SerialPortEvent</code>). The API definition is taken from
 * Sun. So honor them!
 * </p>
 * 
 * @author Jagane Sundar
 */
public class SerialPortEvent
{
	static private Class<?> classSerialPortEvent;

	/** Break interrupt. */
	public static final int BI;

	/** Carrier detect. */
	public static final int CD;

	/** Clear to send. */
	public static final int CTS;

	/** Data available at the serial port. */
	public static final int DATA_AVAILABLE;

	/** Data set ready. */
	public static final int DSR;

	/** Framing error. */
	public static final int FE;

	/** Overrun error. */
	public static final int OE;

	/** Output buffer is empty. */
	public static final int OUTPUT_BUFFER_EMPTY;

	/** Parity error. */
	public static final int PE;

	/** Ring indicator. */
	public static final int RI;
	static
	{
		try
		{
			classSerialPortEvent = Class.forName("javax.comm.SerialPortEvent");
		}
		catch (ClassNotFoundException e1)
		{
			try
			{
				classSerialPortEvent = Class.forName("gnu.io.SerialPortEvent");
			}
			catch (ClassNotFoundException e2)
			{
				throw new RuntimeException("SerialPortEvent class not found");
			}
		}
		try
		{
			// get the value of constants
			Field f;
			f = classSerialPortEvent.getField("BI");
			BI = f.getInt(null);
			f = classSerialPortEvent.getField("CD");
			CD = f.getInt(null);
			f = classSerialPortEvent.getField("CTS");
			CTS = f.getInt(null);
			f = classSerialPortEvent.getField("DATA_AVAILABLE");
			DATA_AVAILABLE = f.getInt(null);
			f = classSerialPortEvent.getField("DSR");
			DSR = f.getInt(null);
			f = classSerialPortEvent.getField("FE");
			FE = f.getInt(null);
			f = classSerialPortEvent.getField("OE");
			OE = f.getInt(null);
			f = classSerialPortEvent.getField("OUTPUT_BUFFER_EMPTY");
			OUTPUT_BUFFER_EMPTY = f.getInt(null);
			f = classSerialPortEvent.getField("PE");
			PE = f.getInt(null);
			f = classSerialPortEvent.getField("RI");
			RI = f.getInt(null);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private Object realObject;

	/**
	 * Constructs a <code>SerialPortEvent</code> with the specified serial
	 * port, event type, old and new values. Application programs should not
	 * directly create <code>SerialPortEvent</code> objects.
	 * 
	 * @param srcport
	 *            source parallel port
	 * @param eventtype
	 *            event type
	 * @param oldvalue
	 *            old value
	 * @param newvalue
	 *            new value
	 */
	public SerialPortEvent(SerialPort srcport, int eventtype, boolean oldvalue, boolean newvalue)
	{
		if (classSerialPortEvent == null) { throw new RuntimeException("SerialPortEvent class not found"); }
		try
		{
			Constructor<?> constr = classSerialPortEvent.getConstructor(SerialPort.class, int.class, boolean.class, boolean.class);
			this.realObject = constr.newInstance(srcport, eventtype, oldvalue, newvalue);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	SerialPortEvent(Object obj)
	{
		if (classSerialPortEvent == null) { throw new RuntimeException("SerialPortEvent class not found"); }
		this.realObject = obj;
	}

	/**
	 * Gets the type of this event.
	 * 
	 * @return integer that can be equal to one of the following static
	 *         variables:
	 *         <code>BI, CD, CTS, DATA_AVAILABLE, DSR, FE, OE, OUTPUT_BUFFER_EMPTY, PE</code>
	 *         or <code>RI</code>.
	 * @since CommAPI 1.1
	 */
	public int getEventType()
	{
		int eventType;
		try
		{
			Method method = classSerialPortEvent.getMethod("getEventType", (java.lang.Class[]) null);
			eventType = (Integer) method.invoke(this.realObject);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(new RuntimeException(e.getTargetException().toString()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return eventType;
	}

	/**
	 * Gets the new value of the state change that caused the SerialPortEvent to
	 * be propagated. For example, when the CD bit changes, newValue reflects
	 * the new value of the CD bit.
	 */
	public boolean getNewValue()
	{
		boolean newValue;
		try
		{
			Method method = classSerialPortEvent.getMethod("getNewValue", (java.lang.Class[]) null);
			newValue = (Boolean) method.invoke(this.realObject);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(new RuntimeException(e.getTargetException().toString()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return newValue;
	}

	/**
	 * Gets the old value of the state change that caused the SerialPortEvent to
	 * be propagated. For example, when the CD bit changes, oldValue reflects
	 * the old value of the CD bit.
	 */
	public boolean getOldValue()
	{
		boolean oldValue;
		try
		{
			Method method = classSerialPortEvent.getMethod("getOldValue", (java.lang.Class[]) null);
			oldValue = (Boolean) method.invoke(this.realObject);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(new RuntimeException(e.getTargetException().toString()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return oldValue;
	}
}
