#!/bin/sh
set -e

# Build Java command with environment variables
JAVA_OPTS="-Dspring.profiles.active=daemon"
JAVA_OPTS="$JAVA_OPTS -Dbackup.directory=${BACKUP_DIRECTORY}"

if [ -n "$SCHEDULED_USERS" ]; then
    JAVA_OPTS="$JAVA_OPTS -Dbackup.scheduled.users=${SCHEDULED_USERS}"
fi

# Execute the application
exec java $JAVA_OPTS -jar gh-backup.jar "$@"
