package io.kodokojo.docker.bdd.docker;

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
