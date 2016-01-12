package io.kodokojo.docker.service;

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DefaultDockerFileRepository implements DockerFileRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDockerFileRepository.class);

    private final Map<ImageName, Set<DockerFile>> dependencies;

    private final Map<ImageName, DockerFile> dockerFiles;

    private final ReentrantReadWriteLock.WriteLock writeLock;

    private final ReentrantReadWriteLock.ReadLock readLock;

    public DefaultDockerFileRepository() {
        this.dependencies = new HashMap<>();
        this.dockerFiles = new HashMap<>();
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        writeLock = lock.writeLock();
        readLock = lock.readLock();
    }

    @Override
    public DockerFile getDockerFileInheriteFrom(DockerFile dockerFile) {
        if (dockerFile == null) {
            throw new IllegalArgumentException("dockerFile must be defined.");
        }
        readLock.lock();
        try {
            DockerFile current = dockerFiles.get(dockerFile.getImageName());
            if (dockerFile.getFrom() == null) {
                throw new IllegalStateException("Dockerfile " + current + " don't have current from image declaration.");
            }
            return dockerFiles.get(current.getFrom());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<DockerFile> getDockerFileChildOf(DockerFile dockerFile) {
        if (dockerFile == null) {
            throw new IllegalArgumentException("dockerFile must be defined.");
        }
        readLock.lock();
        try {
            return dependencies.get(dockerFile.getImageName());
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public DockerFile getDockerFileFromImageName(ImageName imageName) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        readLock.lock();
        try {
            return dockerFiles.get(imageName);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public DockerFile getDockerFileFromImageName(String imageName) {
        if (isBlank(imageName)) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        return getDockerFileFromImageName(new ImageName(imageName));
    }

    @Override
    public void addDockerFile(DockerFile dockerFile) {
        if (dockerFile == null) {
            throw new IllegalArgumentException("dockerFile must be defined.");
        }
        writeLock.lock();
        try {
            Set<DockerFile> value = new HashSet<>();
            dockerFiles.put(dockerFile.getImageName(), dockerFile);
            Set<DockerFile> previous = dependencies.put(dockerFile.getImageName(), value);
            if (CollectionUtils.isNotEmpty(previous)) {
                value.addAll(previous);
            }
            ImageName from = dockerFile.getFrom();
            if (from != null) {
                Set<DockerFile> children = dependencies.get(from);
                if (children == null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Unable to find dockerfile parent {}, create a default one.", from.getFullyQualifiedName());
                    }
                    children = new HashSet<>();
                    DockerFile defaultParent = new DockerFile(from);
                    dockerFiles.put(from, defaultParent);
                } else if (!children.contains(dockerFile)) {
                    children.add(dockerFile);
                }
            }
            Set<DockerFile> dockerfileChildren = dependencies.get(dockerFile.getImageName());
            this.dockerFiles.values().stream().filter(potentialChild -> dockerFile.getImageName().equals(potentialChild.getFrom())).forEach(potentialChild -> {
                if (LOGGER.isDebugEnabled() && !dockerfileChildren.contains(potentialChild)) {
                    LOGGER.debug("Current Dockerfile {} is defined as parent of image {}", dockerFile, potentialChild);
                }
                dockerfileChildren.add(potentialChild);
            });
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void addDockerFile(ImageName imageName) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        addDockerFile(new DockerFile(imageName));
    }

    @Override
    public void addDockerFile(String imageName) {
        if (isBlank(imageName)) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        addDockerFile(new DockerFile(imageName));
    }
}
