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
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class representing an AES algorithm encryption key.
 * The class is based on standard JDK AES implementation (128 bit key). 
 */
public class AESKey extends ASymmetricKey
{
	public AESKey() throws NoSuchAlgorithmException
	{
		setKey(generateKey());
	}

	public AESKey(SecretKeySpec key)
	{
		setKey(key);
	}

	@Override
	public SecretKeySpec generateKey() throws NoSuchAlgorithmException
	{
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128);
		SecretKey secretKey = keyGen.generateKey();
		byte[] raw = secretKey.getEncoded();
		return new SecretKeySpec(raw, "AES");
	}

	@Override
	public byte[] encrypt(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException
	{
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, getKey());
		return cipher.doFinal(message);
	}

	@Override
	public byte[] decrypt(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException	
	{
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, getKey());
		return cipher.doFinal(message);
	}

	public static void main(String[] args)
	{
		try
		{
			AESKey k = new AESKey();
			k.setKey(k.generateKey());
			
			String message = "Hello from Thanasis :)";
			System.out.println(">>> " + message);
			byte[] enc = k.encrypt(message.getBytes());
			byte[] dec = k.decrypt(enc);
			System.out.println(">>> " + asString(dec));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
