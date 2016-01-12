package io.kodokojo.docker.service.back;

import io.kodokojo.docker.model.Image;
import io.kodokojo.docker.model.Layer;
import io.kodokojo.docker.model.RegistryEvent;
import io.kodokojo.docker.model.StringToImageNameConverter;
import io.kodokojo.docker.service.DefaultDockerFileRepository;
import io.kodokojo.docker.service.DockerFileRepository;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

@Ignore
public class DefaultDockerFileBuildOrchestratorTest {

    private DockerFileBuildOrchestrator orchestrator;

    @Before
    public void setup() {
        DockerFileRepository dockerFileRepository = new DefaultDockerFileRepository();
        orchestrator = new DefaultDockerFileBuildOrchestrator(dockerFileRepository);
    }

    @Test
    public void receive_update_once() {

        Image image = new Image(StringToImageNameConverter.convert("jpthiery/busybox"), new ArrayList<>());
        Layer layer = new Layer("sha1:123456", 42);
        RegistryEvent registryEvent = new RegistryEvent(new Date(), RegistryEvent.EventType.PUSH, RegistryEvent.EventMethod.PUT, null, image, layer);
        boolean res = orchestrator.receiveUpdateEvent(registryEvent);

        assertThat(res).isFalse();

    }

}