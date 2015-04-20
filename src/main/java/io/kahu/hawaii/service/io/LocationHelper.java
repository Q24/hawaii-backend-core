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
package io.kahu.hawaii.service.io;

import java.io.File;

public class LocationHelper {

    private String hawaiiServerHome = null;

    public String getHawaiiServerHome() {
        if (hawaiiServerHome == null) {
            hawaiiServerHome = System.getenv("HAWAII_SERVER_HOME");
            if(!hawaiiServerHome.endsWith(File.separator)){
                hawaiiServerHome = hawaiiServerHome + File.separator;
            }
        }
        return hawaiiServerHome;
    }

    private String hawaiiClientDocRoot = null;

    public String getHawaiiClientDocRoot() {
        if (hawaiiClientDocRoot == null) {
            hawaiiClientDocRoot = System.getenv("HAWAII_CLIENT_DOCROOT");
        }

        return hawaiiClientDocRoot;
    }

    private String hawaiiDocumentationDocRoot = null;

    public String getHawaiiDocumentationDocRoot() {
        if (hawaiiDocumentationDocRoot == null) {
            hawaiiDocumentationDocRoot = System.getenv("HAWAII_DOCUMENTATION_DOCROOT");
        }
        return hawaiiDocumentationDocRoot;
    }
}
