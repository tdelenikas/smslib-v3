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

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Group class represent a group of destination numbers.
 */
public class Group
{
	private String groupName;

	private Collection<String> groupNumbers;

	public Group(String myGroupName)
	{
		this.groupName = myGroupName;
		this.groupNumbers = new ArrayList<String>();
	}

	/**
	 * Returns the group name.
	 * 
	 * @return The group name.
	 */
	public String getName()
	{
		return this.groupName;
	}

	/**
	 * Returns the numbers associated with the group.
	 * 
	 * @return The numbers associated with the group.
	 */
	public Collection<String> getNumbers()
	{
		return new ArrayList<String>(this.groupNumbers);
	}

	/**
	 * Adds a number to the group.
	 * 
	 * @param number
	 *            The number to add to the group.
	 */
	public void addNumber(String number)
	{
		this.groupNumbers.add(number);
	}

	/**
	 * Removes a number from the group.
	 * 
	 * @param number
	 *            The number to be removed from the group.
	 * @return True if the removal was a success. False if the number was not
	 *         found.
	 */
	public boolean removeNumber(String number)
	{
		return this.groupNumbers.remove(number);
	}

	/**
	 * Removes all numbers from the group (clears the group).
	 */
	public void clear()
	{
		this.groupNumbers.clear();
	}
}
