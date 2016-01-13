package io.kodokojo.docker.service.back;

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