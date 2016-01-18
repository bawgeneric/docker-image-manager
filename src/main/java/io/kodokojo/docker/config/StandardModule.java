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
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.docker.service.DefaultDockerFileRepository;
import io.kodokojo.docker.service.DefaultDockerImageRepository;
import io.kodokojo.docker.service.DockerFileRepository;
import io.kodokojo.docker.service.DockerImageRepository;
import io.kodokojo.docker.service.actor.DependencyDockerfileUpdateDispatcher;
import io.kodokojo.docker.service.actor.PushEventChecker;
import io.kodokojo.docker.service.actor.PushEventDispatcher;
import io.kodokojo.docker.service.actor.RegistryRequestWorker;
import io.kodokojo.docker.service.back.DefaultDockerFileBuildOrchestrator;
import io.kodokojo.docker.service.back.DockerFileBuildOrchestrator;
import io.kodokojo.docker.service.connector.git.DockerFileSource;
import io.kodokojo.docker.service.connector.git.GitBashbrewDockerFileSource;
import io.kodokojo.docker.utils.properties.PropertyResolver;
import io.kodokojo.docker.utils.properties.provider.OrderedMergedValueProvider;
import io.kodokojo.docker.utils.properties.provider.PropertyValueProvider;
import io.kodokojo.docker.utils.properties.provider.SystemEnvValueProvider;
import io.kodokojo.docker.utils.properties.provider.SystemPropertyValueProvider;

import javax.inject.Named;
import java.io.File;
import java.util.LinkedList;

public class StandardModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(ActorSystem.class).toInstance(ActorSystem.apply("docker-image-manager"));

    }

    @Provides
    @Singleton
    GitBashbrewConfig provideGitBashbrewConfig() {
        LinkedList<PropertyValueProvider> valueProviders = new LinkedList<>();
        OrderedMergedValueProvider valueProvider = new OrderedMergedValueProvider(valueProviders);

        SystemPropertyValueProvider systemPropertyValueProvider = new SystemPropertyValueProvider();
        valueProviders.add(systemPropertyValueProvider);

        SystemEnvValueProvider systemEnvValueProvider = new SystemEnvValueProvider();
        valueProviders.add(systemEnvValueProvider);

        PropertyResolver propertyResolver = new PropertyResolver(valueProvider);
        return propertyResolver.createProxy(GitBashbrewConfig.class);
    }

    @Provides
    @Singleton
    DockerFileSource provideDockerFileSource(DockerFileRepository dockerFileRepository, GitBashbrewConfig gitBashbrewConfig) {
        File baseDire = new File("");
        String workspace = baseDire.getAbsolutePath() + File.separator + "workspace";
        return new GitBashbrewDockerFileSource(workspace, null, gitBashbrewConfig.bashbrewGitUrl(), gitBashbrewConfig.bashbrewLibraryPath(), dockerFileRepository);
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
    @Named("registryRequestWorker")
    ActorRef provideRequestWorker(ActorSystem system) {
        return system.actorOf(Props.create(RegistryRequestWorker.class));
    }

    @Provides
    @Singleton
    @Named("dependencyDockerfileUpdateDispatcher")
    ActorRef provideDependencyDockerfileUpdateDispatcher(ActorSystem system, DockerFileBuildOrchestrator orchestrator) {
        return system.actorOf(Props.create(DependencyDockerfileUpdateDispatcher.class, orchestrator));
    }

    @Provides
    @Singleton
    @Named("pushEventDispatcher")
    ActorRef providePushEventDispatcher(ActorSystem system, @Named("pushEventChecker") ActorRef pushEventChecker, @Named("registryRequestWorker") ActorRef registryRequestWorker) {
        return system.actorOf(Props.create(PushEventDispatcher.class, pushEventChecker, registryRequestWorker));
    }

    @Provides
    @Singleton
    @Named("pushEventChecker")
    ActorRef providePushEventChecker(ActorSystem system, DockerImageRepository dockerImageRepository, @Named("dependencyDockerfileUpdateDispatcher") ActorRef dependencyDockerfileUpdateDispatcher) {
        return system.actorOf(Props.create(PushEventChecker.class, dockerImageRepository, dependencyDockerfileUpdateDispatcher));
    }

    @Provides
    @Singleton
    DockerFileBuildOrchestrator provideDockerFileBuildOrchestrator(DockerFileRepository dockerFileRepository, DockerFileSource dockerFileSource) {
        return new DefaultDockerFileBuildOrchestrator(dockerFileRepository, dockerFileSource);
    }
}
