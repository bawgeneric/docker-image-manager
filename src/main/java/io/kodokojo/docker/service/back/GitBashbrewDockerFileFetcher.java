package io.kodokojo.docker.service.back;

import io.kodokojo.docker.model.*;
import io.kodokojo.docker.service.DockerFileRepository;
import org.apache.commons.io.FileUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
            LOGGER.error("Unable to pull from.", e);
        }

        if (!libraryDirectory.exists()) {
            throw new IllegalStateException("Library directory " + libraryDirectory.getAbsolutePath() + " seems to not exist anymore.");
        }

        List<DockerFileEntry> dockerfileEntries = new ArrayList<>();

        List<File> namespaces = Arrays.asList(libraryDirectory.listFiles()).stream().filter(File::isDirectory).collect(Collectors.toList());
        for (File namespace : namespaces) {
            List<File> names = Arrays.asList(namespace.listFiles()).stream().filter(File::canRead).collect(Collectors.toList());
            for (File name : names) {
                ImageNameBuilder imageNameBuilder = new ImageNameBuilder();
                imageNameBuilder.setNamespace(namespace.getName()).setName(name.getName());
                dockerfileEntries.addAll(convertBashbrewFileToDockerfileEntries(name, imageNameBuilder));
            }
        }

        for (DockerFileEntry entry : dockerfileEntries) {
            DockerFile dockerFile = fetchDockerFileFromGitRepository(entry);
            if (dockerFile != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Create dockerfile {}", dockerFile);
                }
                dockerFileRepository.addDockerFile(dockerFile);
            }
        }

    }

    @Override
    public void fetchDockerFile(ImageName imageName) {
        //  TODO Finish that method!
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private List<DockerFileEntry> convertBashbrewFileToDockerfileEntries(File libraryFile, ImageNameBuilder imageNameBuilder) {
        assert libraryFile != null && libraryFile.canRead() : "libraryFile must be define a readable";
        List<DockerFileEntry> res = new ArrayList<>();

        try {
            String content = FileUtils.readFileToString(libraryFile);
            String[] lines = content.split("\n");
            for (String line : lines) {
                Matcher matcher = LIBRARY_CONTENT_PATTERN.matcher(line);
                if (matcher.find() && matcher.groupCount() == 4) {
                    ImageName imageName = imageNameBuilder.setTag(matcher.group(1)).build();
                    String gitUrl = matcher.group(2);
                    if (StringUtils.isNotBlank(defaultUser) && !gitUrl.contains("@")) {
                        gitUrl = defaultUser + "@" + gitUrl;
                    }
                    DockerFileEntry entry = new DockerFileEntry(imageName, gitUrl, matcher.group(3), matcher.group(4));
                    res.add(entry);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Unable to read file " + libraryFile.getAbsolutePath(), e);
            return res;
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

                LOGGER.debug("Looking for file {}", path);

                treeWalk.setFilter(AndTreeFilter.create(PathFilter.create(path), TreeFilter.ANY_DIFF));// PathFilter.create(entry.getDockerFilePath() + File.separator +"Dockerfile"));
                if (!treeWalk.next()) {
                    //throw new IllegalStateException("Did not find expected file 'Dockerfile'");
                    LOGGER.error("Unable to find Dockerfile for image {}", entry.getImageName().getFullyQualifiedName());
                    return null;
                }

                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                BufferedOutputStream out = new BufferedOutputStream(byteArrayOutputStream);
                loader.copyTo(out);
                out.flush();
                res = byteArrayOutputStream.toString();
                out.close();

            }

            revWalk.dispose();
        } catch (IOException e) {
            LOGGER.error("An error occru while trying to retrieve Dockerfile content for image " + entry.getImageName().getFullyQualifiedName(), e);
            return null;
        }

        return res;
    }

    private class DockerFileEntry {

        private final ImageName imageName;

        private final String gitUrl;

        private final String gitRef;

        private final String dockerFilePath;

        public DockerFileEntry(ImageName imageName, String gitUrl, String gitRef, String dockerFilePath) {
            this.imageName = imageName;
            this.gitUrl = gitUrl;
            this.gitRef = gitRef;
            this.dockerFilePath = dockerFilePath;
        }

        public ImageName getImageName() {
            return imageName;
        }

        public String getGitUrl() {
            return gitUrl;
        }

        public String getGitRef() {
            return gitRef;
        }

        public String getDockerFilePath() {
            return dockerFilePath;
        }

        @Override
        public String toString() {
            return "DockerFileEntry{" +
                    "imageName='" + imageName + '\'' +
                    ", gitUrl='" + gitUrl + '\'' +
                    ", gitRef='" + gitRef + '\'' +
                    ", dockerFilePath='" + dockerFilePath + '\'' +
                    '}';
        }
    }

}
