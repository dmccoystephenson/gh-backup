# gh-backup

A command-line tool built with Spring Boot to backup public GitHub repositories for specified users or organizations.

## Features

- Backup all public repositories from one or more GitHub users/organizations
- Clone new repositories or update existing ones
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

By default, repositories are backed up to the `backups/` directory. To change this:

```bash
java -Dbackup.directory=/path/to/backup -jar target/gh-backup-1.0.0.jar octocat
```

## Output Structure

Repositories are organized by user/organization:

```
backups/
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
4. If a repository already exists, it is updated with `git fetch` and `git pull`

## Rate Limits

- **Anonymous**: 60 requests per hour
- **Authenticated**: 5,000 requests per hour

Using authentication is recommended for backing up users/organizations with many repositories.

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.