package io.kodokojo.docker.service.connector.git;

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

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.StringToImageNameConverter;
import io.kodokojo.docker.service.DefaultDockerFileRepository;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class GitBashbrewDockerFileSourceIntTest {

    private String gitUrl = "git://github.com/kodokojo/acme";

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void fetch_kodokojo_busybox_dockerfile() throws IOException {
        String libraryPath = "bashbrew/library";
        File workspace = tmpFolder.newFolder();
        DefaultDockerFileRepository dockerFileRepository = new DefaultDockerFileRepository();
        DockerFileSource dockerFileSource = new GitBashbrewDockerFileSource(workspace.getAbsolutePath(), null, gitUrl, libraryPath, dockerFileRepository);

        dockerFileSource.fetchDockerFile(StringToImageNameConverter.convert("kodokojo/busybox"));

        DockerFile dockerFile = dockerFileRepository.getDockerFileFromImageName(StringToImageNameConverter.convert("kodokojo/busybox:latest"));
        assertThat(dockerFile).isNotNull();
        assertThat(dockerFile.getFrom().getFullyQualifiedName()).isEqualTo("library/centos:7");
    }

    @Test
    public void fetch_kodokojo_busybox_specific_tag_dockerfile() throws IOException {
        String libraryPath = "bashbrew/library";
        File workspace = tmpFolder.newFolder();
        DefaultDockerFileRepository dockerFileRepository = new DefaultDockerFileRepository();
        DockerFileSource dockerFileSource = new GitBashbrewDockerFileSource(workspace.getAbsolutePath(), null, gitUrl, libraryPath, dockerFileRepository);

        dockerFileSource.fetchDockerFile(StringToImageNameConverter.convert("kodokojo/busybox:1.0.0"));

        DockerFile dockerFile = dockerFileRepository.getDockerFileFromImageName(StringToImageNameConverter.convert("kodokojo/busybox:1.0.0"));
        assertThat(dockerFile).isNotNull();
        assertThat(dockerFile.getFrom().getFullyQualifiedName()).isEqualTo("library/centos:latest");
    }

    @Test
    public void fetch_all_dockerfile() throws IOException {
        String libraryPath = "bashbrew/library";

        File workspace = tmpFolder.newFolder();
        DefaultDockerFileRepository dockerFileRepository = new DefaultDockerFileRepository();
        DockerFileSource dockerFileSource = new GitBashbrewDockerFileSource(workspace.getAbsolutePath(), null, gitUrl, libraryPath, dockerFileRepository);

        dockerFileSource.fetchAllDockerFile();

        DockerFile dockerFile = dockerFileRepository.getDockerFileFromImageName(StringToImageNameConverter.convert("kodokojo/busybox:latest"));
        assertThat(dockerFile).isNotNull();
        assertThat(dockerFile.getFrom().getFullyQualifiedName()).isEqualTo("library/centos:7");
    }

}