# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

## [1.0.0] – 2026-01-01

### Added

- CLI mode: back up public repositories for one or more GitHub users or organizations by passing their names as arguments
- Interactive mode (`-i` flag): prompt-based interface with `backup`, `status`, and `exit` commands
- Web UI mode (`-Dspring.profiles.active=web`): browser-based interface for managing backups
- REST API endpoints (`POST /api/backup`, `GET /api/status`) for programmatic access
- GitHub authentication via `GITHUB_TOKEN` environment variable for higher API rate limits
- Configurable backup directory via `backup.directory` system property (defaults to `~/gh-backups/`)
- Cross-platform path normalization (Linux, macOS, Windows)
- Parallel backup of multiple users and organizations
- Incremental updates: existing repositories are updated with `git fetch` instead of re-cloned
