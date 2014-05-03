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

package org.smslib;

/**
 * Enum representing the status of a GSM Unstructured Supplemental Service Data
 * (USSD) session.
 */
public enum USSDSessionStatus
{
	/**
	 * No further user action required (network initiated USSD-Notify, or no
	 * further information needed after mobile initiated operation)
	 */
	NO_FURTHER_ACTION_REQUIRED(0),
	/**
	 * Further user action required (network initiated USSD-Request, or further
	 * information needed after mobile initiated operation
	 */
	FURTHER_ACTION_REQUIRED(1),
	/**
	 * USSD terminated by network
	 */
	TERMINATED_BY_NETWORK(2),
	/**
	 * Other local client has responded
	 */
	OTHER_CLIENT_RESPONDED(3),
	/**
	 * Operation not supported
	 */
	OPERATION_NOT_SUPPORTED(4),
	/**
	 * Network time out
	 */
	NETWORK_TIMEOUT(5);
	private final int numeric;

	USSDSessionStatus(int aNumeric)
	{
		numeric = aNumeric;
	}

	public int getNumeric()
	{
		return numeric;
	}

	@Override
	public String toString()
	{
		return super.toString() + " (" + numeric + ")";
	}

	public static USSDSessionStatus getByNumeric(int aNumeric)
	{
		for (USSDSessionStatus status : USSDSessionStatus.values())
		{
			if (aNumeric == status.getNumeric()) return status;
		}
		return null;
	}
}
