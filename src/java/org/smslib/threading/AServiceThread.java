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

package org.smslib.threading;

import org.smslib.helper.Logger;

public abstract class AServiceThread extends Thread
{
	private int delay;
	private int initialDelay;
	private boolean enabled;
	private boolean canceled;

	public AServiceThread(String name, int delay, int initialDelay, boolean enabled)
	{
		setName(name);
		setDelay(delay);
		if (enabled) enable();
		else disable();
		this.canceled = false;
		Logger.getInstance().logDebug("Initialized.", null, null);
		start();
	}

	public int getDelay()
	{
		return this.delay;
	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}

	public int getInitialDelay()
	{
		return this.initialDelay;
	}

	public void setInitialDelay(int initialDelay)
	{
		this.initialDelay = initialDelay;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}

	public void enable()
	{
		this.enabled = true;
	}

	public void disable()
	{
		this.enabled = false;
	}

	public boolean isCanceled()
	{
		return this.canceled;
	}

	public void cancel()
	{
		this.canceled = true;
		interrupt();
		try
		{
			this.join();
		}
		catch (InterruptedException e)
		{
			// Ignore this.
		}
	}

	@Override
	public void run()
	{
		while (!isCanceled())
		{
			try
			{
				Logger.getInstance().logDebug((isEnabled() ? "Running..." : "** disabled **"), null, null);
				if (isEnabled()) process();
				sleep(getDelay());
			}
			catch (InterruptedException e)
			{
				if (isCanceled())
				{
					Logger.getInstance().logDebug("Stopped.", null, null);
					break;
				}
				Logger.getInstance().logDebug("Interrupted!", null, null);
			}
			catch (Exception e)
			{
				Logger.getInstance().logError("Error!", e, null);
			}
		}
	}

	public abstract void process() throws Exception;
}
