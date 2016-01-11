package io.kodokojo.docker.service;

import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.Layer;

import java.util.Set;

public interface DockerImageRepository {

    boolean addLayer(ImageName imageName, Layer layer);

    Set<Layer> getlayer(ImageName imageName);

}
