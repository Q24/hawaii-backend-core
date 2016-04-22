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
package io.kahu.hawaii.util.call.example.service;

import io.kahu.hawaii.util.call.example.domain.Person;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("client")
public class ClientResource {
    @GET
    @Path("{client-id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Person getPersonById(@PathParam("client-id") String clientId) {
        System.err.println("SERVER - In get client by id...");

        Person result = new Person();
        if ("10".equals(clientId)) {
            result.setId("10");
            result.setName("Kamehameha");
        }

        return result;
    }


}
