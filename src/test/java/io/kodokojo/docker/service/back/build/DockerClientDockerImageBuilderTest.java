package io.kodokojo.docker.service.back.build;

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
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.command.TagImageCmd;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.DockerFileBuildPlan;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.StringToImageNameConverter;
import io.kodokojo.docker.service.connector.DockerFileProjectFetcher;
import io.kodokojo.docker.service.connector.git.GitDockerFileScmEntry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerClientDockerImageBuilderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClientDockerImageBuilderTest.class);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private DockerClient dockerClient;

    private DockerFileProjectFetcher<GitDockerFileScmEntry> dockerFileProjectFetcher;

    @Before
    public void setup() {
        dockerClient = mock(DockerClient.class);
        dockerFileProjectFetcher = mock(DockerFileProjectFetcher.class);
    }

    @Test
    public void valid_build_execution() throws IOException, InterruptedException {

        DockerClientDockerImageBuilder imageBuilder = new DockerClientDockerImageBuilder(dockerClient, temporaryFolder.newFolder(), dockerFileProjectFetcher);


        ImageName imageName = StringToImageNameConverter.convert("jpthiery/busybox");
        ImageName centos = StringToImageNameConverter.convert("centos");
        DockerFile dockerFile = new DockerFile(imageName, centos);
        GitDockerFileScmEntry dockerFileScmEntry = new GitDockerFileScmEntry(imageName, "git://github.com/kodokojo/acme", "HEAD", "/dockerfile");
        DockerFileBuildPlan dockerFileBuildPlan = new DockerFileBuildPlan(dockerFile, Collections.EMPTY_SET, dockerFileScmEntry, new Date());

        when(dockerFileProjectFetcher.checkoutDockerFileProject(dockerFileScmEntry)).thenReturn(temporaryFolder.newFolder());
        PullImageCmd pullImgCmd  = mock(PullImageCmd.class);
        PullImageResultCallback resultCallback = mock(PullImageResultCallback.class);
        ArgumentCaptor<String> pullImageNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tagImageNameIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tagRegistryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pushCaptor = ArgumentCaptor.forClass(String.class);
        BuildImageCmd buildImageCmd = mock(BuildImageCmd.class);
        BuildImageResultCallback buildImageResultCallback = mock(BuildImageResultCallback.class);
        TagImageCmd tagImageCmd = mock(TagImageCmd.class);
        PushImageCmd pushImageCmd = mock(PushImageCmd.class);
        PushImageResultCallback pushImageResultCallback = mock(PushImageResultCallback.class);

        when(dockerClient.pullImageCmd(pullImageNameCaptor.capture())).thenReturn(pullImgCmd);
        when(pullImgCmd.exec(any())).thenReturn(resultCallback);
        when(resultCallback.awaitCompletion()).thenReturn(resultCallback);

        when(dockerClient.buildImageCmd(any(File.class))).thenReturn(buildImageCmd);
        when(buildImageCmd.exec(any())).thenReturn(buildImageResultCallback);
        when(buildImageResultCallback.awaitImageId()).thenReturn("123456");

        when(dockerClient.tagImageCmd(tagImageNameIdCaptor.capture(), tagRegistryCaptor.capture(),tagCaptor.capture())).thenReturn(tagImageCmd);
        when(tagImageCmd.withForce()).thenReturn(tagImageCmd);
        when(dockerClient.pushImageCmd(pushCaptor.capture())).thenReturn(pushImageCmd);
        when(pushImageCmd.exec(any())).thenReturn(pushImageResultCallback);


        TestDockerImageBuildCallback callback = new TestDockerImageBuildCallback();

        imageBuilder.build(dockerFileBuildPlan, callback, "localhost:5000");

        String expectedImageName = "localhost:5000/jpthiery/busybox:latest";

        assertThat(callback.success).isTrue();
        assertThat(pullImageNameCaptor.getValue()).isEqualTo("centos:latest");
        assertThat(tagImageNameIdCaptor.getValue()).isEqualTo("123456");
        assertThat(tagRegistryCaptor.getValue()).isEqualTo(expectedImageName);
        assertThat(tagCaptor.getValue()).isEqualTo("");
        assertThat(pushCaptor.getValue()).isEqualTo(expectedImageName);
    }

    private class TestDockerImageBuildCallback implements DockerImageBuildCallback {

        private boolean success = false;

        @Override
        public void fromImagePulled(ImageName imageName) {

        }

        @Override
        public void buildBegin(Date beginDate) {

        }

        @Override
        public void buildSuccess(Date endDate) {
            success = true;
        }

        @Override
        public void pushToRepositoryBegin(String repository, Date begin) {

        }

        @Override
        public void pushToRepositoryEnd(String repository, Date begin) {

        }

        @Override
        public void buildFailed(String reason, Date failDate) {
            fail(reason);
        }

        @Override
        public void appendOutput(String output) {
            LOGGER.debug("Output : {}",  output);
        }
    }
}