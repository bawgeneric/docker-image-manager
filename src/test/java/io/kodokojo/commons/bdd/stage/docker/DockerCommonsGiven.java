package io.kodokojo.commons.bdd.stage.docker;

/*
 * #%L
 * commons-image-manager
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
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;
import io.kodokojo.commons.config.DockerConfig;
import io.kodokojo.commons.utils.DockerTestSupport;
import io.kodokojo.commons.utils.docker.DockerSupport;
import io.kodokojo.commons.utils.properties.PropertyResolver;
import io.kodokojo.commons.utils.properties.provider.*;
import org.junit.Rule;

import java.util.*;

public class DockerCommonsGiven<SELF extends DockerCommonsGiven<?>> extends Stage<SELF> {

    public static final String DOCKER_IMAGE_MANAGER_KEY = "commons-image-manager";

    public static final int DOCKER_IMAGE_MANAGER_PORT = 8080;

    @Rule
    @ProvidedScenarioState
    public DockerTestSupport dockerClientSupport = new DockerTestSupport();

    @ProvidedScenarioState
    DockerClient dockerClient;

    @ProvidedScenarioState
    Map<String, String> containers = new HashMap<>();

    protected DockerConfig dockerConfig;

    protected DockerSupport dockerSupport;

    @BeforeScenario
    public void create_a_docker_client() {
        dockerClient = dockerClientSupport.getDockerClient();
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                LinkedList<PropertyValueProvider> propertyValueProviders = new LinkedList<>();

                propertyValueProviders.add(new SystemPropertyValueProvider());
                propertyValueProviders.add(new SystemEnvValueProvider());

                OrderedMergedValueProvider valueProvider = new OrderedMergedValueProvider(propertyValueProviders);
                PropertyResolver resolver = new PropertyResolver(new DockerConfigValueProvider(valueProvider));
                bind(DockerConfig.class).toInstance(resolver.createProxy(DockerConfig.class));
            }
        });
        dockerConfig = injector.getInstance(DockerConfig.class);
        dockerSupport = new DockerSupport(dockerConfig);
    }

    protected String startContainer(CreateContainerCmd createContainerCmd, String name) {
        if (createContainerCmd == null) {
            throw new IllegalArgumentException("createContainerCmd must be defined.");
        }
        CreateContainerResponse response = createContainerCmd.exec();

        dockerClient.startContainerCmd(response.getId()).exec();
        dockerClientSupport.addContainerIdToClean(response.getId());
        containers.put(name, response.getId());
        return response.getId();
    }

    @AfterScenario
    public void tear_down() {
        dockerClientSupport.stopAndRemoveContainer();
    }

    public SELF $_is_pull(@Quoted String imageName) {
        dockerClientSupport.pullImage(imageName);
        return self();
    }

    public SELF $_image_is_started(String imageName, @Hidden int... ports) {

        List<PortBinding> portBindings = new ArrayList<>();
        List<ExposedPort> exposedPorts = new ArrayList<>();
        for (int port : ports) {
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


    public Map<String, String> getContainers() {
        return containers;
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }
}
