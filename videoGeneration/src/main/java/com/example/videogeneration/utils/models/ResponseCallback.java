package com.example.videogeneration.utils.models;

public interface ResponseCallback {
    void onSuccess(CustomResponse response);
    void onError(String errorMessage);
}
