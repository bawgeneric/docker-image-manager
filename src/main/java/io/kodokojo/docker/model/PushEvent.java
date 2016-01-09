package io.kodokojo.docker.model;

import java.util.Date;

public class PushEvent {

    private final Date timestamp;

    private final String actor;

    private final Image image;

    private final Layer specificLayer;

    public PushEvent(Date timestamp, String actor, Image image, Layer specificLayer) {
        this.timestamp = timestamp;
        this.actor = actor;
        this.image = image;
        this.specificLayer = specificLayer;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
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
        return "PushEvent{" +
                "timestamp=" + timestamp +
                ", actor='" + actor + '\'' +
                ", image=" + image +
                ", specificLayer=" + specificLayer +
                '}';
    }
}
