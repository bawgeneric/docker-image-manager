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

import io.kodokojo.commons.docker.model.ImageName;

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
