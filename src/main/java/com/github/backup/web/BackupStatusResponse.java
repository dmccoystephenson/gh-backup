package com.github.backup.web;

import java.util.List;

public class BackupStatusResponse {
    private int totalUsers;
    private int totalRepositories;
    private List<UserBackupInfo> users;

    public BackupStatusResponse() {
    }

    public BackupStatusResponse(int totalUsers, int totalRepositories, List<UserBackupInfo> users) {
        this.totalUsers = totalUsers;
        this.totalRepositories = totalRepositories;
        this.users = users;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalRepositories() {
        return totalRepositories;
    }

    public void setTotalRepositories(int totalRepositories) {
        this.totalRepositories = totalRepositories;
    }

    public List<UserBackupInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserBackupInfo> users) {
        this.users = users;
    }

    public static class UserBackupInfo {
        private String name;
        private int repositoryCount;
        private List<RepositoryInfo> repositories;

        public UserBackupInfo() {
        }

        public UserBackupInfo(String name, int repositoryCount, List<RepositoryInfo> repositories) {
            this.name = name;
            this.repositoryCount = repositoryCount;
            this.repositories = repositories;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getRepositoryCount() {
            return repositoryCount;
        }

        public void setRepositoryCount(int repositoryCount) {
            this.repositoryCount = repositoryCount;
        }

        public List<RepositoryInfo> getRepositories() {
            return repositories;
        }

        public void setRepositories(List<RepositoryInfo> repositories) {
            this.repositories = repositories;
        }
    }

    public static class RepositoryInfo {
        private String name;
        private String lastUpdated;

        public RepositoryInfo() {
        }

        public RepositoryInfo(String name, String lastUpdated) {
            this.name = name;
            this.lastUpdated = lastUpdated;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }
}
