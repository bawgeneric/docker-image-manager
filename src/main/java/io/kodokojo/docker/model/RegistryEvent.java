package io.kodokojo.docker.model;

import java.util.Date;

public class RegistryEvent {

    public enum EventType {
        PUSH,
        PULL;
    }

    public enum EventMethod {
        PUT,
        PULL,
        HEAD;
    }

    private final Date timestamp;

    private final EventType type;

    private final String actor;

    private final EventMethod method;

    private final Image image;

    private final Layer specificLayer;



    public RegistryEvent(Date timestamp, EventType type,EventMethod method,  String actor, Image image, Layer specificLayer) {
        this.timestamp = timestamp;
        this.type = type;
        this.method = method;
        this.actor = actor;
        this.image = image;
        this.specificLayer = specificLayer;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    public EventType getType() {
        return type;
    }

    public EventMethod getMethod() {
        return method;
    }

    public String getActor() {
        return actor;
    }

    public Image getImage() {
        return image;
    }

    public Layer getSpecificLayer() {
        return specificLayer;
    }

    @Override
    public String toString() {
        return "RegistryEvent{" +
                "timestamp=" + timestamp +
                ", image=" + image +
                ", type=" + type +
                ", method=" + method +
                ", actor='" + actor + '\'' +
                ", specificLayer=" + specificLayer +
                '}';
    }
}
