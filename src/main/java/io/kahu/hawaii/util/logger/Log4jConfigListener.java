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

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;

public class Log4jConfigListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String config = System.getenv("log4j.configuration");
        if (StringUtils.isBlank(config)) {
            config = System.getProperty("log4j.configuration");
        }
        if (StringUtils.isNotEmpty(config)) {
            String overrideConfig = StringUtils.replace(config, ".xml", ".local.xml");
            if (!configureAndWatch(overrideConfig)) {
                configureAndWatch(config);
                sce.getServletContext().log("Configured to watch log configuration '" + config + "'");
            } else {
                sce.getServletContext().log("Configured to watch log configuration '" + overrideConfig + "'");
            }
        }
    }

    private boolean configureAndWatch(String cfg) {
        File configFile = new File(cfg);
        if (configFile.exists()) {
            DOMConfigurator.configureAndWatch(cfg);
            return true;
        }

        return false;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

}
