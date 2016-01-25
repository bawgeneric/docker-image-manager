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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import io.kodokojo.commons.docker.model.*;
import io.kodokojo.docker.model.DockerFileBuildPlan;
import io.kodokojo.docker.model.DockerFileNode;
import io.kodokojo.docker.model.HttpVerbe;
import io.kodokojo.docker.model.RestRequest;
import io.kodokojo.docker.service.DockerFileRepository;
import io.kodokojo.docker.service.back.DockerFileBuildOrchestrator;
import io.kodokojo.docker.service.back.DockerFileNodeRepository;
import io.kodokojo.docker.utils.JsonTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ResponseTransformer;
import spark.Spark;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Set;

import static spark.Spark.*;

public class RestEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEntryPoint.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final ThreadLocal<Gson> localGson = new ThreadLocal<Gson>() {
        @Override
        protected Gson initialValue() {
            return new GsonBuilder().create();
        }
    };

    private final ResponseTransformer jsonResponseTransformer;

    private final ActorRef pushEventDispatcher;

    private final DockerFileRepository dockerFileRepository;

    private final DockerFileBuildOrchestrator dockerFileBuildOrchestrator;

    private final DockerFileNodeRepository dockerFileNodeRepository;

    @Inject
    public RestEntryPoint(@Named("pushEventDispatcher") ActorRef pushEventDispatcher, DockerFileRepository dockerFileRepository, DockerFileBuildOrchestrator dockerFileBuildOrchestrator, DockerFileNodeRepository dockerFileNodeRepository) {
        if (pushEventDispatcher == null) {
            throw new IllegalArgumentException("pushEventDispatcher must be defined.");
        }
        this.pushEventDispatcher = pushEventDispatcher;
        if (dockerFileRepository == null) {
            throw new IllegalArgumentException("dockerFileRepository must be defined.");
        }
        this.dockerFileRepository = dockerFileRepository;
        if (dockerFileBuildOrchestrator == null) {
            throw new IllegalArgumentException("dockerFileBuildOrchestrator must be defined.");
        }
        this.dockerFileBuildOrchestrator = dockerFileBuildOrchestrator;
        if (dockerFileNodeRepository == null) {
            throw new IllegalArgumentException("dockerFileNodeRepository must be defined.");
        }
        this.dockerFileNodeRepository = dockerFileNodeRepository;
        jsonResponseTransformer = new JsonTransformer();
    }

    public void start() {
        LOGGER.info("Starting Docker image manager RestEntryPoint");

        Spark.port(8080);


        before("/api/*", ((req, res) -> res.type(JSON_CONTENT_TYPE)));

        get("/api", JSON_CONTENT_TYPE, (request, response) -> {
            response.type(JSON_CONTENT_TYPE);
            return "{\"version\":\"1.0.0\"}";
        });

        post("/registry/events", JSON_CONTENT_TYPE, (request, response) -> {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Receive a push notification, sending to pushEventDispatcher. : {}", request.body());
            }

            HashMap<String, String> header = new HashMap<>();
            Set<String> headerKeys = request.headers();
            for (String headerKey : headerKeys) {
                header.put(headerKey, request.headers(headerKey));
            }

            RestRequest restRequest = new RestRequest(HttpVerbe.POST, header, request.body());

            pushEventDispatcher.tell(restRequest, ActorRef.noSender());
            halt(201);
            return null;
        });

        get("/api/dockerbuildplan/:namespace/:name/:tag", JSON_CONTENT_TYPE, (request, response) -> {

            String namespace = request.params(":namespace");
            String name = request.params(":name");
            String tag = request.params(":tag");

            ImageNameBuilder builder = new ImageNameBuilder();
            builder.setNamespace(namespace);
            builder.setName(name);
            builder.setTag(tag);
            ImageName imageName = builder.build();

            DockerFileBuildPlan dockerFileBuildPlan = dockerFileBuildOrchestrator.getBuildPlan(imageName);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request DockerFileBuildPlan for image {}, found : {}", imageName.getFullyQualifiedName(), dockerFileBuildPlan);
            }

            if (dockerFileBuildPlan == null) {
                halt(404);
            }

            return dockerFileBuildPlan;

        }, jsonResponseTransformer);

        get("/api/repository/:namespace/:name/:tag", JSON_CONTENT_TYPE, (request, response) -> {

            String namespace = request.params(":namespace");
            String name = request.params(":name");
            String tag = request.params(":tag");

            ImageNameBuilder builder = new ImageNameBuilder();
            builder.setNamespace(namespace);
            builder.setName(name);
            builder.setTag(tag);
            ImageName imageName = builder.build();
            DockerFile dockerFile = dockerFileRepository.getDockerFileFromImageName(imageName);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request DockerFile for image {}, found : {}", imageName.getFullyQualifiedName(), dockerFile);
            }

            if (dockerFile == null) {
                halt(404);
            }
            return dockerFile;

        }, jsonResponseTransformer);


        get("/api/dockernode/:namespace/:name/:tag", JSON_CONTENT_TYPE, (request, response) -> {

            String namespace = request.params(":namespace");
            String name = request.params(":name");
            String tag = request.params(":tag");

            ImageNameBuilder builder = new ImageNameBuilder();
            builder.setNamespace(namespace);
            builder.setName(name);
            builder.setTag(tag);
            ImageName imageName = builder.build();

            DockerFileNode dockerFileNode = dockerFileNodeRepository.getDockerFileNode(imageName);

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request DockerFileNode for image {}, found : {}", imageName.getFullyQualifiedName(), dockerFileNode);
            }

            if (dockerFileNode == null) {
                halt(404);
            }

            return dockerFileNode;

        }, jsonResponseTransformer);

        LOGGER.info("Docker image manager RestEntryPoint started");
    }

    public void stop() {
        LOGGER.info("Stopping registry listener");
        Spark.stop();
    }

}
