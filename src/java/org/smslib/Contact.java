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
 * The Contact class represents a phonebook entry.
 */
public class Contact
{
	/*
	 * Contact locations
	 */
	public enum ContactLocation
	{
		/**
		 * Represents the dialled numbers.
		 */
		DIALLED_CALLS,

		/**
		 * Represents the missed inbound calls.
		 */
		MISSED_CALLS,

		/**
		 * Represents the normal phonebook entries stored in the phone's memory.
		 */
		PHONE_ENTRIES,

		/**
		 * Represents the normal phonebook entrues stored in the SIM card.
		 */
		SIM_ENTRIES,

		/**
		 * Represents the phonebook entries from both the phone's memory and SIM card.
		 */
		ALL_ENTRIES,

		/**
		 * Represents an unknown phonebook entry.
		 */
		UNKNOWN_ENTRY
	}

	private String name;

	private String number;

	private String memLoc;

	private int memIndex;

	public Contact(String myName, String myNumber, String myMemLoc, int myMemIndex)
	{
		this.name = myName;
		this.number = myNumber;
		this.memLoc = myMemLoc;
		this.memIndex = myMemIndex;
	}

	String getName()
	{
		return this.name;
	}

	void setName(String name)
	{
		this.name = name;
	}

	String getNumber()
	{
		return this.number;
	}

	void setNumber(String number)
	{
		this.number = number;
	}

	ContactLocation getLocation()
	{
		return convertLocationToType(this.memLoc);
	}

	String getMemLoc()
	{
		return this.memLoc;
	}

	void setMemLoc(String memLoc)
	{
		this.memLoc = memLoc;
	}

	int getMemIndex()
	{
		return this.memIndex;
	}

	void setMemIndex(int myMemIndex)
	{
		this.memIndex = myMemIndex;
	}

	@Override
	public String toString()
	{
		return String.format("Name: %s, Phone: %s, Loc: %s [%s:%d]", getName(), getNumber(), getLocation(), getMemLoc(), getMemIndex());
	}

	public static String convertTypeToLocation(ContactLocation type)
	{
		switch (type)
		{
			case DIALLED_CALLS:
				return "DC";
			case MISSED_CALLS:
				return "MC";
			case PHONE_ENTRIES:
				return "ME";
			case SIM_ENTRIES:
				return "SM";
			case ALL_ENTRIES:
				return "MT";
			default:
				return "";
		}
	}

	public static ContactLocation convertLocationToType(String loc)
	{
		if (loc.equalsIgnoreCase("DC")) return ContactLocation.DIALLED_CALLS;
		else if (loc.equalsIgnoreCase("MC")) return ContactLocation.MISSED_CALLS;
		else if (loc.equalsIgnoreCase("ME")) return ContactLocation.PHONE_ENTRIES;
		else if (loc.equalsIgnoreCase("SM")) return ContactLocation.SIM_ENTRIES;
		else if (loc.equalsIgnoreCase("MT")) return ContactLocation.ALL_ENTRIES;
		else return ContactLocation.UNKNOWN_ENTRY;
	}
}
