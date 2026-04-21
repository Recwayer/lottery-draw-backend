ARG FROM_IMAGE=eclipse-temurin:21-jre

FROM $FROM_IMAGE
WORKDIR /opt/app
USER root
RUN chown -R 1001:0 /opt/app && chmod -R g=u /opt/app
USER 1001
ARG JAR_FILE_PATH=./build/libs/*.jar
COPY "$JAR_FILE_PATH" app.jar
ENV JAVA_OPTIONS "-Dfile.encoding=UTF-8 \
                  -Duser.timezone=UTC"
EXPOSE 8080
CMD java -jar $JAVA_OPTIONS app.jar