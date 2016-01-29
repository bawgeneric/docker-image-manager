package io.kodokojo.docker.model;

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

import java.util.Date;

public class RegistryEvent {

    public enum EventType {
        PUSH,
        PULL
    }

    public enum EventMethod {
        PUT,
        PULL,
        HEAD
    }

    private final Date timestamp;

    private final EventType type;

    private final String actor;

    private final EventMethod method;

    private final Image image;

    private final Layer specificLayer;

    private final String url;

    public RegistryEvent(Date timestamp, EventType type,EventMethod method,  String actor, Image image, Layer specificLayer, String url) {
        this.timestamp = timestamp;
        this.type = type;
        this.method = method;
        this.actor = actor;
        this.image = image;
        this.specificLayer = specificLayer;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "RegistryEvent{" +
                "timestamp=" + timestamp +
                ", type=" + type +
                ", actor='" + actor + '\'' +
                ", method=" + method +
                ", image=" + image +
                ", specificLayer=" + specificLayer +
                ", url='" + url + '\'' +
                '}';
    }
}
