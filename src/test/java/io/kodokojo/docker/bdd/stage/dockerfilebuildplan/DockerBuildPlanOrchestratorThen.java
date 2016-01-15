package io.kodokojo.docker.bdd.stage.dockerfilebuildplan;

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

import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.docker.bdd.stage.AbstractRestStage;
import io.kodokojo.docker.bdd.stage.docker.DockerCommonsGiven;
import io.kodokojo.docker.model.DockerFileBuildPlan;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.StringToImageNameConverter;
import retrofit.RetrofitError;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;

public class DockerBuildPlanOrchestratorThen<SELF extends DockerBuildPlanOrchestratorThen<?>> extends AbstractRestStage<SELF> {

    @ExpectedScenarioState
    Map<String, String> containers = new HashMap<>();

    public SELF docker_build_plan_orchestrator_contain_a_DockerBuildPlan_for_image_$(@Quoted String imageNameStr) {
        if (isBlank(imageNameStr)) {
            throw new IllegalArgumentException("imageNameStr must be defined.");
        }
        ImageName imageName = StringToImageNameConverter.convert(imageNameStr);


        String containerId = containers.get(DockerCommonsGiven.DOCKER_IMAGE_MANAGER_KEY);
        ClientRestEntryPoint restEntryPoint = provideClientRestEntryPoint(containerId, DockerCommonsGiven.DOCKER_IMAGE_MANAGER_PORT);

        DockerFileBuildPlan dockerFileBuildPlan = restEntryPoint.getDockerFileBuildPlan(imageName.getNamespace(), imageName.getName(), imageName.getTag());
        assertThat(dockerFileBuildPlan).isNotNull();

        return self();
    }

    public SELF docker_build_plan_orchestrator_NOT_not_a_DockerBuildPlan_for_image_$(@Quoted String imageNameStr) {
        if (isBlank(imageNameStr)) {
            throw new IllegalArgumentException("imageNameStr must be defined.");
        }
        ImageName imageName = StringToImageNameConverter.convert(imageNameStr);


        String containerId = containers.get(DockerCommonsGiven.DOCKER_IMAGE_MANAGER_KEY);
        ClientRestEntryPoint restEntryPoint = provideClientRestEntryPoint(containerId, DockerCommonsGiven.DOCKER_IMAGE_MANAGER_PORT);
        RetrofitError expected = null;
        try {
            restEntryPoint.getDockerFileBuildPlan(imageName.getNamespace(), imageName.getName(), imageName.getTag());
        } catch (RetrofitError e) {
            if (e.getResponse().getStatus() == 404 ) {expected = e;}

        }

        assertThat(expected).isNotNull();

        return self();
    }

}
