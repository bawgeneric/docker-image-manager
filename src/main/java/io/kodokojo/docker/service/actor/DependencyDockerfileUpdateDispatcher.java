package io.kodokojo.docker.service.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.docker.model.RegistryEvent;

public class DependencyDockerfileUpdateDispatcher extends AbstractActor {

    public DependencyDockerfileUpdateDispatcher() {
        receive(ReceiveBuilder.match(RegistryEvent.class, push -> {
            System.out.println("Create dependency graph for event " +push);
        }).build());
    }
}
