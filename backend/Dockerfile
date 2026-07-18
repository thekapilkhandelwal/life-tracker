FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/life-tracker-api-1.0.0.jar app.jar
# Tuned for free tiers (Back4App ~256MB). Override with JAVA_TOOL_OPTIONS if needed.
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:+UseSerialGC -Xms64m -Xmx180m -XX:MaxMetaspaceSize=96m"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
