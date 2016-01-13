package io.kodokojo.docker.model;

/*
 * #%L
 * docker-image-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class RestRequest {

    private final HttpVerbe action;

    private final Map<String, String> headers;

    private final String body;

    public RestRequest(HttpVerbe action, Map<String, String> headers, String body) {
        if (action == null) {
            throw new IllegalArgumentException("action must be defined.");
        }
        if (headers == null) {
            throw new IllegalArgumentException("headers must be defined.");
        }
        if (isBlank(body)) {
            throw new IllegalArgumentException("body must be defined.");
        }
        this.action = action;
        this.headers = headers;
        this.body = body;
    }

    public HttpVerbe getAction() {
        return action;
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "RestRequest{" +
                "action='" + action + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
