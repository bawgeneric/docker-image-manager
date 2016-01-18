package io.kodokojo.docker.bdd.stage.docker.imagename;

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

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import io.kodokojo.docker.model.ImageName;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.assertj.core.api.Assertions.assertThat;

public class ImageNameThen extends Stage<ImageNameThen> {

    @ExpectedScenarioState
    ImageName imageName;

    ImageNameThen repository_is_not_defined() {
        assertThat(imageName.getRepository()).isNull();
        return self();
    }

    ImageNameThen namespace_is_defined_to_library() {
        assertThat(imageName.getNamespace()).isEqualTo("library");
        return self();
    }

    ImageNameThen tag_is_defined_to_latest() {
        assertThat(imageName.getTag()).isEqualTo("latest");
        return self();
    }

    ImageNameThen repository_is_defined_to_$(@Quoted String repository) {
        if (isNotBlank(repository)) {
            assertThat(imageName.getRepository()).isEqualTo(repository);
        }
        return self();
    }

    ImageNameThen namespace_is_defined_to_$(@Quoted String namespace) {
        if (isNotBlank(namespace)) {
            assertThat(imageName.getNamespace()).isEqualTo(namespace);
        }
        return self();
    }

    ImageNameThen name_is_defined_to_$(@Quoted  String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        assertThat(imageName.getName()).isEqualTo(name);
        return self();
    }


    ImageNameThen tag_is_defined_to_$(@Quoted  String tag) {
        if (isNotBlank(tag)) {
            assertThat(imageName.getTag()).isEqualTo(tag);
        }
        return self();
    }

}

