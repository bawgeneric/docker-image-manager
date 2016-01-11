package io.kodokojo.docker.model;

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
        assertThat(res.getTag()).isNullOrEmpty();

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
        assertThat(res.getTag()).isNullOrEmpty();
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

}