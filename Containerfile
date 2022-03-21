## Stage 1 : build with maven builder image with native capabilities
FROM maven:3.8.3-openjdk-17 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
# USER root
# RUN chown -R quarkus /usr/src/app
# USER quarkus
# we will build a native image using the native maven profile
RUN mvn -f /usr/src/app/pom.xml clean package


# FROM registry.access.redhat.com/ubi8/ubi-minimal:8.4
FROM registry.access.redhat.com/ubi8/openjdk-17:1.11

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 --from=build target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=build target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=build target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=build target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"