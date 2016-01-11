package io.kodokojo.docker.service;

import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.Layer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultDockerImageRepository implements  DockerImageRepository{

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
        if (previous != null && previous.contains(layer)) {
            res = true;
            value.addAll(previous);
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
}
