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

import io.kahu.hawaii.util.call.http.cxf.CxfConfigurator;
import io.kahu.hawaii.util.call.http.cxf.DispatcherTransportFactory;

import java.io.IOException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServiceClientsConfig {
    @Autowired
    private KahuConfig kahuConfig;

    @Bean(destroyMethod = "shutdown")
    public Bus cxf() {
        return BusFactory.getDefaultBus();
    }

    // @Bean
    // public BusWiringBeanFactoryPostProcessor
    // busWiringBeanFactoryPostProcessor() {
    // return new BusWiringBeanFactoryPostProcessor();
    // }
    //
    // @Bean
    // public BusExtensionPostProcessor busExtensionPostProcessor() {
    // return new BusExtensionPostProcessor();
    // }

    @Bean
    public DispatcherTransportFactory dispatcherTransportFactor() throws IOException, JSONException {
        return new DispatcherTransportFactory(kahuConfig.requestCongfigurations(), kahuConfig.requestDispatcher(), cxf(), kahuConfig.logManager());
    }

    @Bean
    public CxfConfigurator cxfConfigurator() throws IOException, JSONException {
        return new CxfConfigurator(dispatcherTransportFactor());
    }
}
