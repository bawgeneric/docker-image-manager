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

import io.kodokojo.commons.docker.model.DockerFile;
import io.kodokojo.commons.docker.model.ImageName;
import io.kodokojo.docker.model.DockerFileBuildPlanResult;
import io.kodokojo.docker.model.DockerFileBuildResponse;
import io.kodokojo.docker.model.DockerFileNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultDockerFileNodeRepository implements DockerFileBuildPlanResultListener, DockerFileNodeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerFileNodeRepository.class);

    private final Map<ImageName, DockerFileNode> index;

    private final Set<DockerFileNode> roots;

    private final ReentrantReadWriteLock.WriteLock writeLock;

    private final ReentrantReadWriteLock.ReadLock readLock;

    @Inject
    public DefaultDockerFileNodeRepository() {

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        writeLock = lock.writeLock();
        readLock = lock.readLock();
        this.roots = new HashSet<>();
        this.index = new HashMap<>();
    }

    @Override
    public void receiveDockerFileBuildPlanResult(DockerFileBuildPlanResult dockerFileBuildPlanResult) {
        if (dockerFileBuildPlanResult == null) {
            throw new IllegalArgumentException("dockerFileBuildPlanResult must be defined.");
        }
        ImageName imageName = dockerFileBuildPlanResult.getDockerFile().getImageName();
        writeLock.lock();
        try {

            DockerFileNode dockerFileNode = index.get(imageName);
            if (dockerFileNode != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Found a DockerFileNode {} for imageName {}.", dockerFileNode, imageName);
                }
                DockerFileBuildResponse buildResponse = dockerFileBuildPlanResult.getBuildResponse();
                if (buildResponse != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Update current state of DockerFileNode.", dockerFileNode);
                    }
                    dockerFileNode.setLastUpdate(buildResponse.getLastUpdateDate());
                    dockerFileNode.setLastSuccessBuild(buildResponse.getBuildSuccessDate());
                    dockerFileNode.setLastFailBuild(buildResponse.getBuildFailDate());
                } else {
                    Set<DockerFileNode> children = dockerFileNode.getChildren();
                    for (DockerFileBuildResponse childResponse : dockerFileBuildPlanResult.getChildrenResult()) {
                        DockerFileNode child = getDockerFileNodeInSet(children, childResponse.getDockerFileBuildRequest().getDockerFile().getImageName());
                        if (child == null) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Add child to DockerFileNode {} for imageName {}.", dockerFileNode, imageName);
                            }
                            Date now = new Date();
                            child = new DockerFileNode(childResponse.getDockerFileBuildRequest().getDockerFile(), null, now, null, null);
                            dockerFileNode.getChildren().add(child);
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Update a child of DockerFileNode {} for imageName {}.", dockerFileNode, imageName);
                            }
                            child.setLastUpdate(childResponse.getLastUpdateDate());
                            child.setLastSuccessBuild(childResponse.getBuildSuccessDate());
                            child.setLastFailBuild(childResponse.getBuildFailDate());
                        }

                    }
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Not DockerFileNode found for imageName {}.", imageName);
                }
                DockerFileBuildResponse buildResponse = dockerFileBuildPlanResult.getBuildResponse();
                DockerFileNode parent = index.get(dockerFileBuildPlanResult.getDockerFile().getFrom());
                if (LOGGER.isDebugEnabled() && parent != null) {
                    LOGGER.debug("Found existing parent node {}", parent);
                }
                Set<DockerFileNode> children = new HashSet<>();
                if (buildResponse != null) {
                    //  BuildPlanResult send for a specific Images
                    dockerFileNode = new DockerFileNode(buildResponse.getDockerFileBuildRequest().getDockerFile(), children, buildResponse.getLastUpdateDate(), buildResponse.getBuildSuccessDate(), buildResponse.getBuildFailDate());
                } else {
                    // Changed seems to be done on a child of this DockerFileNode xhere we don't know existence.
                    dockerFileNode = new DockerFileNode(dockerFileBuildPlanResult.getDockerFile(), children, new Date(), null, null);
                }
                //  Populate children
                for (DockerFileBuildResponse child : dockerFileBuildPlanResult.getChildrenResult()) {
                    DockerFileNode node = new DockerFileNode(child.getDockerFileBuildRequest().getDockerFile(), null, child.getLastUpdateDate(), child.getBuildSuccessDate(), child.getBuildFailDate());
                    node.setBuildOutput(child.getOutput());
                    node.setFailReason(child.getFailedReason());
                    children.add(node);
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Put following entry {} = {}", imageName, dockerFileNode);
            }
            index.put(imageName, dockerFileNode);

            DockerFile parentDockerFile = dockerFileNode.getDockerFile();
            DockerFileNode parenNode = null;
            if (parentDockerFile != null) {
                parenNode = index.get(parentDockerFile.getFrom());
            }
            if (parenNode == null) {
                roots.add(dockerFileNode);
            }
        } finally {
            writeLock.unlock();
        }


    }

    @Override
    public void addDockerFileNode(DockerFileNode dockerFileNode) {
        if (dockerFileNode == null) {
            throw new IllegalArgumentException("dockerFileNode must be defined.");
        }
        writeLock.lock();
        try {
            DockerFileNode previous = index.get(dockerFileNode.getDockerFile().getImageName());
            if (previous != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Replacing DockerFileNode {} by {}.", previous, dockerFileNode);
                }
            }
            if (dockerFileNode.getDockerFile().getFrom() != null) {
                ImageName from = dockerFileNode.getDockerFile().getFrom();
                DockerFileNode parentNode = index.get(from);
                if (parentNode != null) {
                    Set<DockerFileNode> children = parentNode.getChildren();
                    children.add(dockerFileNode);
                }

            } else {
                roots.add(dockerFileNode);
            }
            index.put(dockerFileNode.getDockerFile().getImageName(), dockerFileNode);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public DockerFileNode getDockerFileNode(ImageName imageName) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        readLock.lock();
        try {
            return index.get(imageName);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<DockerFileNode> getRoots() {
        readLock.lock();
        try {
            return new HashSet<>(roots);
        } finally {
            readLock.unlock();
        }
    }

    private static DockerFileNode getDockerFileNodeInSet(Set<DockerFileNode> nodes, ImageName imageName) {
        Iterator<DockerFileNode> iterator = nodes.iterator();
        DockerFileNode res = null;

        while (res == null && iterator.hasNext()) {
            DockerFileNode node = iterator.next();
            if (node.getDockerFile().getImageName().equals(imageName)) {
                res = node;
            }
        }

        return res;
    }
}
