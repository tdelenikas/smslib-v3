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

package org.smslib.modem;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.smslib.Service;
import org.smslib.helper.Logger;

/**
 * Small detector for a CNMI-AT-Command. This class is constructed with a given
 * AT+CNMI=? <br />
 * and searches the best values for mode, mt, bm, ds and bfr for SMSlib.
 * 
 * @author Sebastian Just
 */
public class CNMIDetector
{
	private final static Pattern splitterPattern = Pattern.compile("(\\(\\d([-,]\\d)*\\)|\\d)");

	private String rawSentence = null;

	private final String[] bestMode = new String[] { "3", "2" };

	private String mode = null;

	private final String[] bestMt = new String[] { "1" };

	private String mt = null;

	private final String[] bestBm = new String[] { "0" };

	private String bm = null;

	private final String[] bestDs = new String[] { "2" };

	private String ds = null;

	private final String[] bestBfr = new String[] { "0" };

	private String bfr = null;

	/**
	 * Simple constructor which initializes all fields. <br />
	 * 
	 * @param cnmi
	 *            The AT+CNMI=? response of a GSM device
	 */
	public CNMIDetector(String cnmi)
	{
		this.rawSentence = cnmi;
		detect();
	}

	/**
	 * Converts a given String range in an array which contains all elements.
	 * <br />
	 * There are 3 cases: "(4)" results in {"4"} "(1,2,3") results in
	 * {"1","2","3"} "(5-9)" results in {"5","6","7","8","9"}
	 * 
	 * @param range
	 *            The raw range. Surrounding braces are deleted
	 */
	String[] convertRange(String myRange)
	{
		String range = myRange;
		if (range.startsWith("(")) range = range.substring(1, range.length() - 1);
		String[] retValue = null;
		if (range.indexOf(",") != -1)
		{ //List of values
			retValue = range.split(",");
		}
		else if (range.indexOf("-") != -1)
		{ //A real range
			int pos = range.indexOf("-");
			int begin = Integer.parseInt(range.substring(0, pos));
			int end = Integer.parseInt(range.substring(pos + 1));
			retValue = new String[end - begin + 1];
			for (int i = 0; begin <= end; begin++, i++)
			{
				retValue[i] = String.valueOf(begin);
			}
		}
		else
		{ //No range here
			retValue = new String[1];
			retValue[0] = range;
		}
		return retValue;
	}

	/**
	 * Searches an array for a match. <br />
	 * Searches in availableOptions for searchOptions[0], if nothing is found,
	 * availableOptions is searched for searchedOptions[1]. <br />
	 * This is done until a match is found (or there are no more search patterns
	 * in searchedOptions)
	 * 
	 * @param availableOptions
	 *            array to search in
	 * @param searchedOptions
	 *            elements of patterns
	 * @return the found element from searchedOptions or the last element of
	 *         availableOptions
	 */
	String getBestMatch(String[] availableOptions, String[] searchedOptions)
	{
		for (int i = 0; i < searchedOptions.length; i++)
		{
			String search = searchedOptions[i];
			for (int j = 0; j < availableOptions.length; j++)
			{
				String option = availableOptions[j];
				if (search.equals(option))
				{
					Logger.getInstance().logDebug("CNMI: Found best match: " + search, null, null);
					return search;
				}
			}
		}
		String bestAvailableOption = availableOptions[availableOptions.length - 1];
		Logger.getInstance().logInfo("CNMI: No best match, returning: " + bestAvailableOption, null, null);
		return bestAvailableOption;
	}

	/**
	 * The GLUE. <br />
	 * Splits the rawSentence and sets all fields with the best available
	 * options.
	 */
	void detect()
	{
		Matcher m = splitterPattern.matcher(this.rawSentence);
		List<String> options = new ArrayList<String>();
		while (m.find())
			options.add(m.group());
		if (options.size() < 5) throw new IllegalArgumentException("Missing parameters");
		this.mode = getBestMatch(convertRange(options.get(0)), this.bestMode);
		this.mt = getBestMatch(convertRange(options.get(1)), this.bestMt);
		this.bm = getBestMatch(convertRange(options.get(2)), this.bestBm);
		this.ds = getBestMatch(convertRange(options.get(3)), this.bestDs);
		this.bfr = getBestMatch(convertRange(options.get(4)), this.bestBfr);
	}

	String getMode()
	{
		if (Service.getInstance().getSettings().DISABLE_CMTI) return "0";
		return this.mode;
	}

	String getMt()
	{
		return this.mt;
	}

	String getBm()
	{
		return this.bm;
	}

	String getDs()
	{
		return this.ds;
	}

	String getBfr()
	{
		return this.bfr;
	}

	/**
	 * Returns the best AT+CNMI=-Command for this GSM-device
	 */
	@Override
	public String toString()
	{
		return "AT+CNMI=" + getMode() + "," + getMt() + "," + getBm() + "," + getDs() + "," + getBfr();
	}

	/**
	 * {@link CNMIDetector#toString()} with "\r" at the end
	 */
	public String getATCommand()
	{
		return toString() + "\r";
	}

	/**
	 * {@link CNMIDetector#toString()} with the given ending at the end
	 */
	String getATCommand(String ending)
	{
		return toString() + ending;
	}
}
