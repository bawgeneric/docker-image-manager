package io.kodokojo.docker.service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.google.inject.Inject;
import io.kodokojo.docker.model.RegistryEvent;
import io.kodokojo.docker.model.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

public class PushEventDispatcher extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushEventDispatcher.class);

    @Inject
    public PushEventDispatcher(@Named("pushEventChecker") ActorRef pushEventChecker,@Named("registryRequestWorker")  ActorRef registryRequestWorker) {

        receive(ReceiveBuilder.match(RegistryEvent.class, push -> {
            if (LOGGER.isDebugEnabled()) {
                ActorRef pushPrinter = getContext().actorOf(Props.create(PushPrinter.class));
                pushPrinter.tell(push, self());
            }
            pushEventChecker.tell(push, self());
                })
                        .match(RestRequest.class, request -> {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Receive Request, dispatch this to registryRequestWorker");
                            }
                            registryRequestWorker.tell(request, self());
                        })

                        .matchAny(this::unhandled)
                        .build()
        );
    }
}