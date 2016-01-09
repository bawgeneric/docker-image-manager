package io.kodokojo.docker.model;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jpthiery on 08/01/2016.
 */

public class StringToImageNameConverter implements Function<String, ImageName> {

    public static final Pattern IMAGENAME_PATTERN = Pattern.compile("([^/:\\s]+)(?:/([^:\\s]+))?(?::([^\\s]+))?");

    private static final Logger LOGGER = LoggerFactory.getLogger(StringToImageNameConverter.class);

    @Override
    public ImageName apply(String input) {
        if (StringUtils.isBlank(input)) {
            throw new IllegalArgumentException("input  must be defined.");
        }

        Matcher matcher = IMAGENAME_PATTERN.matcher(input);
        if (matcher.find()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Find {} groups for input {}.",  matcher.groupCount(), input);
            }

            if (LOGGER.isTraceEnabled()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String group = matcher.group(i);
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Group {} : {}", i, group);
                    }

                }
            }

            String namespace = null;
            String name = null;
            String tag = null;

            String gA = matcher.group(1);
            String gB = matcher.group(2);
            String gC = matcher.group(3);

            if (gB == null && gC == null) {
                name = gA;
            } else if (gB == null || gC == null) {
                if (input.contains("/")) {
                    namespace = gA;
                    name = gB;
                } else if (input.contains(":")) {
                    name = gA;
                    tag = gC;
                }
            } else  {
                namespace = gA;
                name = gB;
                tag = gC;

            }

            return new ImageName(namespace, name, tag);
        }

        return null;
    }
}
