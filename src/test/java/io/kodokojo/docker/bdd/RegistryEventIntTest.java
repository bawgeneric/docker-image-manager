package io.kodokojo.docker.bdd;

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

import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.docker.bdd.docker.DockerCommonsGiven;
import io.kodokojo.docker.bdd.docker.DockerCommonsThen;
import io.kodokojo.docker.bdd.docker.DockerCommonsWhen;
import org.junit.Test;

public class RegistryEventIntTest extends ScenarioTest<DockerCommonsGiven, DockerCommonsWhen, DockerCommonsThen> {

    @Test
    public void running_project() {
        given().$_is_pull("busybox:latest")
        .and().kodokojo_docker_image_manager_is_started()
        .and().registry_is_started();
        when().push_image_$_to_registry("busybox");
    }

}
