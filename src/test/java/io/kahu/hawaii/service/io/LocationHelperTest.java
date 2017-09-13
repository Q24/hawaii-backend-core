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

import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
public class LocationHelperTest {
  
    @Test
    public void assureThatHawaiiServerHomeIsRetrievedOkIf() {
        LocationHelper helper = new LocationHelper();
        String serverHome = helper.getHawaiiServerHome();
        assertThat("environment variable HAWAII_SERVER_HOME", serverHome, is(notNullValue()));
    }

    @Test
    public void assureThatHawaiiClientWorkspaceRootIsRetrievedOkIf() {
        LocationHelper helper = new LocationHelper();
        String workspaceRoot = helper.getHawaiiClientWorkspaceRoot();
        assertThat("environment variable HAWAII_CLIENT_WORKSPACE_ROOT", workspaceRoot, is(notNullValue()));
    }
    
    @Test
    public void assureThatHawaiiClientDocRootIsRetrievedOkIf() {
        LocationHelper helper = new LocationHelper();
        String hawaiiClientHome = helper.getHawaiiClientDocRoot();
        assertThat("environment variable HAWAII_CLIENT_DOCROOT", hawaiiClientHome, is(notNullValue()));
    }
    
    @Test
    public void assureThatHawaiiDocumentationDocRootIsRetrievedOk() {
        LocationHelper helper = new LocationHelper();
        String hawaiiDocumenationDocHome = helper.getHawaiiDocumentationDocRoot();
        assertThat("environment variable HAWAII_DOCUMENTATION_DOCROOT", hawaiiDocumenationDocHome, is(notNullValue()));
    }

    @Test
    public void assureThatHawaiiCsvHomeIsRetrievedOk() {
        LocationHelper helper = new LocationHelper();
        String expected = System.getenv("HAWAII_SERVER_HOME");
        if (!expected.endsWith(File.separator)) {
            expected += File.separator;
        }
        assertThat("environment variable HAWAII_CSV_HOME", helper.getHawaiiCsvHome(), is(expected));
        
        String otherValue = "/a/test/value";
        helper.setHawaiiCsvHome(otherValue);
        assertThat("environment variable get be set", helper.getHawaiiCsvHome(), is(otherValue + File.separator));
    }
}
