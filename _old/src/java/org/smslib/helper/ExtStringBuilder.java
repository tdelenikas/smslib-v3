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

/**
 * Buffer around StringBuilder for some convinience methods.
 * 
 * @author Sebastian Just
 */
public class ExtStringBuilder
{
	final StringBuilder sb;

	public ExtStringBuilder()
	{
		this.sb = new StringBuilder();
	}

	public ExtStringBuilder(String str)
	{
		this.sb = new StringBuilder(str);
	}

	public ExtStringBuilder(StringBuilder mySb)
	{
		this.sb = mySb;
	}

	public void replaceAll(String search, int replace)
	{
		replaceAll(search, String.valueOf(replace));
	}

	public void replaceAll(String search, String replace)
	{
		int length = search.length();
		int start = this.sb.indexOf(search);
		while (start != -1)
		{
			this.sb.replace(start, start + length, replace);
			start = this.sb.indexOf(search);
		}
	}

	@Override
	public String toString()
	{
		return this.sb.toString();
	}
}
