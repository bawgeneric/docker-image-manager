# References

## Properties providers

`docker-image-manager` allow to provide properties with following ways :
* Arguments passed to JVM
* Environment variable
* System properties
* Property file
* Consul
* Zookeeper

A property value aggregator could mix several way to access to a value for a given key.
We configure a [PropertyModule](/../src/main/java/io/kodokojo/docker/config/PropertyModule.java) which provide property in this order :
* Arguments passed to JVM
* Environment variable
* System properties
* Property file

When a property is query by is key, the first property value provider which find a non null value will return.
For example, if we query the value of `git.bashbrew.url` as key, `docker-image-manager` will try to lookup :

1. if JVM arguments content `--git.bashbrew.url https://github.com/kodokojo/acme`
2. if Environment variable content `git.bashbrew.url https://github.com/kodokojo/acme` using `System.getProperty()`
3. if System properties content value like `Dgit.bashbrew.url=https://github.com/kodokojo/acme` using `System.getenv()`
4. if property file `applicationConfiguration.properties`content a line like `git.bashbrew.url=https://github.com/kodokojo/acme`

Consul and Zookeeper property provider are not yet integrate. We must defined a property provider selector configurable.

## Property list

`docker-image-manager` may expect to get following properties :
 
 Key                         | Is mandatory ?     | Description
 ----------------------------|--------------------|-------------------------------------
 git.bashbrew.url            | :white_check_mark: | The git url of repository which contain bashbrew library like `git://github.com/kodokojo/acme . To get more informations about bashbrew, look [docker-library](https://github.com/docker-library/official-images)
 git.bashbrew.library.path   | :white_check_mark: | The path in bashbrew git repository which contain library. 
 project.name                | :white_check_mark: | Used in kodokojo to register service
 stack.name                  | :white_check_mark: | Used in kodokojo to register service
 stack.type                  | :white_check_mark: | Used in kodokojo to register service
 registry.host               |                    | The registry host/ip. If not defined, lookup on docker-engine for container with kodokojo labels with match with this kodokojo component.
 registry.port               |                    | The registry port. If not defined, lookup on docker-engine for container with kodokojo labels with match with this kodokojo component.
 