
package org.ajwcc.pduUtils.test;

import org.ajwcc.pduUtils.gsm3040.*;

//PduUtils Library - A Java library for generating GSM 3040 Protocol Data Units (PDUs)
//
//Copyright (C) 2008, Ateneo Java Wireless Competency Center/Blueblade Technologies, Philippines.
//PduUtils is distributed under the terms of the Apache License version 2.0
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
public class ParserTester
{
	public static void main(String[] args) throws Exception
	{
		// load a file for testing
		TestFileReader tfr = new TestFileReader();
		tfr.init("java/org/ajwcc/pduUtils/testData/testPdus.txt");
		String currentLine;
		PduParser parser = new PduParser();
		while ((currentLine = tfr.next()) != null)
		{
			String pduString = currentLine;
			// System.out.println();
			Pdu pdu = parser.parsePdu(pduString);
			System.out.println(pdu);
		}
		tfr.close();
	}
}
