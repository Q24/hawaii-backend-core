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
package io.kahu.hawaii.service.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.validation.SchemaFactory.w3cXmlSchemaFromClasspath;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.validation.Schema;
import static org.hamcrest.CoreMatchers.not;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SiteMapGeneratorTest {

    private SiteMapGenerator siteMapGenerator;
    private OutputStreamWriter writer;
    private ByteArrayOutputStream outStream;
    
    @Before
    public void setUp() throws Exception {
        siteMapGenerator = new SiteMapGenerator();
        outStream = new ByteArrayOutputStream();
        writer = new OutputStreamWriter(outStream);
        
    }
    
    @After
    public void tearDown() throws Exception {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }
    
    @Test
    public void assureThatXmlcontainsFiles() throws Exception {
        
        List<String> fileList = new ArrayList<String>();
        String file1 = "\\klantenservice\\test.shtml";
        String file1Expectation = "/klantenservice/test.shtml";
        String file2 = "\\index.shtml";
        String file2Expectation = "/index.shtml";
        fileList.add(file1);
        fileList.add(file2);
        siteMapGenerator.writeSitemap(writer, fileList);
        String xml = outStream.toString("utf-8");
        
        // this hack is needed for now, the xmlns is snooped off in order to let the tests do their work.
        // this is an open issue on the website: http://code.google.com/p/xml-matchers/wiki/Tutorial
        xml = xml.replace("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">", "<urlset>");
       
        assertThat(the(xml), hasXPath("count(/urlset/url/loc)", equalTo("2")));
        assertThat(the(xml), hasXPath("/urlset/url[1]/loc",  containsString(file1Expectation)));
        assertThat(the(xml), hasXPath("/urlset/url[2]/loc",  containsString(file2Expectation)));
    }
    
    @Test
    public void assureThatXmlComformsToSchema() throws Exception {
        Schema schema = w3cXmlSchemaFromClasspath("sitemap.xsd");
                
        List<String> fileList = new ArrayList<String>();
        String file1 = "\\klantenservice\\test.shtml";
        String file2 = "\\index9.stml";
        fileList.add(file1);
        fileList.add(file2);
        siteMapGenerator.writeSitemap(writer, fileList);
        String xml = outStream.toString("utf-8");
        assertThat(the(xml), conformsTo(schema));
    }

    @Test
    public void assureThatRegexExcludedFilesAreExcluded() throws Exception {
        siteMapGenerator = new SiteMapGenerator("", "/co-browsing.shtml,/support2/.*,/consent-test.shtml");
        
        List<String> fileList = new ArrayList<String>();
        String file1 = "\\klantenservice\\test.shtml";
        String file1Expectation = "/klantenservice/test.shtml";
        String file2 = "\\index.shtml";
        String file2Expectation = "/index.shtml";
        String file3 = "\\support2\\index.shtml";       //should be filtered
        String file4 = "\\co-browsing.shtmlX";
        String file4Expectation = "/co-browsing.shtmlX";
        String file5 = "\\co-browsing.shtml";           //should be filtered
        fileList.add(file1);
        fileList.add(file2);
        fileList.add(file3);
        fileList.add(file4);
        fileList.add(file5);
        siteMapGenerator.writeSitemap(writer, fileList);
        String xml = outStream.toString("utf-8");
        
        // this hack is needed for now, the xmlns is snooped off in order to let the tests do their work.
        // this is an open issue on the website: http://code.google.com/p/xml-matchers/wiki/Tutorial
        xml = xml.replace("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">", "<urlset>");
       
        assertThat(the(xml), hasXPath("count(/urlset/url/loc)", equalTo("3")));
        assertThat(the(xml), hasXPath("/urlset/url[1]/loc",  containsString(file1Expectation)));
        assertThat(the(xml), hasXPath("/urlset/url[2]/loc",  containsString(file2Expectation)));
        assertThat(the(xml), hasXPath("/urlset/url[3]/loc", containsString(file4Expectation)));
        
        System.out.println(xml);
    }
}
