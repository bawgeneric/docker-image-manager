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

import io.kodokojo.commons.model.DockerFile;

import java.util.HashSet;
import java.util.Set;

public class DockerFileNode {

    private final DockerFile parent;

    private final Set<DockerFile> children;

    public DockerFileNode(DockerFile parent, Set<DockerFile> children) {
        if (parent == null) {
            throw new IllegalArgumentException("parent must be defined.");
        }
        if (children == null) {
            this.children = new HashSet<>();
        } else {
            this.children = children;
        }
        this.parent = parent;
    }

    public DockerFile getParent() {
        return parent;
    }

    public Set<DockerFile> getChildren() {
        return new HashSet<>(children);
    }

    @Override
    public String toString() {
        return "DockerFileNode{" +
                "parent=" + parent +
                ", children=" + children +
                '}';
    }
}
