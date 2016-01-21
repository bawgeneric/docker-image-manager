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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.DockerClientException;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.DockerFileBuildRequest;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.service.connector.DockerFileProjectFetcher;
import io.kodokojo.docker.service.connector.git.GitDockerFileScmEntry;
import io.kodokojo.docker.utils.serviceLocator.Service;
import io.kodokojo.docker.utils.serviceLocator.ServiceLocator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class DockerClientDockerImageBuilder implements DockerImageBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClientDockerImageBuilder.class);

    private final File workDir;

    private final DockerClient dockerClient;

    private final DockerFileProjectFetcher<GitDockerFileScmEntry> dockerFileProjectFetcher;

    private ServiceLocator serviceLocator;


    public DockerClientDockerImageBuilder(DockerClient dockerClient, File workDir, DockerFileProjectFetcher<GitDockerFileScmEntry> dockerFileProjectFetcher, ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
        if (dockerClient == null) {
            throw new IllegalArgumentException("dockerClient must be defined.");
        }
        if (dockerFileProjectFetcher == null) {
            throw new IllegalArgumentException("dockerFileProjectFetcher must be defined.");
        }
        this.dockerFileProjectFetcher = dockerFileProjectFetcher;
        if (workDir == null) {
            throw new IllegalArgumentException("workDir must be defined.");
        }
        this.dockerClient = dockerClient;
        this.workDir = workDir;

        if (!workDir.exists()) {
            workDir.mkdirs();
        }
        if (!workDir.exists() || !workDir.isDirectory() || !workDir.canWrite()) {
            throw new IllegalStateException("Unable to write in directory " + workDir.getAbsolutePath());
        }
    }

    @Override
    public void build(DockerFileBuildRequest dockerFileBuildRequest, DockerImageBuildCallback callback) {
        if (dockerFileBuildRequest == null) {
            throw new IllegalArgumentException("dockerFile must be defined.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must be defined.");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trying to Build DockerFileBuildRequest {}", dockerFileBuildRequest);
        }

        Set<Service> registryResults = serviceLocator.getServiceByName("registry");
        if (CollectionUtils.isNotEmpty(registryResults)) {

            Service service = registryResults.iterator().next();
            String registry = service.getHost() + ":" + service.getPort();

            DockerFile dockerFile = dockerFileBuildRequest.getDockerFile();
            ImageName imageName = dockerFile.getImageName();
            if ("scratch".equals(imageName.getName())) {
                callback.buildFailed("we can't build image Scratch", new Date());
            } else {
                if ((isNotBlank(imageName.getRepository()) || isNotBlank(registry))) {
                    ImageName from = dockerFile.getFrom();
                    String path = String.format("/%s/%s/%s", imageName.getNamespace(), imageName.getName(), imageName.getTag());
                    File currentDir = new File(workDir.getAbsolutePath() + path);
                    if (!currentDir.exists()) {
                        currentDir.mkdirs();
                    }

                    GitDockerFileScmEntry dockerFileScmEntry = dockerFileBuildRequest.getDockerFileScmEntry();
                    File projectDir = dockerFileProjectFetcher.checkoutDockerFileProject(dockerFileScmEntry);
                    if (projectDir == null || !projectDir.canRead()) {
                        String message = String.format("Unable to read directory for project %s at following path '%s'", dockerFile.getImageName().getFullyQualifiedName(), projectDir.getAbsolutePath());
                        throw new IllegalStateException(message);
                    }
                    File dockerFileDirectory = new File(projectDir.getAbsolutePath() + File.separator + dockerFileScmEntry.getDockerFilePath());

                    boolean pulled = false;
                    try {
                        dockerClient.pullImageCmd(from.getShortName())
                                .exec(new PullImageResultCallback())
                                .awaitCompletion()
                                .onComplete();
                        callback.fromImagePulled(from);
                        pulled = true;
                    } catch (InterruptedException e) {
                        String reason = String.format("Unable to pull image %s", from.getFullyQualifiedName());
                        LOGGER.error(reason, e);
                        callback.buildFailed(reason, new Date());
                    }

                    if (pulled) {
                        callback.buildBegin(new Date());
                        BuildImageResultCallback resultCallback = new BuildImageResultCallback() {
                            @Override
                            public void onNext(BuildResponseItem item) {
                                callback.appendOutput(item.getStream());
                                super.onNext(item);
                            }
                        };
                        if (LOGGER.isTraceEnabled()) {
                            File dockerFileFile = new File(dockerFileDirectory.getAbsolutePath() + File.separator + "Dockerfile");
                            try {
                                String content = FileUtils.readFileToString(dockerFileFile);
                                LOGGER.trace("Trying to build image {} with following Dockerfile : \n{}\n", imageName.getFullyQualifiedName(), content);
                            } catch (IOException e) {
                                LOGGER.trace("Unable to read content of Dockerfile at following path " + dockerFileFile.getAbsolutePath());
                            }
                        }

                        try {

                            String imageId = dockerClient.buildImageCmd(dockerFileDirectory).exec(resultCallback).awaitImageId();
                            Date buildEnd = new Date();
                            if (isNotBlank(imageId)) {
                                String imageNameToRegistry = null;
                                if (isBlank(imageName.getRepository())) {
                                    registry = registry.endsWith("/") ? registry : registry + "/";
                                    imageNameToRegistry = registry + imageName.getDockerImageName();
                                } else {
                                    imageNameToRegistry = imageName.getDockerImageName();
                                }

                                callback.pushToRepositoryBegin(imageName.getRepository(), new Date());
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Trying to tag imageId {} to {}", imageId, imageNameToRegistry);
                                }
                                if (StringUtils.isNotBlank(imageName.getTag())) {
                                    imageNameToRegistry = imageNameToRegistry.substring(0, (imageNameToRegistry.length() - (imageName.getTag().length() + 1)));
                                }
                                dockerClient.tagImageCmd(imageId, imageNameToRegistry, imageName.getTag() != null ? imageName.getTag() : "").withForce().exec();
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Trying to push imageId {} to registry {}", imageId, imageNameToRegistry.replaceAll(":latest$", ""));
                                }
                                PushImageCmd pushImageCmd = dockerClient.pushImageCmd(imageNameToRegistry);
                                if (StringUtils.isNotBlank(imageName.getTag())) {
                                    pushImageCmd = pushImageCmd.withTag(imageName.getTag());
                                }
                                pushImageCmd.exec(new PushImageResultCallback()).awaitSuccess();
                                Date now = new Date();
                                callback.pushToRepositoryEnd(imageName.getRepository(), now);
                                callback.buildSuccess(now);

                            } else {
                                callback.buildFailed("Not able to build Docker image " + imageName.getFullyQualifiedName(), buildEnd);
                            }
                        } catch (DockerClientException e) {
                            String message = "An error occurre while trying to build image " + imageName.getFullyQualifiedName() + ".";
                            LOGGER.error(message, e);
                            callback.buildFailed(message + " : " + e.getMessage(), new Date());
                        }
                    }
                } else {
                    callback.buildFailed("Repository not defined for image " + imageName.getFullyQualifiedName(), new Date());
                }
            }
        } else {
            callback.buildFailed("No Registry available", new Date());
        }
    }

}
