package com.tsv.file.property;

import org.springframework.stereotype.Component;

@Component
public class FileStorageProperties {
    private String uploadDir;

    public String getUploadDir(String username) {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
}
