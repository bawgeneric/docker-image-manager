package io.kodokojo.docker.utils.serviceLocator.docker;

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
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Filters;
import io.kodokojo.docker.config.KodokojoConfig;
import io.kodokojo.docker.utils.docker.DockerSupport;
import io.kodokojo.docker.utils.serviceLocator.Service;
import io.kodokojo.docker.utils.serviceLocator.ServiceLocator;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.*;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerServiceLocator implements ServiceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerServiceLocator.class);

    public static final String KODOKOJO = "kodokojo-";

    private final DockerSupport dockerSupport;

    private final DockerClient dockerClient;
    
    private final KodokojoConfig kodokojoConfig;

    @Inject
    public DockerServiceLocator(DockerSupport dockerSupport, KodokojoConfig kodokojoConfig) {
        if (dockerSupport == null) {
            throw new IllegalArgumentException("dockerClient must be defined.");
        }
        if (kodokojoConfig == null) {
            throw new IllegalArgumentException("kodokojoConfig must be defined.");
        }
        this.kodokojoConfig = kodokojoConfig;
        this.dockerSupport = dockerSupport;
        this.dockerClient = dockerSupport.createDockerClient();
    }

    @Override
    public Set<Service> getService(String type, String name) {
        List<String> labels = new ArrayList<>(Arrays.asList(
                KODOKOJO + "componentType=" + type,
                KODOKOJO + "componentName=" + name)
        );

        Set<Service> services = searchServicesWithLabel(labels);

        if (services == null && LOGGER.isDebugEnabled() ) {
            LOGGER.debug("No container match name {}.", name);
        }
        return services;
    }

    @Override
    public Set<Service> getServiceByType(String type) {
        List<String> labels = new ArrayList<>(Arrays.asList(KODOKOJO + "componentType=" + type));

        Set<Service> services = searchServicesWithLabel(labels);

        if (services == null && LOGGER.isDebugEnabled() ) {
            LOGGER.debug("No container match type {}.", type);
        }
        return services;
    }

    @Override
    public Set<Service> getServiceByName(String name) {

        List<String> labels = new ArrayList<>(Arrays.asList(KODOKOJO + "componentName=" + name));

        Set<Service> services = searchServicesWithLabel(labels);

        if (services == null && LOGGER.isDebugEnabled() ) {
            LOGGER.debug("No container match name {}.", name);
        }
        return services;
    }

    private Set<Service> searchServicesWithLabel(List<String> labels) {
        assert labels != null : "labels must be defined";
        labels.add(KODOKOJO + "projectName=" + kodokojoConfig.projectName());
        labels.add(KODOKOJO + "stackName=" + kodokojoConfig.stackName());
        labels.add(KODOKOJO + "stackType=" + kodokojoConfig.stackType());
        Filters filters = new Filters()
                .withLabels(labels.toArray(new String[]{}));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DockerServiceLocator lookup container with following criteria {}", filters.toString());
        }
        List<Container> containers = dockerClient.listContainersCmd().withFilters(filters).exec();

        if (CollectionUtils.isNotEmpty(containers)) {
            Set<Service> res = new HashSet<>(containers.size());
            for(Container container : containers) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Lookup list of public port for container : {}", container.getId());
                }
                for (Container.Port port : container.getPorts()) {
                    if (port.getPublicPort() != null && port.getPublicPort() > 0) {
                        String name = container.getLabels().get(KODOKOJO + "componentName");
                        res.add(new Service(name, dockerSupport.getDockerHost(), port.getPublicPort()));
                    }
                }
            }
            return res;
        }
        return null;
    }
}
