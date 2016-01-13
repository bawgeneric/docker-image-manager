package io.kodokojo.docker.bdd.docker;

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
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;
import io.kodokojo.docker.service.DockerClientRule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Rule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DockerCommonsGiven extends Stage<DockerCommonsGiven> {

    @Rule
    @ProvidedScenarioState
    public DockerClientRule dockerClientRule = new DockerClientRule();

    @ProvidedScenarioState
    DockerClient dockerClient;

    @ProvidedScenarioState
    String kodokojoDockerImageContainerName;

    @ProvidedScenarioState
    int registryPort;

    @BeforeScenario
    public void create_a_docker_client() {
        dockerClient = dockerClientRule.getDockerClient();
    }

    public DockerCommonsGiven $_is_pull(@Quoted String imageName) {
        dockerClientRule.pullImage(imageName);
        return self();
    }

    public DockerCommonsGiven kodokojo_docker_image_manager_is_started() {
        dockerClientRule.pullImage("java:8-jre");

        File baseDire = new File("");
        File targetFile = new File(baseDire.getAbsolutePath() + File.separator + "target");
        File projectJarFile = FileUtils.listFiles(targetFile, new RegexFileFilter("docker-image-manager-([\\.\\d]*)(-SNAPSHOT)?.jar"), FalseFileFilter.FALSE).stream().findFirst().get();

        CreateContainerResponse containerId = dockerClient.createContainerCmd("java:8-jre")
                .withExposedPorts(ExposedPort.tcp(8080))
                .withBinds(new Bind(baseDire.getAbsolutePath(), new Volume("/project")))
                .withWorkingDir("/project")
                .withCmd("java", "-jar", "/project/target/" + projectJarFile.getName())
                .exec();
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(containerId.getId()).exec();
        kodokojoDockerImageContainerName = inspectContainerResponse.getName();
        dockerClient.startContainerCmd(containerId.getId()).exec();
        dockerClientRule.addContainerIdToClean(containerId.getId());
        return self();
    }

    public DockerCommonsGiven $_image_is_started(String imageName, @Hidden int ... ports) {

        List<PortBinding> portBindings = new ArrayList<>();
        for(int port : ports) {
            PortBinding binding = new PortBinding(Ports.Binding(null),ExposedPort.tcp(port));
            portBindings.add(binding);
        }
        CreateContainerResponse containerId = dockerClient.createContainerCmd(imageName)
                .withPortBindings(portBindings.toArray(new PortBinding[0]))
                .exec();
        dockerClientRule.addContainerIdToClean(containerId.getId());
        dockerClient.startContainerCmd(containerId.getId()).exec();
        return self();
    }

    public DockerCommonsGiven registry_is_started() {
        File f = new File("src/test/resources/config.yml");
        String configPath = f.getAbsolutePath();
        Ports portBinding = new Ports();
        portBinding.bind(ExposedPort.tcp(5000), Ports.Binding(null));


        CreateContainerResponse registryCmd = dockerClient.createContainerCmd("registry:2")
                .withBinds(new Bind(configPath, new Volume("/etc/docker/registry/config.yml")))
                .withPortBindings(portBinding)
                .withLinks(new Link(kodokojoDockerImageContainerName, "dockerimagemanager"))
                .exec();
        dockerClientRule.addContainerIdToClean(registryCmd.getId());
        dockerClient.startContainerCmd(registryCmd.getId()).exec();
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(registryCmd.getId()).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();

        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(5000));
        registryPort = bindingsExposed[0].getHostPort();
        System.out.println("Registry port " + registryPort);
        return self();
    }

}
