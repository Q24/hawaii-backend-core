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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CryptoUtilTest {

    // Special characters in this case are '/' and '+' which are not suited for use in URL's
    private static final String STRING_WHERE_ENCRYPTED_VERSION_WILL_HAVE_SPECIAL_CHARACTERS =
            "kzASn89awVGYTkrvN8nrXKcAlyt12jCkfIpJEjR8yUqZ9qT53h0thrWoeipLtX0lB6U0FaMPyoy1ZjlKZLcdM5Tebe6pI4BVopEN";
    private static final String STRING_WHERE_ENCRYPTED_VERSION_HAS_NO_SPECIAL_CHARACTERS =
            "nL5jSH7RLhkxfMOkizdDvA31Nm2fltjQAl7Q0aMoJ1epyz1Dw6VpFedkiMOCquIkkhcslRxUsEoHjs92mZzwLj4ZkhAUx33X3zZb";


    @Test
    public void testUnsafeStringWillHaveSpecialChars() throws Exception {
        String encrypted = CryptoUtil.encrypt(STRING_WHERE_ENCRYPTED_VERSION_WILL_HAVE_SPECIAL_CHARACTERS);
        assertTrue("Encrypted string does not contain '+' or '/'", encrypted.contains("+") || encrypted.contains("/"));
        String decrypted = CryptoUtil.decrypt(encrypted);
        assertEquals("Decrypted string is not the same as original", STRING_WHERE_ENCRYPTED_VERSION_WILL_HAVE_SPECIAL_CHARACTERS, decrypted);
    }

    @Test
    public void testUnsafeStringWillHaveNoSpecialCharsWhenUrlSafeEncoded() throws Exception {
        String encrypted = CryptoUtil.encryptUrlSafe(STRING_WHERE_ENCRYPTED_VERSION_WILL_HAVE_SPECIAL_CHARACTERS);
        assertTrue("Encrypted string does contain '+' or '/'", !encrypted.contains("+") && !encrypted.contains("/"));
        String decrypted = CryptoUtil.decrypt(encrypted);
        assertEquals("Decrypted string is not the same as original", STRING_WHERE_ENCRYPTED_VERSION_WILL_HAVE_SPECIAL_CHARACTERS, decrypted);
    }

    @Test
    public void testSafeStringWillNotHaveSpecialChars() throws Exception {
        String encrypted = CryptoUtil.encrypt(STRING_WHERE_ENCRYPTED_VERSION_HAS_NO_SPECIAL_CHARACTERS);
        assertTrue("Encrypted string does contain '+' or '/'", !encrypted.contains("+") && !encrypted.contains("/"));
        String decrypted = CryptoUtil.decrypt(encrypted);
        assertEquals("Decrypted string is not the same as original", STRING_WHERE_ENCRYPTED_VERSION_HAS_NO_SPECIAL_CHARACTERS, decrypted);
    }

    @Test
    public void testSafeStringWillHaveNoSpecialCharsWhenUrlSafeEncoded() throws Exception {
        String encrypted = CryptoUtil.encryptUrlSafe(STRING_WHERE_ENCRYPTED_VERSION_HAS_NO_SPECIAL_CHARACTERS);
        assertTrue("Encrypted string does contain '+' or '/'", !encrypted.contains("+") && !encrypted.contains("/"));
        String decrypted = CryptoUtil.decrypt(encrypted);
        assertEquals("Decrypted string is not the same as original", STRING_WHERE_ENCRYPTED_VERSION_HAS_NO_SPECIAL_CHARACTERS, decrypted);
    }

}
