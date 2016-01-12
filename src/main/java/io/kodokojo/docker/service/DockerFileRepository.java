package io.kodokojo.docker.service;

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;

import java.util.List;
import java.util.Set;

public interface DockerFileRepository {

    DockerFile getDockerFileInheriteFrom(DockerFile dockerFile);

    Set<DockerFile> getDockerFileChildOf(DockerFile dockerFile);

    DockerFile getDockerFileFromImageName(ImageName imageName);

    DockerFile getDockerFileFromImageName(String imageName);

    void addDockerFile(DockerFile dockerFile);

    void addDockerFile(ImageName imageName);

    void addDockerFile(String imageName);

}
