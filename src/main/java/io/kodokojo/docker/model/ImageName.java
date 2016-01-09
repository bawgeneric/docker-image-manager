package io.kodokojo.docker.model;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Jean-Pascal THIERY on 28/10/15.
 */
public class ImageName {

    private static final String DEFAULT_NAMESPACE = "library";

    private final String namespace;

    private final String name;

    private final String tag;

    private final List<String> tags;

    public ImageName(String namespace, String name, String tag) {
        if (StringUtils.isBlank(namespace)) {
            this.namespace = DEFAULT_NAMESPACE;
        } else {
            this.namespace = namespace;
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name  must be defined.");
        }
        this.name = name;
        this.tag = tag;
        this.tags = Collections.singletonList(tag);
    }
    public ImageName(String namespace, String name, List<String> tags) {
        if (StringUtils.isBlank(namespace)) {
            this.namespace = DEFAULT_NAMESPACE;
        } else {
            this.namespace = namespace;
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name  must be defined.");
        }
        if (tags  == null) {
            throw new IllegalArgumentException("tags must be defined.");
        }
        this.name = name;
        this.tag = null;
        this.tags = tags;
    }

    public ImageName(String namespace, String name) {
        this(namespace, name, Collections.EMPTY_LIST);
    }

    public ImageName(String name) {
        this(DEFAULT_NAMESPACE, name);
    }

    public boolean isFullyQualified() {
        return StringUtils.isNotBlank(tag);
    }

    public String getFullyQualifiedName() {
        return StringUtils.isBlank(tag) ? String.format("%s/%s", namespace, name) : String.format("%s/%s:%s", namespace, name, tag);
    }

    public String getShortNameWithoutTag() {
        StringBuilder sb = new StringBuilder();
        if (!DEFAULT_NAMESPACE.equals(namespace)) {
            sb.append(namespace).append("/");
        }
        sb.append(name);
        return sb.toString();
    }

    public String getShortName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getShortNameWithoutTag());
        if (isFullyQualified()) {
            sb.append(":").append(tag);
        }
        return sb.toString();
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "ImageName{" +
                "namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", tags=" + tags +
                '}';
    }
}

