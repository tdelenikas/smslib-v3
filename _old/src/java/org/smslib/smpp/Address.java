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
 * SMPP Address.
 * @author Bassam Al-Sarori
 */
public class Address {

	private TypeOfNumber typeOfNumber=TypeOfNumber.UNKNOWN;
	private NumberingPlanIndicator numberingPlanIndicator=NumberingPlanIndicator.UNKNOWN;
	
	
	public enum TypeOfNumber {
		UNKNOWN((byte)0x00), 
		INTERNATIONAL((byte)0x01), 
		NATIONAL((byte)0x02), 
		NETWORK_SPECIFIC((byte)0x03), 
		SUBSCRIBER_NUMBER((byte)0x04), 
		ALPHANUMERIC((byte)0x05), 
		ABBREVIATED((byte)0x06);
		
		private byte value;
	    
		private TypeOfNumber(byte value) {
			this.value = value;
		}
		
		/**
	     * Get the byte value of the enum constant.
	     * 
		 * @return the byte value.
		 */
		public byte value() {
			return value;
		}
		
		/**
	     * Get the <tt>TypeOfNumber</tt> based on the specified byte value
	     * representation.
	     * 
	     * @param value is the byte value representation.
	     * @return is the enum const related to the specified byte value.
	     * @throws IllegalArgumentException if there is no enum const associated
	     *         with specified byte value.
	     */
		public static TypeOfNumber valueOf(byte value) {
			for (TypeOfNumber val : values()) {
				if (val.value == value)
					return val;
			}
			
			throw new IllegalArgumentException(
		            "No enum const TypeOfNumber with value " + value);
		}
	}
	
	public enum NumberingPlanIndicator{
		UNKNOWN((byte)0x00),
		ISDN((byte)0x01), 
		DATA((byte)0x02), 
		TELEX((byte)0x03), 
		LAND_MOBILE((byte)0x04), 
		NATIONAL((byte)0x08), 
		PRIVATE((byte)0x09), 
		ERMES((byte)0x10), 
		INTERNET((byte)0x14), 
		WAP((byte)0x18);
		
		private byte value;
		private NumberingPlanIndicator(byte value) {
			this.value = value;
		}
		
		/**
	     * Return the value of NPI.
		 * @return the value of NPI.
		 */
		public byte value() {
			return value;
		}
		
		/**
	     * Get the associated <tt>NumberingPlanIndicator</tt> by it's value.
	     * 
		 * @param value is the value.
		 * @return the associated enum const for specified value.
	     * @throws IllegalArgumentException if there is no enum const associated 
	     *      with specified <tt>value</tt>.
		 */
		public static NumberingPlanIndicator valueOf(byte value)
	            throws IllegalArgumentException {
			for (NumberingPlanIndicator val : values()) {
				if (val.value == value)
					return val;
			}
			
			throw new IllegalArgumentException(
		            "No enum const NumberingPlanIndicator with value " + value);
		}
	}
	
	public Address(){
		
	}
	
public Address(TypeOfNumber typeOfNumber,NumberingPlanIndicator numberingPlanIndicator){
		this.typeOfNumber=typeOfNumber;
		this.numberingPlanIndicator=numberingPlanIndicator;
	}

public TypeOfNumber getTypeOfNumber() {
	return typeOfNumber;
}

public NumberingPlanIndicator getNumberingPlanIndicator() {
	return numberingPlanIndicator;
}


}
