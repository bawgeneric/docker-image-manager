package io.kodokojo.docker.service.source;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.kodokojo.docker.config.StandardModule;
import io.kodokojo.docker.service.DockerClientRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

@Ignore
public class RestEntryPointIntTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEntryPointIntTest.class);

    @Rule
    public DockerClientRule dockerClientRule = new DockerClientRule();

    private Injector injector;

    @Before
    public void setup() {

        injector = Guice.createInjector(new StandardModule());

        pull("busybox:latest");
        pull("registry:2");
        pull("jenkins:latest");
    }

    @Test
    public void receive_event_from_registry() {

        DockerClient dockerClient = dockerClientRule.getDockerClient();

        File f = new File("src/test/resources/config.yml");
        String configPath = f.getAbsolutePath();
        Ports portBinding = new Ports();
        portBinding.bind(ExposedPort.tcp(5000), Ports.Binding(null));


        CreateContainerResponse registryCmd = dockerClient.createContainerCmd("registry:2")
                .withBinds(new Bind(configPath, new Volume("/etc/docker/registry/config.yml")))
                .withPortBindings(portBinding)
                .exec();
        dockerClientRule.addContainerIdToClean(registryCmd.getId());
        dockerClient.startContainerCmd(registryCmd.getId()).exec();
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(registryCmd.getId()).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();

        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(5000));
        Integer hostPort = bindingsExposed[0].getHostPort();
        LOGGER.info("Registry started on port {}", hostPort);

        //  Change this by Docker container
        RestEntryPoint restEntryPoint = injector.getInstance(RestEntryPoint.class);
        restEntryPoint.start();

        dockerClient.tagImageCmd("busybox:latest", "localhost:" + hostPort + "/jpthiery/busybox", "").withForce().exec();
        //dockerClient.tagImageCmd("jenkins:latest", "localhost:" + hostPort + "/jpthiery/jenkins", "").withForce().exec();

        LOGGER.info("Pushing image");
        dockerClient.pushImageCmd("localhost:" + hostPort + "/jpthiery/busybox").exec(new PushImageResultCallback()).awaitSuccess();
        //dockerClient.pushImageCmd("localhost:" + hostPort + "/jpthiery/jenkins").exec(new PushImageResultCallback()).awaitSuccess();
        LOGGER.info("Image pushed");

        restEntryPoint.stop();

    }


    private void pull(String imageName) {
        try {
            dockerClientRule.getDockerClient().pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion().onComplete();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}