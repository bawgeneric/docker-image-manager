package io.kodokojo.docker.utils.serviceLocator;

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

import static org.apache.commons.lang.StringUtils.isBlank;

public class Service {

    private final String name;

    private final String host;

    private final int port;

    public Service(String name, String host, int port) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        if (isBlank(host)) {
            throw new IllegalArgumentException("host must be defined.");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port must be upper than 0");
        }
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
