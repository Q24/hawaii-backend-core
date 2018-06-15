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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SiteMapGenerator {

    private String sitemapUrlDomain;
    private List<String> sitemapSkipPages;

    public SiteMapGenerator() {
    }

    public SiteMapGenerator(String sitemapUrlDomain, String sitemapSkipPagesProp) {
        this.sitemapUrlDomain = sitemapUrlDomain;
        this.sitemapSkipPages = ((sitemapSkipPagesProp == null || "".equalsIgnoreCase(sitemapSkipPagesProp)) ? null : Arrays.asList(sitemapSkipPagesProp.split(",")));
    }

    public void writeSitemap(Writer writer, List<String> fileList) throws IOException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Date now = new Date();

            doc.appendChild(doc.createComment("Generated on:" + now.toString()));
            Element rootElement = doc.createElementNS("http://www.sitemaps.org/schemas/sitemap/0.9", "urlset");

            doc.appendChild(rootElement);

            // URL elements
            for (String fileName : fileList) {
                // replace backslashes with forward slashes (on windows)
                fileName = fileName.replace('\\', '/');

                // if the file is part of the skip list, don't add it
                if (skipFilename(fileName)) continue;

                Element url = doc.createElement("url");
                rootElement.appendChild(url);

                Element loc = doc.createElement("loc");
                loc.appendChild(doc.createTextNode(sitemapUrlDomain + fileName));
                url.appendChild(loc);
                rootElement.appendChild(url);
            }
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            DOMSource source = new DOMSource(doc);

            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

    public void generateSiteMap(String rootDirectory) throws IOException {

        HtmlFileListGenerator generator = new HtmlFileListGenerator();
        List<String> fileList = generator.createListOfHtmlFiles(new File(rootDirectory));

        Collections.sort(fileList);

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(rootDirectory + "/sitemap.xml"));
            writeSitemap(writer, fileList);
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

        SiteMapGenerator sm = new SiteMapGenerator();
        LocationHelper docRootHelper = new LocationHelper();
        try {
            sm.generateSiteMap(docRootHelper.getHawaiiClientDocRoot());
            sm.generateSiteMap(docRootHelper.getHawaiiDocumentationDocRoot());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean skipFilename(String fileName) {
        if (sitemapSkipPages != null) {
            for (String skipPage: sitemapSkipPages) {
                if (fileName.matches(skipPage)) return true;
            }
        }
        return false;
    }
}
