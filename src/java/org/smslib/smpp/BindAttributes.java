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

package org.smslib.smpp;


/**
 * SMPP Bind Attributes.
 * @author Bassam Al-Sarori
 *
 */
public class BindAttributes {

	private String systemId;
	private String password;
	private String systemType;
	
	
	private BindType bindType;
	
	private Address bindAddress;
	
	
	public enum BindType{
		/**
		 * RECEIVER
		 */
		RECEIVER("r"),
		/**
		 * TRANSMITTER
		 */
		TRANSMITTER("t"),
		/**
		 * TRANSCEIVER
		 */
		TRANSCEIVER("tr");
		
		private String value;
		private BindType(String value){
			this.value=value;
		}

		
		 public static BindType getByShortName(String value)
         throws IllegalArgumentException {
     for (BindType item : values()) {
         if (item.value().equals(value)) {
             return item;
         }
     }
     throw new IllegalArgumentException(
             "No enum const BindType with value " + value);
 }
		  public String value() {
		        return value;
		    }
	}

	public BindAttributes(String systemId, String password, String systemType,
			BindType bindType) {
		this(systemId,password,systemType,bindType,new Address());
	}
	
	public BindAttributes(String systemId, String password, String systemType,
			BindType bindType, Address bindAddress) {
		
		this.systemId = systemId;
		this.password = password;
		this.systemType = systemType;
		this.bindType = bindType;
		this.bindAddress=bindAddress;
		
	}

	public String getSystemId() {
		return systemId;
	}

	public String getPassword() {
		return password;
	}

	public String getSystemType() {
		return systemType;
	}

	public BindType getBindType() {
		return bindType;
	}

	public Address getBindAddress() {
		return bindAddress;
	}


	
	
	
}
