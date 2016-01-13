package io.kodokojo.docker.bdd.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.Hidden;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class DockerCommonsGiven<SELF extends DockerCommonsGiven<SELF>> extends Stage<SELF> {

    @ProvidedScenarioState
    DockerClient dockerClient;

    public SELF create_a_docker_client_on_socker_$(String serverUrl, @Hidden String certificatPath) {
        DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder().withUri(serverUrl).withDockerCertPath(certificatPath).build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
        return self();
    }

}
