package io.kodokojo.docker.service;

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
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.squareup.okhttp.*;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerClientRule extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClientRule.class);

    private DockerClient dockerClient;

    private List<String> containerToClean;

    private String remoteDaemonDockerIp;

    public DockerClientRule(DockerClientConfig config) {
        dockerClient = DockerClientBuilder.getInstance(config).build();
        if (isNotWorking(dockerClient)) {
            String userHome = System.getProperty("user.home");
            config = DockerClientConfig.createDefaultConfigBuilder().withUri("https://192.168.99.100:2376").withDockerCertPath(userHome + "/.docker/machine/machines/default").build();
            dockerClient = DockerClientBuilder.getInstance(config).build();
        }
        remoteDaemonDockerIp = config.getUri() != null ? config.getUri().getHost() : "127.0.0.1";
        containerToClean = new ArrayList<>();
    }

    public DockerClientRule() {
        this(DockerClientConfig.createDefaultConfigBuilder().build());
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

    @Override
    protected void before() throws Throwable {
        super.before();
        containerToClean.clear();
    }

    @Override
    protected void after() {
        super.after();
        stopAndRemoveContainer();
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

    private boolean isNotWorking(DockerClient dockerClient) {
        try {
            dockerClient.listImagesCmd().exec();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public String getServerIp() {
        return remoteDaemonDockerIp;
    }

    public void waitUntilHttpRequestRespond(String url, int time) {
        waitUntilHttpRequestRespond(url, time, null);
    }

    public void waitUntilHttpRequestRespond(String url, int time, TimeUnit unit) {
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
                    System.err.println("Break");
                    break;
                }
                now = System.currentTimeMillis();
                until = endTime - now;
                //System.out.println("Until " + until);
            }
        } while (until > 0 && !available);
        System.out.println(url + " " + (available ? "Success" : "Failed after " + nbTry + " try"));

    }

    private boolean tryRequest(HttpUrl url, OkHttpClient httpClient) {
        try {
            Request request = new Request.Builder().url(url).get().build();
            Call call = httpClient.newCall(request);
            Response response = call.execute();
            return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }

}
