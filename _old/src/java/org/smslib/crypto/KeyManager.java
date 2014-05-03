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

package org.smslib.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.smslib.SMSLibException;

/**
 * The KeyManager class handles the association of a number (originator or
 * recipient) with a cryptographic key.
 */
public class KeyManager
{
	static private KeyManager _instance = null;

	HashMap<String, AKey> keys;

	private KeyManager()
	{
		this.keys = new HashMap<String, AKey>();
	}

	static public KeyManager getInstance()
	{
		if (_instance == null) _instance = new KeyManager();
		return _instance;
	}

	/**
	 * Associates a mobile number with an encryption key.
	 * 
	 * @param mobileNumber
	 *            The mobile number which will be associated with the encryption
	 *            key.
	 * @param key
	 *            The encryption key.
	 * @see AKey
	 * @see #registerKey(String, AKey)
	 * @see #unregisterAllKeys()
	 */
	public void registerKey(String mobileNumber, AKey key)
	{
		this.keys.put((mobileNumber.charAt(0) == '+' ? mobileNumber.substring(1) : mobileNumber), key);
	}

	/**
	 * Removes the association of a mobile number with a key.
	 * 
	 * @param mobileNumber
	 *            The mobile number which will be removed from the key
	 *            associations.
	 * @return The encryption key which was associated with the specific mobile
	 *         number.
	 * @see AKey
	 * @see #registerKey(String, AKey)
	 */
	public AKey unregisterKey(String mobileNumber)
	{
		return this.keys.remove((mobileNumber.charAt(0) == '+' ? mobileNumber.substring(1) : mobileNumber));
	}

	/**
	 * Removes all associations of mobile numbers and encryption keys.
	 */
	public void unregisterAllKeys()
	{
		this.keys.clear();
	}

	/**
	 * Returns the encryption key of the specified mobile number. Returns null
	 * if there is no association.
	 * 
	 * @param mobileNumber
	 *            The mobile number to look for.
	 * @return The encryption key, null if no key was previously associated.
	 */
	public AKey getKey(String mobileNumber)
	{
		if (mobileNumber == null) return null;
		return this.keys.get((mobileNumber.charAt(0) == '+' ? mobileNumber.substring(1) : mobileNumber));
	}

	/**
	 * Encrypts the specified message with the encryption key already associated
	 * with the specified mobile number.
	 * 
	 * @param mobileNumber
	 *            The mobile number which the message will be send to / received
	 *            from.
	 * @param message
	 *            The decrypted message.
	 * @return The encrypted message.
	 * @throws SMSLibException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 */
	public byte[] encrypt(String mobileNumber, byte[] message) throws SMSLibException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException
	{
		AKey k = getKey(mobileNumber);
		if (k == null) throw new SMSLibException("Could not find Encryption Key for the specific number.");
		else if (k instanceof ASymmetricKey) return ((ASymmetricKey) k).encrypt(message);
		else return new byte[0];
	}

	/**
	 * Decrypts the specified message with the encryption key already associated
	 * with the specified mobile number.
	 * 
	 * @param mobileNumber
	 *            The mobile number which the message received from.
	 * @param message
	 *            The encrypted message.
	 * @return The decrypted message.
	 * @throws SMSLibException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 */
	public byte[] decrypt(String mobileNumber, byte[] message) throws SMSLibException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException
	{
		AKey k = getKey(mobileNumber);
		if (k == null) throw new SMSLibException("Could not find Encryption Key for the specific number.");
		else if (k instanceof ASymmetricKey) return ((ASymmetricKey) k).decrypt(message);
		else return new byte[0];
	}
}
