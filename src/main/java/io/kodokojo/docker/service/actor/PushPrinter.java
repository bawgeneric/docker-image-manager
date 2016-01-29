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
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.docker.model.RegistryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushPrinter extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushPrinter.class);


    public PushPrinter() {
        receive(ReceiveBuilder.match(RegistryEvent.class, p -> LOGGER.info("Listener receive Push event {}", p)).matchAny(o -> LOGGER.error("Listener Unexpected object receive {}.", o))
                        .build()
        );
    }
}
