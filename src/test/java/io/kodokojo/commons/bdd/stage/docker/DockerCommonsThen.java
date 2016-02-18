package io.kodokojo.commons.bdd.stage.docker;

/*
 * #%L
 * commons-image-manager
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
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.async.ResultCallbackTemplate;
import com.tngtech.jgiven.CurrentStep;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.attachment.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DockerCommonsThen<SELF extends DockerCommonsThen<SELF>> extends Stage<SELF> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerCommonsThen.class);

    @ExpectedScenarioState
    DockerClient dockerClient;

    @ExpectedScenarioState
    String containerId;

    @ExpectedScenarioState
    String containerName;

    @ExpectedScenarioState
    Map<String, String> containers = new HashMap<>();

    @ExpectedScenarioState
    CurrentStep currentStep;

    public SELF waiting_$_seconds(int seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        return self();
    }

    public SELF attach_log() {
        return attach_log(this.containerId);
    }

    public SELF attach_docker_image_manager_logs() {
        return attach_log(containers.get(DockerCommonsGiven.DOCKER_IMAGE_MANAGER_KEY));
    }

    public SELF attach_log(String containerId) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        try {
            LogBuilder logBuilder = dockerClient.logContainerCmd(containerId).withStdOut().withStdErr().withTailAll().exec(new LogBuilder()).awaitCompletion();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Attach following log : \n{}",logBuilder.getLog());
            }
            Attachment logAttachment = Attachment.plainText(logBuilder.getLog()).withFileName("log.txt").withTitle("Log of container " + containerName);
            currentStep.addAttachment(logAttachment);
        } catch (InterruptedException e) {
            LOGGER.error("Unable to retrieve log", e);
            Thread.interrupted();
        }

        return self();
    }

    private class LogBuilder extends ResultCallbackTemplate<LogBuilder, Frame> {

        private final StringBuilder sb = new StringBuilder();

        @Override
        public void onNext(Frame frame) {
            sb.append(new String(frame.getPayload()));
        }

        public String getLog() {
            return sb.toString();
        }
    }

}
