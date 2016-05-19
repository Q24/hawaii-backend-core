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
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;

public class JKSVerifierFactory {
    public static JWSVerifier get(JWK jwk) throws JOSEException {
        JWSVerifier verifier = null;
        if (jwk instanceof RSAKey) {
            verifier = new RSASSAVerifier((RSAKey) jwk);
        } else if (jwk instanceof ECKey) {
            verifier = new ECDSAVerifier((ECKey) jwk);
        } else if (jwk instanceof OctetSequenceKey) {
            verifier = new MACVerifier((OctetSequenceKey) jwk);
        } else {
            System.err.println("Unknown key type: " + jwk);
        }

        return verifier;
    }
}
