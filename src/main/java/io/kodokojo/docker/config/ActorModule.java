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
import io.kodokojo.docker.service.DockerImageRepository;
import io.kodokojo.docker.service.back.DockerFileBuildOrchestrator;
import io.kodokojo.docker.service.back.build.DockerImageBuilder;
import io.kodokojo.docker.service.actor.*;

import javax.inject.Named;

public class ActorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ActorSystem.class).toInstance(ActorSystem.apply("commons-image-manager"));
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
    ActorRef provideDependencyDockerfileUpdateDispatcher(ActorSystem system, DockerFileBuildOrchestrator orchestrator, @Named("dockerImageBuilder") ActorRef dockerImageBuilder) {
        return system.actorOf(Props.create(DependencyDockerfileUpdateDispatcher.class, orchestrator, dockerImageBuilder));
    }

    @Provides
    @Singleton
    @Named("dockerImageBuilder")
    ActorRef provideDockerImageBuilder(ActorSystem system, DockerImageBuilder builder) {
        return system.actorOf(Props.create(DockerImageBuilderWorker.class, builder));
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

}
