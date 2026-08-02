# Commands Reference

gh-backup is invoked from the command line via the built JAR. This document lists all supported arguments and modes.

## Basic Syntax

```
java [JVM_OPTIONS] -jar target/gh-backup-1.0.0.jar [OPTIONS] [USER/ORG...]
```

## Positional Arguments

### `<user/org> [user/org2] ...`

**Description:** One or more GitHub usernames or organization names whose public repositories should be backed up.  
**Required:** Yes (unless using interactive mode or web mode)  
**Example:**
```bash
java -jar target/gh-backup-1.0.0.jar octocat github spring-projects
```

## Options

### `-i` – Interactive Mode

**Description:** Start the tool in interactive mode, providing a prompt for issuing commands without restarting the application.  
**Usage:**
```bash
java -jar target/gh-backup-1.0.0.jar -i
```

### Interactive Mode Commands

Once in interactive mode, the following commands are available at the `>` prompt:

| Command | Description |
|---------|-------------|
| `backup <user/org>` | Back up all public repositories for the given GitHub user or organization |
| `status` | Display the backup directory path and a summary of all backed-up repositories |
| `exit` | Exit interactive mode and terminate the application |

## JVM System Properties

These are passed with `-D` before the `-jar` flag.

### `-Dbackup.directory=<path>`

**Description:** Override the directory where repositories are saved.  
**Default:** `~/gh-backups/` (the `gh-backups` folder in the current user's home directory)  
**Example:**
```bash
java -Dbackup.directory=/mnt/storage/github-backups -jar target/gh-backup-1.0.0.jar octocat
```

### `-Dspring.profiles.active=web`

**Description:** Start the application as a web server instead of a CLI tool.  
**Default:** CLI mode  
**Example:**
```bash
java -Dspring.profiles.active=web -jar target/gh-backup-1.0.0.jar
```

### `-Dspring.profiles.active=daemon`

**Description:** Start the application as a background daemon that runs scheduled backups automatically, instead of a CLI tool. Requires `backup.scheduled.users` to be set.  
**Default:** CLI mode  
**Example:**
```bash
java -Dspring.profiles.active=daemon -Dbackup.scheduled.users=octocat,github -jar target/gh-backup-1.0.0.jar
```

### `-Dbackup.scheduled.users=<user/org1,user/org2,...>`

**Description:** Comma-separated list of GitHub users/organizations to back up automatically in daemon mode. Only effective when running with `-Dspring.profiles.active=daemon`.  
**Default:** *(empty)*  
**Example:**
```bash
java -Dspring.profiles.active=daemon -Dbackup.scheduled.users=octocat,github -jar target/gh-backup-1.0.0.jar
```

### `-Dbackup.scheduled.interval.ms=<milliseconds>`

**Description:** Delay between scheduled backups in daemon mode. Only effective when running with `-Dspring.profiles.active=daemon`.  
**Default:** `86400000` (24 hours)  
**Example:**
```bash
java -Dspring.profiles.active=daemon -Dbackup.scheduled.users=octocat -Dbackup.scheduled.interval.ms=3600000 -jar target/gh-backup-1.0.0.jar
```

### `-Dserver.port=<port>`

**Description:** Set the HTTP port for the web server (only effective in web mode).  
**Default:** `8080`  
**Example:**
```bash
java -Dspring.profiles.active=web -Dserver.port=9000 -jar target/gh-backup-1.0.0.jar
```

## Environment Variables

### `GITHUB_TOKEN`

**Description:** A GitHub personal access token used to authenticate API requests. Increases the API rate limit from 60 to 5,000 requests per hour.  
**Required:** No  
**Example:**
```bash
export GITHUB_TOKEN=ghp_yourTokenHere
java -jar target/gh-backup-1.0.0.jar octocat
```

## Web API Endpoints

When running in web mode (`-Dspring.profiles.active=web`), the following REST endpoints are available:

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/backup` | Start a backup for a given user or organization |
| `GET` | `/api/status` | Return backup status and a list of all backed-up repositories |

### POST /api/backup

**Request body:**
```json
{ "username": "octocat" }
```

**Response:**
```json
{ "message": "Backup completed for octocat", "repositoryCount": 8 }
```

### GET /api/status

**Response:**
```json
{
  "backupDirectory": "/home/user/gh-backups",
  "users": ["octocat"],
  "repositories": [
    { "name": "Hello-World", "owner": "octocat", "lastUpdated": "2026-01-10T12:30:45" }
  ]
}
```
