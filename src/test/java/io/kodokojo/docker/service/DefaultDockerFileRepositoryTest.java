package io.kodokojo.docker.service;

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
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.StringToImageNameConverter;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultDockerFileRepositoryTest {

    @Test
    public void add_root_dockerfile() {
        DockerFileRepository repository  = new DefaultDockerFileRepository();
        String imageName = "busybox";
        repository.addDockerFile(new DockerFile(imageName));
        DockerFile result = repository.getDockerFileFromImageName(imageName);
        assertThat(result).isNotNull();
        assertThat(result.getImageName()).isNotNull();
        assertThat(result.getImageName().getShortName()).isEqualTo(imageName);
    }

    @Test
    public void get_child() {
        DockerFileRepository repository  = new DefaultDockerFileRepository();
        String busybox = "busybox";
        ImageName imageName = StringToImageNameConverter.convert(busybox);
        DockerFile parent = new DockerFile(imageName);
        repository.addDockerFile(parent);


        ImageName childImageName = StringToImageNameConverter.convert("jpthiery/busy");
        DockerFile childDockerFile = new DockerFile(childImageName,imageName, null);
        repository.addDockerFile(childDockerFile);

        DockerFile result = repository.getDockerFileFromImageName(childImageName);
                assertThat(result).isNotNull();
        assertThat(result.getImageName()).isNotNull();
        assertThat(result.getImageName().getShortName()).isEqualTo(childImageName.getShortName());
        assertThat(result.getFrom().getFullyQualifiedName()).isEqualTo(imageName.getFullyQualifiedName());

    }

    @Test
    public void add_parent_after_child() {
        DockerFileRepository repository  = new DefaultDockerFileRepository();
        String parent = "busybox";
        String child = "child";

        ImageName parentImageName = StringToImageNameConverter.convert(parent);
        DockerFile parentDockerFile = new DockerFile(parentImageName);
        DockerFile childDockerFile = new DockerFile(StringToImageNameConverter.convert(child), parentImageName);

        repository.addDockerFile(childDockerFile);
        repository.addDockerFile(parentDockerFile);

        Set<DockerFile> dockerFileChildOf = repository.getDockerFileChildOf(parentDockerFile);
        assertThat(dockerFileChildOf).isNotEmpty()
                .extracting("imageName.name")
                .contains("child");
    }


}