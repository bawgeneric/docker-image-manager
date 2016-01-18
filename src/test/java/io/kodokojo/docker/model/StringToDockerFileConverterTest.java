package io.kodokojo.docker.model;

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

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class StringToDockerFileConverterTest {


    //TODO Test dockerfile From and maintainer with space
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

    @Test
    public void with_multiple_whitespace() {
        ImageName imageName = StringToImageNameConverter.convert("kodokojo/testB:dev");
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