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
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ScenarioState;

import java.util.ArrayList;
import java.util.List;

public class DockerCommonsThen<SELF extends DockerCommonsThen<SELF>> extends Stage<SELF> {

    @ExpectedScenarioState
    DockerClient dockerClient;

    @ExpectedScenarioState(resolution = ScenarioState.Resolution.NAME)
    List<String> containersToRemove = new ArrayList<>();

    public SELF print_container_logs() {
        LogContainerResultCallback resultCallback = new LogContainerResultCallback();
        try {
            dockerClient.logContainerCmd(containersToRemove.get(0)).withStdOut().exec(resultCallback).awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return self();
    }

}
