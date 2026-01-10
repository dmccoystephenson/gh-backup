# gh-backup

A tool built with Spring Boot to backup public GitHub repositories for specified users or organizations. Available as both a command-line tool and a web application.

## Features

- **Web UI** for easy backup management through your browser
- Backup all public repositories from one or more GitHub users/organizations
- Clone new repositories or update existing ones
- **Interactive mode** for easier management and status viewing
- Support for authenticated and anonymous GitHub API access
- Parallel backup of multiple users/organizations
- Organized backup structure by user/organization

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (for building from source)
- Git (for cloning repositories)

## Installation

### Building from Source

```bash
git clone https://github.com/dmccoystephenson/gh-backup.git
cd gh-backup
mvn clean package
```

The executable JAR will be created at `target/gh-backup-1.0.0.jar`

## Usage

### Web UI Mode (Recommended)

Start the web server:

```bash
java -Dbackup.mode=web -jar target/gh-backup-1.0.0.jar
```

Then open your browser and navigate to `http://localhost:8080` to access the web interface.

The web UI allows you to:
- Create new backups by entering a GitHub user or organization name
- View the status of all your backups
- See a list of all backed up repositories with their last update times

To use a custom port:
```bash
java -Dbackup.mode=web -Dserver.port=9000 -jar target/gh-backup-1.0.0.jar
```

### Interactive Mode

Start the tool in interactive mode for easier management:

```bash
java -jar target/gh-backup-1.0.0.jar -i
```

In interactive mode, you can:
- Type `backup <user/org>` to backup a user or organization's repositories
- Type `status` to view current backup status and list all backed up repositories
- Type `exit` to quit

Example session:
```
> backup octocat
Fetching repositories for: octocat
Found 8 public repositories
...

> status
Backup Status
=============
Backup directory: /path/to/backups

octocat/ (8 repositories)
  - Hello-World (last updated: 2026-01-10 12:30:45)
  - Spoon-Knife (last updated: 2026-01-10 12:30:47)
  ...

Total: 1 users/organizations, 8 repositories

> exit
Exiting interactive mode...
```

### Basic Usage

Backup repositories for one or more users/organizations:

```bash
java -jar target/gh-backup-1.0.0.jar <user/org1> [user/org2] ...
```

### Examples

Backup repositories from a single user:
```bash
java -jar target/gh-backup-1.0.0.jar octocat
```

Backup repositories from multiple users/organizations:
```bash
java -jar target/gh-backup-1.0.0.jar octocat github spring-projects
```

### Using GitHub Authentication

For higher rate limits and access to more API features, set the `GITHUB_TOKEN` environment variable:

```bash
export GITHUB_TOKEN=your_github_token_here
java -jar target/gh-backup-1.0.0.jar octocat
```

To create a GitHub personal access token:
1. Go to GitHub Settings → Developer settings → Personal access tokens
2. Click "Generate new token"
3. Select scopes (public_repo is sufficient for public repositories)
4. Copy the generated token

### Custom Backup Directory

By default, repositories are backed up to `~/gh-backups/` (user home directory). This works on both Linux and Windows. To use a custom location:

```bash
java -Dbackup.directory=/path/to/backup -jar target/gh-backup-1.0.0.jar octocat
```

Windows example:
```bash
java -Dbackup.directory=C:\Backups\GitHub -jar target/gh-backup-1.0.0.jar octocat
```

The backup directory path is automatically converted to an absolute path and normalized for cross-platform compatibility.

## Output Structure

Repositories are organized by user/organization in the backup directory:

```
~/gh-backups/
├── octocat/
│   ├── Hello-World/
│   ├── Spoon-Knife/
│   └── ...
├── github/
│   ├── docs/
│   ├── roadmap/
│   └── ...
└── ...
```

## How It Works

1. The tool connects to the GitHub API (authenticated or anonymously)
2. For each specified user/organization, it fetches all public repositories
3. Each repository is cloned to the local backup directory
4. If a repository already exists, it is updated with `git fetch`

## Rate Limits

- **Anonymous**: 60 requests per hour
- **Authenticated**: 5,000 requests per hour

Using authentication is recommended for backing up users/organizations with many repositories.

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.