package io.kodokojo.docker.service;

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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerClientRule extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClientRule.class);

    private DockerClient dockerClient;

    private List<String> containerToClean;

    public DockerClientRule() {
        dockerClient = DockerClientBuilder.getInstance().build();
        if (isNotWorking(dockerClient)) {
            String userHome = System.getProperty("user.home");
            DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder().withUri("https://192.168.99.100:2376").withDockerCertPath(userHome + "/.docker/machine/machines/default").build();
            dockerClient = DockerClientBuilder.getInstance(config).build();
        }
        containerToClean = new ArrayList<>();
    }

    public void addContainerIdToClean(String id) {
        containerToClean.add(id);
    }

    public void pullImage(String image) {
        if (isBlank(image)) {
            throw new IllegalArgumentException("image must be defined.");
        }
        if (dockerClient == null) {
            throw new IllegalArgumentException("dockerClient must be defined.");
        }
        try {
            dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion().onComplete();
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to pull java image", e);
        }
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        containerToClean.clear();
    }

    @Override
    protected void after() {
        super.after();
        containerToClean.forEach(id -> {
            dockerClient.stopContainerCmd(id).exec();
            dockerClient.removeContainerCmd(id).exec();
            LOGGER.debug("Stopped and removed container id {}", id);
        });
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    private boolean isNotWorking(DockerClient dockerClient) {
        try {
            dockerClient.listImagesCmd().exec();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
