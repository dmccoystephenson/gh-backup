package com.github.backup.web;

public class BackupRequest {
    private String userOrOrg;

    public BackupRequest() {
    }

    public BackupRequest(String userOrOrg) {
        this.userOrOrg = userOrOrg;
    }

    public String getUserOrOrg() {
        return userOrOrg;
    }

    public void setUserOrOrg(String userOrOrg) {
        this.userOrOrg = userOrOrg;
    }
}
