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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tngtech.jgiven.CurrentStep;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.attachment.Attachment;
import io.kodokojo.commons.bdd.RetrofitEntrypointSupport;
import io.kodokojo.docker.bdd.stage.AbstractRestStage;
import io.kodokojo.commons.bdd.stage.docker.DockerCommonsGiven;
import io.kodokojo.commons.docker.model.DockerFile;
import io.kodokojo.commons.docker.model.ImageName;
import io.kodokojo.commons.docker.model.StringToImageNameConverter;
import io.kodokojo.docker.model.DockerFileNode;
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

    @ProvidedScenarioState
    CurrentStep currentStep;


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

        DockerFile dockerFile = restEntryPoint.getDockerFile(imageName.getNamespace(), imageName.getName(), imageName.getTag());
        LOGGER.debug("Retrive Dockerfile for image {}: {}", imageName.getFullyQualifiedName(), dockerFile);

        assertThat(dockerFile).isNotNull();

        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        String content = gson.toJson(dockerFile);
        Attachment attachment = Attachment.plainText(content).withTitle("DockerFile for " +imageName.getFullyQualifiedName()).withFileName("dockerfile.json");
        currentStep.addAttachment(attachment);

        return self();
    }

    public SELF repository_contain_a_Dockerfile_node_of_$_image(@Quoted String imageNameStr) {
        if (isBlank(imageNameStr)) {
            throw new IllegalArgumentException("imageNameStr must be defined.");
        }
        ImageName imageName = StringToImageNameConverter.convert(imageNameStr);

        if (restEntryPoint == null) {
            String containerId = containers.get(DockerCommonsGiven.DOCKER_IMAGE_MANAGER_KEY);
            String url = dockerClientSupport.getHttpContainerUrl(containerId, 8080);
            restEntryPoint = provideClientRestEntryPoint(url);
        }

        DockerFileNode dockerFileNode = restEntryPoint.getDockerFileNode(imageName.getNamespace(), imageName.getName(), imageName.getTag());
        LOGGER.debug("Retrive Dockerfile node for image {}: {}", imageName.getFullyQualifiedName(), dockerFileNode);

        assertThat(dockerFileNode).isNotNull();

        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        String content = gson.toJson(dockerFileNode);
        Attachment attachment = Attachment.plainText(content).withTitle("DockerFileNode for " +imageName.getFullyQualifiedName()).withFileName("dockerNodefile.json");
        currentStep.addAttachment(attachment);

        return self();
    }
    public SELF repository_contain_a_Dockerfile_node_of_$_image_build_with_success(@Quoted String imageNameStr) {
        if (isBlank(imageNameStr)) {
            throw new IllegalArgumentException("imageNameStr must be defined.");
        }
        ImageName imageName = StringToImageNameConverter.convert(imageNameStr);

        if (restEntryPoint == null) {
            String containerId = containers.get(DockerCommonsGiven.DOCKER_IMAGE_MANAGER_KEY);
            String url = dockerClientSupport.getHttpContainerUrl(containerId, 8080);
            restEntryPoint = provideClientRestEntryPoint(url);
        }

        DockerFileNode dockerFileNode = RetrofitEntrypointSupport.retriveFromRestEntrypoint(() -> restEntryPoint.getDockerFileNode(imageName.getNamespace(), imageName.getName(), imageName.getTag()), 20000);
        LOGGER.debug("Retrive Dockerfile node for image {}: {}", imageName.getFullyQualifiedName(), dockerFileNode);

        assertThat(dockerFileNode).isNotNull();

        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        String content = gson.toJson(dockerFileNode);
        Attachment attachment = Attachment.plainText(content).withTitle("DockerFileNode for " +imageName.getFullyQualifiedName()).withFileName("dockerNodefile.json");
        currentStep.addAttachment(attachment);

        for(DockerFileNode child : dockerFileNode.getChildren()) {
            assertThat(child.getLastSuccessBuild()).isNotNull();
            assertThat(child.getLastFailBuild()).isNull();
        }

        return self();
    }

}

