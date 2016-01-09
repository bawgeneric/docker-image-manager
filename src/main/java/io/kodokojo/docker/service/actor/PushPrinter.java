package io.kodokojo.docker.service.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.docker.model.PushEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jpthiery on 08/01/2016.
 */
public class PushPrinter extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushPrinter.class);


    public PushPrinter() {
        receive(ReceiveBuilder.match(PushEvent.class, p -> {
                    LOGGER.info("Listener receive Push event {}", p);
                }).matchAny(o -> LOGGER.error("Listener Unexpected object receive {}.", o))
                        .build()
        );
    }
}
