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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DockerFileNode {

    private final DockerFile dockerFile;

    private Date lastUpdate;

    private Date lastSuccessBuild;

    private Date lastFailBuild;

    private String buildOutput;

    private String failReason;

    private final Set<DockerFileNode> children;

    public DockerFileNode(DockerFile dockerFile,  Set<DockerFileNode> children, Date lastUpdate, Date lastSuccessBuild, Date lastFailBuild) {
        if (dockerFile == null) {
            throw new IllegalArgumentException("dockerFile must be defined.");
        }
        if (children == null) {
            this.children = new HashSet<>();
        } else {
            this.children = children;
        }
    //    this.parent = parent;
        this.dockerFile = dockerFile;
        if (lastUpdate == null) {
            this.lastUpdate = new Date();
        } else {
            this.lastUpdate = lastUpdate;
        }
        this.lastSuccessBuild = lastSuccessBuild;
        this.lastFailBuild = lastFailBuild;
    }


    public DockerFile getDockerFile() {
        return dockerFile;
    }

    public Set<DockerFileNode> getChildren() {
        return children;
    }

    public void setLastUpdate(Date lastUpdate) {
        if (lastUpdate == null) {
            throw new IllegalArgumentException("lastUpdate must be defined.");
        }
        if (this.lastUpdate == null ||lastUpdate.after(this.lastUpdate)) {
            this.lastUpdate = lastUpdate;
        }
    }

    public void setLastSuccessBuild(Date lastSuccessBuild) {
        if (lastSuccessBuild == null) {
            throw new IllegalArgumentException("lastSuccessBuild must be defined.");
        }
        if (this.lastSuccessBuild == null || lastSuccessBuild.after(this.lastSuccessBuild)) {
            this.lastSuccessBuild = lastSuccessBuild;
        }
    }

    public void setLastFailBuild(Date lastFailBuild) {
        if (lastFailBuild == null) {
            throw new IllegalArgumentException("lastFailBuild must be defined.");
        }
        if (this.lastFailBuild == null || lastFailBuild.after(this.lastFailBuild)) {
            this.lastFailBuild = lastFailBuild;
        }
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public Date getLastSuccessBuild() {
        return lastSuccessBuild;
    }

    public Date getLastFailBuild() {
        return lastFailBuild;
    }

    public String getBuildOutput() {
        return buildOutput;
    }

    public void setBuildOutput(String buildOutput) {
        this.buildOutput = buildOutput;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    @Override
    public String toString() {
        return "DockerFileNode{" +
                "dockerFile=" + dockerFile +
                ", lastUpdate=" + lastUpdate +
                ", lastSuccessBuild=" + lastSuccessBuild +
                ", lastFailBuild=" + lastFailBuild +
                ", buildOutput='" + buildOutput + '\'' +
                ", failReason='" + failReason + '\'' +
                ", children=" + children +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerFileNode that = (DockerFileNode) o;

        return dockerFile.equals(that.dockerFile);

    }

    @Override
    public int hashCode() {
        return dockerFile.hashCode();
    }
}
