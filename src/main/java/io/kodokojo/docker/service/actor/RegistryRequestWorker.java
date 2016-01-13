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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import io.kodokojo.docker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;

public class RegistryRequestWorker extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryRequestWorker.class);

    private static final String PUSH = "push";

    public static final String TARGET = "target";

    public static final String SIZE = "size";

    public static final String DIGEST = "digest";

    public static final String REPOSITORY = "repository";

    public static final String EVENTS = "events";

    public static final String ACTION = "action";

    public static final String REQUEST = "request";

    public static final String METHOD = "method";

    @Inject
    public RegistryRequestWorker() {
        JsonParser parser = new JsonParser();
        receive(ReceiveBuilder
                .match(RestRequest.class, request -> {
                    JsonElement jsonElement = parser.parse(request.getBody());
                    JsonArray events = jsonElement.getAsJsonObject().getAsJsonArray(EVENTS);

                    for (JsonElement eventElement : events) {
                        JsonObject jsObjEvent = eventElement.getAsJsonObject();
                        String action = jsObjEvent.getAsJsonPrimitive(ACTION).getAsString();
                        RegistryEvent registryEvent = PUSH_EVENT_FUNCTION.apply(jsObjEvent);
                        sender().tell(registryEvent, self());
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Receive a {} action", action);
                        }

                    }
                })
                .matchAny(this::unhandled).build());
    }

    private static final Function<JsonObject, RegistryEvent> PUSH_EVENT_FUNCTION = event -> {
        JsonObject target = event.getAsJsonObject(TARGET);
        int size = target.getAsJsonPrimitive(SIZE).getAsInt();
        String digest = target.getAsJsonPrimitive(DIGEST).getAsString();
        RegistryEvent.EventType eventType = RegistryEvent.EventType.valueOf(event.getAsJsonPrimitive(ACTION).getAsString().toUpperCase());

        JsonObject request = event.getAsJsonObject(REQUEST);
        RegistryEvent.EventMethod method = RegistryEvent.EventMethod.valueOf(request.getAsJsonPrimitive(METHOD).getAsString().toUpperCase());


        Layer layer = new Layer(digest, size);
        ImageName name = new StringToImageNameConverter().apply(target.getAsJsonPrimitive(REPOSITORY).getAsString());
        return new RegistryEvent(new Date(), eventType, method, null, new Image(name, new ArrayList<>()), layer);
    };

}
