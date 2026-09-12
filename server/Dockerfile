FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /workspace
COPY server/pom.xml server/pom.xml
RUN mvn -B -f server/pom.xml dependency:go-offline
COPY server server
RUN mvn -B -f server/pom.xml clean package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /workspace/server/target/hahaen-ledger-server-1.0.0.jar app.jar

ENV JAVA_TOOL_OPTIONS="-Dio.netty.noUnsafe=true"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
