package io.kodokojo.docker.bdd.stage.docker;

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

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import io.kodokojo.docker.service.DockerClientSupport;

import java.io.IOException;

public class DockerRegistryThen<SELF extends DockerRegistryThen<SELF>> extends DockerCommonsThen<SELF> {

    @ExpectedScenarioState
    DockerClientSupport dockerClientSupport;

    public SELF docker_image_manager_receive_image_$(String imageName) {
        String containerId = containers.get(DockerCommonsGiven.DOCKER_IMAGE_MANAGER_KEY);

        int dockerClientRuleExposedPort = dockerClientSupport.getExposedPort(containerId, 8080);
        OkHttpClient httpClient = new OkHttpClient();

        String url = "http://" + dockerClientSupport.getServerIp() + ":" + dockerClientRuleExposedPort + "/api";
        HttpUrl httpUrl = HttpUrl.parse(url);

        Request request = new Request.Builder().url(httpUrl).get().build();

        try {
            Response response = httpClient.newCall(request).execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return self();
    }


}
