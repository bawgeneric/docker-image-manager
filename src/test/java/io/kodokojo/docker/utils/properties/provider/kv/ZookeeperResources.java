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

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class ZookeeperResources extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperResources.class);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ZooKeeperServer server;

    private ZooKeeper zooKeeper;

    private String zkUrl;

    public ZooKeeperServer getServer() {
        return server;
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public String getZkUrl() {
        return zkUrl;
    }

    @Override
    protected void before() throws Throwable {
        temporaryFolder.create();
        try {
            File dir = temporaryFolder.newFolder();


            server = new ZooKeeperServer(dir, dir, 2000);
            ServerCnxnFactory standaloneServerFactory = ServerCnxnFactory.createFactory(0, 200);
            int zkPort = standaloneServerFactory.getLocalPort();

            standaloneServerFactory.startup(server);
            zkUrl = "localhost:" + zkPort;
            zooKeeper = new ZooKeeper(zkUrl, 1000, (event) -> {
            });

            LOGGER.info("Zookeeper instance started on port {} with work directory {}", zkPort, dir.getAbsoluteFile());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void after() {
        temporaryFolder.delete();
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        server.shutdown();
    }
}
