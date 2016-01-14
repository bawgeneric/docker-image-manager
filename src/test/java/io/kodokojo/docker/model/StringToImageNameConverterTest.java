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

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringToImageNameConverterTest {


    private StringToImageNameConverter converter;

    @Before
    public void setup() {
        this.converter = new StringToImageNameConverter();
    }

    @Test
    public void valid_fully_qualified_input() {
        String input = "grafana/grafana:2.1.3-alpha";
        ImageName res = converter.apply(input);

        assertThat(res).isNotNull();
        assertThat(res.getNamespace()).isNotEmpty();
        assertThat(res.getNamespace()).isEqualTo("grafana");
        assertThat(res.getName()).isNotEmpty();
        assertThat(res.getName()).isEqualTo("grafana");
        assertThat(res.getTag()).isNotEmpty();
        assertThat(res.getTag()).isEqualTo("2.1.3-alpha");
    }
    @Test
    public void valid_with_namespace_and_name() {
        String input = "grafana/grafana";
        ImageName res = converter.apply(input);

        assertThat(res).isNotNull();
        assertThat(res.getNamespace()).isNotEmpty();
        assertThat(res.getNamespace()).isEqualTo("grafana");
        assertThat(res.getName()).isNotEmpty();
        assertThat(res.getName()).isEqualTo("grafana");
        assertThat(res.getTag()).isEqualTo("latest");

    }

    @Test
    public void valide_with_only_name() {
        String input = "grafana";
        ImageName res = converter.apply(input);

        assertThat(res).isNotNull();
        assertThat(res.getNamespace()).isNotEmpty();
        assertThat(res.getNamespace()).isEqualTo("library");
        assertThat(res.getName()).isNotEmpty();
        assertThat(res.getName()).isEqualTo("grafana");
        assertThat(res.getTag()).isEqualTo("latest");
    }
    @Test
    public void valide_with_name_and_tag() {
        String input = "grafana:2.1.3-beta";
        ImageName res = converter.apply(input);

        assertThat(res).isNotNull();
        assertThat(res.getNamespace()).isNotEmpty();
        assertThat(res.getNamespace()).isEqualTo("library");
        assertThat(res.getName()).isNotEmpty();
        assertThat(res.getName()).isEqualTo("grafana");
        assertThat(res.getTag()).isNotEmpty();
        assertThat(res.getTag()).isEqualTo("2.1.3-beta");
    }

    @Test
    public void valide_with_repository_with_name_and_tag() {
        String input = "dockerregistry.kodokojo.io:5000/grafana:2.1.3-beta";
        ImageName res = converter.apply(input);

        assertThat(res).isNotNull();
        assertThat(res.getRepository()).isNotEmpty();
        assertThat(res.getRepository()).isEqualTo("dockerregistry.kodokojo.io:5000");
        assertThat(res.getNamespace()).isNotEmpty();
        assertThat(res.getNamespace()).isEqualTo("library");
        assertThat(res.getName()).isNotEmpty();
        assertThat(res.getName()).isEqualTo("grafana");
        assertThat(res.getTag()).isNotEmpty();
        assertThat(res.getTag()).isEqualTo("2.1.3-beta");
    }
    @Test
    public void valide_with_repository_with_namespace_with_name_and_tag() {
        String input = "dockerregistry.kodokojo.io:5000/jpthiery/grafana:2.1.3-beta";
        ImageName res = converter.apply(input);

        assertThat(res).isNotNull();
        assertThat(res.getRepository()).isNotEmpty();
        assertThat(res.getRepository()).isEqualTo("dockerregistry.kodokojo.io:5000");
        assertThat(res.getNamespace()).isNotEmpty();
        assertThat(res.getNamespace()).isEqualTo("jpthiery");
        assertThat(res.getName()).isNotEmpty();
        assertThat(res.getName()).isEqualTo("grafana");
        assertThat(res.getTag()).isNotEmpty();
        assertThat(res.getTag()).isEqualTo("2.1.3-beta");
    }



}