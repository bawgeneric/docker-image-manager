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
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.docker.model.RegistryEvent;
import io.kodokojo.docker.service.back.DockerFileBuildOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class DependencyDockerfileUpdateDispatcher extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyDockerfileUpdateDispatcher.class);

    private final DockerFileBuildOrchestrator dockerFileBuildOrchestrator;

    @Inject
    public DependencyDockerfileUpdateDispatcher(DockerFileBuildOrchestrator dockerFileBuildOrchestrator) {
        if (dockerFileBuildOrchestrator == null) {
            throw new IllegalArgumentException("dockerFileBuildOrchestrator must be defined.");
        }
        this.dockerFileBuildOrchestrator = dockerFileBuildOrchestrator;
        receive(ReceiveBuilder.match(RegistryEvent.class, push -> {
            boolean dockerFileBuildPlan = this.dockerFileBuildOrchestrator.receiveUpdateEvent(push);
            if (dockerFileBuildPlan) {
                LOGGER.info("Adding new DockerFileBuildPlan {}", dockerFileBuildOrchestrator.getBuildPlan(push.getImage().getName()));
            }
        }).build());
    }
}
