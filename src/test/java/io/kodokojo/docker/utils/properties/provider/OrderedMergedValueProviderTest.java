package io.kodokojo.docker.utils.properties.provider;

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