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
package io.kahu.hawaii.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

public class HawaiiAppContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        env.getPropertySources().addFirst(getPropertySource());
    }

    private PropertySource<Object> getPropertySource() {
        List<Resource> resources = new ArrayList<Resource>();

        String configurationFiles = System.getProperty("properties.configuration");
        String[] files = StringUtils.split(configurationFiles, ",");
        for (String configFileName : files) {
            try {
                File file = new File(configFileName);
                if (file.exists()) {
                    resources.add(0, new FileSystemResource(file));
                } else {
                    System.err.println("Configured properties file '" + configFileName + "' does not exist.");
                }
            } catch (Throwable t) {
                System.err.print("Error loading properties from file '" + configFileName + "': " + t.getMessage());
                resources = null;
            }
        }

        try {
            CompositePropertySource propertySource = new CompositePropertySource("properties");
            for (Resource resource : resources) {
                propertySource.addPropertySource(new ResourcePropertySource(resource));
            }
            return propertySource;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
