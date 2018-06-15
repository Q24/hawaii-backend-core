/**
 * Copyright 2014-2018 Q24
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kahu.hawaii.util.encryption;

import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.security.GeneralSecurityException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class CryptoUtil {
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * I generated the Key/IV using the following openssl command.
     * 
     * <pre>
     * openssl enc -aes-128-cbc -k this_keeps_leon_happy -P -md sha1
     * 
     * salt=FAA594CDB607153F
     * key=437910A4AC2CCC7576D4FAE1AC2395D3
     * iv =9D19DA92D4B60651EE2030BDB8F157A9
     * </pre>
     */
    private static final String KEY = "437910A4AC2CCC7576D4FAE1AC2395D3";
    private static final String INIT_VECTOR = "9D19DA92D4B60651EE2030BDB8F157A9";

    public static String decrypt(String encrypted) throws ServerException {
        return decrypt(encrypted, getKey(), getInitVector());
    }

    public static String decrypt(String encrypted, String key, String initVector) throws ServerException {
        try {
            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, key, initVector);

            byte[] decrypted = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(decrypted);
        } catch (GeneralSecurityException e) {
            throw new ServerException(ServerError.ENCRYPTION, e);
        }
    }

    public static String encrypt(String unencrypted) throws ServerException {
        return encrypt(unencrypted, getKey(), getInitVector());
    }

    public static String encrypt(String unencrypted, String key, String initVector) throws ServerException {
        try {
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, key, initVector);

            byte[] encrypted = cipher.doFinal(unencrypted.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (GeneralSecurityException e) {
            throw new ServerException(ServerError.ENCRYPTION, e);
        }
    }

    private static Cipher initCipher(int mode, String key, String initVector) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");

        SecretKeySpec secretKeySpec = new SecretKeySpec(hexStringToByteArray(key), "AES");
        IvParameterSpec initVectorSpec = new IvParameterSpec(hexStringToByteArray(initVector));
        cipher.init(mode, secretKeySpec, initVectorSpec);

        return cipher;
    }

    private static byte[] hexStringToByteArray(String s) {
        final int len = s.length();

        // "111" is not a valid hex encoding.
        if (len % 2 != 0) {
            throw new IllegalArgumentException("hexBinary needs to be even-length: " + s);
        }

        byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int high = hexToBin(s.charAt(i));
            int low = hexToBin(s.charAt(i + 1));
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("contains illegal character for hexBinary: " + s);
            }

            out[i / 2] = (byte) (high * 16 + low);
        }

        return out;
    }

    private static int hexToBin(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        }
        return -1;
    }

    private static String getEnv() {
        return System.getenv("KI_ONI");
    }

    private static String getKey() {
        String key = KEY;
        String env = getEnv();
        if (env != null) {
            key = env.substring(0, env.indexOf(':'));
        }
        return key;
    }

    private static String getInitVector() {
        String initVector = INIT_VECTOR;
        String env = getEnv();
        if (env != null) {
            initVector = env.substring(env.indexOf(':') + 1);
        }
        return initVector;
    }
}
