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
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.command.TagImageCmd;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import io.kodokojo.commons.docker.model.*;
import io.kodokojo.commons.docker.fetcher.DockerFileProjectFetcher;
import io.kodokojo.commons.docker.fetcher.git.GitDockerFileScmEntry;
import io.kodokojo.commons.utils.servicelocator.Service;
import io.kodokojo.commons.utils.servicelocator.ServiceLocator;
import io.kodokojo.docker.model.DockerFileBuildRequest;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(DataProviderRunner.class)
public class DockerClientDockerImageBuilderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClientDockerImageBuilderTest.class);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private DockerClient dockerClient;

    private DockerFileProjectFetcher<GitDockerFileScmEntry> dockerFileProjectFetcher;

    private ServiceLocator serviceLocator;

    @DataProvider
    public static Object[][] dataProviderAdd() {
        // @formatter:off
        return new Object[][] {
                { "jpthiery/busybox", "centos:latest", "registry.docker.kodokojo.io", 5000, "registry.docker.kodokojo.io:5000/jpthiery/busybox", "centos:latest", "latest", true },
                { "jpthiery/busybox:dev", "centos", "registry.docker.kodokojo.io", 5000, "registry.docker.kodokojo.io:5000/jpthiery/busybox", "centos:latest", "dev", true },
                { "jpthiery/busybox", "centos:latest", null, 0, "jpthiery/busybox", "centos:latest", "latest", false },
                { "jpthiery/busybox", "centos", null, 0, "jpthiery/busybox", "centos:latest", "latest", false },
                { "jpthiery/busybox:1.0.0", "centos:7", null, 0, "jpthiery/busybox", "centos:7", "1.0.0", false }
        };
        // @formatter:on
    }


    @Before
    public void setup() {
        dockerClient = mock(DockerClient.class);
        dockerFileProjectFetcher = mock(DockerFileProjectFetcher.class);
        serviceLocator = mock(ServiceLocator.class);
    }

    @Test
    @UseDataProvider("dataProviderAdd")
    public void build_image(String image, String from, String registryHost, int port, String expectedImageName, String pullExpected, String tagExpected, boolean pushed) throws IOException, InterruptedException {
        ImageName imageName = StringToImageNameConverter.convert(image);
        ImageName imageNameFrom = StringToImageNameConverter.convert(from);
        DockerClientDockerImageBuilder imageBuilder = new DockerClientDockerImageBuilder(dockerClient, temporaryFolder.newFolder(), dockerFileProjectFetcher, serviceLocator);

        DockerFile dockerFile = new DockerFile(imageName, imageNameFrom);
        GitDockerFileScmEntry dockerFileScmEntry = new GitDockerFileScmEntry(imageName, "git://github.com/kodokojo/acme", "HEAD", "/dockerfile");
        DockerFileBuildRequest dockerFileBuildRequest = new DockerFileBuildRequest(dockerFile, dockerFileScmEntry);

        Set<Service> services = new HashSet<>();
        if (StringUtils.isNotBlank(registryHost)) {
            services.add(new Service("registry", registryHost, port));
        }
        when(serviceLocator.getServiceByName(any())).thenReturn(services);

        when(dockerFileProjectFetcher.checkoutDockerFileProject(dockerFileScmEntry)).thenReturn(temporaryFolder.newFolder());

        ArgumentCaptor<String> tagImageNameIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tagRegistryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pushCaptor = ArgumentCaptor.forClass(String.class);
        BuildImageCmd buildImageCmd = mock(BuildImageCmd.class);
        ResultBuildCallbackAppendOutput buildImageResultCallback = mock(ResultBuildCallbackAppendOutput.class);
        TagImageCmd tagImageCmd = mock(TagImageCmd.class);
        PushImageCmd pushImageCmd = mock(PushImageCmd.class);
        PushImageResultCallback pushImageResultCallback = mock(PushImageResultCallback.class);

        when(dockerClient.buildImageCmd(any(File.class))).thenReturn(buildImageCmd);
        when(buildImageCmd.exec(any())).thenReturn(buildImageResultCallback);
        when(buildImageResultCallback.awaitImageId()).thenReturn("123456");

        when(dockerClient.tagImageCmd(tagImageNameIdCaptor.capture(), tagRegistryCaptor.capture(),tagCaptor.capture())).thenReturn(tagImageCmd);
        when(tagImageCmd.withForce()).thenReturn(tagImageCmd);
        when(tagImageCmd.withTag(any())).thenReturn(tagImageCmd);
        if (pushed) {
            when(dockerClient.pushImageCmd(pushCaptor.capture())).thenReturn(pushImageCmd);
            when(pushImageCmd.withTag(any())).thenReturn(pushImageCmd);
            when(pushImageCmd.exec(any())).thenReturn(pushImageResultCallback);
        }

        TestDockerImageBuildCallback callback = new TestDockerImageBuildCallback();

        imageBuilder.build(dockerFileBuildRequest, callback, pushed);


        assertThat(callback.success).isTrue();
        assertThat(tagImageNameIdCaptor.getValue()).isEqualTo("123456");
        assertThat(tagRegistryCaptor.getValue()).isEqualTo(expectedImageName);
        assertThat(tagCaptor.getValue()).isEqualTo(tagExpected);
        if (pushed) {
            assertThat(pushCaptor.getValue()).isEqualTo(expectedImageName);
        }
    }

    private class TestDockerImageBuildCallback implements DockerImageBuildCallback {

        private boolean success = false;

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