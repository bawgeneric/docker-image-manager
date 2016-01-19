package io.kodokojo.docker.service.connector.git;

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

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.StringToDockerFileConverter;
import io.kodokojo.docker.service.connector.DockerFileProjectFetcher;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isBlank;

public class GitDockerFileProjectFetcher implements DockerFileProjectFetcher<GitDockerFileScmEntry> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitDockerFileProjectFetcher.class);

    private static final String DOCKERFILE_GIT_DIRECTORY = "dockerfile/";

    private static final String GIT_DIRECTORY = ".git";

    private static final long DEFAULT_DOCKERFILE_GIT_PULL_DELAY = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);

    private final long delayPeriodBetweenPullDockerFileGit;

    private final Map<ImageName, Long> lastDockerFileGitPullDates;

    private final File dockerfileGitDir;

    public GitDockerFileProjectFetcher(String workspace, long delayPeriodBetweenPullDockerFileGit) {
        if (isBlank(workspace)) {
            throw new IllegalArgumentException("dockerfileGitDirPath must be defined.");
        }
        this.dockerfileGitDir = new File(workspace + File.separator + DOCKERFILE_GIT_DIRECTORY);
        if (!dockerfileGitDir.exists()) {
            dockerfileGitDir.mkdirs();
        }
        this.delayPeriodBetweenPullDockerFileGit = delayPeriodBetweenPullDockerFileGit;
        this.lastDockerFileGitPullDates = new HashMap<>();
    }

    public GitDockerFileProjectFetcher(String dockerfileGitDirPath) {
        this(dockerfileGitDirPath, DEFAULT_DOCKERFILE_GIT_PULL_DELAY);
    }

    @Override
    public DockerFile fetchDockerFileScmEntry(GitDockerFileScmEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must be defined.");
        }

        ImageName imageName = entry.getImageName();
        String pathname = dockerfileGitDir.getAbsolutePath() + File.separator + imageName.getNamespace() + File.separator + imageName.getName();
        File currentDir = new File(pathname);

        checkoutDockerFileProject(entry);

        String gitPath = currentDir.getAbsolutePath() + File.separator + GIT_DIRECTORY;
        DockerFile dockerFile = null;
        try {
            Repository repository  = new FileRepository(gitPath);

            String fileContent = getFileContent(repository, entry);
            if (StringUtils.isNotBlank(fileContent)) {
                dockerFile = StringToDockerFileConverter.convertToDockerFile(entry.getImageName(), fileContent);
            }
            repository.close();
        } catch (IOException e) {
            LOGGER.error("Not able to plug already existing repot " + gitPath, e);
            return null;
        }
        return dockerFile;
    }

    @Override
    public File checkoutDockerFileProject(GitDockerFileScmEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must be defined.");
        }
        ImageName imageName = entry.getImageName();
        String pathname = dockerfileGitDir.getAbsolutePath() + File.separator + imageName.getNamespace() + File.separator + imageName.getName();
        File currentDir = new File(pathname);
        Repository repository = null;

        if (!currentDir.exists()) {
            currentDir.mkdirs();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Clone {} in directory {}", entry.getGitUrl(), currentDir.getAbsolutePath());
            }
            try {
                Git git = Git.cloneRepository().setURI(entry.getGitUrl()).setDirectory(currentDir).call();
                repository = git.getRepository();
            } catch (GitAPIException e) {
                LOGGER.error("Unable to clone repository " + entry.getGitUrl(), e);
                return null;
            }
        } else {
            String gitPath = currentDir.getAbsolutePath() + File.separator + GIT_DIRECTORY;
            File gitDir = new File(gitPath);
            try {
                repository = new FileRepository(gitDir);
                pullDockerFileRepository(repository, entry.getImageName());
            } catch (IOException e) {
                LOGGER.error("Not able to plug already existing repot " + gitDir.getAbsolutePath(), e);
                return null;
            }
        }
        repository.close();
        return currentDir;
    }

    private void pullDockerFileRepository(Repository repository, ImageName imageName) {
        assert repository != null : "repository must be defined";
        assert imageName != null : "imageName must be defined";

        long now = System.currentTimeMillis();

        Long tmpLastGitPull = lastDockerFileGitPullDates.get(imageName);
        long lastGitPull = tmpLastGitPull != null ? tmpLastGitPull : 0;
        long timeSinceLastPull = Math.abs(now - lastGitPull);
        if (timeSinceLastPull > delayPeriodBetweenPullDockerFileGit) {
            try {
                Git.wrap(repository).pull().call();
                lastDockerFileGitPullDates.put(imageName, now);
            } catch (GitAPIException e) {
                LOGGER.error("Unable to pull from " + repository.toString(), e);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Last pull on Git repository for image {} done to soon, abort the Git pull.", imageName);

        }
    }

    //Source : https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/api/ReadFileFromCommit.java
    private String getFileContent(Repository repository, GitDockerFileScmEntry entry) {

        String res = null;
        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId lastCommitId = repository.resolve(entry.getGitRef());
            RevCommit commit = revWalk.parseCommit(lastCommitId);
            RevTree tree = commit.getTree();

            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                String path = entry.getDockerFilePath() + File.separator + "Dockerfile";
                path = path.startsWith("./") ? path.substring(2) : path;

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Looking for file {}", path);
                }

                treeWalk.setFilter(AndTreeFilter.create(PathFilter.create(path), TreeFilter.ANY_DIFF));
                if (!treeWalk.next()) {
                    LOGGER.error("Unable to find Dockerfile for image {}", entry.getImageName().getFullyQualifiedName());
                    return null;
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                res = new String(loader.getBytes());

            }

            revWalk.dispose();
        } catch (IOException e) {
            LOGGER.error("An error occur while trying to retrieve Dockerfile content for image " + entry.getImageName().getFullyQualifiedName(), e);
            return null;
        }

        return res;
    }

}
