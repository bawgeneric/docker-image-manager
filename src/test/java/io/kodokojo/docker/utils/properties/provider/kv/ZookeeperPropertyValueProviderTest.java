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

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.common.IOUtils;
import org.apache.zookeeper.data.Stat;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ZookeeperPropertyValueProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperPropertyValueProviderTest.class);

    public static final String MACONFIG_KEY_A = "/maconfig/keyA";

    @Rule
    public ZookeeperResources zookeeperResources = new ZookeeperResources();


    @Test
    public void request_a_string_from_zookeeper() {

        ZooKeeper zooKeeper = zookeeperResources.getZooKeeper();

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
            zooKeeper.setData("/maconfig/keyA", "value1".getBytes(), exists.getVersion());
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        ZookeeperPropertyValueProvider valueProvider = new ZookeeperPropertyValueProvider(zookeeperResources.getZkUrl());
        String value = valueProvider.providePropertyValue(String.class, "/maconfig/keyA");

        assertThat(value).isEqualTo("value1");
        IOUtils.closeStream(valueProvider);

    }


}