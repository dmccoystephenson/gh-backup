#!/bin/sh
set -e

set -- java "-Dspring.profiles.active=daemon"

# Set backup directory if provided (defaults to /backups in Dockerfile ENV)
if [ -n "$BACKUP_DIRECTORY" ]; then
    set -- "$@" "-Dbackup.directory=${BACKUP_DIRECTORY}"
fi

# Set scheduled users if provided
if [ -n "$SCHEDULED_USERS" ]; then
    set -- "$@" "-Dbackup.scheduled.users=${SCHEDULED_USERS}"
fi

# Execute the application
set -- "$@" -jar gh-backup.jar "$@"
exec "$@"
