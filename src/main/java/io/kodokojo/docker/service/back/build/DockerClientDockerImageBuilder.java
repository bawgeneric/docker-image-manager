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
import com.github.dockerjava.core.command.PushImageResultCallback;
import io.kodokojo.commons.docker.fetcher.DockerFileProjectFetcher;
import io.kodokojo.commons.docker.fetcher.git.GitDockerFileScmEntry;
import io.kodokojo.commons.docker.model.DockerFile;
import io.kodokojo.commons.docker.model.ImageName;
import io.kodokojo.commons.model.Service;
import io.kodokojo.commons.utils.servicelocator.ServiceLocator;
import io.kodokojo.docker.model.DockerFileBuildRequest;
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
            boolean workDirCreated = workDir.mkdirs();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.info("Working diretory {} {}", workDir.getAbsolutePath(), workDirCreated ? "created" : "Not created");
            }
        }
        if (!workDir.exists() || !workDir.isDirectory() || !workDir.canWrite()) {
            throw new IllegalStateException("Unable to write in directory " + workDir.getAbsolutePath());
        }
    }

    @Override
    public void build(DockerFileBuildRequest dockerFileBuildRequest, DockerImageBuildCallback callback) {
        build(dockerFileBuildRequest, callback, true);
    }

    @Override
    public void build(DockerFileBuildRequest dockerFileBuildRequest, DockerImageBuildCallback callback, boolean push) {
        if (dockerFileBuildRequest == null) {
            throw new IllegalArgumentException("dockerFile must be defined.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must be defined.");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trying to Build DockerFileBuildRequest {}", dockerFileBuildRequest);
        }
        DockerFile dockerFile = dockerFileBuildRequest.getDockerFile();
        ImageName imageName = dockerFile.getImageName();

        if ("scratch".equals(imageName.getName())) {
            callback.buildFailed("we can't build image Scratch", new Date());
        } else {
            String path = String.format("/%s/%s/%s", imageName.getNamespace(), imageName.getName(), imageName.getTag());
            File currentDir = new File(workDir.getAbsolutePath() + path);
            if (!currentDir.exists()) {
                currentDir.mkdirs();
            }

            //  TODO Try to use url option to build image instead of checkout project.

            GitDockerFileScmEntry dockerFileScmEntry = dockerFileBuildRequest.getDockerFileScmEntry();
            File projectDir = dockerFileProjectFetcher.checkoutDockerFileProject(dockerFileScmEntry);
            if (!projectDir.canRead()) {
                String message = String.format("Unable to read directory for project %s at following path '%s'", dockerFile.getImageName().getFullyQualifiedName(),projectDir.getAbsolutePath());
                throw new IllegalStateException(message);
            }
            File dockerFileDirectory = new File(projectDir.getAbsolutePath() + File.separator + dockerFileScmEntry.getDockerFilePath());


            callback.buildBegin(new Date());


            if (LOGGER.isTraceEnabled()) {
                File dockerFileFile = new File(dockerFileDirectory.getAbsolutePath() + File.separator + "Dockerfile");
                try {
                    String content = FileUtils.readFileToString(dockerFileFile);
                    LOGGER.trace("Trying to build image {} with following Dockerfile : \n{}\n", imageName.getFullyQualifiedName(), content);
                } catch (IOException e) {
                    LOGGER.trace("Unable to read content of Dockerfile at following path " + dockerFileFile.getAbsolutePath());
                }
            }

            String imageId = null;
            try {
                imageId = dockerClient.buildImageCmd(dockerFileDirectory).exec( new ResultBuildCallbackAppendOutput(callback)).awaitImageId();
            } catch (DockerClientException e) {
                String message = "An error occurre while trying to build image " + imageName.getFullyQualifiedName() + ".";
                LOGGER.error(message, e);
                callback.buildFailed(message + " : " + e.getMessage(), new Date());
            }
            Date buildEnd = new Date();
            if (isNotBlank(imageId)) {
                boolean res;
                if (push) {

                    res = tagAndPushImage(imageName, imageId, callback);
                } else {
                    res = tagImage(imageName, imageId, callback, false);
                }
                if (res) {
                    Date now = new Date();
                    callback.buildSuccess(now);
                }
            } else {
                callback.buildFailed("Not able to build Docker image " + imageName.getFullyQualifiedName(), buildEnd);
            }

        }

    }

    private String getImageDockerCmdName(ImageName imageName, boolean push) {
        String imageNameToRegistry = imageName.getShortName();

        if (StringUtils.isNotBlank(imageName.getTag())) {
            int endIndex = imageNameToRegistry.length() - (imageName.getTag().length() + 1);
            imageNameToRegistry = imageNameToRegistry.substring(0, endIndex);
        }
        if (push) {
            String registry = imageName.getRepository();
            if (StringUtils.isBlank(registry)) {
                registry = getRegistryUrl(registry);
            }

            if (isBlank(imageName.getRepository()) && isNotBlank(registry)) {
                registry = registry.endsWith("/") ? registry : registry + "/";
                imageNameToRegistry = registry + imageNameToRegistry;
            }
        }
        return imageNameToRegistry;
    }

    private String getRegistryUrl(String registry) {
        Set<Service> registryResults = serviceLocator.getServiceByName("registry");
        String res = registry;
        if (CollectionUtils.isNotEmpty(registryResults)) {

            Service service = registryResults.iterator().next();
            res = service.getHost() + ":" + service.getPort();
        }
        return res;
    }

    private boolean tagAndPushImage(ImageName imageName, String imageId, DockerImageBuildCallback callback) {
        String imageNameToRegistry = getImageDockerCmdName(imageName, true);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trying to tag imageId {} to {}", imageId, imageNameToRegistry);
        }
        String registry = imageName.getRepository();
        if (StringUtils.isBlank(registry)) {
            registry = getRegistryUrl(registry);
        }

        if (StringUtils.isBlank(registry) && StringUtils.isBlank(imageName.getRepository())) {
            callback.buildFailed("No Registry available", new Date());
        } else {


            boolean tagged = tagImage(imageName, imageId, callback, true);
            if (tagged) {
                callback.pushToRepositoryBegin(imageName.getRepository(), new Date());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Trying to push imageId {} to registry {}", imageId, imageNameToRegistry.replaceAll(":latest$", ""));
                }
                PushImageCmd pushImageCmd = dockerClient.pushImageCmd(imageNameToRegistry);

                if (StringUtils.isNotBlank(imageName.getTag())) {
                    pushImageCmd = pushImageCmd.withTag(imageName.getTag());
                }
                try {
                    pushImageCmd.exec(new PushImageResultCallback()).awaitSuccess();
                    callback.pushToRepositoryEnd(imageName.getRepository(), new Date());
                    return true;
                } catch (DockerClientException e) {
                    String message = "An error occure while trying to push image " + imageName.getFullyQualifiedName() + ".";
                    LOGGER.error(message, e);
                    callback.buildFailed(message + " : " + e.getMessage(), new Date());
                }
            }
        }
        return false;
    }

    private boolean tagImage(ImageName imageName, String imageId, DockerImageBuildCallback callback, boolean push) {
        String imageNameToRegistry = getImageDockerCmdName(imageName, push);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trying to tag imageId {} to {}", imageId, imageNameToRegistry);
        }
        try {
            dockerClient.tagImageCmd(imageId, imageNameToRegistry, imageName.getTag() != null ? imageName.getTag() : "").withForce().exec();
        } catch (DockerClientException e) {
            String message = "An error occurre while trying to tag image " + imageName.getFullyQualifiedName() + ".";
            LOGGER.error(message, e);
            callback.buildFailed(message + " : " + e.getMessage(), new Date());
            return false;
        }
        return true;
    }


}
