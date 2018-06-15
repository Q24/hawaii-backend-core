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
package io.kahu.hawaii.util.call.http;

import io.kahu.hawaii.util.exception.ServerException;

import java.util.HashMap;
import java.util.Map;

public class TestHttpHeaderProvider implements HttpHeaderProvider {

    @Override
    public Map<String, String> getHeaders() throws ServerException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("TestHeader1", "TestValue1");
        headers.put("TestHeader2", "TestValue2");
        return headers;
    }

}
