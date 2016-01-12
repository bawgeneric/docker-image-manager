package io.kodokojo.docker.service.actor;

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
