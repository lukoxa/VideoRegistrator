package com.lukoxa.videoreg;

public interface ErrorHandler {
    void onError(String prefix, Throwable t);
}