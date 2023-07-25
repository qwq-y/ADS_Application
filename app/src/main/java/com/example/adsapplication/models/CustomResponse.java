package com.example.adsapplication.models;

import java.io.File;
import java.util.Map;

public class CustomResponse {
    private String message;
    private String status;
    private File video;

    public CustomResponse() {}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public File getVideo() {
        return video;
    }

    public void setVideo(File video) {
        this.video = video;
    }
}


