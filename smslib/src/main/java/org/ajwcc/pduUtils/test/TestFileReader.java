
package org.ajwcc.pduUtils.test;

import java.io.*;

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
public class TestFileReader
{
	private FileReader fr;

	private BufferedReader br;

	private String currentLine;

	private boolean trim = true;
	private boolean skipBlanksAndComments = true;
	
	public void init(String fileName) throws Exception
	{
		fr = new FileReader(fileName);
		br = new BufferedReader(fr);
		currentLine = null;
	}

	public void setTrim(boolean b)
	{
		trim = b;
	}

    public void setSkipBlanksAndComment(boolean b)
    {
        skipBlanksAndComments = b;
    }
	
	public void close()
	{
		try
		{
			br.close();
		}
		catch (Exception e)
		{
		}
		try
		{
			fr.close();
		}
		catch (Exception e)
		{
		}
		currentLine = null;
	}

	public String next()
	{
		try
		{
			while ((currentLine = br.readLine()) != null)
			{
				if (currentLine.trim().equals(""))
				{
				    if (skipBlanksAndComments)
				    {
				        continue;
				    }
				    else
				    {
				        return currentLine;
				    }
				}
				else if (currentLine.trim().startsWith("#"))
				{
                    if (skipBlanksAndComments)
                    {
                        continue;
                    }
                    else
                    {
                        return currentLine;
                    }
				}
				else
				{
					if (trim)
					{
						currentLine = currentLine.trim();
					}
					return currentLine;
				}
			}
		}
		catch (Exception e)
		{
		}
		return null;
	}
}
