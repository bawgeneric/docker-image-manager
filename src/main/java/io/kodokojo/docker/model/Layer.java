package io.kodokojo.docker.model;

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
