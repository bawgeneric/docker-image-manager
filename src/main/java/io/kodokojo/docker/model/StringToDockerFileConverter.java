package io.kodokojo.docker.model;

import io.kodokojo.docker.model.DockerFile;
import io.kodokojo.docker.model.ImageName;
import io.kodokojo.docker.model.StringToImageNameConverter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;

public class StringToDockerFileConverter  {

    private static final Pattern FROM_PATTERN = Pattern.compile("^FROM (.*)$");

    private static final Pattern MAINTAINER_PATTERN = Pattern.compile("^MAINTAINER (.*)$");

    public static DockerFile convertToDockerFile(ImageName imageName, String content) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        if (isBlank(content)) {
            throw new IllegalArgumentException("content must be defined.");
        }
        String[] splitted = content.split("\n");
        List<String> lines = Arrays.asList(splitted);
        Iterator<String> it = lines.iterator();
        String from = null;
        String maintainer = null;
        while((from == null || maintainer == null) && it.hasNext()) {
            String line = it.next();
            Matcher fromMatcher = FROM_PATTERN.matcher(line);
            if (from == null && fromMatcher.find()) {
                from = fromMatcher.group(1);
            } else {
                Matcher maintainerMatcher = MAINTAINER_PATTERN.matcher(line);
                if (maintainerMatcher.find()) {
                    maintainer = maintainerMatcher.group(1);
                }
            }
        }
        DockerFile res = new DockerFile(imageName, StringToImageNameConverter.convert(from), maintainer);
        return res;
    }

}
