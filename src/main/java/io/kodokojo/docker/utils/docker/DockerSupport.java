package io.kodokojo.docker.utils.docker;

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
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import io.kodokojo.docker.config.DockerConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DockerSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerSupport.class);

    private final DockerConfig dockerConfig;

    @Inject
    public DockerSupport(DockerConfig dockerConfig) {
        if (dockerConfig == null) {
            throw new IllegalArgumentException("dockerConfig must be defined.");
        }
        this.dockerConfig = dockerConfig;
    }

    public DockerClient createDockerClient() {
        DockerClientConfig config = null;
        if (StringUtils.isBlank(dockerConfig.dockerServerUrl())) {
            config = DockerClientConfig.createDefaultConfigBuilder().build();
        } else {
            config = DockerClientConfig.createDefaultConfigBuilder()
                    .withDockerCertPath(dockerConfig.dockerCertPath())
                    .withUri(dockerConfig.dockerServerUrl())
                    .build();
        }
        return DockerClientBuilder.getInstance(config).build();
    }

    public String getDockerHost() {
        if (StringUtils.isBlank(dockerConfig.dockerServerUrl())) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw new IllegalStateException("Unable to retrive public local host Address.", e);
            }
        }
        String host = dockerConfig.dockerServerUrl().replaceAll("^http(s)?://", "").replaceAll(":\\d+$", "");

        return host;
    }

}
