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

import com.nimbusds.jose.jwk.JWKSet;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.http.HttpRequestBuilder;

public class UrlJsonWebKeySetRepository implements JsonWebKeySetRepository {
    private static final long ONE_HOUR = 60 * 60 * 1000L;
    private final long cacheTime;
    private final HttpRequestBuilder<String> jwksRequest;

    private JWKSet jwks;
    private Long refreshTime = 0L;



    public UrlJsonWebKeySetRepository(HttpRequestBuilder<String> jwksRequest) {
        this(jwksRequest, ONE_HOUR);
    }

    public UrlJsonWebKeySetRepository(HttpRequestBuilder<String> jwksRequest, long cacheTime) {
        this.jwksRequest = jwksRequest;
        this.cacheTime = cacheTime;
    }

    @Override
    public JWKSet get(String iss, String kid) {
        boolean loadFromSso = false;
        if (jwks == null || refreshTime < System.currentTimeMillis()) {
            loadFromSso = true;
        } else {
            loadFromSso = !hasKey(jwks, kid);
        }

        if (loadFromSso) {
            try {
                Response<String> response =  jwksRequest.newInstance().build().execute();
                // TODO Get cache header from response
                jwks = JWKSet.parse(response.get());

                if (!hasKey(jwks, kid)) {
                    // TODO throw exception
                    System.err.println("Configuration mismatch, jwks '" + response.get() + "' does not contain '" + kid + "'.");
                }
                refreshTime = System.currentTimeMillis() + cacheTime;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return jwks;
    }

    private boolean hasKey(JWKSet jwks, String kid) {
        return jwks.getKeyByKeyId(kid) != null;
    }

}
