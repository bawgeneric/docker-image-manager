package io.kodokojo.docker.service.connector.git;

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
