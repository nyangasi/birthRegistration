/*
 * Copyright (C) 2015 UNICEF Tanzania.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tz.co.rita.utils;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Util class to perform encryption/decryption over strings. <br/>
 */
@SuppressLint("TrulyRandom")
public final class UtilsEncryption {


    /**
     * Avoid instantiation. <br/>
     */
    private UtilsEncryption() {
    }

    /**
     * The HEX characters
     */
    private final static String HEX = "0123456789ABCDEF";

    /**
     * Decode an HEX encoded string into a byte[]. <br/>
     *
     * @param the HEX string value
     * @return the decoded byte[]
     */
    protected static byte[] fromHex(String value) {
        int len = value.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(value.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    /**
     * Encode a byte[] into an HEX string. <br/>
     *
     * @param the byte[] value
     * @return the HEX encoded string
     */
    protected static String toHex(byte[] value) {
        if (value == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(2 * value.length);
        for (int i = 0; i < value.length; i++) {
            byte b = value[i];

            result.append(HEX.charAt((b >> 4) & 0x0f));
            result.append(HEX.charAt(b & 0x0f));
        }
        return result.toString();
    }

    /**
     * Generate a secret key. <br/>
     *
     * @param passphraseOrPin a char[] representation of the pass phrase or pin
     * @param saltString a string to serve as the salt
     * @return the secret key.
     */
    private static SecretKey generateKey(char[] passphraseOrPin, String saltString)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase
        // computation time. You should select a value that causes computation
        // to take >100ms.
        final int iterations = 1000;

        // Generate a 256-bit key
        final int outputKeyLength = 256;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(passphraseOrPin, fromHex(saltString), iterations,
                outputKeyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey;
    }

    @SuppressLint("TrulyRandom")
    public static String generateSaltString() {
        SecureRandom sr = new SecureRandom();
        byte[] output = new byte[16];
        sr.nextBytes(output);
        return toHex(output);
    }

    /**
     * Process the given input with the provided mode. <br/>
     *
     * @param the cipher mode
     * @param the value to process
     * @return the processed value as byte[]
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeySpecException
     */
    public static String encryptPassword(String password, String saltString)

    {
        SecretKey secretKey;
        Cipher cipher;
        byte[] encrypted;
        try {
            secretKey = generateKey(password.toCharArray(), saltString);
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            encrypted = cipher.doFinal(password.getBytes());
            return toHex(encrypted);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }


    public static String encryptMessage(String message)

    {
        String ALGO = "AES";
        byte[] z = "The quick fox jumped over the lazy brown dog".getBytes();

        try {
            Key key = new SecretKeySpec(z, ALGO);
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(message.getBytes());
            String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
            return encryptedValue;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }
}