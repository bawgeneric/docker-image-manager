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

import org.junit.Test;

import java.util.LinkedList;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderedMergedValueProviderTest {

    private static final String KEY = "key";

    private static final String KEY_2 = "key2";

    @Test
    public void read_property_in_right_order() {
        LinkedList<PropertyValueProvider> valueProviders = new LinkedList<>();

        Properties properties = new Properties();
        properties.setProperty(
                KEY, "value1");
        PropertiesValueProvider valueProvider = new PropertiesValueProvider(properties);
        valueProviders.add(valueProvider);

        properties = new Properties();
        properties.setProperty(
                KEY, "value20");
        properties.setProperty(KEY_2, "value21");
        valueProvider = new PropertiesValueProvider(properties);
        valueProviders.add(valueProvider);

        valueProviders.add(new SystemPropertyValueProvider());

        OrderedMergedValueProvider mergedValueProvider = new OrderedMergedValueProvider(valueProviders);

        String value = mergedValueProvider.providePropertyValue(String.class, KEY);
        assertThat(value).isEqualTo("value1");

        value = mergedValueProvider.providePropertyValue(String.class, KEY_2);
        assertThat(value).isEqualTo("value21");

    }

}