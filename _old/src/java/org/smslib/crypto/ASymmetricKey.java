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
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Abstract class representing an encryption key of a symmetric encryption
 * algorithm.
 */
public abstract class ASymmetricKey extends AKey
{
	private SecretKeySpec key;

	/**
	 * Returns the encryption key.
	 * 
	 * @return The encryption key.
	 */
	public SecretKeySpec getKey()
	{
		return this.key;
	}

	/**
	 * Sets the encryption key.
	 * 
	 * @param key
	 *            The encryption key.
	 */
	public void setKey(SecretKeySpec key)
	{
		this.key = key;
	}

	/**
	 * Key generation.<br>
	 * The method should be implemented in the descending classes, according to
	 * the implementation.
	 * 
	 * @return The generated encryption key.
	 * @throws NoSuchAlgorithmException
	 */
	public abstract SecretKeySpec generateKey() throws NoSuchAlgorithmException;

	/**
	 * Message encryption.<br>
	 * The method should be implemented in the descending classes, according to
	 * the implementation.
	 * 
	 * @param message
	 *            The message to be encrypted.
	 * @return The encrypted message.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidKeyException
	 */
	public abstract byte[] encrypt(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException;

	/**
	 * Message decryption.<br>
	 * The method should be implemented in the descending classes, according to
	 * the implementation.
	 * 
	 * @param message
	 *            The message to be decrypted.
	 * @return The decrypted message.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidKeyException
	 */
	public abstract byte[] decrypt(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException;
}
