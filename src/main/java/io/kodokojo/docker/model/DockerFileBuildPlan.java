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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DockerFileBuildPlan {

    private final DockerFile dockerFile;

    private final Set<DockerFileBuildPlan> children;

    private Date launchBuildDate;

    private Date buildDate;

    private Date lastUpdateDate;

    public DockerFileBuildPlan(DockerFile dockerFile, Set<DockerFileBuildPlan> children, Date lastUpdateDate) {
        if (dockerFile == null) {
            throw new IllegalArgumentException("dockerFile must be defined.");
        }
        if (children == null) {
            throw new IllegalArgumentException("children must be defined.");
        }
        if (lastUpdateDate == null) {
            throw new IllegalArgumentException("lastUpdateDate must be defined.");
        }

        this.dockerFile = dockerFile;
        this.children = children;
        this.lastUpdateDate = null;
        this.buildDate = null;
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        if (this.lastUpdateDate.before(lastUpdateDate)) {
            this.lastUpdateDate = lastUpdateDate;
        }
    }

    public Date getLaunchBuildDate() {
        return launchBuildDate;
    }

    public void setLaunchBuildDate(Date launchBuildDate) {
        this.launchBuildDate = launchBuildDate;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(Date buildDate) {
        this.buildDate = buildDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public DockerFile getDockerFile() {
        return dockerFile;
    }

    public Set<DockerFileBuildPlan> getChildren() {
        return new HashSet<>(children);
    }

    @Override
    public String toString() {
        return "DockerFileBuildPlan{" +
                "dockerFile=" + dockerFile +
                ", children=" + children +
                '}';
    }
}
