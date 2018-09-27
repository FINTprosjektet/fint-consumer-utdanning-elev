FROM gradle:4.6.0-jdk8-alpine as builder
USER root
COPY . .
ARG apiVersion
RUN gradle --no-daemon -PapiVersion=${apiVersion} build

FROM openjdk:8-jre-alpine
COPY --from=builder /home/gradle/build/deps/external/*.jar /data/
COPY --from=builder /home/gradle/build/deps/fint/*.jar /data/
COPY --from=builder /home/gradle/build/libs/fint-consumer-utdanning-elev-*.jar /data/fint-consumer-utdanning-elev.jar
CMD ["java", "-XX:+ExitOnOutOfMemoryError", "-jar", "/data/fint-consumer-utdanning-elev.jar"]
