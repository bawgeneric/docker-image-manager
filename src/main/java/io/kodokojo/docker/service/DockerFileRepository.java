package io.kodokojo.docker.service;

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;

import java.util.List;

public interface DockerFileRepository {

    DockerFile getDockerFileInheriteFrom(DockerFile dockerFile);

    List<DockerFile> getDockerFileChildOf(DockerFile dockerFile);

    DockerFile getDockerFileFromImageName(ImageName imageName);

    DockerFile getDockerFileFromImageName(String imageName);

    void addDockerFile(DockerFile dockerFile);

    void addDockerFile(ImageName imageName);

    void addDockerFile(String imageName);

}
