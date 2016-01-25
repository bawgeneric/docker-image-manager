package io.kodokojo.docker.service.back;

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
import io.kodokojo.commons.docker.model.*;
import io.kodokojo.docker.model.*;
import io.kodokojo.docker.service.DockerFileRepository;
import io.kodokojo.commons.docker.fetcher.DockerFileSource;
import io.kodokojo.commons.docker.fetcher.git.GitDockerFileScmEntry;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class DefaultDockerFileBuildOrchestrator implements DockerFileBuildOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerFileBuildOrchestrator.class);

    private final DockerFileSource<GitDockerFileScmEntry> dockerFileSource;

    private final DockerFileRepository dockerFileRepository;

    private final Map<ImageName, DockerFileBuildPlan> buildPlan;

    private final ReentrantReadWriteLock.WriteLock writeLock;

    private final ReentrantReadWriteLock.ReadLock readLock;

    @Inject
    public DefaultDockerFileBuildOrchestrator(DockerFileRepository dockerFileRepository, DockerFileSource<GitDockerFileScmEntry> dockerFileSource) {
        if (dockerFileRepository == null) {
            throw new IllegalArgumentException("dockerFileRepository must be defined.");
        }
        if (dockerFileSource == null) {
            throw new IllegalArgumentException("dockerFileSource must be defined.");
        }
        this.dockerFileRepository = dockerFileRepository;
        this.dockerFileSource = dockerFileSource;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        writeLock = lock.writeLock();
        readLock = lock.readLock();
        buildPlan = new HashMap<>();
    }

    @Override
    public DockerFileBuildPlan receiveUpdateEvent(RegistryEvent registryEvent) {
        if (registryEvent == null) {
            throw new IllegalArgumentException("registryEvent must be defined.");
        }
        DockerFileBuildPlan dockerFileBuildPlan = null;
        readLock.lock();
        ImageName imageName = registryEvent.getImage().getName();
        try {
            dockerFileBuildPlan = buildPlan.get(imageName);
        } finally {
            readLock.unlock();
        }
        if (dockerFileBuildPlan == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating a new DockerBuildPlan for image {}.", imageName.getFullyQualifiedName());
            }
            DockerFile current = dockerFileRepository.getDockerFileFromImageName(imageName);
            if (current == null) {
                Set<DockerFile> dockerFiles = dockerFileSource.fetchDockerFile(imageName);
                if (CollectionUtils.isNotEmpty(dockerFiles)) {
                    dockerFileRepository.addAllDockerFile(dockerFiles);
                    current = dockerFileRepository.getDockerFileFromImageName(imageName);
                } else {
                    LOGGER.error("Unable to retrieve DockerFile for image {}", imageName.getFullyQualifiedName());
                    return null;
                }
            }
            DockerFileBuildPlan created = create(current, registryEvent.getTimestamp());
            if (CollectionUtils.isNotEmpty(created.getChildren().keySet())) {
                writeLock.lock();
                try {
                    dockerFileBuildPlan = buildPlan.get(imageName);
                    //  Check DockerBuildPLan not already Added since the read check had been unlocked.
                    if (dockerFileBuildPlan == null) {
                        buildPlan.put(imageName, created);
                        created.setLastUpdateDate(new Date());
                        return created;
                    }
                } finally {
                    writeLock.unlock();
                }
            } else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Aborting DockerBuildPlan for image {}, this image haven't any Child", imageName.getFullyQualifiedName());
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DockerBuildPlan already exist for image {}.", imageName.getFullyQualifiedName());
        }

        //  Ensure we have last version fo DockerBuildPlan
        readLock.lock();
        try {
            dockerFileBuildPlan = buildPlan.get(imageName);
            if (dockerFileBuildPlan != null) {
            //  DockerBuildPlan already exist, update the last date for update.
                dockerFileBuildPlan.setLastUpdateDate(registryEvent.getTimestamp());
            }
        } finally {
            readLock.unlock();
        }

        return null;
    }

    @Override
    public DockerFileBuildPlan getBuildPlan(ImageName imageName) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        readLock.lock();
        try {
            DockerFileBuildPlan res = buildPlan.get(imageName);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Request DockerBuildPlan for image {}. Return : {}", imageName.getFullyQualifiedName(), res);
            }
            return res;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public DockerFileBuildPlanResult receiveDockerBuildRequest(DockerFileBuildRequest dockerFileBuildRequest) {
        if (dockerFileBuildRequest == null) {
            throw new IllegalArgumentException("dockerFileBuildRequest must be defined.");
        }
        throw new UnsupportedOperationException("Not yet coded, sorry.");
    }

    @Override
    public DockerFileBuildPlanResult receiveDockerBuildResponse(DockerFileBuildResponse dockerFileBuildResponse) {
        if (dockerFileBuildResponse == null) {
            throw new IllegalArgumentException("dockerFileBuildResponse must be defined.");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Receive DockerBuildPlanResponse : {}", dockerFileBuildResponse);
        }
        writeLock.lock();
        try {
            DockerFileBuildRequest dockerFileBuildRequest = dockerFileBuildResponse.getDockerFileBuildRequest();
            DockerFile dockerFile = dockerFileBuildRequest.getDockerFile();
            ImageName from = dockerFile.getFrom();
            DockerFileBuildPlan dockerFileBuildPlan= buildPlan.get(from);


            ImageName imageName = dockerFile.getImageName();
            if (dockerFileBuildPlan == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable to find a child DockerBuildPlan for image {}; lookup in buildPlan repository.", imageName);
                }
                dockerFileBuildPlan = buildPlan.get(imageName);
                if (dockerFileBuildPlan != null) {
                    dockerFileBuildPlan.setDockerFileBuildResponse(dockerFileBuildResponse);
                }
            }

            if (dockerFileBuildPlan == null) {
                throw new IllegalStateException("We don't have any build plan for image " + imageName.getFullyQualifiedName() + ".");
            }

            if (dockerFileBuildPlan.getDockerFile().getImageName().equals(dockerFileBuildResponse.getDockerFileBuildRequest().getDockerFile().getFrom())) {

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Affect Result {} to {} key",dockerFileBuildResponse ,  dockerFileBuildResponse.getDockerFileBuildRequest());
                }
                dockerFileBuildPlan.getChildren().put(dockerFileBuildResponse.getDockerFileBuildRequest(), dockerFileBuildResponse);
            }
            dockerFileBuildPlan.setLastUpdateDate(dockerFileBuildResponse.getLastUpdateDate());
            if (dockerBuildPlanIsFinish(dockerFileBuildPlan)) {
                DockerFileBuildPlanResult response = new DockerFileBuildPlanResult(dockerFileBuildPlan.getDockerFile(),new HashSet<>(dockerFileBuildPlan.getChildren().values()),dockerFileBuildPlan.getDockerFileBuildResponse());
                buildPlan.remove(dockerFileBuildPlan.getDockerFile().getImageName());
                return response;
            }
        } finally {
            writeLock.unlock();
        }
        return null;
    }

    private DockerFileBuildPlan create(DockerFile current, Date timestamp) {
        Set<DockerFile> dockerFileChildOf = dockerFileRepository.getDockerFileChildOf(current);
        Map<DockerFileBuildRequest, DockerFileBuildResponse> children = new HashMap<>();
        if (CollectionUtils.isNotEmpty(dockerFileChildOf)) {
            List<DockerFileBuildRequest> createdChildren = dockerFileChildOf.stream().map(dockerFileChild -> new DockerFileBuildRequest(dockerFileChild, dockerFileSource.getDockerFileScmEntry(dockerFileChild.getImageName()))).collect(Collectors.toList());
            createdChildren.forEach(child -> children.put(child, null));
        }
        GitDockerFileScmEntry dockerFileScmEntry = dockerFileSource.getDockerFileScmEntry(current.getImageName());
        DockerFileBuildPlan res = new DockerFileBuildPlan(current, children, dockerFileScmEntry, timestamp);
        if (LOGGER.isTraceEnabled()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            LOGGER.trace("Create following DockerBuildPlan for image {} : {}", current.getImageName().getFullyQualifiedName(), gson.toJson(res));
        }
        return res;

    }

    private boolean dockerBuildPlanIsFinish(DockerFileBuildPlan dockerFileBuildPlan) {
        assert dockerFileBuildPlan != null : "dockerFileBuildPlan must be defined";
        boolean res = true;
        Iterator<Map.Entry<DockerFileBuildRequest, DockerFileBuildResponse>> iterator = dockerFileBuildPlan.getChildren().entrySet().iterator();
        while(res && iterator.hasNext()) {
            Map.Entry<DockerFileBuildRequest, DockerFileBuildResponse> entry = iterator.next();
            res = entry.getValue() != null;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DockerFileBuildPlan {} is {} ", dockerFileBuildPlan, res ? "Complete" : "working in progress");
        }
        return res;
    }
}
