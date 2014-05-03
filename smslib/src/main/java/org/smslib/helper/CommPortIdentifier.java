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

package org.smslib.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Communications port management.
 * <p>
 * <b>Please note: </b>This is a wrapper around
 * <code>javax.comm.CommPortIdentifier</code> (and so
 * <code>gnu.io.CommPortIdentifier</code>). The API definition is taken from
 * Sun. So honor them!
 * </p>
 * <code>CommPortIdentifier</code> is the central class for controlling access
 * to communications ports. It includes methods for:
 * </p>
 * <ul>
 * <li> Determining the communications ports made available by the driver. </li>
 * <li> Opening communications ports for I/O operations. </li>
 * <li> Determining port ownership. </li>
 * <li> Resolving port ownership contention. </li>
 * <li> Managing events that indicate changes in port ownership status. </li>
 * </ul>
 * <p>
 * An application first uses methods in <code>CommPortIdentifier</code> to
 * negotiate with the driver to discover which communication ports are available
 * and then select a port for opening. It then uses methods in other classes
 * like <code>CommPort</code>, <code>ParallelPort</code> and
 * <code>SerialPort</code> to communicate through the port.
 * </p> *
 * 
 * @author gwellisch
 */
public class CommPortIdentifier
{
	static private Class<?> classCommPortIdentifier;

	public static final int PORT_SERIAL;
	static
	{
		try
		{
			classCommPortIdentifier = Class.forName("javax.comm.CommPortIdentifier");
		}
		catch (ClassNotFoundException e1)
		{
			try
			{
				classCommPortIdentifier = Class.forName("gnu.io.CommPortIdentifier");
			}
			catch (ClassNotFoundException e2)
			{
				throw new RuntimeException("CommPortIdentifier class not found");
			}
		}
		try
		{
			Field f;
			f = classCommPortIdentifier.getField("PORT_SERIAL");
			PORT_SERIAL = f.getInt(null);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private Object realObject;

	protected CommPortIdentifier(Object myRealObject)
	{
		this.realObject = myRealObject;
	}

	/**
	 * Returns the port type.
	 * 
	 * @return portType - PORT_SERIAL or PORT_PARALLEL
	 */
	public int getPortType()
	{
		try
		{
			Method method = classCommPortIdentifier.getMethod("getPortType", (java.lang.Class[]) null);
			return (Integer) method.invoke(this.realObject);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(new RuntimeException(e.getTargetException().toString()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the name of the port.
	 * 
	 * @return the name of the port
	 */
	public String getName()
	{
		try
		{
			Method method = classCommPortIdentifier.getMethod("getName", (java.lang.Class[]) null);
			return (String) method.invoke(this.realObject);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(new RuntimeException(e.getTargetException().toString()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	// Note: using SerialPort instead of CommPort
	public SerialPort open(String appname, int timeout)
	{
		Class<?>[] paramTypes = new Class<?>[] { String.class, int.class };
		try
		{
			Method method = classCommPortIdentifier.getMethod("open", paramTypes);
			return new SerialPort(method.invoke(this.realObject, appname, timeout));
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(new RuntimeException(e.getTargetException().toString()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Obtains an enumeration object that contains a
	 * <code>CommPortIdentifier</code> object for each port in the system.
	 * 
	 * @return <code>Enumeration</code> that can be used to enumerate all the
	 *         ports known to the system
	 */
	public static Enumeration<CommPortIdentifier> getPortIdentifiers()
	{
		if (classCommPortIdentifier == null) { throw new RuntimeException("CommPortIdentifier class not found"); }
		Enumeration<CommPortIdentifier> list;
		try
		{
			// get the enumeration of real objects
			Method method = classCommPortIdentifier.getMethod("getPortIdentifiers", (java.lang.Class[]) null);
			CommPortIdentifier type = null;
			list = ReflectionHelper.invokeAndCastEnumeration(type, method, null);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		// wrap the real objects
		Vector<CommPortIdentifier> vec = new Vector<CommPortIdentifier>();
		while (list.hasMoreElements())
			vec.add(new CommPortIdentifier(list.nextElement()));
		return vec.elements();
	}

	/**
	 * Obtains a CommPortIdentifier object by using a port name. The port name
	 * may have been stored in persistent storage by the application.
	 * 
	 * @param portName
	 *            name of the port to open
	 * @return <code>CommPortIdentifier</code> object
	 * @throws RuntimeException
	 *             (wrapping a NoSuchPortException) if the port does not exist
	 */
	public static CommPortIdentifier getPortIdentifier(String portName)
	{
		if (classCommPortIdentifier == null) { throw new RuntimeException("CommPortIdentifier class not found"); }
		CommPortIdentifier port;
		try
		{
			//get the string of real objects
			Method method = classCommPortIdentifier.getMethod("getPortIdentifier", String.class);
			port = new CommPortIdentifier(method.invoke(null, portName));
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(new RuntimeException(e.getTargetException().toString()));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return port;
	}
}
