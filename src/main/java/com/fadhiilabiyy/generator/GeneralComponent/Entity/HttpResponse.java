package com.fadhiilabiyy.generator.GeneralComponent.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.util.Date;

public class HttpResponse<T> {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Asia/Jakarta")
    String timeStamp = new Date().toString();
    int responseCode;

    HttpStatus httpStatus;
    String responseReason;
    String responseMessage;
    int page;
    int size;
    int totalPage;
    int totalData;
    T responseData;

    public HttpResponse(int responseCode, HttpStatus httpStatus, String responseReason, String responseMessage) {
        this.responseCode = responseCode;
        this.httpStatus = httpStatus;
        this.responseReason = responseReason;
        this.responseMessage = responseMessage;
    }

    public HttpResponse(String responseMessage, int responseCode, String responseReason, T responseData) {
        this.responseMessage = responseMessage;
        this.responseCode = responseCode;
        this.responseReason = responseReason;
        this.responseData = responseData;
    }

    public HttpResponse(String responseMessage, int responseCode, String responseReason) {
        this.responseMessage = responseMessage;
        this.responseCode = responseCode;
        this.responseReason = responseReason;
        this.responseData = null;
    }

    public HttpResponse(String responseMessage, int responseCode, String responseReason, T responseData, int page, int size, int totalData, int totalPage) {
        this.responseMessage = responseMessage;
        this.responseCode = responseCode;
        this.responseReason = responseReason;
        this.responseData = responseData;
        this.page = page;
        this.size = size;
        this.totalData = totalData;
        this.totalPage = totalPage;
    }
}
