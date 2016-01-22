package io.kodokojo.docker;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.kodokojo.commons.config.*;
import io.kodokojo.docker.config.ActorModule;
import io.kodokojo.docker.config.StandardServiceModule;
import io.kodokojo.docker.service.DockerFileRepository;
import io.kodokojo.commons.docker.fetcher.DockerFileSource;
import io.kodokojo.docker.service.source.RestEntryPoint;
import io.kodokojo.docker.config.DockerModule;
import io.kodokojo.docker.config.PropertyModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        Injector injector = Guice.createInjector( new PropertyModule(), new StandardServiceModule(), new ActorModule(), new DockerModule());

        RestEntryPoint entryPoint = injector.getInstance(RestEntryPoint.class);
        KodokojoConfig kodokojoConfig = injector.getInstance(KodokojoConfig.class);

        DockerFileRepository dockerFileRepository = injector.getInstance(DockerFileRepository.class);
        DockerFileSource dockerFileSource = injector.getInstance(DockerFileSource.class);
        LOGGER.info("Docker-image-manager for project '{}' on stack '{} {}' fetching Dockerfiles.");
        dockerFileRepository.addAllDockerFile(dockerFileSource.fetchAllDockerFile());
        dockerFileRepository.addAllDockerFile(dockerFileSource.fetchAllDockerFile());

        entryPoint.start();
        LOGGER.info("Docker-image-manager for project '{}' on stack '{} {}' SUCCESSFULLY Start.", kodokojoConfig.projectName(), kodokojoConfig.stackType(), kodokojoConfig.stackName());

    }

}
