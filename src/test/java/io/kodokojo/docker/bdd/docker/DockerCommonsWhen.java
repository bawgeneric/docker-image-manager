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
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DockerCommonsWhen extends Stage<DockerCommonsWhen> {

    @ExpectedScenarioState
    DockerClient dockerClient;

    @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME)
    List<String> containersToRemove = new ArrayList<>();

    @ExpectedScenarioState
    String kodokojoDockerImageContainerName;

    @ExpectedScenarioState
    int registryPort;

    public DockerCommonsWhen push_image_$_to_registry(@Quoted String imageName) {
        if (registryPort <= 0) {
            throw new IllegalStateException("Registry port not available");
        }
        String imageNameToRegistry = "localhost:" + registryPort + "/" + imageName;
        dockerClient.tagImageCmd(imageName, imageNameToRegistry, "").withForce().exec();
        dockerClient.pushImageCmd(imageNameToRegistry).exec(new PushImageResultCallback()).awaitSuccess();
        return self();
    }
}
