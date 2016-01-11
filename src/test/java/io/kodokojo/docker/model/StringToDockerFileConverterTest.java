package io.kodokojo.docker.model;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class StringToDockerFileConverterTest {

    @Test
    public void valid_from_and_maintainer() {
        ImageName imageName = StringToImageNameConverter.convert("kodokojo/testA:dev");
        String content = getDockerfileContent(imageName);

        DockerFile dockerFile = StringToDockerFileConverter.convertToDockerFile(imageName, content);

        assertThat(dockerFile).isNotNull();
        assertThat(dockerFile.getMaintainer()).isNotNull().isEqualTo("Jean-Pascal THIERY <jpthiery@xebia.fr>");
        assertThat(dockerFile.getFrom()).isNotNull();
        assertThat(dockerFile.getFrom().getName()).isEqualTo("busybox");
    }


    private String getDockerfileContent(ImageName imageName) {
        assert imageName != null : "ImageName must be defined";
        try {
            String pathname = String.format("src/test/resources/DockerfileRef/library/%s/%s/Dockerfile", imageName.getNamespace(), imageName.getName());
            return FileUtils.readFileToString(new File(pathname));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return null;
    }

}