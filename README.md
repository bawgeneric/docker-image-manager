# docker-image-manager

Automatise build of you image, including Docker image which inherit a given image.

 A docker build will be triggered by :
 * a docker registry push event [Docker registry documentation](https://docs.docker.com/registry/notifications/)
 * (via a REST API)
  
This tools is used by Kodo-kojo project.

# Quick start

## Requirements

This project require you get following component installed :
* Java 1.8
* Maven 3
* Docker 1.8 or newer

## Build

Clone following project :
```
git clone https://github.com/kodokojo/docker-image-manager.git
cd docker-image-manager
```

Now, `docker-image-manager` require to get artifact of project [commons](https://github.com/kodokojo/commons.git) installed on your local repository.

Then :
```
mvn -P docker clean verify
```
The `-P docker` will activate the Maven profile `docker` which build the docker image `kodokojo/docker-image-manager`. 

## Run
The following command will launch a Docker Registry v2 container and a `kodokojo/docker-image-manager` container.
```
docker-compose -f src/test/resources/docker-compose.yml -p dckomgmgt up -d
```


To trigger a build to trial :
```
docker pull busybox:latest
docker tag busybox:tag localhost:<EXPOSED_PORT>
docker push localhost:<EXPOSED_PORT>
```

# Links

* [Reference](doc/reference.md).
* [REST API](doc/api.md).
* [Architecture](doc/architecture.md).

# License

`docker-image-manager` is a licensed under [GNU General Public License v3](http://www.gnu.org/licenses/gpl-3.0.en.html).

# Contribute

Please, read following [Contribution page](CONTRIBUTE.md)

# Technology inside

* Java
* Docker
* Maven
* JGit
* Akka
* Java-Docker
* Sparkjava
* Guice
* Retrofit
* Gson

## Supported
* Consul
* Zookeeper

Thanks to all those Open source project !

 
 