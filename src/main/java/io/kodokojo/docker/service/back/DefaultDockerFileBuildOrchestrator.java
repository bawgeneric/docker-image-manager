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

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.DockerFileBuildPlan;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.RegistryEvent;
import io.kodokojo.docker.service.DockerFileRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class DefaultDockerFileBuildOrchestrator implements DockerFileBuildOrchestrator{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerFileBuildOrchestrator.class);

    private final DockerFileRepository dockerFileRepository;

    private final Map<ImageName, DockerFileBuildPlan> buildPlan;

    private final ReentrantReadWriteLock.WriteLock writeLock;

    private final ReentrantReadWriteLock.ReadLock readLock;

    @Inject
    public DefaultDockerFileBuildOrchestrator(DockerFileRepository dockerFileRepository) {
        if (dockerFileRepository == null) {
            throw new IllegalArgumentException("dockerFileRepository must be defined.");
        }
        this.dockerFileRepository = dockerFileRepository;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        writeLock = lock.writeLock();
        readLock = lock.readLock();
        buildPlan = new HashMap<>();
    }

    @Override
    public boolean receiveUpdateEvent(RegistryEvent registryEvent) {
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
            DockerFile current = dockerFileRepository.getDockerFileFromImageName(imageName);
            if (current == null) {
                LOGGER.error("Unable to retrieve DockerFile for image {}", imageName.getFullyQualifiedName());
                return false;
            }
            DockerFileBuildPlan created = create(current, registryEvent.getTimestamp());
            writeLock.lock();
            try {
                dockerFileBuildPlan = buildPlan.get(imageName);
                if (dockerFileBuildPlan == null) {
                    buildPlan.put(imageName, created);
                    return true;
                }
            } finally {
                writeLock.lock();
            }
        }
        //  DockerBuildPlan already exist, update the last date for update.
        dockerFileBuildPlan.setLastUpdateDate(registryEvent.getTimestamp());

        return false;
    }

    @Override
    public DockerFileBuildPlan getBuildPlan(ImageName imageName) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        readLock.lock();
        try {
            return buildPlan.get(imageName);
        } finally {
            readLock.unlock();
        }
    }

    private DockerFileBuildPlan create(DockerFile current, Date timestamp) {
        Set<DockerFile> dockerFileChildOf = dockerFileRepository.getDockerFileChildOf(current);
        Set<DockerFileBuildPlan> children = new HashSet<>();
        if (CollectionUtils.isNotEmpty(dockerFileChildOf)) {
            children.addAll(dockerFileChildOf.stream().map(dockerFileChild -> create(dockerFileChild, timestamp)).collect(Collectors.toList()));
        }
        return new DockerFileBuildPlan(current, children, timestamp);

    }

}
