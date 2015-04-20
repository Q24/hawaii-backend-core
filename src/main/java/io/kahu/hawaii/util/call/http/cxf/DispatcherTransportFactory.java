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

import io.kahu.hawaii.util.call.dispatch.RequestConfigurations;
import io.kahu.hawaii.util.call.dispatch.RequestDispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import io.kahu.hawaii.util.logger.LogManager;

import org.apache.cxf.Bus;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http.DestinationRegistryImpl;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.http.AddressType;
import org.apache.cxf.wsdl11.WSDLEndpointFactory;

/**
 * extends AbstractTransportFactory implements WSDLEndpointFactory,
 * ConduitInitiator, DestinationFactory
 */
public class DispatcherTransportFactory extends AbstractTransportFactory implements WSDLEndpointFactory, ConduitInitiator {
    private final RequestConfigurations requestConfigurations;
    private final RequestDispatcher requestDispatcher;
    private final LogManager logManager;

    //@formatter:off
    public static final List<String> DEFAULT_NAMESPACES =
                Arrays.asList(
                            "http://cxf.apache.org/transports/http",
                            "http://cxf.apache.org/transports/http/configuration",
                            "http://schemas.xmlsoap.org/wsdl/http",
                            "http://schemas.xmlsoap.org/wsdl/http/");
    //@formatter:on

    /**
     * This constant holds the prefixes served by this factory.
     */
    private static final Set<String> URI_PREFIXES = new HashSet<String>();
    static {
        URI_PREFIXES.add("http://");
        URI_PREFIXES.add("https://");
    }

    private final DestinationRegistry registry;

    public DispatcherTransportFactory(RequestConfigurations requestConfigurations, RequestDispatcher requestDispatcher, Bus bus, LogManager logManager) {
        super(DEFAULT_NAMESPACES);
        this.registry = new DestinationRegistryImpl();
        this.requestConfigurations = requestConfigurations;
        this.requestDispatcher = requestDispatcher;
        this.logManager = logManager;
        setBus(bus);
    }

    @Override
    public void setBus(Bus b) {
        super.setBus(b);
    }

    public DestinationRegistry getRegistry() {
        return registry;
    }

    /**
     * This call is used by CXF ExtensionManager to inject the
     * activationNamespaces
     *
     * @param ans
     *            The transport ids.
     */
    public void setActivationNamespaces(Collection<String> ans) {
        setTransportIds(new ArrayList<String>(ans));
    }

    @Override
    public EndpointInfo createEndpointInfo(ServiceInfo serviceInfo, BindingInfo b, List<?> ees) {
        if (ees != null) {
            for (Object extensor : ees) {
                if (extensor instanceof HTTPAddress) {
                    final HTTPAddress httpAdd = (HTTPAddress) extensor;

                    EndpointInfo info = new HttpEndpointInfo(serviceInfo, "http://schemas.xmlsoap.org/wsdl/http/");
                    info.setAddress(httpAdd.getLocationURI());
                    info.addExtensor(httpAdd);
                    return info;
                } else if (extensor instanceof AddressType) {
                    final AddressType httpAdd = (AddressType) extensor;

                    EndpointInfo info = new HttpEndpointInfo(serviceInfo, "http://schemas.xmlsoap.org/wsdl/http/");
                    info.setAddress(httpAdd.getLocation());
                    info.addExtensor(httpAdd);
                    return info;
                }
            }
        }

        HttpEndpointInfo hei = new HttpEndpointInfo(serviceInfo, "http://schemas.xmlsoap.org/wsdl/http/");
        AddressType at = new HttpAddressType();
        hei.addExtensor(at);

        return hei;
    }

    @Override
    public void createPortExtensors(EndpointInfo ei, Service service) {
        // TODO
    }

    @Override
    public Set<String> getUriPrefixes() {
        return URI_PREFIXES;
    }

    private static class HttpEndpointInfo extends EndpointInfo {
        AddressType saddress;

        HttpEndpointInfo(ServiceInfo serv, String trans) {
            super(serv, trans);
        }

        @Override
        public void setAddress(String s) {
            super.setAddress(s);
            if (saddress != null) {
                saddress.setLocation(s);
            }
        }

        @Override
        public void addExtensor(Object el) {
            super.addExtensor(el);
            if (el instanceof AddressType) {
                saddress = (AddressType) el;
            }
        }
    }

    private static class HttpAddressType extends AddressType implements HTTPAddress, SOAPAddress {
        private static final long serialVersionUID = 7048265985129995746L;

        public HttpAddressType() {
            super();
            setElementType(new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address"));
        }

        @Override
        public String getLocationURI() {
            return getLocation();
        }

        @Override
        public void setLocationURI(String locationURI) {
            setLocation(locationURI);
        }

    }

    /**
     * This call creates a new HTTPConduit for the endpoint. It is equivalent to
     * calling getConduit without an EndpointReferenceType.
     */
    @Override
    public Conduit getConduit(EndpointInfo endpointInfo) throws IOException {
        return getConduit(endpointInfo, endpointInfo.getTarget());
    }

    /**
     * This call creates a new HTTP Conduit based on the EndpointInfo and
     * EndpointReferenceType. TODO: What are the formal constraints on
     * EndpointInfo and EndpointReferenceType values?
     */
    @Override
    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType target) throws IOException {
        return new HttpViaDispatcherConduit(bus, endpointInfo, target, requestConfigurations, requestDispatcher, logManager);
    }
}
