package io.kodokojo.docker.model;

public class Layer {

    private final String digest;

    private final int size;

    public Layer(String digest,int size) {
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
}
