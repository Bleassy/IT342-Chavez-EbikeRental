FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only the files needed for a build to leverage Docker layer caching
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY src ./src

RUN chmod +x mvnw || true
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the executable jar from the maven build stage
COPY --from=build /workspace/target/*.jar /app/app.jar

# Let Render (or the operator) set PORT; default to 8080 for local runs
ENV PORT=8080
EXPOSE 8080

# Allow extra JVM options via JAVA_OPTS
ENV JAVA_OPTS=""

CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
