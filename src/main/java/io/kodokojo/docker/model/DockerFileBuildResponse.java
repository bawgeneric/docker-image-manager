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

public class DockerFileBuildResponse {

    private final DockerFileBuildRequest dockerFileBuildRequest;

    private final Date launchBuildDate;

    private final Date buildSuccessDate;

    private final Date buildFailDate;

    private final Date lastUpdateDate;

    private final String output;

    private final String failedReason;

    public DockerFileBuildResponse(DockerFileBuildRequest dockerFileBuildRequest, Date launchBuildDate, Date buildSuccessDate, Date buildFailDate, Date lastUpdateDate, String output, String failedReason) {
        if (dockerFileBuildRequest == null) {
            throw new IllegalArgumentException("dockerFileBuildRequest must be defined.");
        }
        this.dockerFileBuildRequest = dockerFileBuildRequest;
        this.launchBuildDate = launchBuildDate;
        this.buildSuccessDate = buildSuccessDate;
        this.buildFailDate = buildFailDate;
        this.lastUpdateDate = lastUpdateDate;
        this.output = output;
        this.failedReason = failedReason;
    }

    public DockerFileBuildRequest getDockerFileBuildRequest() {
        return dockerFileBuildRequest;
    }

    public Date getLaunchBuildDate() {
        return launchBuildDate;
    }

    public Date getBuildSuccessDate() {
        return buildSuccessDate;
    }

    public Date getBuildFailDate() {
        return buildFailDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public String getOutput() {
        return output;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public boolean isSuccess() {
        return buildSuccessDate != null;
    }

    @Override
    public String toString() {
        return "DockerFileBuildResponse{" +
                "dockerFileBuildRequest=" + dockerFileBuildRequest +
                ", launchBuildDate=" + launchBuildDate +
                ", buildSuccessDate=" + buildSuccessDate +
                ", buildFailDate=" + buildFailDate +
                ", lastUpdateDate=" + lastUpdateDate +
                ", output='" + output + '\'' +
                ", failedReason='" + failedReason + '\'' +
                '}';
    }
}
