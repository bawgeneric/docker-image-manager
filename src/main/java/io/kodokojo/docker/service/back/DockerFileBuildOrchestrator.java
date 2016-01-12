package io.kodokojo.docker.service.back;

import io.kodokojo.docker.model.DockerFileBuildPlan;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.RegistryEvent;

public interface DockerFileBuildOrchestrator {

    /**
     * Publish a {@link RegistryEvent} to the <code>DockerFileBuildOrchestrator</code>.
     * @param registryEvent The event to publish
     * @return <code>true</code> if the event create a new {@link DockerFileBuildPlan}.
     */
    boolean receiveUpdateEvent(RegistryEvent registryEvent);

    DockerFileBuildPlan getBuildPlan(ImageName imageName);

}
