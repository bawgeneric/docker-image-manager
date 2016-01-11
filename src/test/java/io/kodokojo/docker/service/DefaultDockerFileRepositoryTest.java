package io.kodokojo.docker.service;

import io.kodokojo.docker.model.DockerFile;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DefaultDockerFileRepositoryTest {

    @Test
    public void add_root_dockerfile() {
        DockerFileRepository repository  = new DefaultDockerFileRepository();
        String imageName = "busybox";
        repository.addDockerFile(new DockerFile(imageName));
        DockerFile result = repository.getDockerFileFromImageName(imageName);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getImageName()).isNotNull();
        Assertions.assertThat(result.getImageName().getShortName()).isEqualTo(imageName);
    }



}