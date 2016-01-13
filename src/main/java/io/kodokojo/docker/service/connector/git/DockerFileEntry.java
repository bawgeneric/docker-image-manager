package io.kodokojo.docker.service.connector.git;

import io.kodokojo.docker.model.ImageName;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerFileEntry {

    private final ImageName imageName;

    private final String gitUrl;

    private final String gitRef;

    private final String dockerFilePath;

    protected DockerFileEntry(ImageName imageName, String gitUrl, String gitRef, String dockerFilePath) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        if (isBlank(gitUrl)) {
            throw new IllegalArgumentException("gitUrl must be defined.");
        }
        if (isBlank(gitRef)) {
            this.gitRef = "HEAD";
        } else {
            this.gitRef = gitRef;
        }
        if (isBlank(dockerFilePath)) {
            throw new IllegalArgumentException("dockerFilePath must be defined.");
        }
        this.imageName = imageName;
        this.gitUrl = gitUrl;
        this.dockerFilePath = dockerFilePath;
    }

    public ImageName getImageName() {
        return imageName;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public String getGitRef() {
        return gitRef;
    }

    public String getDockerFilePath() {
        return dockerFilePath;
    }

    @Override
    public String toString() {
        return "DockerFileEntry{" +
                "imageName='" + imageName + '\'' +
                ", gitUrl='" + gitUrl + '\'' +
                ", gitRef='" + gitRef + '\'' +
                ", dockerFilePath='" + dockerFilePath + '\'' +
                '}';
    }
}
