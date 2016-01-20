package io.kodokojo.docker.utils;

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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.squareup.okhttp.*;
import org.apache.commons.lang.StringUtils;
import org.junit.Assume;
import org.junit.rules.ExternalResource;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isBlank;

//  TODO Move this class in a docker-commons project
public class DockerClientSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClientSupport.class);

    private DockerClient dockerClient;

    private List<String> containerToClean;

    private String remoteDaemonDockerIp;

    private final boolean dockerIsPresent;

    public DockerClientSupport(DockerClientConfig config) {
        dockerClient = DockerClientBuilder.getInstance(config).build();
        if (isNotWorking(dockerClient)) {
            String userHome = System.getProperty("user.home");
            config = DockerClientConfig.createDefaultConfigBuilder().withUri("https://192.168.99.100:2376").withDockerCertPath(userHome + "/.docker/machine/machines/default").build();
            dockerClient = DockerClientBuilder.getInstance(config).build();
            LOGGER.warn("Unable to connect to Docker daemon with default configuration, try to connect to a local Docker machine instance launch under name 'default' available on socket 'https://192.168.99.100:2376'");
        }
        remoteDaemonDockerIp = config.getUri() != null ? config.getUri().getHost() : "127.0.0.1";
        containerToClean = new ArrayList<>();
        dockerIsPresent = isDockerWorking();
    }

    public DockerClientSupport() {
        this(DockerClientConfig.createDefaultConfigBuilder().build());
    }

    public boolean isDockerIsPresent() {
        return dockerIsPresent;
    }

    public void addContainerIdToClean(String id) {
        containerToClean.add(id);
    }

    public void pullImage(String image) {
        if (isBlank(image)) {
            throw new IllegalArgumentException("image must be defined.");
        }
        if (dockerClient == null) {
            throw new IllegalArgumentException("dockerClient must be defined.");
        }
        try {
            dockerClient.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion().onComplete();
        } catch (InterruptedException e) {
            throw new RuntimeException("Unable to pull java image", e);
        }
    }


    public String getContainerName(String containerId) {
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(containerId).exec();
        return inspectContainerResponse.getName();
    }

    public int getExposedPort(String containerId, int containerPort) {
        InspectContainerResponse inspectContainerResponse = dockerClient.inspectContainerCmd(containerId).exec();
        Map<ExposedPort, Ports.Binding[]> bindings = inspectContainerResponse.getNetworkSettings().getPorts().getBindings();
        Ports.Binding[] bindingsExposed = bindings.get(ExposedPort.tcp(containerPort));
        if (bindingsExposed == null) {
            return -1;
        }
        return bindingsExposed[0].getHostPort();
    }

    public String getHttpContainerUrl(String containerId, int containerPort) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://").append(getServerIp()).append(":").append(getExposedPort(containerId, containerPort));
        return sb.toString();
    }

    public void stopAndRemoveContainer() {
        containerToClean.forEach(id -> {
            dockerClient.stopContainerCmd(id).exec();
            dockerClient.removeContainerCmd(id).exec();
            LOGGER.debug("Stopped and removed container id {}", id);
        });
        containerToClean.clear();
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public boolean isDockerWorking() {
        return !isNotWorking(dockerClient);
    }

    private boolean isNotWorking(DockerClient dockerClient) {
        if (dockerClient == null) {
            return true;
        }
        try {
            Version version = dockerClient.versionCmd().exec();

            return version == null || StringUtils.isBlank(version.getGitCommit());
        } catch (Exception e) {
            return true;
        }
    }

    public String getServerIp() {
        return remoteDaemonDockerIp;
    }

    public boolean waitUntilHttpRequestRespond(String url, int time) {
        return waitUntilHttpRequestRespond(url, time, null);
    }

    public boolean waitUntilHttpRequestRespond(String url, int time, TimeUnit unit) {
        if (isBlank(url)) {
            throw new IllegalArgumentException("url must be defined.");
        }

        long now = System.currentTimeMillis();
        long delta = unit != null ? TimeUnit.MILLISECONDS.convert(time, unit) : time;
        long endTime = now + delta;
        long until = 0;


        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl httpUrl = HttpUrl.parse(url);

        int nbTry = 0;
        boolean available = false;
        do {
            nbTry++;
            available = tryRequest(httpUrl, httpClient);
            if (!available) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    break;
                }
                now = System.currentTimeMillis();
                until = endTime - now;
            }
        } while (until > 0 && !available);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(url + " " + (available ? "Success" : "Failed after " + nbTry + " try"));
        }
        return available;
    }

    private boolean tryRequest(HttpUrl url, OkHttpClient httpClient) {
        try {
            Request request = new Request.Builder().url(url).get().build();
            Call call = httpClient.newCall(request);
            Response response = call.execute();
            boolean isSuccesseful = response.isSuccessful();
            response.body().close();
            return isSuccesseful;
        } catch (IOException e) {
            return false;
        }
    }

}
