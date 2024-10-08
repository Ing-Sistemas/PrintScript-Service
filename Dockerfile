FROM gradle:8.10.1-jdk21 AS build
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN  gradle build
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/home/gradle/src/build/libs/permissionsManager-0.0.1-SNAPSHOT.jar"]