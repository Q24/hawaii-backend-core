/**
 * Copyright 2015 Q24
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
package io.kahu.hawaii.sso.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;

public class JKSSignerFactory {
    public static JWSSigner get(JWK jwk) throws JOSEException {
        JWSSigner signer = null;
        if (jwk.isPrivate()) {
            if (jwk instanceof RSAKey) {
                signer = new RSASSASigner((RSAKey) jwk);
            } else if (jwk instanceof ECKey) {
                signer = new ECDSASigner((ECKey) jwk);
            } else if (jwk instanceof OctetSequenceKey) {
                signer = new MACSigner((OctetSequenceKey) jwk);
            } else {
                System.err.println("Unknown key type: " + jwk);
            }
        } else {
            System.err.println("Key is not private: " + jwk);
        }

        return signer;
    }
}
