package io.kodokojo.docker.model;

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

import io.kodokojo.commons.docker.model.DockerFile;

import java.util.Set;

public class DockerFileBuildPlanResult {

    private final DockerFile dockerFile;

    private final Set<DockerFileBuildResponse> childrenResult;

    private final DockerFileBuildResponse buildResponse;

    public DockerFileBuildPlanResult(DockerFile dockerFile, Set<DockerFileBuildResponse> childrenResult, DockerFileBuildResponse buildResponse) {
        if (dockerFile == null) {
            throw new IllegalArgumentException("dockerFile must be defined.");
        }
        if (childrenResult == null) {
            throw new IllegalArgumentException("childrenResult must be defined.");
        }
        this.dockerFile = dockerFile;
        this.childrenResult = childrenResult;
        this.buildResponse = buildResponse;
    }

    public DockerFileBuildPlanResult(DockerFile dockerFile, Set<DockerFileBuildResponse> childrenResult) {
        this(dockerFile, childrenResult, null);
    }

    public DockerFile getDockerFile() {
        return dockerFile;
    }

    public Set<DockerFileBuildResponse> getChildrenResult() {
        return childrenResult;
    }

    public DockerFileBuildResponse getBuildResponse() {
        return buildResponse;
    }

    @Override
    public String toString() {
        return "DockerFileBuildPlanResultListener{" +
                "dockerFile=" + dockerFile +
                ", childrenResult=" + childrenResult +
                ", buildResponse=" + buildResponse +
                '}';
    }
}
