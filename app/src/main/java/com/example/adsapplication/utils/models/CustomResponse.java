package com.example.adsapplication.utils.models;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CustomResponse implements Serializable {

    private String status;
    private String message;
    private String video;
    private List<String> images;

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

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}


