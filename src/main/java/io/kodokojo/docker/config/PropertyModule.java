package io.kodokojo.docker.config;

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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kodokojo.commons.config.DockerConfig;
import io.kodokojo.commons.config.KodokojoConfig;
import io.kodokojo.commons.utils.properties.PropertyResolver;
import io.kodokojo.commons.utils.properties.provider.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

public class PropertyModule extends AbstractModule{

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyModule.class);

    public static final String APPLICATION_CONFIGURATION_PROPERTIES = "applicationConfiguration.properties";

    private String[] args;

    public PropertyModule(String[] args) {
        this.args = args;
    }

    @Override
    protected void configure() {
        //
    }

    @Provides
    @Singleton
    PropertyValueProvider providePropertyValueProvider() {
        LinkedList<PropertyValueProvider> valueProviders = new LinkedList<>();
        OrderedMergedValueProvider valueProvider = new OrderedMergedValueProvider(valueProviders);

        AbstarctStringPropertyValueProvider workSpaceValueProvider = new AbstarctStringPropertyValueProvider() {
            @Override
            protected String provideValue(String key) {
                File baseDire = new File("");
                String workspace = baseDire.getAbsolutePath() + File.separator + "workspace";
                if ("workspace".equals(key)) {
                    return workspace;
                } else if ("dockerFileProject".equals(key)) {
                    return workspace + File.separator + "dockerfileProjects";
                } else if ("dockerFile.buildDir".equals(key)) {
                    return workspace + File.separator + "dockerImage" + File.separator + "build";
                }
                return null;
            }
        };

        valueProviders.add(workSpaceValueProvider);

        if (args != null && args.length > 0) {
            JavaArgumentPropertyValueProvider javaArgumentPropertyValueProvider = new JavaArgumentPropertyValueProvider(args);
            valueProviders.add(javaArgumentPropertyValueProvider);
        }


        SystemEnvValueProvider systemEnvValueProvider = new SystemEnvValueProvider();
        valueProviders.add(systemEnvValueProvider);

        SystemPropertyValueProvider systemPropertyValueProvider = new SystemPropertyValueProvider();
        valueProviders.add(systemPropertyValueProvider);

        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream(APPLICATION_CONFIGURATION_PROPERTIES);
            Properties properties = new Properties();
            properties.load(in);
            PropertiesValueProvider propertiesValueProvider = new PropertiesValueProvider(properties);
            valueProviders.add(propertiesValueProvider);
        } catch (IOException e) {
            LOGGER.error("Unable to load properties file " + APPLICATION_CONFIGURATION_PROPERTIES, e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        return valueProvider;
    }

    @Provides
    @Singleton
    DockerConfig provideDockerConfig(PropertyValueProvider valueProvider) {
        PropertyResolver propertyResolver = new PropertyResolver(new DockerConfigValueProvider(valueProvider));
        return propertyResolver.createProxy(DockerConfig.class);
    }

    @Provides
    @Singleton
    GitBashbrewConfig provideGitBashbrewConfig(PropertyValueProvider valueProvider) {
        PropertyResolver propertyResolver = new PropertyResolver(valueProvider);
        return propertyResolver.createProxy(GitBashbrewConfig.class);
    }

    @Provides
    @Singleton
    ApplicationConfig provideApplicationConfig(PropertyValueProvider valueProvider) {
        PropertyResolver propertyResolver = new PropertyResolver(valueProvider);
        return propertyResolver.createProxy(ApplicationConfig.class);
    }

    @Provides
    @Singleton
    KodokojoConfig provideKodokojoConfig(PropertyValueProvider valueProvider) {
        PropertyResolver propertyResolver = new PropertyResolver(valueProvider);
        return propertyResolver.createProxy(KodokojoConfig.class);
    }

}
