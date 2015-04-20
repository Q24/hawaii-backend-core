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
package io.kahu.hawaii.util.call.http.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CxfConfigurator implements ApplicationContextAware {
    private static final String TRANSPORT_IDENTIFIER = "http://cxf.apache.org/transports/http";
    private final ConduitInitiator conduitInitiator;

    public CxfConfigurator(ConduitInitiator conduitInitiator) {
        this.conduitInitiator = conduitInitiator;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Bus bus = BusFactory.getThreadDefaultBus();

        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator(TRANSPORT_IDENTIFIER, conduitInitiator);
    }

}
