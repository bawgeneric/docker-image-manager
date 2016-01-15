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
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.*;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.StringToImageNameConverter;
import org.apache.commons.lang.StringUtils;

//  TODO Move this class in a docker-commons project
public class DockerCommonsWhen extends Stage<DockerCommonsWhen> {

    @ExpectedScenarioState
    DockerClient dockerClient;

    @ExpectedScenarioState
    int registryPort;

    public DockerCommonsWhen push_image_$_to_registry(@Quoted String imageName) {
        if (registryPort <= 0) {
            throw new IllegalStateException("Registry port not available");
        }
        ImageName converted = StringToImageNameConverter.convert(imageName);
        StringBuilder sb = new StringBuilder();
        sb.append("localhost:").append(registryPort).append("/");
        if (StringUtils.isNotBlank(converted.getNamespace())) {
            sb.append(converted.getNamespace()).append("/");
        }
        sb.append(converted.getName());
        String imageNameToRegistry = sb.toString();
        dockerClient.tagImageCmd(imageName, imageNameToRegistry, "").withForce().exec();

        dockerClient.pushImageCmd(imageNameToRegistry).exec(new PushImageResultCallback()).awaitSuccess();
        return self();
    }



}
