package io.kodokojo.docker.service.back.build;

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

import io.kodokojo.docker.model.DockerFileBuildRequest;
import io.kodokojo.docker.model.DockerFileBuildResponseBuilder;

import java.util.Date;

public abstract class AbstractBuildRequestDockerImageBuildCallback implements DockerImageBuildCallback {

    protected final DockerFileBuildResponseBuilder dockerFileBuildResponseBuilder;

    protected final DockerFileBuildRequest dockerFileBuildRequest;

    public AbstractBuildRequestDockerImageBuildCallback(DockerFileBuildRequest dockerFileBuildRequest) {
        this.dockerFileBuildRequest = dockerFileBuildRequest;
        this.dockerFileBuildResponseBuilder = new DockerFileBuildResponseBuilder(dockerFileBuildRequest);
        dockerFileBuildResponseBuilder.setLaunchBuildDate(new Date());
    }

    @Override
    public void buildSuccess(Date endDate) {
        dockerFileBuildResponseBuilder.setLastUpdateDate(endDate);
        dockerFileBuildResponseBuilder.setBuildSuccessDate(endDate);
    }

    @Override
    public void buildFailed(String reason, Date failDate) {
        dockerFileBuildResponseBuilder.setLastUpdateDate(failDate);
        dockerFileBuildResponseBuilder.setBuildFailDate(failDate);
        dockerFileBuildResponseBuilder.setFailedReason(reason);
    }

    @Override
    public void buildBegin(Date beginDate) {
        dockerFileBuildResponseBuilder.setLaunchBuildDate(beginDate);
    }

    @Override
    public void appendOutput(String output) {
        dockerFileBuildResponseBuilder.appendOutput(output);
    }
}
