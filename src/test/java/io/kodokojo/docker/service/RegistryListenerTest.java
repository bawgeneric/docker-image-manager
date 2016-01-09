package io.kodokojo.docker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegistryListenerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryListenerTest.class);

    private DockerClient dockerClient;

    private List<String> containerIdToStop;

    @Before
    public void setup() {

        DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder().build();
        dockerClient = DockerClientBuilder.getInstance(config).build();

        pull("busybox:latest");
        pull("registry:2");

        containerIdToStop = new ArrayList<>();
    }


    @Test
    public void receive_event_from_registry() {

        File f = new File("src/test/resources/config.yml");
        String configPath = f.getAbsolutePath();
        Ports portBinding = new Ports();
        portBinding.bind(ExposedPort.tcp(5000), Ports.Binding(null));

        CreateContainerResponse registryCmd = dockerClient.createContainerCmd("registry:2")
                .withBinds(new Bind(configPath, new Volume("/etc/docker/registry/config.yml")))
                .withPortBindings(portBinding)
                .exec();
        containerIdToStop.add(registryCmd.getId());
        dockerClient.startContainerCmd(registryCmd.getId()).exec();
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(registryCmd.getId()).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();

        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(5000));
        Integer hostPort = bindingsExposed[0].getHostPort();

        LOGGER.info("Registry started on port {}", hostPort);
        RegistryListener registryListener = new RegistryListener();
        registryListener.start();

        dockerClient.tagImageCmd("busybox:latest", "localhost:" + hostPort + "/jpthiery/busybox", "").withForce().exec();

        LOGGER.info("Pushing image");
        dockerClient.pushImageCmd("localhost:" + hostPort + "/jpthiery/busybox").exec(new PushImageResultCallback()).awaitSuccess();
        LOGGER.info("Image pushed");

        registryListener.stop();

    }

    @After
    public void tearDown() {
        for (String id : containerIdToStop) {
            dockerClient.stopContainerCmd(id).exec();
            dockerClient.removeContainerCmd(id).exec();
        }
    }

    private void pull(String imageName) {
        try {
            dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion().onComplete();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}