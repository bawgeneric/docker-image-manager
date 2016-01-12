package io.kodokojo.docker.service.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorWorker extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorWorker.class);

    public ErrorWorker() {
        receive(ReceiveBuilder.match(String.class, e -> {
            LOGGER.error("An Error Occur while processing a message: {}", e);
        }).build());
    }
}
