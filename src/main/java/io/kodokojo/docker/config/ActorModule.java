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
import io.kodokojo.docker.service.DockerImageRepository;
import io.kodokojo.docker.service.actor.*;
import io.kodokojo.docker.service.back.DockerFileBuildOrchestrator;
import io.kodokojo.docker.service.back.DockerFileBuildPlanResultListener;
import io.kodokojo.docker.service.back.build.DockerImageBuilder;

import javax.inject.Named;

public class ActorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ActorSystem.class).toInstance(ActorSystem.apply("commons-image-manager"));
    }


    @Provides
    @Named("registryRequestWorker")
    ActorRef provideRequestWorker(ActorSystem system) {
        return system.actorOf(Props.create(RegistryRequestWorker.class));
    }

    @Provides
    @Named("dockerFileBuildPlanWorker")
    ActorRef provideDockerFileBuildPlanWorker(ActorSystem system, DockerFileBuildOrchestrator orchestrator, @Named("dockerImageBuilder") ActorRef dockerImageBuilder, @Named("dockerBuildPlanResultListener") ActorRef dockerBuildPlanResultListener) {
        return system.actorOf(Props.create(DockerFileBuildPlanWorker.class, orchestrator, dockerImageBuilder, dockerBuildPlanResultListener));
    }

    @Provides
    @Named("dockerImageBuilder")
    ActorRef provideDockerImageBuilder(ActorSystem system, DockerImageBuilder builder) {
        return system.actorOf(Props.create(DockerImageBuilderWorker.class, builder));
    }

    @Provides
    @Named("pushEventDispatcher")
    ActorRef providePushEventDispatcher(ActorSystem system, @Named("pushEventChecker") ActorRef pushEventChecker, @Named("registryRequestWorker") ActorRef registryRequestWorker) {
        return system.actorOf(Props.create(PushEventDispatcher.class, pushEventChecker, registryRequestWorker));
    }

    @Provides
    @Named("pushEventChecker")
    ActorRef providePushEventChecker(ActorSystem system, DockerImageRepository dockerImageRepository, @Named("dockerFileBuildPlanWorker") ActorRef dockerFileBuildPlanWorker) {
        return system.actorOf(Props.create(PushEventChecker.class, dockerImageRepository, dockerFileBuildPlanWorker));
    }

    @Provides
    @Named("dockerBuildPlanResultListener")
    ActorRef provideDockerFileBuildPlanResultWorker(ActorSystem system, DockerFileBuildPlanResultListener dockerFileBuildPlanResultListener) {
        return system.actorOf(Props.create(DockerFileBuildPlanResultWorker.class, dockerFileBuildPlanResultListener));
    }

}
