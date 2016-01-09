package io.kodokojo.docker.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import io.kodokojo.docker.service.actor.Master;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import static spark.Spark.halt;
import static spark.Spark.post;

public class RegistryListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryListener.class);

    private static final String JSON_CONTENT_TYPE = "application/json";

    private ActorSystem system;


    public void start() {
        LOGGER.info("Starting registry listener");
        system = ActorSystem.create("docker-image");

        Spark.port(8080);

        post("/events", JSON_CONTENT_TYPE, (request, response) -> {
            //LOGGER.info("Receive a push notification, sending to master. : {}", request.body());
            ActorRef master = system.actorOf(Props.create(Master.class));
            master.tell(request.body(), ActorRef.noSender());
            halt(200);
            return null;
        });
    }

    public void stop() {
        LOGGER.info("Stopping registry listener");
        Spark.stop();
        if (system != null) {
            system.shutdown();
            system = null;
        }
    }

}
