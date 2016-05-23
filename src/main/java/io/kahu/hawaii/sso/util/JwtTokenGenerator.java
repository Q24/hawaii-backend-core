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
