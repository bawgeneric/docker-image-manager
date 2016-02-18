package io.kodokojo.docker.bdd.stage;

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

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.*;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import io.kodokojo.commons.bdd.stage.docker.DockerCommonsGiven;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApplicationGiven <SELF extends ApplicationGiven<?>> extends DockerCommonsGiven<SELF> {


    public static final String DOCKER_IMAGE_MANAGER_KEY = "commons-image-manager";

    public static final int DOCKER_IMAGE_MANAGER_PORT = 8080;

    @ProvidedScenarioState
    String containerName;

    @ProvidedScenarioState
    String containerId;

    @ProvidedScenarioState
    int registryPort;

    private boolean dockerManagerLinked = false;

    public SELF kodokojo_docker_image_manager_is_started() {
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
        File projectJarFile = FileUtils.listFiles(targetFile, new RegexFileFilter("docker-image-manager-([\\.\\d]*)(-SNAPSHOT)?-runnable.jar"), FalseFileFilter.FALSE).stream().findFirst().get();

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
        CreateContainerCmd createContainerCmd = getDockerClient().createContainerCmd("java:8-jre")
                .withPortBindings(portBinding)
                .withExposedPorts(exposedPort)
                .withLabels(labels)
                .withWorkingDir("/project")
                .withCmd("java","-Dproject.name=Acme", "-Dstack.name=DevA", "-Dstack.type=Build", "-Dlogback.configurationFile=/project/int-logback-config.xml", "-Dgit.bashbrew.url=git://github.com/kodokojo/acme", "-Dgit.bashbrew.library.path=bashbrew/library", "-jar", "/project/app.jar");

        createContainerCmd = createContainerCmd.withEnv("DOCKER_HOST=unix:///var/run/docker.sock");
        bind.add(new Bind("/var/run/docker.sock", new Volume("/var/run/docker.sock")));

        CreateContainerResponse containerResponseId = createContainerCmd.withBinds(bind.toArray(new Bind[0])).exec();

        this.containerId = containerResponseId.getId();
        getContainers().put(DOCKER_IMAGE_MANAGER_KEY, containerId);
        this.containerName = dockerClientSupport.getContainerName(this.containerId);
        getDockerClient().startContainerCmd(containerResponseId.getId()).exec();
        dockerClientSupport.addContainerIdToClean(containerResponseId.getId());

        String url = dockerClientSupport.getHttpContainerUrl(containerId, 8080) + "/api";

        int timeout = 14000;
        boolean available = dockerSupport.waitUntilHttpRequestRespond(url, timeout);
        if (!available) {
            throw new IllegalStateException("Unable to obtain an available Docker image manager after " + timeout);
        }
        return self();
    }

    public SELF registry_is_started() {

        Ports portBinding = new Ports();
        portBinding.bind(ExposedPort.tcp(5000), Ports.Binding(null));

        Map<String, String> labels = new HashMap<>();
        String prefix = "kodokojo-";
        labels.put(prefix + "projectName", "Acme");
        labels.put(prefix + "stackName", "DevA");
        labels.put(prefix + "stackType", "Build");
        labels.put(prefix + "componentType", "dockerRegistry");
        labels.put(prefix + "componentName", "registry");
        CreateContainerCmd createContainerCmd = getDockerClient().createContainerCmd("registry:2")
                .withPortBindings(portBinding)
                .withLabels(labels);
        if (dockerManagerLinked) {
            File f = new File("src/test/resources/config.yml");
            String configPath = f.getAbsolutePath();
            createContainerCmd = createContainerCmd.withLinks(new Link(containerId, "dockerimagemanager"))
                    .withBinds(new Bind(configPath, new Volume("/etc/docker/registry/config.yml")));
        }
        CreateContainerResponse registryCmd = createContainerCmd
                .exec();
        dockerClientSupport.addContainerIdToClean(registryCmd.getId());
        getDockerClient().startContainerCmd(registryCmd.getId()).exec();
        InspectContainerResponse inspectContainerResponse = getDockerClient().inspectContainerCmd(registryCmd.getId()).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();

        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(5000));
        registryPort = bindingsExposed[0].getHostPort();
        String url = dockerClientSupport.getHttpContainerUrl(registryCmd.getId(), 5000) + "/v2/";
        dockerSupport.waitUntilHttpRequestRespond(url, 2500);
        return self();
    }

    public SELF registry_send_notification_to_docker_image_manager() {
        dockerManagerLinked = true;
        return self();
    }
}
