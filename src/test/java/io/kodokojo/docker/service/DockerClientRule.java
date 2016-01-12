package io.kodokojo.docker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DockerClientRule extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClientRule.class);

    private DockerClient dockerClient;

    private List<String> containerToClean;

    public DockerClientRule() {
        dockerClient = DockerClientBuilder.getInstance().build();
        if (isNotWorking(dockerClient)) {
            String userHome = System.getProperty("user.home");
            DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder().withUri("https://192.168.99.100:2376").withDockerCertPath(userHome + "/.docker/machine/machines/default").build();
            dockerClient = DockerClientBuilder.getInstance(config).build();
        }
        containerToClean = new ArrayList<>();
    }

    public void addContainerIdToClean(String id) {
        containerToClean.add(id);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        containerToClean.clear();
    }

    @Override
    protected void after() {
        super.after();
        containerToClean.forEach(id -> {
            dockerClient.stopContainerCmd(id).exec();
            dockerClient.removeContainerCmd(id).exec();
            LOGGER.debug("Stopped and removed container id {}", id);
        });
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    private boolean isNotWorking(DockerClient dockerClient) {
        try {
            dockerClient.listImagesCmd().exec();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
