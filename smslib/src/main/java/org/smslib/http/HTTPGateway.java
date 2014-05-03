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

package org.smslib.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.smslib.AGateway;
import org.smslib.helper.Logger;

class HTTPGateway extends AGateway
{
	public HTTPGateway(String id)
	{
		super(id);
	}

	List<String> HttpPost(URL url, List<HttpHeader> requestList) throws IOException
	{
		List<String> responseList = new ArrayList<String>();
		URLConnection con;
		BufferedReader in;
		OutputStreamWriter out;
		StringBuffer req;
		String line;
		Logger.getInstance().logInfo("HTTP POST: " + url, null, getGatewayId());
		con = url.openConnection();
		con.setConnectTimeout(20000);
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		out = new OutputStreamWriter(con.getOutputStream());
		req = new StringBuffer();
		for (int i = 0, n = requestList.size(); i < n; i++)
		{
			if (i != 0) req.append("&");
			req.append(requestList.get(i).key);
			req.append("=");
			if (requestList.get(i).unicode)
			{
				StringBuffer tmp = new StringBuffer(200);
				byte[] uniBytes = requestList.get(i).value.getBytes("UnicodeBigUnmarked");
				for (int j = 0; j < uniBytes.length; j++)
					tmp.append(Integer.toHexString(uniBytes[j]).length() == 1 ? "0" + Integer.toHexString(uniBytes[j]) : Integer.toHexString(uniBytes[j]));
				req.append(tmp.toString().replaceAll("ff", ""));
			}
			else req.append(URLEncoder.encode(requestList.get(i).value, "utf-8"));
		}
		out.write(req.toString());
		out.flush();
		out.close();
		in = new BufferedReader(new InputStreamReader((con.getInputStream())));
		while ((line = in.readLine()) != null)
			responseList.add(line);
		in.close();
		return responseList;
	}

	List<String> HttpGet(URL url) throws IOException
	{
		List<String> responseList = new ArrayList<String>();
		Logger.getInstance().logInfo("HTTP GET: " + url, null, getGatewayId());
		URLConnection con = url.openConnection();
		con.setConnectTimeout(20000);
		con.setAllowUserInteraction(false);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			responseList.add(inputLine);
		in.close();
		return responseList;
	}

	String ExpandHttpHeaders(List<HttpHeader> httpHeaderList)
	{
		StringBuffer buffer = new StringBuffer();
		for (HttpHeader h : httpHeaderList)
		{
			buffer.append(h.key);
			buffer.append("=");
			buffer.append(h.value);
			buffer.append("&");
		}
		return buffer.toString();
	}

	class HttpHeader
	{
		public String key;

		public String value;

		public boolean unicode;

		public HttpHeader()
		{
			this.key = "";
			this.value = "";
			this.unicode = false;
		}

		public HttpHeader(String myKey, String myValue, boolean myUnicode)
		{
			this.key = myKey;
			this.value = myValue;
			this.unicode = myUnicode;
		}
	}

	String calculateMD5(String in)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] pre_md5 = md.digest(in.getBytes("LATIN1"));
			StringBuilder md5 = new StringBuilder();
			for (int i = 0; i < 16; i++)
			{
				if (pre_md5[i] < 0)
				{
					md5.append(Integer.toHexString(256 + pre_md5[i]));
				}
				else if (pre_md5[i] > 15)
				{
					md5.append(Integer.toHexString(pre_md5[i]));
				}
				else
				{
					md5.append("0" + Integer.toHexString(pre_md5[i]));
				}
			}
			return md5.toString();
		}
		catch (UnsupportedEncodingException ex)
		{
			Logger.getInstance().logError("Unsupported encoding.", ex, getGatewayId());
			return "";
		}
		catch (NoSuchAlgorithmException ex)
		{
			Logger.getInstance().logError("No such algorithm.", ex, getGatewayId());
			return "";
		}
	}

	@Override
	public int getQueueSchedulingInterval()
	{
		return 500;
	}
}
