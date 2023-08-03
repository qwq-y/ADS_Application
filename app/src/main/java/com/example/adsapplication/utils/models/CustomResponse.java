package com.example.adsapplication.utils.models;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CustomResponse implements Serializable {
    private String message;
    private String status;
    private String video;
    private String image;
    private List<List<Float>> coordinates;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<List<Float>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Float>> coordinates) {
        this.coordinates = coordinates;
    }
}


