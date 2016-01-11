package io.kodokojo.docker.service.source;

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;

public interface DockerFileSourceConnector {

    DockerFile fetchDockerFile(ImageName imageName);

}
