package io.kodokojo.docker.service.source;

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;

public class GitDockerFileSourceConnector implements DockerFileSourceConnector {

    private String gitUrl;

    private String pathToLibrary;

    @Override
    public DockerFile fetchDockerFile(ImageName imageName) {
        return null;
    }
}
