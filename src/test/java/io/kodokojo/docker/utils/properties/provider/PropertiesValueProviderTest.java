package io.kodokojo.docker.utils.properties.provider;

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

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesValueProviderTest {

    private static final String VALID_KEY = "valid";

    private static final String A_VALUE_VALUE = "aValue";

    private PropertiesValueProvider propertiesValueProvider;

    @Before
    public void setup() {
        Properties properties = new Properties();
        propertiesValueProvider = new PropertiesValueProvider(properties);

        properties.setProperty(VALID_KEY, A_VALUE_VALUE);
    }

    @Test
    public void valid_entry() {

        String result = propertiesValueProvider.providePropertyValue(String.class, "valid");

        assertThat(result).isEqualTo(A_VALUE_VALUE);

    }

}