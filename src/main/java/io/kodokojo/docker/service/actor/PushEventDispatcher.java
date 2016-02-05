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

        receive(ReceiveBuilder.match(RegistryEvent.class, registryEvent -> {
            if (LOGGER.isDebugEnabled()) {
                ActorRef pushPrinter = getContext().actorOf(Props.create(PushPrinter.class));
                pushPrinter.tell(registryEvent, self());
            }
            pushEventChecker.tell(registryEvent, self());
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