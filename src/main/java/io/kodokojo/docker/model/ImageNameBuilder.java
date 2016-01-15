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

import org.apache.commons.lang.StringUtils;

//  TODO Move this class in a docker-commons project
public class ImageNameBuilder {

    private String repository;

    private String namespace;

    private String name;

    private String tag;

    public ImageNameBuilder() {
        super();
    }

    public ImageName build() {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name  must be defined.");
        }
        return new ImageName(repository, namespace, name, tag);
    }

    public ImageNameBuilder setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public ImageNameBuilder setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public ImageNameBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ImageNameBuilder setTag(String tag) {
        this.tag = tag;
        return this;
    }
}
