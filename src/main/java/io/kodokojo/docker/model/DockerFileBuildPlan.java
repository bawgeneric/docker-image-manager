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
import io.kodokojo.commons.docker.fetcher.git.GitDockerFileScmEntry;

import java.util.*;

public class DockerFileBuildPlan {

    private final DockerFile dockerFile;

    private final Map<DockerFileBuildRequest, DockerFileBuildResponse> children;

    private final GitDockerFileScmEntry dockerFileScmEntry;

    private Date lastUpdateDate;

    private DockerFileBuildResponse dockerFileBuildResponse;

    public DockerFileBuildPlan(DockerFile dockerFile, Map<DockerFileBuildRequest, DockerFileBuildResponse> children,GitDockerFileScmEntry dockerFileScmEntry, Date lastUpdateDate) {
        if (dockerFile == null) {
            throw new IllegalArgumentException("dockerFile must be defined.");
        }
        if (children == null) {
            throw new IllegalArgumentException("children must be defined.");
        }
        if (dockerFileScmEntry == null) {
            throw new IllegalArgumentException("dockerFileScmEntry must be defined.");
        }
        if (lastUpdateDate == null) {
            throw new IllegalArgumentException("lastUpdateDate must be defined.");
        }

        this.dockerFile = dockerFile;
        this.children = children;
        this.dockerFileScmEntry =  dockerFileScmEntry;
        this.lastUpdateDate = null;
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        if (this.lastUpdateDate.before(lastUpdateDate)) {
            this.lastUpdateDate = lastUpdateDate;
        }
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public DockerFileBuildResponse getDockerFileBuildResponse() {
        return dockerFileBuildResponse;
    }

    public void setDockerFileBuildResponse(DockerFileBuildResponse dockerFileBuildResponse) {
        this.dockerFileBuildResponse = dockerFileBuildResponse;
    }

    public DockerFile getDockerFile() {
        return dockerFile;
    }

    public GitDockerFileScmEntry getDockerFileScmEntry() {
        return dockerFileScmEntry;
    }

    public Map<DockerFileBuildRequest, DockerFileBuildResponse> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerFileBuildPlan that = (DockerFileBuildPlan) o;

        return dockerFile.equals(that.dockerFile);

    }

    @Override
    public int hashCode() {
        return dockerFile.hashCode();
    }

    @Override
    public String toString() {
        return "DockerFileBuildPlan{" +
                "dockerFile=" + dockerFile +
                ", children=" + children +
                ", dockerFileScmEntry=" + dockerFileScmEntry +
                ", lastUpdateDate=" + lastUpdateDate +
                ", dockerFileBuildResponse=" + dockerFileBuildResponse +
                '}';
    }
}
