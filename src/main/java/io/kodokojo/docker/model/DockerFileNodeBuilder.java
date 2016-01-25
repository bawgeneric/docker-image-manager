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
import java.util.Set;

public class DockerFileNodeBuilder {

    private DockerFile dockerFile;

    private Date lastUpdate;

    private Date lastSuccessBuild;

    private Date lastFailBuild;

    private Set<DockerFileNode> children;

    public DockerFileNodeBuilder(DockerFileNode reference) {
        if (reference != null) {
                    setChildren(reference.getChildren())
                    .setDockerFile(reference.getDockerFile())
                    .setLastFailBuild(reference.getLastFailBuild())
                    .setLastSuccessBuild(reference.getLastSuccessBuild())
                    .setLastUpdate(reference.getLastUpdate());
        }
    }

    public DockerFileNodeBuilder() {
        /* */
    }

    public DockerFileNode build() {
        return new DockerFileNode( dockerFile, children, lastUpdate, lastSuccessBuild, lastFailBuild);
    }

    public DockerFileNodeBuilder setDockerFile(DockerFile dockerFile) {
        this.dockerFile = dockerFile;
        return this;
    }

    public DockerFileNodeBuilder setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    public DockerFileNodeBuilder setLastSuccessBuild(Date lastSuccessBuild) {
        this.lastSuccessBuild = lastSuccessBuild;
        return this;
    }

    public DockerFileNodeBuilder setLastFailBuild(Date lastFailBuild) {
        this.lastFailBuild = lastFailBuild;
        return this;
    }

    public DockerFileNodeBuilder setChildren(Set<DockerFileNode> children) {
        this.children = children;
        return this;
    }
}
