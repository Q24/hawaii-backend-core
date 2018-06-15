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
package io.kahu.hawaii.util.pagination;

public interface PageMetadataKeyConstants {

    /**
     * Key indicating the content of the paged result (the actual data).
     */
    String CONTENT = "content";

    /**
     * Key indicating the page metadata.
     */
    String PAGE_METADATA = "page_metadata";

    /**
     * Key indicating the page size (number of elements per page).
     */
    String SIZE = "size";

    /**
     * Key indicating the total number of elements.
     */
    String TOTAL_ELEMENTS = "total_elements";

    /**
     * Key indicating the total number of pages.
     */
    String TOTAL_PAGES = "total_pages";

    /**
     * Key indicating the current page number.
     */
    String NUMBER = "number";

    /**
     * Key indicating the number of elements on the current page.
     */
    String NUMBER_OF_ELEMENTS = "number_of_elements";

    /**
     * Key indicating the number of elements was higher than the maximum.
     */
    String TOO_MANY_ELEMENTS = "too_many_elements";

}
