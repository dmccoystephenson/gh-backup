# Configuration Guide

Configuration is provided through Spring Boot property files and JVM system properties.

The main configuration file is `src/main/resources/application.properties`. Values can be overridden at runtime with `-D<property>=<value>`.

---

## backup.directory

**Type:** string (file path)  
**Default:** `${user.home}/gh-backups`  
**Description:** The directory where cloned repositories are stored. The path is resolved relative to the user's home directory by default and is normalized for cross-platform compatibility (Linux, macOS, Windows).

**Override at runtime:**
```bash
java -Dbackup.directory=/mnt/storage/github-backups -jar target/gh-backup-1.0.0.jar octocat
```

Windows example:
```bat
java -Dbackup.directory=C:\Backups\GitHub -jar target/gh-backup-1.0.0.jar octocat
```

---

## backup.progress.overwrite

**Type:** boolean  
**Default:** `true`  
**Description:** Controls how clone and fetch progress is rendered during a backup. When `true`, progress percentages are rewritten in place on a single line using a carriage return. When `false`, each update is printed on its own line, which is easier to read in log files and CI output that do not interpret carriage returns. This value is read directly from the JVM system properties, so it must be passed with `-D` rather than set in `application.properties`.

**Override at runtime:**
```bash
java -Dbackup.progress.overwrite=false -jar target/gh-backup-1.0.0.jar octocat
```

---

## backup.mode

**Type:** string (`cli` | `web` | `daemon`)  
**Default:** `cli`  
**Description:** Controls whether the application runs as a command-line tool (`cli`), a web server (`web`), or a background daemon that runs scheduled backups (`daemon`). Use `web` or `daemon` by activating the corresponding Spring profile.

**Override at runtime:**
```bash
java -Dspring.profiles.active=web -jar target/gh-backup-1.0.0.jar
```

The `web` profile (`application-web.properties`) automatically sets `backup.mode=web` and enables the servlet web application type. The `daemon` profile (`application-daemon.properties`) automatically sets `backup.mode=daemon` and disables the embedded web server.

---

## backup.scheduled.users

**Type:** string (comma-separated list)  
**Default:** *(empty)*  
**Description:** The GitHub users/organizations to back up automatically when running in daemon mode (`-Dspring.profiles.active=daemon`). If empty, the daemon logs a warning on startup and performs no scheduled backups.

**Override at runtime:**
```bash
java -Dspring.profiles.active=daemon -Dbackup.scheduled.users=octocat,github -jar target/gh-backup-1.0.0.jar
```

---

## backup.scheduled.interval.ms

**Type:** integer (milliseconds)  
**Default:** `86400000` (24 hours)  
**Description:** The delay between the end of one scheduled backup and the start of the next, when running in daemon mode. The first backup runs immediately on startup.

**Override at runtime:**
```bash
java -Dspring.profiles.active=daemon -Dbackup.scheduled.users=octocat -Dbackup.scheduled.interval.ms=3600000 -jar target/gh-backup-1.0.0.jar
```

---

## spring.main.web-application-type

**Type:** string (`none` | `servlet`)  
**Default:** `none` (CLI mode)  
**Description:** Spring Boot property that controls whether an embedded web server is started. Set to `servlet` when using web mode. Managed automatically by the `web` Spring profile.

---

## server.port

**Type:** integer  
**Default:** `8080`  
**Description:** The HTTP port on which the web server listens. Only effective when running in web mode.

**Override at runtime:**
```bash
java -Dspring.profiles.active=web -Dserver.port=9000 -jar target/gh-backup-1.0.0.jar
```

---

## logging.level.com.github.backup

**Type:** string (log level)  
**Default:** `INFO`  
**Description:** Log level for application classes. Increase to `DEBUG` for verbose output during troubleshooting.

**Override at runtime:**
```bash
java -Dlogging.level.com.github.backup=DEBUG -jar target/gh-backup-1.0.0.jar octocat
```

---

## logging.level.root

**Type:** string (log level)  
**Default:** `WARN`  
**Description:** Log level for everything outside the application's own packages, including Spring Boot and the GitHub and JGit libraries. It is deliberately set below `logging.level.com.github.backup` so that CLI output stays readable; raise it when framework or library behavior needs to be diagnosed.

**Override at runtime:**
```bash
java -Dlogging.level.root=INFO -jar target/gh-backup-1.0.0.jar octocat
```

---

## GITHUB_TOKEN (environment variable)

**Type:** string  
**Default:** not set  
**Description:** A GitHub personal access token used to authenticate GitHub API requests. When set, the rate limit increases from 60 to 5,000 requests per hour. Not set via `application.properties`; pass it as an environment variable.

```bash
export GITHUB_TOKEN=ghp_yourTokenHere
java -jar target/gh-backup-1.0.0.jar octocat
```

---

## Docker environment variables

When the image built from the included `Dockerfile` is used, the container always starts in daemon mode. `docker-entrypoint.sh` translates the following environment variables into JVM system properties, and `docker-compose.yml` reads them from a `.env` file (see `.env.example`).

| Variable | Maps to | Default in the image | Description |
|----------|---------|----------------------|-------------|
| `SCHEDULED_USERS` | `backup.scheduled.users` | *(empty)* | Comma-separated list of GitHub users/organizations to back up automatically. Passed only when non-empty. |
| `BACKUP_DIRECTORY` | `backup.directory` | `/backups` | Backup directory inside the container. The `docker-compose.yml` service mounts `./backups` at this path so backups persist on the host. |
| `GITHUB_TOKEN` | *(read directly by the application)* | *(empty)* | GitHub personal access token, as described above. |

`backup.scheduled.interval.ms` has no corresponding environment variable, so a container built from this image backs up every 24 hours unless the entrypoint is overridden.

**Example `.env`:**
```
GITHUB_TOKEN=ghp_yourTokenHere
SCHEDULED_USERS=octocat,github
```
