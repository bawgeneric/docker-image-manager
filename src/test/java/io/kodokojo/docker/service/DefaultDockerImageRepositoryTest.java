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
import io.kodokojo.commons.docker.model.StringToImageNameConverter;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultDockerImageRepositoryTest {

    @Test
    public void adding_new_layer() {

        ImageName imageName = StringToImageNameConverter.convert("library/busybox:dev");
        Layer layer = new Layer("sha1:123456", 32);

        DockerImageRepository dockerImageRepository = new DefaultDockerImageRepository();
        boolean alreadyExist = dockerImageRepository.addLayer(imageName, layer);

        assertThat(alreadyExist).isFalse();

        Set<Layer> resLayer = dockerImageRepository.getlayer(new ImageName("library", "busybox", "dev"));
        assertThat(resLayer).isNotEmpty();
        assertThat(resLayer).extracting("digest").contains("sha1:123456");

    }

    @Test
    public void adding_layer_twice() {

        ImageName imageName = StringToImageNameConverter.convert("library/busybox:dev");
        Layer layer = new Layer("sha1:123456", 32);

        DockerImageRepository dockerImageRepository = new DefaultDockerImageRepository();
        boolean alreadyExist = dockerImageRepository.addLayer(imageName, layer);

        assertThat(alreadyExist).isFalse();

        Set<Layer> resLayer = dockerImageRepository.getlayer(new ImageName("library", "busybox", "dev"));
        assertThat(resLayer).isNotEmpty();
        assertThat(resLayer).extracting("digest").contains("sha1:123456");

        alreadyExist = dockerImageRepository.addLayer(imageName, layer);
        assertThat(alreadyExist).isTrue();
    }

}