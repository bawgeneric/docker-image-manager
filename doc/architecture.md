# Architecture

![Architecture](/doc/docker-image-manager.png)

# Process details

![Process details](/doc/docker-image-manager_process.png)
1. Receive a push event notification from Docker registry
2. Send the Rest event to a [RegistryRequestWorker](/src/main/java/io/kodokojo/docker/service/actor/RegistryRequestWorker.java)
3. which may produce an [RegistryEvent](/src/main/java/io/kodokojo/docker/model/RegistryEvent.java)
4. Submit the RegistryEvent to PushEventChecker which may check if the push Event match to an create or update layer event.
5. Then, send an update event to requesting a build for child of Docker image which come from layer update event.
6. For each Docker image child, push a Docker image build request, which contain SCM reference to be able to find corresponding Dockerfile.
7. Change state of parent Docker image build plan, notify the DockerNodeRepository and push the new Docker image to a Registry.
 