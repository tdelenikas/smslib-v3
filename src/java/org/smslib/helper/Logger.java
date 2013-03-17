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

import java.io.File;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;

public class Logger
{
	private static Logger logger = new Logger();

	org.apache.log4j.Logger log4jLogger;

	private static final String FQCN = Logger.class.getName();

	protected Logger()
	{
		if (System.getProperties().getProperty("java.vm.name").equalsIgnoreCase("ikvm.net"))
		{
			File f = new File("log4j.properties");
			if (!f.exists()) log4jLogger = null;
			else
			{
				log4jLogger = org.apache.log4j.Logger.getLogger("smslib");
				PropertyConfigurator.configure("log4j.properties");
			}
		}
		else
		{
			log4jLogger = org.apache.log4j.Logger.getLogger("smslib");
			//PropertyConfigurator.configure("log4j.properties");
		}
	}

	public static Logger getInstance()
	{
		if(Logger.logger == null) Logger.logger = new Logger();
		return Logger.logger;
	}

	public static void setInstance(Logger logger)
	{
		Logger.logger = logger;
	}

	public void logInfo(String message, Exception e, String gatewayId)
	{
		if (log4jLogger == null) return;
		log4jLogger.log(FQCN, Level.INFO, formatMessage(message, gatewayId), e);
	}

	public void logWarn(String message, Exception e, String gatewayId)
	{
		if (log4jLogger == null) return;
		log4jLogger.log(FQCN, Level.WARN, formatMessage(message, gatewayId), e);
	}

	public void logDebug(String message, Exception e, String gatewayId)
	{
		if (log4jLogger == null) return;
		log4jLogger.log(FQCN, Level.DEBUG, formatMessage(message, gatewayId), e);
	}

	public void logError(String message, Exception e, String gatewayId)
	{
		if (log4jLogger == null) return;
		log4jLogger.log(FQCN, Level.ERROR, formatMessage(message, gatewayId), e);
	}

	private String formatMessage(String message, String gatewayId)
	{
		return ((gatewayId == null) ? message : "GTW: " + gatewayId + ": " + message);
	}
}
