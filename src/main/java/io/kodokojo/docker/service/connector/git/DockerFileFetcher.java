package io.kodokojo.docker.service.connector.git;

import io.kodokojo.docker.model.ImageName;

public interface DockerFileFetcher {

    void fetchAllDockerFile();

    void fetchDockerFile(ImageName imageName);

}
