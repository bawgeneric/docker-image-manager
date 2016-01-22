package io.kodokojo.docker.utils.properties.provider.kv;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.kodokojo.docker.model.*;
import io.kodokojo.docker.service.connector.git.GitDockerFileScmEntry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.common.IOUtils;
import org.apache.zookeeper.data.Stat;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ZookeeperJsonObjectValueProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperJsonObjectValueProviderTest.class);

    public static final String MACONFIG_KEY_A = "/maconfig/keyA";


    @Rule
    public ZookeeperResources zookeeperResources = new ZookeeperResources();

    @Test
    public void request_a_DockerBuildPlan_from_zookeeper() {
        ZooKeeper zooKeeper = zookeeperResources.getZooKeeper();
        ImageName imageName = StringToImageNameConverter.convert("jpthiery/busybox:1.0.0");
        try {
            Stat existsRoot = zooKeeper.exists("/maconfig", false);

            if (existsRoot == null) {
                zooKeeper.create("/maconfig", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOGGER.debug("Create root /maconfig");
            }
            Stat exists = zooKeeper.exists(MACONFIG_KEY_A, false);
            if (exists == null) {
                String stat = zooKeeper.create("/maconfig/keyA", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                LOGGER.debug("Create /maconfig/keyA");
            }

            exists = zooKeeper.exists("/maconfig/keyA", false);
            HashMap<DockerFileBuildRequest,DockerFileBuildResponse> children = new HashMap();
            DockerFile dockerFile = new DockerFile(imageName);
            GitDockerFileScmEntry dockerFileScmEntry = new GitDockerFileScmEntry(imageName,"git://github.com/kodokojo/acme", "HEAD", "dockerfiles/busybox");

            DockerFileBuildPlan dockerFileBuildPlan = new DockerFileBuildPlan(dockerFile,children, dockerFileScmEntry,new Date());
            //Gson gson = new GsonBuilder().registerTypeAdapter(GitDockerFileScmEntry.class, new GsonInterfaceAdapter<GitDockerFileScmEntry>()).create();
            Gson gson = new GsonBuilder().create();
            LOGGER.debug("Add follonwing data in Zookeeper node '/maconfig/keyA':\n{}",gson.toJson(dockerFileBuildPlan) );
            String data = gson.toJson(dockerFileBuildPlan);
            zooKeeper.setData("/maconfig/keyA", data.getBytes(), exists.getVersion());
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        ZookeeperJsonObjectValueProvider zookeeperJsonObjectValueProvider = new ZookeeperJsonObjectValueProvider(zookeeperResources.getZkUrl(), null);
        DockerFileBuildPlan dockerFileBuildPlan = zookeeperJsonObjectValueProvider.providePropertyValue(DockerFileBuildPlan.class, "/maconfig/keyA");

        assertThat(dockerFileBuildPlan).isNotNull();
        assertThat(dockerFileBuildPlan.getDockerFile().getImageName()).isEqualTo(imageName);

        IOUtils.closeStream(zookeeperJsonObjectValueProvider);

    }

}