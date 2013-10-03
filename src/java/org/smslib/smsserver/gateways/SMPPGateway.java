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

package org.smslib.smsserver.gateways;

import java.lang.reflect.Constructor;
import java.util.Properties;

import org.smslib.smpp.AbstractSMPPGateway;
import org.smslib.smpp.Address;
import org.smslib.smpp.BindAttributes;
import org.smslib.smpp.Address.NumberingPlanIndicator;
import org.smslib.smpp.Address.TypeOfNumber;
import org.smslib.smpp.BindAttributes.BindType;

/**
 * <b>SMSServer Application Gateway.</b>
 * 
 * @author Bassam Al-Sarori
 */
public class SMPPGateway extends AGateway
{
	public SMPPGateway(String myGatewayId, Properties myProps, org.smslib.smsserver.SMSServer myServer)
	{
		super(myGatewayId, myProps, myServer);
		setDescription(myGatewayId+" SMPP Gateway.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void create() throws Exception
	{
		String implClass=getProperty("impl");
		Class<AbstractSMPPGateway> clazz=(Class<AbstractSMPPGateway>) Class.forName(implClass);
		Class<?>[] classArgs=new Class[]{String.class,String.class,int.class,BindAttributes.class};
		Constructor<AbstractSMPPGateway> con= clazz.getConstructor(classArgs);
		
		String host=getProperty("host");
		Integer port=Integer.parseInt(getProperty("port"));
		Object args[]=new Object[]{getGatewayId(),host,port,getBindAttributes()};
		AbstractSMPPGateway gateway=con.newInstance(args);
		String enquireLink=getProperty("enquirelink");
		if(enquireLink!=null && !enquireLink.isEmpty()){
			gateway.setEnquireLink(Integer.parseInt(enquireLink));
		}
		String ton=getProperty("sourceton");
		TypeOfNumber typeOfNumber=(ton==null)?TypeOfNumber.UNKNOWN:TypeOfNumber.valueOf(Byte.parseByte(ton));
		
		String npi=getProperty("sourcenpi");
		NumberingPlanIndicator numberingPlanIndicator=(npi==null)?NumberingPlanIndicator.UNKNOWN:NumberingPlanIndicator.valueOf(Byte.parseByte(npi));
		
		gateway.setSourceAddress(new Address(typeOfNumber, numberingPlanIndicator));
		
		ton=getProperty("destton");
		typeOfNumber=(ton==null)?TypeOfNumber.UNKNOWN:TypeOfNumber.valueOf(Byte.parseByte(ton));
		
		npi=getProperty("destnpi");
		numberingPlanIndicator=(npi==null)?NumberingPlanIndicator.UNKNOWN:NumberingPlanIndicator.valueOf(Byte.parseByte(npi));
		
		
		gateway.setDestinationAddress(new Address(typeOfNumber, numberingPlanIndicator));
		setGateway(gateway);
		
	
	}
	
	private BindAttributes getBindAttributes(){
		String systemId=getProperty("systemid");
		String password=getProperty("password");
		String systemType=getProperty("systemtype");
		BindType bindType=BindType.getByShortName(getProperty("bindtype"));
		
		String ton=getProperty("bindton");
		TypeOfNumber typeOfNumber=(ton==null)?TypeOfNumber.UNKNOWN:TypeOfNumber.valueOf(Byte.parseByte(ton));
		
		String npi=getProperty("bindnpi");
		NumberingPlanIndicator numberingPlanIndicator=(npi==null)?NumberingPlanIndicator.UNKNOWN:NumberingPlanIndicator.valueOf(Byte.parseByte(npi));
		
		return new BindAttributes(systemId, password, systemType, bindType, new Address(typeOfNumber, numberingPlanIndicator));
	}
	
	private String getProperty(String name){
		String propertyValue=getProperties().getProperty(getGatewayId() + "." +name);
		if(propertyValue!=null)
			return propertyValue.trim();
		else
		return propertyValue;
	}
	
}
