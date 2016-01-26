FROM java:8-jre

LABEL kodokojo-componentType="docker-image-manager"

ADD docker-image-manager.jar /project/docker-image-manager.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/project/docker-image-manager.jar"]