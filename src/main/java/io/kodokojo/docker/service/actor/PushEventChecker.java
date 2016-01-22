package io.kodokojo.docker.service.actor;

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

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.commons.model.ImageName;
import io.kodokojo.docker.model.RegistryEvent;
import io.kodokojo.docker.service.DockerImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class PushEventChecker extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushEventChecker.class);

    private final DockerImageRepository dockerImageRepository;

    @Inject
    public PushEventChecker(DockerImageRepository dockerImageRepository, ActorRef dependencyDockerfileUpdateDispatcher) {
        if (dockerImageRepository == null) {
            throw new IllegalArgumentException("dockerImageRepository must be defined.");
        }
        this.dockerImageRepository = dockerImageRepository;
        receive(ReceiveBuilder.match(
                RegistryEvent.class, event -> {
                    ImageName name = event.getImage().getName();
                    boolean alreadyExist = this.dockerImageRepository.addLayer(name, event.getSpecificLayer());
                    if (!alreadyExist) {
                        dependencyDockerfileUpdateDispatcher.tell(event, self());
                    } else if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Receive a push event for commons image {} which already exist.", name.getFullyQualifiedName());
                    }
                }
        ).matchAny(this::unhandled).build());
    }
}
