package io.kodokojo.docker.service.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.kodokojo.docker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;

public class RequestWorker extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestWorker.class);

    private static final String PUSH = "push";

//    private final JsonParser parser;

    private static final Function<JsonObject, PushEvent> PUSH_EVENT_FUNCTION = event -> {
        JsonObject target = event.getAsJsonObject("target");
        int size = target.getAsJsonPrimitive("size").getAsInt();
        String digest = target.getAsJsonPrimitive("digest").getAsString();
        Layer layer = new Layer(digest, size);
        ImageName name = new StringToImageNameConverter().apply(target.getAsJsonPrimitive("repository").getAsString());
        return new PushEvent(new Date(), null, new Image(name, new ArrayList<>()), layer);
    };

    public RequestWorker() {
        //parser = new JsonParser();
        receive(ReceiveBuilder
                .match(String.class, request -> {
                    //LOGGER.info("Receive Request, parsing body {}", request);
                    JsonParser parser = new JsonParser();
                    JsonElement jsonElement = parser.parse(request);
                    JsonArray events = jsonElement.getAsJsonObject().getAsJsonArray("events");
                    //LOGGER.info("Json events size : {}", events.size());
                    for (JsonElement eventElement : events) {
                        LOGGER.info("Receive Request, parsing push event");
                        JsonObject jsObjEvent = eventElement.getAsJsonObject();
                        String action = jsObjEvent.getAsJsonPrimitive("action").getAsString();
                        if (PUSH.equals(action)) {
                            PushEvent pushEvent = PUSH_EVENT_FUNCTION.apply(jsObjEvent);
                            sender().tell(pushEvent, self());
                            LOGGER.info("Receive Request, dispatching push event to master {}.", pushEvent);
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Receive a {} action", action);
                            }
                        }
                    }
                })
                .matchAny(o -> LOGGER.warn("Master Receive not machting object {}.", o.toString())).build());
    }

}
