package io.kodokojo.docker.model;

import java.util.ArrayList;
import java.util.List;

public class Image {

    private final ImageName name;

    private final List<Layer> layers;

    public Image(ImageName name, List<Layer> layers) {
        this.name = name;
        this.layers = layers;
    }

    public ImageName getName() {
        return name;
    }

    public List<Layer> getLayers() {
        return new ArrayList<>(layers);
    }

    @Override
    public String toString() {
        return "Image{" +
                "name=" + name +
                ", layers=" + layers +
                '}';
    }
}
