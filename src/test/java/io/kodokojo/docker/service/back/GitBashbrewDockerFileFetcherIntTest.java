package io.kodokojo.docker.service.back;

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.StringToImageNameConverter;
import io.kodokojo.docker.service.DefaultDockerFileRepository;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class GitBashbrewDockerFileFetcherIntTest {

    private String gitUrl = "git@github.com:kodokojo/acme.git";

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void fetch_all_dockerfile() throws IOException {
        String libraryPath = "bashbrew/library";

        File workspace = tmpFolder.newFolder();
        DefaultDockerFileRepository dockerFileRepository = new DefaultDockerFileRepository();
        DockerFileFetcher dockerFileFetcher = new GitBashbrewDockerFileFetcher(workspace.getAbsolutePath(), "git", gitUrl, libraryPath, dockerFileRepository);

        dockerFileFetcher.fetchAllDockerFile();

        DockerFile dockerFile = dockerFileRepository.getDockerFileFromImageName(StringToImageNameConverter.convert("kodokojo/busybox:latest"));
        assertThat(dockerFile).isNotNull();
        assertThat(dockerFile.getFrom().getFullyQualifiedName()).isEqualTo("library/centos");
        tmpFolder.delete();

    }

}