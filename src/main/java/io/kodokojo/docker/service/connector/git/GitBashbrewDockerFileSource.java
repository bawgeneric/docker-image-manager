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
import io.kodokojo.docker.model.ImageNameBuilder;
import io.kodokojo.docker.model.StringToDockerFileConverter;
import io.kodokojo.docker.service.DockerFileRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;

public class GitBashbrewDockerFileSource implements DockerFileSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitBashbrewDockerFileSource.class);

    private static final Pattern LIBRARY_CONTENT_PATTERN = Pattern.compile("^([^ #]*): ([^ @]*)@([^ ]*) ([^ ]*)$");

    private static final String DOCKERFILE_GIT_DIRECTORY = "dockerfile/";

    private static final String BASHBREW_GIT_DIRECTORY = "bashbrew/";

    private static final String GIT_DIRECTORY = ".git";

    private static final long DEFAULT_BASHBREW_PULL_DELAY = TimeUnit.MILLISECONDS.convert(1,TimeUnit.MINUTES);

    private static final long DEFAULT_DOCKERFILE_GIT_PULL_DELAY = TimeUnit.MILLISECONDS.convert(5,TimeUnit.MINUTES);

    private final File libraryDirectory;

    private final Git git;

    private final DockerFileRepository dockerFileRepository;

    private final String defaultUser;

    private final File dockerfileGitDir;

    private final long delayPeriodBetweenPullBaswbrewGit;

    private final long delayPeriodBetweenPullDockerFileGit;

    private final Map<ImageName, Long> lastDockerFileGitPullDates;

    private long lastBashbrewPullDate = 0;

    public GitBashbrewDockerFileSource(String localWorkspace, String defaultUser, String bashbrewGitUrl, String bashbrewGitLibraryPath, DockerFileRepository dockerFileRepository, long delayPeriodBetweenPullBaswbrewGit, long delayPeriodBetweenPullDockerFileGit) {
        if (isBlank(localWorkspace)) {
            throw new IllegalArgumentException("localWorkspace must be defined.");
        }
        if (dockerFileRepository == null) {
            throw new IllegalArgumentException("dockerFileRepository must be defined.");
        }
        this.dockerFileRepository = dockerFileRepository;
        this.defaultUser = defaultUser;
        this.delayPeriodBetweenPullBaswbrewGit = delayPeriodBetweenPullBaswbrewGit;
        this.delayPeriodBetweenPullDockerFileGit = delayPeriodBetweenPullDockerFileGit;
        this.lastDockerFileGitPullDates = new HashMap<>();

        File workspace = new File(localWorkspace);
        if (!workspace.exists()) {
            workspace.mkdirs();
        }
        if (workspace.isDirectory() && workspace.canRead() && workspace.canWrite()) {
        } else {
            throw new IllegalArgumentException("Unable to read or write in directory " + localWorkspace);
        }

        if (isBlank(bashbrewGitUrl)) {
            throw new IllegalArgumentException("bashbrewGitUrl must be defined.");
        }

        dockerfileGitDir = new File(workspace.getAbsolutePath() + File.separator + DOCKERFILE_GIT_DIRECTORY);
        dockerfileGitDir.mkdirs();

        File bashbrewGitDir = new File(workspace.getAbsolutePath() + File.separator + BASHBREW_GIT_DIRECTORY);
        String gitPath = bashbrewGitDir.getAbsolutePath() + File.separator + GIT_DIRECTORY;
        File bashbrewGitWorkerDir = new File(gitPath);

        if (bashbrewGitWorkerDir.exists()) {
            try {
                Repository repository = new FileRepository(bashbrewGitWorkerDir);
                git = Git.wrap(repository);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Plug repository {}.", bashbrewGitDir);
                }
            } catch (IOException e) {
                throw new IllegalStateException("An unexpected error occur while trying to plug Git repository " + bashbrewGitUrl, e);
            }

        } else {
            bashbrewGitDir.mkdirs();

            try {
                git = Git.cloneRepository().setURI(bashbrewGitUrl).setDirectory(bashbrewGitDir).call();
                LOGGER.info("Clone repository {}.", bashbrewGitUrl);
            } catch (GitAPIException e) {
                throw new IllegalStateException("An unexpected error occur while trying to clone Git repository " + bashbrewGitUrl, e);
            }
        }
        libraryDirectory = new File(bashbrewGitDir + File.separator + bashbrewGitLibraryPath);
    }

    public GitBashbrewDockerFileSource(String localWorkspace, String defaultUser, String bashbrewGitUrl, String bashbrewGitLibraryPath, DockerFileRepository dockerFileRepository) {
        this(localWorkspace, defaultUser, bashbrewGitUrl, bashbrewGitLibraryPath, dockerFileRepository, DEFAULT_BASHBREW_PULL_DELAY, DEFAULT_DOCKERFILE_GIT_PULL_DELAY);
    }

    @Override
    public void fetchAllDockerFile() {

        if (!libraryDirectory.exists()) {
            throw new IllegalStateException("Library directory " + libraryDirectory.getAbsolutePath() + " seems to not exist anymore.");
        }

        pullBashbrewRepository();

        List<DockerFileEntry> dockerFileEntries = new ArrayList<>();

        FileUtils.listFilesAndDirs(libraryDirectory, DirectoryFileFilter.DIRECTORY, TrueFileFilter.INSTANCE).forEach(namespace -> {
            FileUtils.listFiles(namespace, TrueFileFilter.TRUE, null).forEach(name -> {
                ImageNameBuilder imageNameBuilder = new ImageNameBuilder();
                imageNameBuilder.setNamespace(namespace.getName()).setName(name.getName());
                try {
                    String dockerFileContent = FileUtils.readFileToString(name);
                    dockerFileEntries.addAll(convertBashbrewFileToDockerfileEntries(dockerFileContent, null, imageNameBuilder));
                } catch (IOException e) {
                    LOGGER.error("Unable to read content of file " + name.getAbsolutePath(), e);
                }
            });
        });

        addDockerFilesToDockerFileRepository(dockerFileEntries);
        LOGGER.info("Fetch {} Dockerfile.", dockerFileEntries.size());

    }

    private void addDockerFilesToDockerFileRepository(List<DockerFileEntry> dockerFileEntries) {
        for (DockerFileEntry entry : dockerFileEntries) {
            DockerFile dockerFile = fetchDockerFileFromGitRepository(entry);
            if (dockerFile != null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Create dockerfile {}", dockerFile);
                }
                dockerFileRepository.addDockerFile(dockerFile);
            }
        }
    }


    @Override
    public boolean fetchDockerFile(ImageName imageName) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }

        pullBashbrewRepository();

        StringBuilder sb = new StringBuilder();
        sb.append(libraryDirectory.getAbsolutePath()).append(File.separator);
        if (!imageName.isRootImage()) {
            sb.append(imageName.getNamespace()).append(File.separator);
        }
        sb.append(imageName.getName());
        String libraryFilePath = sb.toString();
        File libraryFile = new File(libraryFilePath);
        try {
            String libraryFileContent = FileUtils.readFileToString(libraryFile);
            ImageNameBuilder imageNameBuilder = new ImageNameBuilder();
            imageNameBuilder.setNamespace(imageName.getNamespace());
            imageNameBuilder.setName(imageName.getName());
            List<DockerFileEntry> dockerFileEntries = convertBashbrewFileToDockerfileEntries(libraryFileContent, StringUtils.isBlank(imageName.getTag()) ? null : imageName.getTag(), imageNameBuilder);
            addDockerFilesToDockerFileRepository(dockerFileEntries);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("DockerFile {} successfully fetched : {}", imageName.getFullyQualifiedName() ,dockerFileEntries.toString());
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Unable to read content of file " + libraryFile.getAbsolutePath(), e);
            return false;
        }
    }

    private List<DockerFileEntry> convertBashbrewFileToDockerfileEntries(String libraryFileContent, String tag, ImageNameBuilder imageNameBuilder) {
        assert StringUtils.isNotBlank(libraryFileContent) : "libraryFileContent must be define.";
        List<DockerFileEntry> res = new ArrayList<>();

        String[] lines = libraryFileContent.split("\n");
        for (String line : lines) {
            Matcher matcher = LIBRARY_CONTENT_PATTERN.matcher(line);
            if (matcher.find() && matcher.groupCount() == 4) {

                String currentTag = matcher.group(1);
                if (StringUtils.isBlank(tag) || tag.equals(currentTag)) {
                    ImageName imageName = imageNameBuilder.setTag(currentTag).build();
                    String gitUrl = matcher.group(2);
                    //TODO Check url schemas to add this user only if it ssh connection only or remove this pattern
                    if (StringUtils.isNotBlank(defaultUser) && !gitUrl.contains("@")) {
                        gitUrl = defaultUser + "@" + gitUrl;
                    }
                    DockerFileEntry entry = new DockerFileEntry(imageName, gitUrl, matcher.group(3), matcher.group(4));
                    res.add(entry);
                }
            }
        }

        return res;
    }

    private DockerFile fetchDockerFileFromGitRepository(DockerFileEntry entry) {
        assert entry != null : "entry must be defined";

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
            } catch (IOException e) {
                LOGGER.error("Not able to plug already existing repot " + gitDir.getAbsolutePath(), e);
                return null;
            }
        }
        DockerFile dockerFile = null;

        String fileContent = getFileContent(repository, entry);
        if (StringUtils.isNotBlank(fileContent)) {
            dockerFile = StringToDockerFileConverter.convertToDockerFile(entry.getImageName(), fileContent);
        }
        repository.close();
        return dockerFile;

    }

    //Source : https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/api/ReadFileFromCommit.java
    private String getFileContent(Repository repository, DockerFileEntry entry) {
        pullDockerFileRepository(repository, entry);
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

    private void pullDockerFileRepository(Repository repository, DockerFileEntry entry) {
        assert repository != null : "repository must be defined";
        assert entry != null : "entry must be defined";

        long now = System.currentTimeMillis();

        ImageName imageName = entry.getImageName();
        Long tmpLastGitPull = lastDockerFileGitPullDates.get(imageName);
        long lastGitPull = tmpLastGitPull != null ? tmpLastGitPull : 0;
        long timeSinceLastPull = Math.abs(now - lastGitPull);
        if (timeSinceLastPull > delayPeriodBetweenPullDockerFileGit) {
            try {
                Git.wrap(repository).pull().call();
                lastDockerFileGitPullDates.put(imageName, now);
            } catch (GitAPIException e) {
                LOGGER.error("Unable to pull from " + git.getRepository().toString(), e);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Last pull on Git repository for image {} done to soon, abort the Git pull.", imageName);

        }
    }


    private void pullBashbrewRepository() {
        long now = System.currentTimeMillis();
        long timeSinceLastPull = Math.abs(now - lastBashbrewPullDate);
        if (timeSinceLastPull > delayPeriodBetweenPullBaswbrewGit) {
            try {
                PullResult call = git.pull().call();
                lastBashbrewPullDate = now;
                if (LOGGER.isDebugEnabled()) {
                    if (call.isSuccessful()) {
                        LOGGER.debug("Successfully pull git repot {}", call.getFetchedFrom());
                    } else {
                        LOGGER.debug("Fail to pull git repot {}", call.getFetchedFrom());
                    }
                }
            } catch (GitAPIException e) {
                LOGGER.error("Unable to pull from " + git.getRepository().toString(), e);
            }
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Last pull on Bashbrew Git done to soon, abort the Git pull.");
        }
    }


}
