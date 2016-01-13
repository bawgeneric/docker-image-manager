package io.kodokojo.docker.service.connector.git;

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.ImageNameBuilder;
import io.kodokojo.docker.model.StringToDockerFileConverter;
import io.kodokojo.docker.service.DockerFileRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.output.ByteArrayOutputStream;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;

public class GitBashbrewDockerFileFetcher implements DockerFileFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitBashbrewDockerFileFetcher.class);

    private static final Pattern LIBRARY_CONTENT_PATTERN = Pattern.compile("^([^ #]*): ([^ @]*)@([^ ]*) ([^ ]*)$");

    private static final String DOCKERFILE_GIT_DIRECTORY = "dockerfile/";

    private static final String BASHBREW_GIT_DIRECTORY = "bashbrew/";

    private static final String GIT_DIRECTORY = ".git";

    private final File libraryDirectory;

    private final Git git;

    private final DockerFileRepository dockerFileRepository;

    private final String defaultUser;

    private final File dockerfileGitDir;

    public GitBashbrewDockerFileFetcher(String localWorkspace, String defaultUser, String bashbrewGitUrl, String bashbrewGitLibraryPath, DockerFileRepository dockerFileRepository) {
        if (isBlank(localWorkspace)) {
            throw new IllegalArgumentException("localWorkspace must be defined.");
        }
        if (dockerFileRepository == null) {
            throw new IllegalArgumentException("dockerFileRepository must be defined.");
        }
        this.dockerFileRepository = dockerFileRepository;
        this.defaultUser = defaultUser;
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

        File bashbrewGitDir = new File(workspace.getAbsolutePath() + File.separator + BASHBREW_GIT_DIRECTORY);
        bashbrewGitDir.mkdirs();

        dockerfileGitDir = new File(workspace.getAbsolutePath() + File.separator + DOCKERFILE_GIT_DIRECTORY);
        dockerfileGitDir.mkdirs();

        try {
            git = Git.cloneRepository().setURI(bashbrewGitUrl).setDirectory(bashbrewGitDir).call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("An unexpected error occur while trying to clone Git repository " + bashbrewGitUrl, e);
        }
        libraryDirectory = new File(bashbrewGitDir + File.separator + bashbrewGitLibraryPath);
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
    public void fetchDockerFile(ImageName imageName) {
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
        } catch (IOException e) {
            LOGGER.error("Unable to read content of file " + libraryFile.getAbsolutePath(), e);
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


    private void pullBashbrewRepository() {
        try {
            PullResult call = git.pull().call();
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
    }


}
