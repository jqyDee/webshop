# Build Frontend
FROM node:20 AS frontend-build
WORKDIR /app/frontend

# Copy only package files here for better caching (Docker image layers!)
COPY src/main/frontend/package*.json ./ 
RUN npm ci

# Copy the rest and build
COPY src/main/frontend/ .
RUN npm run build


# Build Backend
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

# Copy pom, download dependencies (again, layering)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Copy source folder
COPY src/main/java ./src/main/java
COPY src/main/resources ./src/main/resources

# Copy react build into static resources
RUN mkdir -p src/main/resources/static
COPY --from=frontend-build /app/frontend/build ./src/main/resources/static/

# Package Backend app; no testing here! (see separate pipeline step!)
RUN mvn -q -B package -DskipTests -Dskip.frontend=true -Dspring.profiles.active=prod


# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar

# Limit memory for deployment server restriction reasons
# TODO test if better option: -XX:MaxRAMPercentage=75
ENV JAVA_OPTS="-Xmx256m"

EXPOSE 8080

RUN useradd -ms /bin/bash appuser
RUN echo '#!/bin/sh\nexec java $JAVA_OPTS -Dspring.profiles.active=prod -jar /app/app.jar' > /app/entrypoint.sh && chmod +x /app/entrypoint.sh && chown appuser:appuser /app/app.jar /app/entrypoint.sh
USER appuser

ENTRYPOINT ["/app/entrypoint.sh"]
