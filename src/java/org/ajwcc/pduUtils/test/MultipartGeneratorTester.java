
package org.ajwcc.pduUtils.test;

import org.ajwcc.pduUtils.gsm3040.*;
import org.ajwcc.pduUtils.gsm3040.ie.*;

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
public class MultipartGeneratorTester
{
	public static void main(String[] args)
	{
		SmsSubmitPdu pdu = PduFactory.newSmsSubmitPdu();
		//        pdu.setSmscAddressType(PduUtils.ADDRESS_TYPE_INTERNATIONAL | PduUtils.ADDRESS_NUMBER_PLAN_ID_TELEPHONE);
		//        pdu.setSmscAddress("639170000240");
		pdu.setSmscInfoLength(0);
		pdu.setAddressType(PduUtils.ADDRESS_TYPE_INTERNATIONAL | PduUtils.ADDRESS_NUMBER_PLAN_ID_TELEPHONE);
		pdu.setAddress("09063137023");
		//        pdu.setTimestamp(new Date());
		//        String decodedText = "3 8 Thank you for using this service.  Your transaction has been logged as TXN 7 abcdef 7 Thank you for using this service.  Your transaction has been logged as TXN 7 abcdefz";
		//        pdu.setDecodedText(decodedText);
		//        pdu.setDataCodingScheme(PduUtils.DCS_ENCODING_UCS2);
		String decodedText = "3 8 Thank you for using this service.  Your transaction has been logged as TXN 7 abcdef 7 Thank you for using this service.  Your transaction has been logged as TXN 7 abcdefz";
		pdu.setDataBytes(decodedText.getBytes());
		pdu.setValidityPeriod(8);
		pdu.setProtocolIdentifier(0);
		// reference data from j2me app
		// 3 8 Thank you for using this service.  Your transaction has been logged as TXN 7 abcdef 7 Thank you for using this service.  Your transaction has been logged as TXN 7 abcdefz
		// 7bit
		// PDU1: 0065000C913609363107320000A00B0003040201150415B3FDE8CC403810151D76AF41F9771D647ECB41F579DA7D06D1D1E939685E96DBD3E3B20B04CABEEB72105D1E76CFC3637AFAED06A1C37390B85C7683D8EFF3B94C0685E7202AD609BA81C2E231B96C06DD405474D8BD06E5DF7590F92D07D5E769F7194447A7E7A079596E4F8FCB2E1028FBAECB417479D83D0F8FE9E9B71B840ECF41
		// PDU2: 0065000C9136093631073200002A0B0003040202050415B3FDE888CB653788FD3E9FCB6450780EA2629DA01B282C1E93CB663D
		// ucs2
		// PDU1: 0051000B819060137320F300085F8B0C050415B300000804ED0903010033002000380020005400680061006E006B00200079006F007500200066006F00720020007500730069006E00670020007400680069007300200073006500720076006900630065002E002000200059006F007500720020007400720061006E00730061006300740069006F006E00200068006100730020006200650065
		// PDU2: 0051000B819060137320F300085F8B0C050415B300000804ED090302006E0020006C006F0067006700650064002000610073002000540058004E002000370020006100620063006400650066002000370020005400680061006E006B00200079006F007500200066006F00720020007500730069006E00670020007400680069007300200073006500720076006900630065002E002000200059
		// PDU3: 0051000B819060137320F300085F6D0C050415B300000804ED090303006F007500720020007400720061006E00730061006300740069006F006E00200068006100730020006200650065006E0020006C006F0067006700650064002000610073002000540058004E002000370020006100620063006400650066007A
		pdu.addInformationElement(InformationElementFactory.generatePortInfo(5555, 0));
		PduGenerator generator = new PduGenerator();
		// pdu.getMpMaxNo() initially will be 1 but will be changed to the real max value if concat is needed
		// upon initial generation, so this needs to get read dynamically not stored in a local variable
		// this would require the the pdu object to be maintained 
		// across calls to generator.generatePduString() to get multiple parts
		for (int i = 1; i <= pdu.getMpMaxNo(); i++)
		{
			String pduString = generator.generatePduString(pdu, 4, i);
			System.out.println(pduString);
			System.out.println("Parsing:");
			PduParser parser = new PduParser();
			System.out.println(parser.parsePdu(pduString));
		}
		// alternative (does not require the PDU object after generation)
		// the mpRefNo supplied is the mpRefNo used in case the pdu requires
		// multiple pdu strings to send out
		// actual use of the mpRefNo can be seen by the size of the
		// pdu list.  If size() > 1 then the counter used for it should
		// be incremented
		//        List<String> pduStringList = generator.generatePduList(pdu, 4);
		//        for (String pduString : pduStringList)
		//        {
		//            System.out.println("Parsing:");
		//            PduParser parser = new PduParser();
		//            System.out.println(parser.parsePdu(pduString));
		//        }
	}
}
