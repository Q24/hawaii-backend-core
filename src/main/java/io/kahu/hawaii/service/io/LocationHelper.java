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

import org.springframework.util.Assert;

import java.io.File;

public class LocationHelper {

    private String hawaiiWorkspaceHome = null;

    public String getHawaiiWorkspaceHomeRoot() {
        if (hawaiiWorkspaceHome == null) {
            hawaiiWorkspaceHome = System.getenv("HAWAII__WORKSPACE_HOME");
        }
        return hawaiiWorkspaceHome;
    }

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

    private String hawaiiCsvHome = null;

    public String getHawaiiCsvHome() {
        if (hawaiiCsvHome == null) {
            //use server-home as a default:
            setHawaiiCsvHome(getHawaiiServerHome());
        }
        return hawaiiCsvHome;
    }

    public void setHawaiiCsvHome(String hawaiiCsvHome) {
        Assert.hasLength(hawaiiCsvHome);
        if(!hawaiiCsvHome.endsWith(File.separator)) {
            hawaiiCsvHome += File.separator;
        }
        this.hawaiiCsvHome = hawaiiCsvHome;
    }
}
