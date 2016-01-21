package io.kodokojo.docker.service.actor;

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

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.docker.model.DockerFileBuildRequest;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.service.back.build.DockerImageBuildCallback;
import io.kodokojo.docker.service.back.build.DockerImageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class DockerImageBuilderWorker extends AbstractActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerImageBuilderWorker.class);

    public DockerImageBuilderWorker(DockerImageBuilder dockerImageBuilder) {
        receive(ReceiveBuilder.match(DockerFileBuildRequest.class, dockerFileBuildRequest -> {

            dockerImageBuilder.build(dockerFileBuildRequest, new WorkerDockerImageBuildCallback(dockerFileBuildRequest.getDockerFile().getImageName()));

        }).matchAny(this::unhandled).build());
    }

    private class WorkerDockerImageBuildCallback implements DockerImageBuildCallback {

        private ImageName buildImageName;

        private WorkerDockerImageBuildCallback(ImageName buildImageName) {
            this.buildImageName = buildImageName;
        }

        @Override
        public void fromImagePulled(ImageName imageName) {
            LOGGER.info("ImageName {} Pulled.", imageName.getFullyQualifiedName());
        }

        @Override
        public void buildBegin(Date beginDate) {
            LOGGER.info("Build of image {} start", buildImageName.getFullyQualifiedName());
        }

        @Override
        public void buildSuccess(Date endDate) {
            LOGGER.info("Build of image {} SUCCESS", buildImageName.getFullyQualifiedName());

        }

        @Override
        public void pushToRepositoryBegin(String repository, Date begin) {

        }

        @Override
        public void pushToRepositoryEnd(String repository, Date begin) {

        }

        @Override
        public void buildFailed(String reason, Date failDate) {
            LOGGER.info("Build of image {} FAILED : {}", buildImageName.getFullyQualifiedName(), reason);

        }

        @Override
        public void appendOutput(String output) {
            LOGGER.info("Build of image {} in progress :{}", buildImageName.getFullyQualifiedName(), output);

        }
    }

}
