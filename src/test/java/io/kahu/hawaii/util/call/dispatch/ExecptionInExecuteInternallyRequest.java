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
package io.kahu.hawaii.util.call.dispatch;

import io.kahu.hawaii.util.call.AbstractAbortableRequest;
import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.log.CallLogger;
import io.kahu.hawaii.util.call.statistics.RequestStatistic;
import io.kahu.hawaii.util.exception.ServerException;
import org.apache.cxf.endpoint.Server;

public class ExecptionInExecuteInternallyRequest extends AbstractAbortableRequest<String, String> {

    private final RuntimeException runtimeException;
    private final ServerException serverException;
    private final Error error;

    public ExecptionInExecuteInternallyRequest(RequestContext<String> context, ResponseHandler<String, String> responseHandler, CallLogger<String> logger, ServerException serverException) {
        super(null, context, responseHandler, logger);
        setStatistic(new RequestStatistic());
        setResponse(new Response<>(this, this.getStatistic(), null));
        this.runtimeException = null;
        this.serverException = serverException;
        this.error = null;
    }
    public ExecptionInExecuteInternallyRequest(RequestContext<String> context, ResponseHandler<String, String> responseHandler, CallLogger<String> logger, RuntimeException runtimeException) {
        super(null, context, responseHandler, logger);
        setStatistic(new RequestStatistic());
        setResponse(new Response<>(this, this.getStatistic(), null));
        this.runtimeException = runtimeException;
        this.serverException = null;
        this.error = null;
    }
    public ExecptionInExecuteInternallyRequest(RequestContext<String> context, ResponseHandler<String, String> responseHandler, CallLogger<String> logger, Error error) {
        super(null, context, responseHandler, logger);
        setStatistic(new RequestStatistic());
        setResponse(new Response<>(this, this.getStatistic(), null));
        this.runtimeException = null;
        this.serverException = null;
        this.error = error;
    }

    public Response<String> doExecute() throws Throwable {
        if (error != null) {
            throw error;
        }
        return super.doExecute();
    }

    @Override
    protected void executeInternally(ResponseHandler<String, String> responseHandler, Response<String> response) throws ServerException {
        if (runtimeException != null) {
            throw runtimeException;
        }

        if (serverException != null) {
            throw serverException;
        }
    }

    @Override
    protected void abortInternally() {

    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getCallName() {
        return null;
    }

}
