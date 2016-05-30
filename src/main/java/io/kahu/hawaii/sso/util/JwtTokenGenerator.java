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

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.UUID;

public class JwtTokenGenerator {
    private JsonWebKeySetRepository jsonWebKeySetRepository;

    public JwtTokenGenerator(JsonWebKeySetRepository jsonWebKeySetRepository) {
        this.jsonWebKeySetRepository = jsonWebKeySetRepository;
    }

    public JWT generate(String iss, String sub, String kid) throws Exception {
        JWKSet jwkSet = jsonWebKeySetRepository.get(iss, sub);


        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issuer(iss)
                .issueTime(new Date())
                .subject(sub)
                .jwtID(UUID.randomUUID().toString()); // set a random NONCE in the middle of it

        JWTClaimsSet claims = builder.build();

        JWK jwk = jwkSet.getKeyByKeyId(kid);

        JWSSigner signer = JKSSignerFactory.get(jwk);
        Algorithm algorithm = jwk.getAlgorithm();
        JWSAlgorithm alg;
        if (algorithm == null) {
             alg = JWSAlgorithm.RS256;
        } else {
            alg = JWSAlgorithm.parse(algorithm.getName());
        }

        JWSHeader header = new JWSHeader(alg, null, null, null, null, null, null, null, null, null, kid, null, null);

        SignedJWT signed = new SignedJWT(header, claims);
        signed.sign(signer);



        return signed;
    }

}
