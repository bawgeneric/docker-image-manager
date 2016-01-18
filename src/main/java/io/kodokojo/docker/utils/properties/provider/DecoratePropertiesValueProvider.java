package io.kodokojo.docker.utils.properties.provider;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DecoratePropertiesValueProvider extends AbstarctStringPropertyValueProvider {

    private final PropertiesValueProvider propertiesValueProvider;

    public DecoratePropertiesValueProvider(PropertiesValueProvider propertiesValueProvider) {
        if (propertiesValueProvider == null) {
            throw new IllegalArgumentException("propertiesValueProvider must be defined.");
        }
        this.propertiesValueProvider = propertiesValueProvider;
    }

    @Override
    protected String provideValue(String key) {
        if (isBlank(key)) {
            throw new IllegalArgumentException("key must be defined.");
        }
        return providePropertyValue(String.class, key);
    }
}
