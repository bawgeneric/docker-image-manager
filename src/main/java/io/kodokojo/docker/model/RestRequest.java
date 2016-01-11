package io.kodokojo.docker.model;

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
