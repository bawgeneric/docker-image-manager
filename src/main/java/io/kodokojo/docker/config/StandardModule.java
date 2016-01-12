package io.kodokojo.docker.config;

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

import javax.inject.Named;

public class StandardModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(ActorSystem.class).toInstance(ActorSystem.apply("docker-image-manager"));

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
    ActorRef provideDependencyDockerfileUpdateDispatcher(ActorSystem system,DockerFileBuildOrchestrator orchestrator) {
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
    DockerFileBuildOrchestrator provideDockerFileBuildOrchestrator(DockerFileRepository dockerFileRepository) {
        return new DefaultDockerFileBuildOrchestrator(dockerFileRepository);
    }
}
