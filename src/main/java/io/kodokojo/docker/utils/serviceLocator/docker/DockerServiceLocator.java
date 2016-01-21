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
import io.kodokojo.docker.utils.docker.DockerSupport;
import io.kodokojo.docker.utils.serviceLocator.Service;
import io.kodokojo.docker.utils.serviceLocator.ServiceLocator;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerServiceLocator implements ServiceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerServiceLocator.class);

    private final DockerSupport dockerSupport;

    private final DockerClient dockerClient;

    @Inject
    public DockerServiceLocator(DockerSupport dockerSupport) {
        if (dockerSupport == null) {
            throw new IllegalArgumentException("dockerClient must be defined.");
        }
        this.dockerSupport = dockerSupport;
        this.dockerClient = dockerSupport.createDockerClient();
    }

    @Override
    public Set<Service> getServiceByName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        List<Container> containers = dockerClient.listContainersCmd().withFilters(new Filters().withLabels("kodokojo-type=registry")).exec();

        if (CollectionUtils.isNotEmpty(containers)) {
            Set<Service> res = new HashSet<>(containers.size());
            for(Container container : containers) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Lookup list of public port for container : {}", container.getId());
                }
                for (Container.Port port : container.getPorts()) {
                    if (port.getPublicPort() != null && port.getPublicPort() > 0) {
                        res.add(new Service(name, dockerSupport.getDockerHost(), port.getPublicPort()));
                    }
                }
            }
            return res;
        } else if (LOGGER.isDebugEnabled() ) {
            LOGGER.debug("No container match criteria {}.", name);
        }
        return null;
    }
}
