package io.kodokojo.docker.service;

import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.Layer;
import io.kodokojo.docker.model.StringToImageNameConverter;
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

}