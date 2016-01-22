package io.kodokojo.docker.bdd.stage.restentrypoint;

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

import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.docker.bdd.stage.AbstractRestStage;
import io.kodokojo.commons.bdd.stage.docker.DockerCommonsGiven;
import io.kodokojo.commons.model.DockerFile;
import io.kodokojo.commons.model.ImageName;
import io.kodokojo.commons.model.StringToImageNameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

public class RestEntryPointThen<SELF extends RestEntryPointThen<?>> extends AbstractRestStage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEntryPointThen.class);

    @ExpectedScenarioState
    Map<String, String> containers = new HashMap<>();

    private ClientRestEntryPoint restEntryPoint;

    public SELF repository_contain_a_Dockerfile_of_$_image(@Quoted String imageNameStr) {
        if (isBlank(imageNameStr)) {
            throw new IllegalArgumentException("imageNameStr must be defined.");
        }
        ImageName imageName = StringToImageNameConverter.convert(imageNameStr);

        if (restEntryPoint == null) {
            String containerId = containers.get(DockerCommonsGiven.DOCKER_IMAGE_MANAGER_KEY);
            String url = dockerClientSupport.getHttpContainerUrl(containerId, 8080);
            restEntryPoint = provideClientRestEntryPoint(url);
        }

        /*
        String httpContainerUrl = dockerClientSupport.getHttpContainerUrl(containers.get(DockerCommonsGiven.DOCKER_IMAGE_MANAGER_KEY), 8080);
        LOGGER.debug("Docker image {}/api/repository/{}/{}/{}" , httpContainerUrl, imageName.getNamespace(), imageName.getName(), imageName.getTag());
        */

        DockerFile dockerFile = restEntryPoint.getDockerFile(imageName.getNamespace(), imageName.getName(), imageName.getTag());
        LOGGER.debug("Retrive Dockerfile for image {}: {}", imageName.getFullyQualifiedName(), dockerFile);

        assertThat(dockerFile).isNotNull();

        return self();
    }

}

