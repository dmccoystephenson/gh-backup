# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install git (required for cloning repositories)
RUN apk add --no-cache git

# Copy the built jar from build stage
COPY --from=build /app/target/gh-backup-1.0.0.jar gh-backup.jar

# Create backup directory
RUN mkdir -p /backups

# Set default environment variables
ENV BACKUP_DIRECTORY=/backups
ENV GITHUB_TOKEN=""
ENV SCHEDULED_USERS=""

# Expose port for web mode (optional)
EXPOSE 8080

# Create entrypoint script
COPY docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

# Default to daemon mode with scheduled backups
ENTRYPOINT ["/app/docker-entrypoint.sh"]
