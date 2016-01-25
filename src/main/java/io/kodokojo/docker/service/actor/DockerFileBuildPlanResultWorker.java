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
import io.kodokojo.docker.model.DockerFileBuildPlanResult;
import io.kodokojo.docker.service.back.DockerFileBuildPlanResultListener;

public class DockerFileBuildPlanResultWorker extends AbstractActor {

    public DockerFileBuildPlanResultWorker(DockerFileBuildPlanResultListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must be defined.");
        }
        receive(ReceiveBuilder.match(DockerFileBuildPlanResult.class, listener::receiveDockerFileBuildPlanResult)
                .matchAny(this::unhandled)
        .build());
    }
}
