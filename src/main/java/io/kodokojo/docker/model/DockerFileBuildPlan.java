package io.kodokojo.docker.model;

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
