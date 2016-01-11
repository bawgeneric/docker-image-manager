package io.kodokojo.docker.service.source;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.inject.Inject;
import io.kodokojo.docker.model.HttpVerbe;
import io.kodokojo.docker.model.RestRequest;
import io.kodokojo.docker.model.StringToDockerFileConverter;
import io.kodokojo.docker.service.DefaultDockerImageRepository;
import io.kodokojo.docker.service.DockerImageRepository;
import io.kodokojo.docker.service.actor.PushEventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Set;

import static spark.Spark.halt;
import static spark.Spark.post;

public class RestEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEntryPoint.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final ActorRef pushEventDispatcher;

    @Inject
    public RestEntryPoint(@Named("pushEventDispatcher") ActorRef pushEventDispatcher) {
        if (pushEventDispatcher == null) {
            throw new IllegalArgumentException("pushEventDispatcher must be defined.");
        }
        this.pushEventDispatcher = pushEventDispatcher;
    }

    public void start() {
        LOGGER.info("Starting registry listener");


        Spark.port(8080);

        post("/registry/events", JSON_CONTENT_TYPE, (request, response) -> {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Receive a push notification, sending to pushEventDispatcher. : {}", request.body());
            }
//            LOGGER.debug("Receive a push notification, sending to pushEventDispatcher. : {}", request.body());

            HashMap<String, String> header = new HashMap<>();
            Set<String> headerKeys = request.headers();
            for (String headerKey : headerKeys) {
                header.put(headerKey, request.headers(headerKey));
            }

            RestRequest restRequest = new RestRequest(HttpVerbe.POST, header, request.body());

            pushEventDispatcher.tell(restRequest, ActorRef.noSender());
            halt(200);
            return null;
        });
    }

    public void stop() {
        LOGGER.info("Stopping registry listener");
        Spark.stop();
    }

}
