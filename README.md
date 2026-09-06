# gh-backup

## Description

gh-backup is a Spring Boot tool for backing up public GitHub repositories for specified users or organizations. It is available as a command-line tool, a web application, and a daemon that runs scheduled backups automatically (with Docker support).

## Installation

### First Time Installation

1. Ensure [Java 17+](https://adoptium.net/) and [Maven 3.6+](https://maven.apache.org/) are installed.
2. Clone the repository:
   ```bash
   git clone https://github.com/Stephenson-Software/gh-backup.git
   cd gh-backup
   ```
3. Build the project:
   ```bash
   mvn clean package
   ```
4. The executable JAR will be created at `target/gh-backup-2.0.0-SNAPSHOT-8-8-2026.jar`.

## Usage

### Documentation

- [User Guide](USER_GUIDE.md) – Getting started and common scenarios
- [Commands Reference](COMMANDS.md) – Complete list of all CLI commands and options
- [Configuration Guide](CONFIG.md) – Detailed configuration options

## Support

You can find the support Discord server [here](https://discord.gg/xXtuAQ2).

### Experiencing a bug?

Please fill out a bug report [here](https://github.com/Stephenson-Software/gh-backup/issues/new).

- [Known Bugs](https://github.com/Stephenson-Software/gh-backup/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

## Contributing

- [CONTRIBUTING.md](CONTRIBUTING.md)

## Testing

### Unit Tests

Linux / macOS:

```bash
mvn clean test
```

Windows:

```bat
mvn clean test
```

If you see `BUILD SUCCESS`, the tests have passed.

### Entrypoint Script Test

`docker-entrypoint.sh` is covered by a shell test that runs it against a stub `java` and asserts the arguments it builds. A POSIX shell is required, so on Windows it is run under WSL or Git Bash.

Linux / macOS:

```bash
sh docker-entrypoint-test.sh
```

Windows (Git Bash / WSL):

```bash
sh docker-entrypoint-test.sh
```

If you see `All docker-entrypoint.sh tests passed.`, the tests have passed.

## Development

### Building and Running Locally

1. Clone the repository and build:
   ```bash
   mvn clean package
   ```
2. Run in CLI mode:
   ```bash
   java -jar target/gh-backup-2.0.0-SNAPSHOT-8-8-2026.jar octocat
   ```
3. Run in web mode:
   ```bash
   java -Dspring.profiles.active=web -jar target/gh-backup-2.0.0-SNAPSHOT-8-8-2026.jar
   ```
   Then open `http://localhost:8080` in your browser.
4. Run in daemon mode (automatic scheduled backups):
   ```bash
   java -Dspring.profiles.active=daemon -Dbackup.scheduled.users=octocat,github -jar target/gh-backup-2.0.0-SNAPSHOT-8-8-2026.jar
   ```
   See [Docker Deployment](#docker-deployment-daemon-mode) below for a containerized setup, and [COMMANDS.md](COMMANDS.md) for all daemon options.

## Docker Deployment (Daemon Mode)

The repository includes a `Dockerfile` and `docker-compose.yml` for running gh-backup as a background service that backs up configured users/organizations every 24 hours by default.

1. Create a `.env` file from the example and configure it:
   ```bash
   cp .env.example .env
   ```
2. Start the daemon:
   ```bash
   docker-compose up -d
   ```
3. View logs:
   ```bash
   docker-compose logs -f
   ```
4. Stop the daemon:
   ```bash
   docker-compose down
   ```

See [CONFIG.md](CONFIG.md) for the full list of daemon configuration options.

## Authors and Acknowledgement

### Developers

| Name | Main Contributions |
|------|--------------------|
| dmccoystephenson | Initial development and maintenance |

## License

This project is licensed under the [MIT License](LICENSE).

You are free to use, modify, and distribute this software under the terms of the MIT License.

See the [LICENSE](LICENSE) file for the full license text.

## Project Status

This project is in active development.

### Changelog

See [CHANGELOG.md](CHANGELOG.md) for a release-by-release summary of changes.