package io.kodokojo.docker.model;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Layer {

    private final String digest;

    private final int size;

    public Layer(String digest,int size) {
        if (isBlank(digest)) {
            throw new IllegalArgumentException("digest must be defined.");
        }
        this.size = size;
        this.digest = digest;
    }

    public int getSize() {
        return size;
    }

    public String getDigest() {
        return digest;
    }

    @Override
    public String toString() {
        return "Layer{" +
                "digest='" + digest + '\'' +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Layer layer = (Layer) o;

        if (size != layer.size) return false;
        return digest.equals(layer.digest);

    }

    @Override
    public int hashCode() {
        int result = digest.hashCode();
        result = 31 * result + size;
        return result;
    }
}
