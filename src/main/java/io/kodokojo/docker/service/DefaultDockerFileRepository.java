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

    private final Map<ImageName,List<DockerFile>> dependencies;

    private final Map<ImageName,DockerFile> dockerFiles;

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

        return null;
    }

    @Override
    public List<DockerFile> getDockerFileChildOf(DockerFile dockerFile) {
        return null;
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
            ArrayList<DockerFile> value = new ArrayList<>();
            dockerFiles.put(dockerFile.getImageName(), dockerFile);
            List<DockerFile> previous = dependencies.put(dockerFile.getImageName(), value);
            if (CollectionUtils.isNotEmpty(previous)) {
                value.addAll(previous);
            }
            ImageName from = dockerFile.getFrom();
            if (from != null) {
                List<DockerFile> children = dependencies.get(from);
                if (children != null) {
                    if (!children.contains(dockerFile)) {
                        children.add(dockerFile);
                    }
                } else {
                    LOGGER.info("Not able to found DockerFile parent {} in repository", from);
                }
            }
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
