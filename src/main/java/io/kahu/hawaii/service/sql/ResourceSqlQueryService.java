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
package io.kahu.hawaii.service.sql;

import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class ResourceSqlQueryService implements SqlQueryService {

    private final Map<String, String> cachedQueries = new HashMap<String, String>();

    private final LogManager logManager;

    public ResourceSqlQueryService(LogManager logManager) {
        this.logManager = logManager;
    }

    @Override
    public String getSqlQuery(String resourceName, String queryName) throws ServerException {
        String resourcePath = buildResourcePath(resourceName, queryName);
        if (!cachedQueries.containsKey(resourcePath)) {
            logManager.debug(CoreLoggers.SERVER, "Loading " + resourcePath + " and adding it to the cache.");
            cachedQueries.put(resourcePath, loadSqlQuery(resourcePath));
        } else {
            logManager.debug(CoreLoggers.SERVER, "Getting SQL query defined in '" + resourcePath + "' from the cache.");
        }
        return cachedQueries.get(resourcePath);
    }

    private String buildResourcePath(String path, String queryId) {
        StringBuilder builder = new StringBuilder();
        if (path != null) {
            if (!path.startsWith("/")) {
                builder.append("/");
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            builder.append(path);
        }
        builder.append("/").append(queryId);
        if (!queryId.endsWith(".sql")) {
            builder.append(".sql");
        }
        return builder.toString();
    }

    private String loadSqlQuery(String resourcePath) throws ServerException {
        List<String> lines = readQuery(resourcePath);
        String query = makeQuery(lines);
        return query;
    }

    private List<String> readQuery(String resourcePath) throws ServerException {
        InputStream resource = getClass().getResourceAsStream(resourcePath);
        if (resource == null) {
            throw new ServerException(ServerError.IO, "error reading SQL query from file");
        }
        try {
            return IOUtils.readLines(resource);
        } catch (IOException e) {
            throw new ServerException(ServerError.IO, "error reading SQL query from file");
        }
    }

    private String makeQuery(List<String> lines) {
        StringBuilder query = new StringBuilder();
        boolean isComment = false;
        boolean inBlockComment = false;
        for (String line : lines) {
            line = StringUtils.trim(line);

            if (line.startsWith("/*")) {
                inBlockComment = true;
            }

            if (line.startsWith("--")) {
                isComment = true;
            }

            if (line.endsWith(";")) {
                // Oracle does not like SQL statements via JDBC that end with
                // ';'.
                line = line.substring(0, line.length() - 1);
            }

            if (StringUtils.isNotBlank(line) && !(isComment || inBlockComment)) {
                if (query.length() != 0) {
                    query.append(" ");
                }
                query.append(line);
            }

            isComment = false;
            if (line.startsWith("*/")) {
                inBlockComment = false;
            }
        }
        return query.toString();
    }
}
