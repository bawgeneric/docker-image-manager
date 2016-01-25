package io.kodokojo.docker.service.back;

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

import io.kodokojo.commons.docker.model.*;
import io.kodokojo.docker.model.*;

public interface DockerFileBuildOrchestrator {

    /**
     * Publish a {@link RegistryEvent} to the <code>DockerFileBuildOrchestrator</code>.
     * @param registryEvent The event to publish
     * @return <code>true</code> if the event create a new {@link DockerFileBuildPlan}.
     */
    DockerFileBuildPlan receiveUpdateEvent(RegistryEvent registryEvent);

    DockerFileBuildPlan getBuildPlan(ImageName imageName);

    DockerFileBuildPlanResult receiveDockerBuildRequest(DockerFileBuildRequest dockerFileBuildRequest);

    /**
     * Publish a {@link DockerFileBuildResponse} to the Orchestrator which may return a {@link DockerFileBuildPlanResult} in case of build completed.
     * @param dockerFileBuildResponse The build response sent by tht {@link io.kodokojo.docker.service.back.build.DockerImageBuilder}
     * @return a result of the Build plan, <code>null</code> if Build plan is not finish.
     */
    DockerFileBuildPlanResult receiveDockerBuildResponse(DockerFileBuildResponse dockerFileBuildResponse);
}
