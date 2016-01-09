package io.kodokojo.docker.service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.docker.model.PushEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Master extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Master.class);

    public Master() {

        ActorRef requestWorker = getContext().actorOf(Props.create(RequestWorker.class));
        ActorRef pushPrinter = getContext().actorOf(Props.create(PushPrinter.class));

        receive(ReceiveBuilder.match(PushEvent.class, push -> {
                LOGGER.info("Receive Push event, dispatch this to push event writer.");
                pushPrinter.tell(push, self());
            })
            .match(String.class, request -> {
                LOGGER.info("Receive Request, dispatch this to requestWorker");
                requestWorker.tell(request, self());
            })
            .matchAny(this::unhandled)
            .build()
        );
    }
}