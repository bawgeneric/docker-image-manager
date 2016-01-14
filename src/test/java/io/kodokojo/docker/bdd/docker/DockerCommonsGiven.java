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
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;
import io.kodokojo.docker.service.DockerClientRule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Rule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerCommonsGiven extends Stage<DockerCommonsGiven> {

    public static final String DOCKER_IMAGE_MANAGER_KEY = "docker-image-manager";

    @Rule
    @ProvidedScenarioState
    public DockerClientRule dockerClientRule = new DockerClientRule();

    @ProvidedScenarioState
    DockerClient dockerClient;

    @ProvidedScenarioState
    String containerName;

    @ProvidedScenarioState
    String containerId;

    @ProvidedScenarioState
    int registryPort;

    @ProvidedScenarioState
    Map<String, String> containers = new HashMap<>();

    @BeforeScenario
    public void create_a_docker_client() {
        dockerClient = dockerClientRule.getDockerClient();
    }

    @AfterScenario
    public void tear_down() {
        dockerClientRule.stopAndRemoveContainer();
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

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(8080);
        portBinding.bind(exposedPort, Ports.Binding(null));

        CreateContainerResponse containerResponseId = dockerClient.createContainerCmd("java:8-jre")
                .withBinds(new Bind(baseDire.getAbsolutePath(), new Volume("/project")))
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort)
                .withWorkingDir("/project")
                .withCmd("java", "-jar", "/project/target/" + projectJarFile.getName())
                .exec();
        this.containerId = containerResponseId.getId();
        containers.put(DOCKER_IMAGE_MANAGER_KEY, containerId);
        this.containerName = dockerClientRule.getContainerName(this.containerId);
        dockerClient.startContainerCmd(containerResponseId.getId()).exec();
        dockerClientRule.addContainerIdToClean(containerResponseId.getId());

        int portService = dockerClientRule.getExposedPort(containerId, 8080);
        String url = "http://" + dockerClientRule.getServerIp() + ":" + portService + "/api";

        dockerClientRule.waitUntilHttpRequestRespond(url, 5000);
        return self();
    }


    public DockerCommonsGiven $_image_is_started(String imageName, @Hidden int ... ports) {

        List<PortBinding> portBindings = new ArrayList<>();
        List<ExposedPort> exposedPorts = new ArrayList<>();
        for(int port : ports) {
            ExposedPort exposedPort = ExposedPort.tcp(port);
            exposedPorts.add(exposedPort);
            PortBinding binding = new PortBinding(Ports.Binding(null), exposedPort);
            portBindings.add(binding);
        }
        CreateContainerResponse containerResponseId = dockerClient.createContainerCmd(imageName)
                .withPortBindings(portBindings.toArray(new PortBinding[0]))
                .withExposedPorts(exposedPorts.toArray(new ExposedPort[0]))
                .exec();
        /*
        this.containerId = containerResponseId.getId();
        this.containerName = dockerClientRule.getContainerName(this.containerId);
        */
        dockerClientRule.addContainerIdToClean(containerResponseId.getId());
        dockerClient.startContainerCmd(containerResponseId.getId()).exec();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                .withLinks(new Link(containerId, "dockerimagemanager"))
                .exec();
        dockerClientRule.addContainerIdToClean(registryCmd.getId());
        dockerClient.startContainerCmd(registryCmd.getId()).exec();
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(registryCmd.getId()).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();

        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(5000));
        registryPort = bindingsExposed[0].getHostPort();

        String url = "http://" + dockerClientRule.getServerIp() + ":" + registryPort +"/v2/";
        dockerClientRule.waitUntilHttpRequestRespond(url, 2500);

        return self();
    }

}
