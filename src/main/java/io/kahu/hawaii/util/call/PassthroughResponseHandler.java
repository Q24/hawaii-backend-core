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
package io.kahu.hawaii.util.call;

import io.kahu.hawaii.util.exception.ServerException;
import org.apache.http.annotation.ThreadSafe;

/**
 * Warning, this class exposes the results of the backend request directly to the caller. The result
 * may be stale, closed, null, non existant or faulty g=by then.
 *
 * <em>This leads to considerable resource leaks.</em> Suggested usage: Only for testing purposes.
 * @param <T>
 */
@ThreadSafe
public class PassthroughResponseHandler<T> implements ResponseHandler<T, T>{
    @Override
    public void addToResponse(T payload, Response<T> response) throws ServerException {
        if (payload != null) {
            response.setRawPayload(payload.toString());
        }
        response.set(payload);
    }
}
