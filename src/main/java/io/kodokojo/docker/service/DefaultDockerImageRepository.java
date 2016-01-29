package io.kodokojo.docker.service;

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

import io.kodokojo.commons.docker.model.ImageName;
import io.kodokojo.docker.model.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultDockerImageRepository implements DockerImageRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerImageRepository.class);

    private Map<ImageName, Set<Layer>> repository;

    public DefaultDockerImageRepository() {
        repository = new ConcurrentHashMap<>();
    }

    @Override
    public boolean addLayer(ImageName imageName, Layer layer) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        if (layer == null) {
            throw new IllegalArgumentException("layer must be defined.");
        }
        boolean res = false;

        HashSet<Layer> value = new HashSet<>();
        value.add(layer);
        Set<Layer> previous = repository.put(imageName, value);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Add layer to image {} with size {} and digest {}.", imageName.getFullyQualifiedName(), layer.getSize(), layer.getDigest());
        }
        if (previous != null && previous.contains(layer)) {
            res = true;
            value.addAll(previous);
        }
        return res;
    }

    @Override
    public boolean addLayer(Set<ImageName> imageNames, Layer layer) {
        if (imageNames == null) {
            throw new IllegalArgumentException("imageNames must be defined.");
        }
        if (layer == null) {
            throw new IllegalArgumentException("layer must be defined.");
        }
        boolean res = false;
        for (ImageName imageName : imageNames) {
            boolean current = addLayer(imageName, layer);
            if (current) {
                res = true;
            }
        }
        return res;
    }

    @Override
    public Set<Layer> getlayer(ImageName imageName) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        Set<Layer> layers = repository.get(imageName);
        return new HashSet<>(layers);
    }

    @Override
    public Set<ImageName> getImageNamesFromLayer(Layer layer) {
        if (layer == null) {
            throw new IllegalArgumentException("layer must be defined.");
        }
        return repository.entrySet().stream()
                .filter(entry -> entry.getValue().contains(layer))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
