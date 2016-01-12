package io.kodokojo.docker.model;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerFile {

    private final ImageName imageName;

    private final ImageName from;

    private final String maintainer;

    public DockerFile(ImageName imageName, ImageName from, String maintainer) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        this.imageName = imageName;
        this.from = from;
        this.maintainer = maintainer;
    }

    public DockerFile(ImageName imageName) {
        this(imageName,null,null);
    }

    public DockerFile(ImageName imageName, ImageName from) {
        this(imageName, from, null);
    }

    public DockerFile(String imageName) {
        this(new ImageName(imageName));
    }

    public ImageName getImageName() {
        return imageName;
    }

    public ImageName getFrom() {
        return from;
    }

    public String getMaintainer() {
        return maintainer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerFile that = (DockerFile) o;

        return imageName.equals(that.imageName);

    }

    @Override
    public int hashCode() {
        return imageName.hashCode();
    }

    @Override
    public String toString() {
        return "DockerFile{" +
                "imageName=" + imageName +
                ", from=" + from +
                ", maintainer='" + maintainer + '\'' +
                '}';
    }

}

