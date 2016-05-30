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

import com.nimbusds.jwt.JWT;

public class
ClientTokenGenerator {
    static String jwk = "{\"keys\":[{\n" +
            "        \"p\": \"58NCrVZwYQmyGbzdoiHfVkIES1xLViXvRuxOfRGujT2TtwnWkzTTMDEnhhNljhZ6c98gLIACknmNjN_Lrky0JO1079tYrORcU5OEF7K2V1BBoIH6sxEvl6918_lLJjyicu25YTFJM-qQAeuVVb-Y0fWz91Md50IN4f2a8PhRczs\",\n" +
            "            \"kty\": \"RSA\",\n" +
            "            \"q\": \"0XtnBQuIZXTRCp256OVU--U-REy_FOhkVmuqhFCWUi8cvPz50T9yaCMy22tdbRqKnpy2DhXCU20T-oJhzLzINvpF4Bu2TnJ2TX8ymHTF35Zk21rV6Ybvw0DQ6G6H4rO3MK_x-OgzW2vmV6ejzUWG0YbzhTewM7S4pfwRYq6HpNc\",\n" +
            "            \"d\": \"SN7y9iZXSJA1tKx8W1CBFzizBciUIVoP58vL_9HdoJfsXyH9tzNwoCW7iwQ2-SOses8jswu-KEM353ycWQgMbP3JOq0ChnPpuE6hwEZHJR6uJ6wyUkiP9dGx8gH6u76y67JGWRphWK3Vb9jN0T1IgLdvdVIGlP4qnY-zNsDKbwHxOGZAHCPpjcWZYSFRIGM2TlGvXt1pHkz9de-fKXYZcIn67mc53Vzih4uheAECADr7pKYlxFwTAADFPBKXpKFjE_IY8TUtBU_Y_xMeaPqOKAVdFiFZGmW2RxYFJUrQxy1dH_VIQkEtIvW4vP7vgcDhfdzAQ8j-gPp9ivvlQ21xiQ\",\n" +
            "            \"e\": \"AQAB\",\n" +
            "            \"kid\": \"my_key_id\",\n" +
            "            \"qi\": \"s-hTaYmc1s9jRKhdFtk7t9o_eeLTqYT5v1pWlLIEjhV1sDz87talMVtTriw_81V16xGnM5gDAK-FkkgCSpJAySLkboE824K1cJDYVFBk_jXR_4unhpjEdZqp1X-k3KHMvb-kXwxVO4CcIrs80V_8w2r4cYVIHb7wT-Isv3mznsg\",\n" +
            "            \"dp\": \"Q3TGvHLzRJsw1mLtU1puicmvPEwBtXJXIZp4AdkaqAtrKhRX-wKeEWHNMi4FGgUa4QzpbWpY1U9BzuX4f6Pdrv3aUlHSjoftA3plMQyYG_PFFjWexW3UH183gqploBx33-GnWk2hE9ZK0fRCw_F_RmUwW2U6x-um5McOyOJNuq8\",\n" +
            "            \"dq\": \"sTOgK8ZbtOciRcayku4b-5EqIQCCyx7icbBV5-N7pT2xI7UjZfwzvR-5T578wuQmypFp93Q0k_m7YreUH2q6OrdvEGqgxq9Qn7GvjQOKtt4zptKqDQdWHmbf0R5e2EiJ-Cd9mr5hYz5c9V0b1PKxXuC3csNuy-5eHBqkYkOc_rU\",\n" +
            "            \"n\": \"vaYhhK3vMdQbIcDHK4oeiS2BZcxV5uRXSKSldTEyoZ1HF2KuA2MJCyVD79AzDAsJUSQdYbCDoRqylT3zKri15oaznhAyS724MI5tFx4lfG4R6oM-LhoB0DyWQBXs58yWUYCzsA_iQ9wdf5shLqcbH3P0f_GlBlSy9ZfeEg4Kh6phNfSVrYvFwk-SS9u9OYzAcxkkSu6QLeIJqVou4KUnYs1Ruv9nFT9u8B41XvzTZOBZ8mksHSNK_gffTr0P65aAwqLExoHHS1Ldu5edl5XS7zm9Q_Fwp3s-CNx3TtYBDMvJM-1FO_a_uq_3uXwTJdJEi0u1jvxneRKuAtZQmFaSjQ\"\n" +
            "    }]}";

//
//    tapir@ucalegon ~/mitre/src/hawaii-sso/sso/json-web-key-generator $ java -jar target/json-web-key-generator-0.3-SNAPSHOT-jar-with-dependencies.jar -p RS256 -i my_key_id -p -s 2048 -t RSA
//    Full key:
//    {
//        "p": "58NCrVZwYQmyGbzdoiHfVkIES1xLViXvRuxOfRGujT2TtwnWkzTTMDEnhhNljhZ6c98gLIACknmNjN_Lrky0JO1079tYrORcU5OEF7K2V1BBoIH6sxEvl6918_lLJjyicu25YTFJM-qQAeuVVb-Y0fWz91Md50IN4f2a8PhRczs",
//            "kty": "RSA",
//            "q": "0XtnBQuIZXTRCp256OVU--U-REy_FOhkVmuqhFCWUi8cvPz50T9yaCMy22tdbRqKnpy2DhXCU20T-oJhzLzINvpF4Bu2TnJ2TX8ymHTF35Zk21rV6Ybvw0DQ6G6H4rO3MK_x-OgzW2vmV6ejzUWG0YbzhTewM7S4pfwRYq6HpNc",
//            "d": "SN7y9iZXSJA1tKx8W1CBFzizBciUIVoP58vL_9HdoJfsXyH9tzNwoCW7iwQ2-SOses8jswu-KEM353ycWQgMbP3JOq0ChnPpuE6hwEZHJR6uJ6wyUkiP9dGx8gH6u76y67JGWRphWK3Vb9jN0T1IgLdvdVIGlP4qnY-zNsDKbwHxOGZAHCPpjcWZYSFRIGM2TlGvXt1pHkz9de-fKXYZcIn67mc53Vzih4uheAECADr7pKYlxFwTAADFPBKXpKFjE_IY8TUtBU_Y_xMeaPqOKAVdFiFZGmW2RxYFJUrQxy1dH_VIQkEtIvW4vP7vgcDhfdzAQ8j-gPp9ivvlQ21xiQ",
//            "e": "AQAB",
//            "kid": "my_key_id",
//            "qi": "s-hTaYmc1s9jRKhdFtk7t9o_eeLTqYT5v1pWlLIEjhV1sDz87talMVtTriw_81V16xGnM5gDAK-FkkgCSpJAySLkboE824K1cJDYVFBk_jXR_4unhpjEdZqp1X-k3KHMvb-kXwxVO4CcIrs80V_8w2r4cYVIHb7wT-Isv3mznsg",
//            "dp": "Q3TGvHLzRJsw1mLtU1puicmvPEwBtXJXIZp4AdkaqAtrKhRX-wKeEWHNMi4FGgUa4QzpbWpY1U9BzuX4f6Pdrv3aUlHSjoftA3plMQyYG_PFFjWexW3UH183gqploBx33-GnWk2hE9ZK0fRCw_F_RmUwW2U6x-um5McOyOJNuq8",
//            "dq": "sTOgK8ZbtOciRcayku4b-5EqIQCCyx7icbBV5-N7pT2xI7UjZfwzvR-5T578wuQmypFp93Q0k_m7YreUH2q6OrdvEGqgxq9Qn7GvjQOKtt4zptKqDQdWHmbf0R5e2EiJ-Cd9mr5hYz5c9V0b1PKxXuC3csNuy-5eHBqkYkOc_rU",
//            "n": "vaYhhK3vMdQbIcDHK4oeiS2BZcxV5uRXSKSldTEyoZ1HF2KuA2MJCyVD79AzDAsJUSQdYbCDoRqylT3zKri15oaznhAyS724MI5tFx4lfG4R6oM-LhoB0DyWQBXs58yWUYCzsA_iQ9wdf5shLqcbH3P0f_GlBlSy9ZfeEg4Kh6phNfSVrYvFwk-SS9u9OYzAcxkkSu6QLeIJqVou4KUnYs1Ruv9nFT9u8B41XvzTZOBZ8mksHSNK_gffTr0P65aAwqLExoHHS1Ldu5edl5XS7zm9Q_Fwp3s-CNx3TtYBDMvJM-1FO_a_uq_3uXwTJdJEi0u1jvxneRKuAtZQmFaSjQ"
//    }
//
//    Public key:
//    {
//        "kty": "RSA",
//            "e": "AQAB",
//            "kid": "my_key_id",
//            "n": "vaYhhK3vMdQbIcDHK4oeiS2BZcxV5uRXSKSldTEyoZ1HF2KuA2MJCyVD79AzDAsJUSQdYbCDoRqylT3zKri15oaznhAyS724MI5tFx4lfG4R6oM-LhoB0DyWQBXs58yWUYCzsA_iQ9wdf5shLqcbH3P0f_GlBlSy9ZfeEg4Kh6phNfSVrYvFwk-SS9u9OYzAcxkkSu6QLeIJqVou4KUnYs1Ruv9nFT9u8B41XvzTZOBZ8mksHSNK_gffTr0P65aAwqLExoHHS1Ldu5edl5XS7zm9Q_Fwp3s-CNx3TtYBDMvJM-1FO_a_uq_3uXwTJdJEi0u1jvxneRKuAtZQmFaSjQ"
//    }
    public static void main(String[] args) throws Exception {
        JsonWebKeySetRepository jsonWebKeySetRepository = new SimpleJsonWebKeySetRepository(jwk);
        JwtTokenGenerator generator = new JwtTokenGenerator(jsonWebKeySetRepository);

        JWT token = generator.generate("auto-login-web-app", "user@example.com", "my_key_id");

        System.err.println(token.serialize());

        JwtTokenDecoder decoder = new JwtTokenDecoder(jsonWebKeySetRepository);
        decoder.decode(token.serialize());
    }
}
