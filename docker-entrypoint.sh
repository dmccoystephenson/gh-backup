#!/bin/sh
set -e

# Build Java command with environment variables
JAVA_OPTS="-Dspring.profiles.active=daemon"

# Set backup directory if provided (defaults to /backups in Dockerfile ENV)
if [ -n "$BACKUP_DIRECTORY" ]; then
    JAVA_OPTS="$JAVA_OPTS -Dbackup.directory=${BACKUP_DIRECTORY}"
fi

# Set scheduled users if provided
if [ -n "$SCHEDULED_USERS" ]; then
    JAVA_OPTS="$JAVA_OPTS -Dbackup.scheduled.users=${SCHEDULED_USERS}"
fi

# Execute the application
exec java $JAVA_OPTS -jar gh-backup.jar "$@"
