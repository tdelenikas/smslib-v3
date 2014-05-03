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

import java.net.URL;
import java.util.Date;
import org.ajwcc.pduUtils.gsm3040.*;
import org.ajwcc.pduUtils.wappush.*;

public class OutboundWapSIMessage extends OutboundBinaryMessage
{
	private static final long serialVersionUID = 2L;

	public enum WapSISignals
	{
		NONE, LOW, MEDIUM, HIGH, DELETE
	}

	protected URL url;

	protected Date createDate, expireDate;

	protected WapSISignals signal;

	protected String siId;

	protected String indicationText;

	public OutboundWapSIMessage(String myRecipient, URL myUrl, Date myCreateDate, Date myExpireDate, WapSISignals mySignal, String myIndicationText)
	{
		super();
		this.url = myUrl;
		this.createDate = new java.util.Date(myCreateDate.getTime());
		this.expireDate = new java.util.Date(myExpireDate.getTime());
		this.signal = mySignal;
		this.setIndicationText(myIndicationText);
		setSrcPort(9200);
		setDstPort(2948);
		setType(MessageTypes.WAPSI);
		setEncoding(MessageEncodings.ENC8BIT);
		this.setRecipient(myRecipient);
	}

	public OutboundWapSIMessage(String myRecipient, URL myUrl, String text)
	{
		this(myRecipient, myUrl, new java.util.Date(), new java.util.Date(), WapSISignals.MEDIUM, text);
	}

	public OutboundWapSIMessage(String myRecipient, URL myUrl, String text, Date myExpireDate, WapSISignals mySignal)
	{
		this(myRecipient, myUrl, new java.util.Date(), myExpireDate, mySignal, text);
	}

	public void setIndicationText(String s)
	{
		this.indicationText = s;
	}

	public String getIndicationText()
	{
		return this.indicationText;
	}

	public Date getCreateDate()
	{
		return new java.util.Date(this.createDate.getTime());
	}

	public void setCreateDate(Date myCreateDate)
	{
		this.createDate = new java.util.Date(myCreateDate.getTime());
	}

	public Date getExpireDate()
	{
		return new java.util.Date(this.expireDate.getTime());
	}

	public void setExpireDate(Date myExpireDate)
	{
		this.expireDate = new java.util.Date(myExpireDate.getTime());
	}

	public WapSISignals getSignal()
	{
		return this.signal;
	}

	public void setSignal(WapSISignals mySignal)
	{
		this.signal = mySignal;
	}

	public URL getUrl()
	{
		return this.url;
	}

	public void setUrl(URL myUrl)
	{
		this.url = myUrl;
	}

	public String getSiId()
	{
		return this.siId;
	}

	public void setSiId(String mySiId)
	{
		this.siId = mySiId;
	}

	@Override
	protected WapSiPdu createPduObject()
	{
		WapSiPdu pdu;
		if (getStatusReport())
		{
			pdu = PduFactory.newWapSiPdu(PduUtils.TP_SRR_REPORT | PduUtils.TP_VPF_INTEGER);
		}
		else
		{
			pdu = PduFactory.newWapSiPdu();
		}
		return pdu;
	}

	@Override
	protected void initPduObject(SmsSubmitPdu pdu, String smscNumber)
	{
		// store basic info
		super.initPduObject(pdu, smscNumber);
		// store wap si specific info
		WapSiPdu wapSiPdu = (WapSiPdu) pdu;
		wapSiPdu.setIndicationText(this.indicationText);
		wapSiPdu.setUrl(this.url.toString());
		wapSiPdu.setCreateDate(this.createDate);
		wapSiPdu.setExpireDate(this.expireDate);
		wapSiPdu.setWapSignalFromString(this.signal.toString());
		wapSiPdu.setSiId(this.siId);
		super.setDataBytes(wapSiPdu.getDataBytes());
	}

	@Override
	public byte[] getDataBytes()
	{
		SmsSubmitPdu pdu = createPduObject();
		initPduObject(pdu, "");
		return super.getDataBytes();
	}

	@Override
	public void setDataBytes(byte[] b)
	{
		throw new RuntimeException("setDataBytes() not supported for WapSi Message");
	}

	@Override
	public void addDataBytes(byte[] b)
	{
		throw new RuntimeException("addDataBytes() not supported for WapSi Message");
	}
}
