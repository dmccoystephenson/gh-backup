package com.github.backup.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class BackupRequest {
    @NotBlank(message = "User or organization name is required")
    @Pattern(regexp = "^[a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38}$", 
             message = "Invalid GitHub username format")
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
