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

import com.github.dockerjava.api.DockerClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.commons.config.KodokojoConfig;
import io.kodokojo.commons.docker.fetcher.DockerFileSource;
import io.kodokojo.commons.docker.fetcher.git.GitBashbrewDockerFileSource;
import io.kodokojo.commons.docker.fetcher.git.GitDockerFileProjectFetcher;
import io.kodokojo.commons.utils.docker.DockerSupport;
import io.kodokojo.commons.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.commons.utils.servicelocator.MergedServiceLocator;
import io.kodokojo.commons.utils.servicelocator.ServiceLocator;
import io.kodokojo.commons.utils.servicelocator.docker.DockerServiceLocator;
import io.kodokojo.commons.utils.servicelocator.property.PropertyServiceLocator;
import io.kodokojo.docker.service.DefaultDockerFileRepository;
import io.kodokojo.docker.service.DefaultDockerImageRepository;
import io.kodokojo.docker.service.DockerFileRepository;
import io.kodokojo.docker.service.DockerImageRepository;
import io.kodokojo.docker.service.back.*;
import io.kodokojo.docker.service.back.build.DockerClientDockerImageBuilder;
import io.kodokojo.docker.service.back.build.DockerImageBuilder;

import java.io.File;
import java.util.LinkedList;

public class StandardServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        DefaultDockerFileNodeRepository defaultDockerFileNodeRepository = new DefaultDockerFileNodeRepository();
        bind(DockerFileNodeRepository.class).toInstance(defaultDockerFileNodeRepository);
        bind(DockerFileBuildPlanResultListener.class).toInstance(defaultDockerFileNodeRepository);
    }

    @Provides
    @Singleton
    DockerFileSource provideDockerFileSource(GitDockerFileProjectFetcher gitDockerFileProjectFetcher, GitBashbrewConfig gitBashbrewConfig, ApplicationConfig applicationConfig) {
        String workspace = applicationConfig.workspace();
        return new GitBashbrewDockerFileSource(workspace, null, gitBashbrewConfig.bashbrewGitUrl(), gitBashbrewConfig.bashbrewLibraryPath(), gitDockerFileProjectFetcher);
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
    ServiceLocator provideServiceLocator(DockerSupport dockerSupport, KodokojoConfig kodokojoConfig, PropertyValueProvider propertyValueProvider) {
        LinkedList<ServiceLocator> serviceLocators = new LinkedList<>();
        serviceLocators.add(new PropertyServiceLocator(propertyValueProvider));
        serviceLocators.add(new DockerServiceLocator(dockerSupport, kodokojoConfig));
        return new MergedServiceLocator(serviceLocators);
    }

    @Provides
    @Singleton
    DockerImageBuilder provideDockerImageBuilder(ApplicationConfig applicationConfig, DockerClient dockerClient, GitDockerFileProjectFetcher dockerFileProjectFetcher, ServiceLocator serviceLocator) {
        return new DockerClientDockerImageBuilder(dockerClient, new File(applicationConfig.dockerImageBuildDir()), dockerFileProjectFetcher, serviceLocator);

    }




}
