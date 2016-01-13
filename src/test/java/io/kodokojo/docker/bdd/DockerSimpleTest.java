package io.kodokojo.docker.bdd;

import com.tngtech.jgiven.junit.ScenarioTest;
import io.kodokojo.docker.bdd.docker.DockerCommonsGiven;
import io.kodokojo.docker.bdd.docker.DockerCommonsThen;
import io.kodokojo.docker.bdd.docker.DockerCommonsWhen;
import org.junit.Test;

public class DockerSimpleTest extends ScenarioTest<DockerCommonsGiven, DockerCommonsWhen, DockerCommonsThen> {

    @Test
    public void running_registry() {
        given().create_a_docker_client_on_socker_$("https://192.168.99.100:2376", "/Users/jpthiery/.docker/machine/machines/default");
        when().launch_image_$("registry:2", 5000);
        then().print_container_logs();
    }

}
