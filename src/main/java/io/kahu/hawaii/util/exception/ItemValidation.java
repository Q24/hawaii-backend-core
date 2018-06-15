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
package io.kahu.hawaii.util.exception;

import org.springframework.util.Assert;

public class ItemValidation {
    private String key;
    private HawaiiItemValidationError error;

    public ItemValidation(String key, HawaiiItemValidationError error) {
        Assert.hasLength(key);
        Assert.notNull(error);
        this.key = key;
        this.error = error;
    }

    public String getKey() {
        return key;
    }

    public HawaiiItemValidationError getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemValidation that = (ItemValidation) o;

        if (!error.equals(that.error)) return false;
        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + error.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("[ '%s', '%s' ]", key, error.getName());
    }

}
