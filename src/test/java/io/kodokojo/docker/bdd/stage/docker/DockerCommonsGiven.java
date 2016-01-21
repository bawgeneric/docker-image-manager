package io.kodokojo.docker.bdd.stage.docker;

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
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;
import io.kodokojo.docker.config.DockerConfig;
import io.kodokojo.docker.config.PropertyModule;
import io.kodokojo.docker.utils.DockerClientSupport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;

import java.io.File;
import java.util.*;

//  TODO Move this class in a docker-commons project
public class DockerCommonsGiven extends Stage<DockerCommonsGiven> {

    public static final String DOCKER_IMAGE_MANAGER_KEY = "docker-image-manager";

    public static final int DOCKER_IMAGE_MANAGER_PORT = 8080;

    @Rule
    @ProvidedScenarioState
    public DockerClientSupport dockerClientSupport = new DockerClientSupport();

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

    private DockerConfig dockerConfig;

    @BeforeScenario
    public void create_a_docker_client() {
        dockerClient = dockerClientSupport.getDockerClient();
        Injector injector = Guice.createInjector(new PropertyModule());
        dockerConfig = injector.getInstance(DockerConfig.class);
    }

    @AfterScenario
    public void tear_down() {
        dockerClientSupport.stopAndRemoveContainer();
    }

    public DockerCommonsGiven $_is_pull(@Quoted String imageName) {
        dockerClientSupport.pullImage(imageName);
        return self();
    }

    public DockerCommonsGiven kodokojo_docker_image_manager_is_started() {
        dockerClientSupport.pullImage("java:8-jre");

        File baseDire = new File("");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseDire.getAbsolutePath()).append(File.separator);
        stringBuilder.append("src").append(File.separator).append("test").append(File.separator).append("resources");
        File testResources = new File(stringBuilder.toString());
        stringBuilder = new StringBuilder(testResources.getAbsolutePath());
        stringBuilder.append(File.separator).append("int-logback-config.xml");
        String logbackConfigPath = stringBuilder.toString();
        File logbakcConfigFile = new File(logbackConfigPath);
        File targetFile = new File(baseDire.getAbsolutePath() + File.separator + "target");
        File projectJarFile = FileUtils.listFiles(targetFile, new RegexFileFilter("docker-image-manager-([\\.\\d]*)(-SNAPSHOT)?.jar"), FalseFileFilter.FALSE).stream().findFirst().get();

        Ports portBinding = new Ports();
        ExposedPort exposedPort = ExposedPort.tcp(8080);
        portBinding.bind(exposedPort, Ports.Binding(null));

        ArrayList<Bind> bind = new ArrayList<>(Arrays.asList(new Bind(projectJarFile.getAbsolutePath(), new Volume("/project/app.jar")),
                new Bind(logbakcConfigFile.getAbsolutePath(), new Volume("/project/int-logback-config.xml"))
        ));
        Map<String, String> labels = new HashMap<>();
        String prefix = "kodokojo-";
        labels.put(prefix + "projectName", "Acme");
        labels.put(prefix + "stackName", "DevA");
        labels.put(prefix + "stackType", "Build");
        labels.put(prefix + "componentType", "dockerImageManager");
        labels.put(prefix + "componentName", "dockerImageManager");
        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd("java:8-jre")
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort)
                .withLabels(labels)
                .withWorkingDir("/project")
                .withCmd("java", "-Dproject.name=Acme","-Dstack.name=DevA","-Dstack.type=Build","-Dlogback.configurationFile=/project/int-logback-config.xml", "-Dgit.bashbrew.url=git://github.com/kodokojo/acme", "-Dgit.bashbrew.library.path=bashbrew/library", "-jar", "/project/app.jar");

        if (dockerConfig != null && StringUtils.isNotBlank(dockerConfig.dockerServerUrl())) {
            createContainerCmd = createContainerCmd.withEnv("DOCKER_HOST="+dockerConfig.dockerServerUrl(), "DOCKER_CERT_PATH="+dockerConfig.dockerCertPath());
            bind.add(new Bind(dockerConfig.dockerCertPath(), new Volume(dockerConfig.dockerCertPath())));
        }

        CreateContainerResponse containerResponseId = createContainerCmd.withBinds(bind.toArray(new Bind[0])).exec();

        this.containerId = containerResponseId.getId();
        containers.put(DOCKER_IMAGE_MANAGER_KEY, containerId);
        this.containerName = dockerClientSupport.getContainerName(this.containerId);
        dockerClient.startContainerCmd(containerResponseId.getId()).exec();
        dockerClientSupport.addContainerIdToClean(containerResponseId.getId());

        String url = dockerClientSupport.getHttpContainerUrl(containerId, 8080) + "/api";

        int timeout = 10000;
        boolean available = dockerClientSupport.waitUntilHttpRequestRespond(url, timeout);
        if (!available) {
            throw new IllegalStateException("Unable to obtain an available Docker image manager after " + timeout);
        }
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
        Map<String, String> labels = new HashMap<>();
        CreateContainerResponse containerResponseId = dockerClient.createContainerCmd(imageName)
                .withPortBindings(portBindings.toArray(new PortBinding[0]))
                .withExposedPorts(exposedPorts.toArray(new ExposedPort[0]))
                .exec();

        dockerClientSupport.addContainerIdToClean(containerResponseId.getId());
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

        Map<String, String> labels = new HashMap<>();
        String prefix = "kodokojo-";
        labels.put(prefix + "projectName", "Acme");
        labels.put(prefix + "stackName", "DevA");
        labels.put(prefix + "stackType", "Build");
        labels.put(prefix + "componentType", "dockerRegistry");
        labels.put(prefix + "componentName", "registry");
        CreateContainerResponse registryCmd = dockerClient.createContainerCmd("registry:2")
                .withBinds(new Bind(configPath, new Volume("/etc/docker/registry/config.yml")))
                .withPortBindings(portBinding)
                .withLinks(new Link(containerId, "dockerimagemanager"))
                .withLabels(labels)
                .exec();
        dockerClientSupport.addContainerIdToClean(registryCmd.getId());
        dockerClient.startContainerCmd(registryCmd.getId()).exec();
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(registryCmd.getId()).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();

        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(5000));
        registryPort = bindingsExposed[0].getHostPort();
        String url = dockerClientSupport.getHttpContainerUrl(registryCmd.getId(), 5000) + "/v2/";
        dockerClientSupport.waitUntilHttpRequestRespond(url, 2500);
        return self();
    }

}
