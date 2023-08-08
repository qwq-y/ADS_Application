package com.example.adsapplication.utils.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CustomResponse implements Parcelable {

    private String status;
    private String message;
    private String video;
    private List<String> images;

    public CustomResponse() {
        images = new ArrayList<>();
    }

    protected CustomResponse(Parcel in) {
        status = in.readString();
        message = in.readString();
        video = in.readString();
        images = in.createStringArrayList();
    }

    public static final Creator<CustomResponse> CREATOR = new Creator<CustomResponse>() {
        @Override
        public CustomResponse createFromParcel(Parcel in) {
            return new CustomResponse(in);
        }

        @Override
        public CustomResponse[] newArray(int size) {
            return new CustomResponse[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeString(message);
        dest.writeString(video);
        dest.writeStringList(images);
    }
}

