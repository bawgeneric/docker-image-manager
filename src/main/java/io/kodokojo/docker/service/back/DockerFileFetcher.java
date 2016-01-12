package io.kodokojo.docker.service.back;

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;

public interface DockerFileFetcher {

    void fetchAllDockerFile();

    void fetchDockerFile(ImageName imageName);

}
