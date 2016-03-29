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
package io.kahu.hawaii.util.pagination;

import io.kahu.hawaii.util.json.AbstractJsonWriter;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.data.domain.Page;

public class PageMetadataWriter extends AbstractJsonWriter<Page<?>> implements PageMetadataKeyConstants {

    @Override
    protected void writeJson(Page<?> page, JSONObject json) throws JSONException {
        json.put(TOTAL_ELEMENTS, page.getTotalElements());
        if (!page.hasContent() && page.getTotalElements() > 0) {
            json.put(TOO_MANY_ELEMENTS, true);
        } else {
            json.put(SIZE, page.getSize());
            json.put(TOTAL_PAGES, page.getTotalPages());
            json.put(NUMBER, page.getNumber());
            json.put(NUMBER_OF_ELEMENTS, page.getNumberOfElements());
        }
    }
}
