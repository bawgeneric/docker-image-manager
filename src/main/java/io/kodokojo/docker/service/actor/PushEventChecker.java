package io.kodokojo.docker.service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.inject.Inject;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.RegistryEvent;
import io.kodokojo.docker.service.DockerImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushEventChecker extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushEventChecker.class);

    private final DockerImageRepository dockerImageRepository;

    @Inject
    public PushEventChecker(DockerImageRepository dockerImageRepository) {
        if (dockerImageRepository == null) {
            throw new IllegalArgumentException("dockerImageRepository must be defined.");
        }
        this.dockerImageRepository = dockerImageRepository;
        receive(ReceiveBuilder.match(
                RegistryEvent.class, event -> {
                    ImageName name = event.getImage().getName();
                    boolean alreadyExist = this.dockerImageRepository.addLayer(name, event.getSpecificLayer());
                    if (!alreadyExist) {
                        ActorRef dependencyDispatcher = getContext().actorOf(Props.create(DependencyDockerfileUpdateDispatcher.class));
                        dependencyDispatcher.tell(event, self());
                    } else if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Receive a push event for docker image {} which already exist.", name.getFullyQualifiedName());
                    }
                }
        ).matchAny(this::unhandled).build());
    }
}
