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

## backup.mode

**Type:** string (`cli` | `web`)  
**Default:** `cli`  
**Description:** Controls whether the application runs as a command-line tool (`cli`) or as a web server (`web`). Use `web` by activating the `web` Spring profile.

**Override at runtime:**
```bash
java -Dspring.profiles.active=web -jar target/gh-backup-1.0.0.jar
```

The `web` profile (`application-web.properties`) automatically sets `backup.mode=web` and enables the servlet web application type.

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

## GITHUB_TOKEN (environment variable)

**Type:** string  
**Default:** not set  
**Description:** A GitHub personal access token used to authenticate GitHub API requests. When set, the rate limit increases from 60 to 5,000 requests per hour. Not set via `application.properties`; pass it as an environment variable.

```bash
export GITHUB_TOKEN=ghp_yourTokenHere
java -jar target/gh-backup-1.0.0.jar octocat
```
