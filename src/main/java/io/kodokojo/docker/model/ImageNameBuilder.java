package io.kodokojo.docker.model;

import org.apache.commons.lang.StringUtils;

public class ImageNameBuilder {

    private String repository;

    private String namespace;

    private String name;

    private String tag;

    public ImageNameBuilder() {
        super();
    }

    public ImageName build() {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name  must be defined.");
        }
        return new ImageName(repository, namespace, name, tag);
    }

    public ImageNameBuilder setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public ImageNameBuilder setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public ImageNameBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ImageNameBuilder setTag(String tag) {
        this.tag = tag;
        return this;
    }
}
