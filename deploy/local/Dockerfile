FROM eclipse-temurin:25-jre@sha256:dbb43d81099eec91e0800226ed2296d03146ee7857165b95112372ad0d67e87f
ARG VCS_REF
LABEL org.opencontainers.image.revision=$VCS_REF io.haen.project="ledger"
WORKDIR /app
COPY app.jar /app/app.jar
ENV JAVA_TOOL_OPTIONS="-Dio.netty.noUnsafe=true -Duser.timezone=Asia/Shanghai"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
