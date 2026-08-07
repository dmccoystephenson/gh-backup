# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

- Daemon mode (`-Dspring.profiles.active=daemon`): runs scheduled backups automatically for configured users/organizations at a configurable interval (`backup.scheduled.users`, `backup.scheduled.interval.ms`), defaulting to every 24 hours
- Docker support (`Dockerfile`, `docker-compose.yml`, `.env.example`) for running daemon mode as a containerized background service

### Fixed

- Documentation accuracy: the `--interactive` flag, the `quit` command alias, the `backup.progress.overwrite` and `logging.level.root` properties, the Docker environment variables, and the web API error responses are now documented, and the repository links in `README.md` and `CONTRIBUTING.md` point at `Stephenson-Software/gh-backup`

## [1.0.0] – 2026-01-01

### Added

- CLI mode: back up public repositories for one or more GitHub users or organizations by passing their names as arguments
- Interactive mode (`-i`/`--interactive` flag): prompt-based interface with `backup`, `status`, `help`, and `exit` commands
- Web UI mode (`-Dspring.profiles.active=web`): browser-based interface for managing backups
- REST API endpoints (`POST /api/backups`, `GET /api/backups/status`) for programmatic access
- GitHub authentication via `GITHUB_TOKEN` environment variable for higher API rate limits
- Configurable backup directory via `backup.directory` system property (defaults to `~/gh-backups/`)
- Cross-platform path normalization (Linux, macOS, Windows)
- Backing up multiple users and organizations in a single invocation, processed one after another
- Incremental updates: existing repositories are updated with `git fetch` instead of re-cloned
