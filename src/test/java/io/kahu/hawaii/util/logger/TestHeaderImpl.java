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
package io.kahu.hawaii.util.logger;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * A derived Header implementation that implements equals(). This allows two
 * lists of Headers to be compared for equality using .equals().
 * 
 * @author ErnstJan.Plugge
 */
public class TestHeaderImpl extends BasicHeader {
    public TestHeaderImpl(String name, String value) {
        super(name, value);
        assert name != null;
        assert value != null;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() ^ getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Header)) {
            return false;
        }
        Header that = (Header) obj;
        return this.getName().equals(that.getName()) && this.getValue().equals(that.getValue());
    }
}
