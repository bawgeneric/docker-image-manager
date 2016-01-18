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

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

@As("String to ImageName converter")
public class StringToImageNameConverterIntTest extends ScenarioTest<ImageNameGiven, ImageNameWhen, ImageNameThen> {

    @Test
    public void simple_image_name() {
        String imageNmae = "busybox";
        given().having_a_image_name_$(imageNmae);
        when().convert_it();
        then().repository_is_not_defined()
                .and().namespace_is_defined_to_library()
                .and().name_is_defined_to_$(imageNmae)
                .and().tag_is_defined_to_latest();
    }

    @Test
    public void image_name_with_tag() {
        String imageNmae = "busybox:1.0.0-alpha";
        given().having_a_image_name_$(imageNmae);
        when().convert_it();
        then().repository_is_not_defined()
                .and().namespace_is_defined_to_library()
                .and().name_is_defined_to_$("busybox")
                .and().tag_is_defined_to_$("1.0.0-alpha");
    }


    @Test
    public void image_name_with_namespace_and_tag() {
        String imageNmae = "jpthiery/busybox:1.0.0";
        given().having_a_image_name_$(imageNmae);
        when().convert_it();
        then().repository_is_not_defined()
                .and().namespace_is_defined_to_$("jpthiery")
                .and().name_is_defined_to_$("busybox")
                .and().tag_is_defined_to_$("1.0.0");
    }

    @Test
    public void image_name_with_repository_namespace_and_tag() {
        String imageNmae = "docker.io:5000/jpthiery/busybox:1.0.0";
        given().having_a_image_name_$(imageNmae);
        when().convert_it();
        then().repository_is_defined_to_$("docker.io:5000")
                .and().namespace_is_defined_to_$("jpthiery")
                .and().name_is_defined_to_$("busybox")
                .and().tag_is_defined_to_$("1.0.0");
    }
    @Test
    public void image_name_with_upper_case_name() {
        String imageNmae = "DOCKER.IO:5000/JPTHIERY/BUSYBOX:1.0.0";
        given().having_a_image_name_$(imageNmae);
        when().convert_it();
        then().repository_is_defined_to_$("docker.io:5000")
                .and().namespace_is_defined_to_$("jpthiery")
                .and().name_is_defined_to_$("busybox")
                .and().tag_is_defined_to_$("1.0.0");
    }

}
