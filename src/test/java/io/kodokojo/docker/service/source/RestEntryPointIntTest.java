package io.kodokojo.docker.service.source;

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
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.kodokojo.docker.config.StandardModule;
import io.kodokojo.docker.service.DockerClientSupport;
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
    public DockerClientSupport dockerClientSupport = new DockerClientSupport();

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

        DockerClient dockerClient = dockerClientSupport.getDockerClient();

        File f = new File("src/test/resources/config.yml");
        String configPath = f.getAbsolutePath();
        Ports portBinding = new Ports();
        portBinding.bind(ExposedPort.tcp(5000), Ports.Binding(null));


        CreateContainerResponse registryCmd = dockerClient.createContainerCmd("registry:2")
                .withBinds(new Bind(configPath, new Volume("/etc/docker/registry/config.yml")))
                .withPortBindings(portBinding)
                .exec();
        dockerClientSupport.addContainerIdToClean(registryCmd.getId());
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
            dockerClientSupport.getDockerClient().pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitCompletion().onComplete();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}