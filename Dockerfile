FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src src
COPY ui ui

# Package the static dashboard inside Spring Boot so one Render service runs both layers.
RUN mkdir -p src/main/resources/static \
	&& cp -R ui/. src/main/resources/static/ \
	&& mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/leadproject-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
