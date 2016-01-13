package io.kodokojo.docker.bdd.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;

import java.util.ArrayList;
import java.util.List;

public class DockerCommonsWhen<SELF extends DockerCommonsWhen<SELF>> extends Stage<SELF> {

    @ExpectedScenarioState
    DockerClient dockerClient;

    @ProvidedScenarioState(resolution = ScenarioState.Resolution.NAME)
    List<String> containersToRemove = new ArrayList<>();

    public SELF launch_image_$(String imageName, @Hidden int ... ports) {

        List<PortBinding> portBindings = new ArrayList<>();
        for(int port : ports) {
            PortBinding binding = new PortBinding(Ports.Binding(null),ExposedPort.tcp(port));
            portBindings.add(binding);
        }
        CreateContainerResponse containerId = dockerClient.createContainerCmd(imageName)
                .withPortBindings(portBindings.toArray(new PortBinding[0]))
                .exec();
        containersToRemove.add(containerId.getId());
        dockerClient.startContainerCmd(containerId.getId()).exec();
        return self();
    }

}
