#!/bin/sh
# Tests docker-entrypoint.sh by putting a stub `java` on PATH that prints its
# arguments instead of starting a JVM, then asserting the argument list the
# entrypoint builds for each combination of the environment variables it reads.
#
# Each argument is printed wrapped in brackets, so a value containing a space is
# distinguishable from two separate arguments.
#
# Run from the repository root:
#   sh docker-entrypoint-test.sh

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ENTRYPOINT="$SCRIPT_DIR/docker-entrypoint.sh"

if [ ! -f "$ENTRYPOINT" ]; then
    echo "docker-entrypoint.sh not found at $ENTRYPOINT" >&2
    exit 1
fi

STUB_BIN=$(mktemp -d)
trap 'rm -rf "$STUB_BIN"' EXIT INT TERM

cat > "$STUB_BIN/java" <<'STUB'
#!/bin/sh
for arg in "$@"; do
    printf '[%s]' "$arg"
done
echo
STUB
chmod +x "$STUB_BIN/java"

PROFILE="[-Dspring.profiles.active=daemon]"
JAR="[-jar][gh-backup.jar]"

failures=0

# Runs the entrypoint with only the given NAME=VALUE assignments exported, so a
# variable set in the caller's environment cannot leak into a case. Callers
# invoke this inside a command substitution, so the changes stay in a subshell.
run_entrypoint() {
    unset BACKUP_DIRECTORY SCHEDULED_USERS BACKUP_INTERVAL_MS
    for assignment in "$@"; do
        case $assignment in
            BACKUP_DIRECTORY=*)
                BACKUP_DIRECTORY=${assignment#*=}
                export BACKUP_DIRECTORY
                ;;
            SCHEDULED_USERS=*)
                SCHEDULED_USERS=${assignment#*=}
                export SCHEDULED_USERS
                ;;
            BACKUP_INTERVAL_MS=*)
                BACKUP_INTERVAL_MS=${assignment#*=}
                export BACKUP_INTERVAL_MS
                ;;
            *)
                echo "Not a variable docker-entrypoint.sh reads: $assignment" >&2
                exit 1
                ;;
        esac
    done
    PATH="$STUB_BIN:$PATH" sh "$ENTRYPOINT"
}

expect() {
    description=$1
    expected=$2
    shift 2

    actual=$(run_entrypoint "$@")

    if [ "$actual" = "$expected" ]; then
        echo "PASS: $description"
    else
        echo "FAIL: $description"
        echo "  expected: $expected"
        echo "  actual:   $actual"
        failures=$((failures + 1))
    fi
}

expect "no variables set" \
    "$PROFILE$JAR"

expect "BACKUP_DIRECTORY only" \
    "$PROFILE[-Dbackup.directory=/data/backups]$JAR" \
    "BACKUP_DIRECTORY=/data/backups"

expect "SCHEDULED_USERS only" \
    "$PROFILE[-Dbackup.scheduled.users=octocat,github]$JAR" \
    "SCHEDULED_USERS=octocat,github"

expect "BACKUP_INTERVAL_MS only" \
    "$PROFILE[-Dbackup.scheduled.interval.ms=3600000]$JAR" \
    "BACKUP_INTERVAL_MS=3600000"

expect "all variables set, in declaration order" \
    "$PROFILE[-Dbackup.directory=/backups][-Dbackup.scheduled.users=octocat][-Dbackup.scheduled.interval.ms=60000]$JAR" \
    "BACKUP_DIRECTORY=/backups" "SCHEDULED_USERS=octocat" "BACKUP_INTERVAL_MS=60000"

# The Dockerfile ships SCHEDULED_USERS and BACKUP_INTERVAL_MS as empty defaults,
# so an empty value must be treated as unset rather than passed through as an
# empty system property.
expect "empty variables add no arguments" \
    "$PROFILE$JAR" \
    "BACKUP_DIRECTORY=" "SCHEDULED_USERS=" "BACKUP_INTERVAL_MS="

expect "value containing a space stays a single argument" \
    "$PROFILE[-Dbackup.directory=/data/my backups]$JAR" \
    "BACKUP_DIRECTORY=/data/my backups"

if [ "$failures" -eq 0 ]; then
    echo "All docker-entrypoint.sh tests passed."
    exit 0
fi

echo "$failures docker-entrypoint.sh test(s) failed." >&2
exit 1
