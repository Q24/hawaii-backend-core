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
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

public class JwtTokenDecoder implements TokenDecoder, InitializingBean {

    private JsonWebKeySetRepository jsonWebKeySetRepository;

    public JwtTokenDecoder(JsonWebKeySetRepository jsonWebKeySetRepository) {
        this.jsonWebKeySetRepository = jsonWebKeySetRepository;
    }

    @Override
    public Map<String, ?> decode(String tokenValue) {
        String token = tokenValue.replaceAll("\n", "");
        String[] parts = token.split("\\.");
        String header = new Base64URL(parts[0]).decodeToString();
        String payload = new Base64URL(parts[1]).decodeToString();

        return decode(header, payload, tokenValue);
    }

    public Map<String, ?> decode(String header, String payload, String tokenValue) {
        try {
            JSONObject json = new JSONObject(header);
            String kid = json.getString("kid");

            json = new JSONObject(payload);
            String iss = json.getString("iss");

            JWSVerifier verifier = getVerificationKeyResolver(iss, kid);

            SignedJWT jwt = SignedJWT.parse(tokenValue);

            if (jwt.verify(verifier)) {
                return jwt.getJWTClaimsSet().getClaims();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private JWSVerifier getVerificationKeyResolver(String iss, String kid) throws JOSEException {
        JWK jwk = jsonWebKeySetRepository.get(iss, kid).getKeyByKeyId(kid);
        return JKSVerifierFactory.get(jwk);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
