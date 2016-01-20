package io.kodokojo.docker.config;

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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.github.dockerjava.api.DockerClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.docker.service.DefaultDockerFileRepository;
import io.kodokojo.docker.service.DefaultDockerImageRepository;
import io.kodokojo.docker.service.DockerFileRepository;
import io.kodokojo.docker.service.DockerImageRepository;
import io.kodokojo.docker.service.actor.*;
import io.kodokojo.docker.service.back.DefaultDockerFileBuildOrchestrator;
import io.kodokojo.docker.service.back.DockerFileBuildOrchestrator;
import io.kodokojo.docker.service.back.build.DockerClientDockerImageBuilder;
import io.kodokojo.docker.service.back.build.DockerImageBuilder;
import io.kodokojo.docker.service.connector.DockerFileSource;
import io.kodokojo.docker.service.connector.git.GitBashbrewDockerFileSource;
import io.kodokojo.docker.service.connector.git.GitDockerFileProjectFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.File;

public class StandardServiceModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardServiceModule.class);

    @Override
    protected void configure() {
        //
    }

    @Provides
    @Singleton
    DockerFileSource provideDockerFileSource(DockerFileRepository dockerFileRepository, GitDockerFileProjectFetcher gitDockerFileProjectFetcher, GitBashbrewConfig gitBashbrewConfig, ApplicationConfig applicationConfig) {
        String workspace = applicationConfig.workspace();
        return new GitBashbrewDockerFileSource(workspace, null, gitBashbrewConfig.bashbrewGitUrl(), gitBashbrewConfig.bashbrewLibraryPath(), dockerFileRepository, gitDockerFileProjectFetcher);
    }

    @Provides
    @Singleton
    GitDockerFileProjectFetcher provideGitDockerFileProjectFetcher(ApplicationConfig applicationConfig) {
        return new GitDockerFileProjectFetcher(applicationConfig.dockerFileProject());
    }

    @Provides
    @Singleton
    DockerImageRepository provideDockerImageRepository() {
        return new DefaultDockerImageRepository();
    }

    @Provides
    @Singleton
    DockerFileRepository provideDockerFileRepository() {
        return new DefaultDockerFileRepository();
    }

    @Provides
    @Singleton
    DockerFileBuildOrchestrator provideDockerFileBuildOrchestrator(DockerFileRepository dockerFileRepository, DockerFileSource dockerFileSource) {
        return new DefaultDockerFileBuildOrchestrator(dockerFileRepository, dockerFileSource);
    }

    @Provides
    @Singleton
    DockerImageBuilder provideDockerImageBuilder(ApplicationConfig applicationConfig, DockerConfig dockerConfig, DockerClient dockerClient, GitDockerFileProjectFetcher dockerFileProjectFetcher) {
        DockerClientDockerImageBuilder dockerClientDockerImageBuilder = new DockerClientDockerImageBuilder(dockerClient, new File(applicationConfig.dockerImageBuildDir()), dockerFileProjectFetcher);
        dockerClientDockerImageBuilder.defineRefistry(dockerConfig.dockerRegistryUrl());
        return dockerClientDockerImageBuilder;
    }



}
