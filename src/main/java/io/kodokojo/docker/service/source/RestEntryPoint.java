package io.kodokojo.docker.service.source;

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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.inject.Inject;
import io.kodokojo.docker.model.HttpVerbe;
import io.kodokojo.docker.model.RestRequest;
import io.kodokojo.docker.model.StringToDockerFileConverter;
import io.kodokojo.docker.service.DefaultDockerImageRepository;
import io.kodokojo.docker.service.DockerImageRepository;
import io.kodokojo.docker.service.actor.PushEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Set;

import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

public class RestEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEntryPoint.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final ActorRef pushEventDispatcher;

    @Inject
    public RestEntryPoint(@Named("pushEventDispatcher") ActorRef pushEventDispatcher) {
        if (pushEventDispatcher == null) {
            throw new IllegalArgumentException("pushEventDispatcher must be defined.");
        }
        this.pushEventDispatcher = pushEventDispatcher;
    }

    public void start() {
        LOGGER.info("Starting Docker image manager RestEntryPoint");

        Spark.port(8080);

        get("/api", JSON_CONTENT_TYPE, (request, response) -> {
            response.type(JSON_CONTENT_TYPE);
            return "{\"version\":\"1.0.0\"}";
        });

        post("/registry/events", JSON_CONTENT_TYPE, (request, response) -> {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Receive a push notification, sending to pushEventDispatcher. : {}", request.body());
            }
//            LOGGER.debug("Receive a push notification, sending to pushEventDispatcher. : {}", request.body());

            HashMap<String, String> header = new HashMap<>();
            Set<String> headerKeys = request.headers();
            for (String headerKey : headerKeys) {
                header.put(headerKey, request.headers(headerKey));
            }

            RestRequest restRequest = new RestRequest(HttpVerbe.POST, header, request.body());

            pushEventDispatcher.tell(restRequest, ActorRef.noSender());
            halt(200);
            return null;
        });

        //get("/repository/");

        LOGGER.info("Docker image manager RestEntryPoint started");
    }

    public void stop() {
        LOGGER.info("Stopping registry listener");
        Spark.stop();
    }

}
