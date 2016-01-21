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

public class DockerFileBuildResponseBuilder {

    private final DockerFileBuildRequest dockerFileBuildRequest;

    private final StringBuilder output;

    private Date launchBuildDate;

    private Date buildSuccessDate;

    private Date buildFailDate;

    private Date lastUpdateDate;

    private String faildReason;

    public DockerFileBuildResponseBuilder(DockerFileBuildRequest dockerFileBuildRequest) {
        this.dockerFileBuildRequest = dockerFileBuildRequest;
        this.output = new StringBuilder();
    }

    public DockerFileBuildResponse build() {
        return new DockerFileBuildResponse(dockerFileBuildRequest, launchBuildDate, buildSuccessDate, buildFailDate, lastUpdateDate, output.toString(), faildReason);
    }

    public DockerFileBuildResponseBuilder setLaunchBuildDate(Date launchBuildDate) {
        if (launchBuildDate != null && this.launchBuildDate != null && launchBuildDate.after(this.launchBuildDate)) {
            this.launchBuildDate = launchBuildDate;
        }
        return this;
    }

    public DockerFileBuildResponseBuilder setBuildSuccessDate(Date buildSuccessDate) {
        this.buildSuccessDate = buildSuccessDate;
        return this;
    }

    public DockerFileBuildResponseBuilder setBuildFailDate(Date buildFailDate) {
        this.buildFailDate = buildFailDate;
        return this;
    }

    public DockerFileBuildResponseBuilder setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
        return this;
    }

    public DockerFileBuildResponseBuilder appendOutput(String output) {
        this.output.append(output);
        return this;
    }

    public DockerFileBuildResponseBuilder setFailedReason(String failedReason) {
        this.faildReason = failedReason;
        return this;
    }
}

