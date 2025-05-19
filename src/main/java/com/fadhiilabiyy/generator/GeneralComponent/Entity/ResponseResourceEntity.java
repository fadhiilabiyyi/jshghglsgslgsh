package com.fadhiilabiyy.generator.GeneralComponent.Entity;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Set;

public class ResponseResourceEntity<T> {
    public ResponseEntity<HttpResponse<T>> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse<>(message, httpStatus.value(), httpStatus.getReasonPhrase().toUpperCase()), httpStatus);
    }

    public ResponseEntity<HttpResponse<T>> responseWithData(HttpStatus httpStatus, String message, T responseData) {
        return new ResponseEntity<>(new HttpResponse<>(message, httpStatus.value(), httpStatus.getReasonPhrase().toUpperCase(),
                responseData), httpStatus);
    }

    public ResponseEntity<HttpResponse<Object>> responseWithDataObject(HttpStatus httpStatus, String message, Object responseData) {
        return new ResponseEntity<>(new HttpResponse<>(message, httpStatus.value(), httpStatus.getReasonPhrase().toUpperCase(),
                responseData), httpStatus);
    }

    public ResponseEntity<HttpResponse<Collection<T>>> responseWithListData(HttpStatus httpStatus, String message, Collection<T> responseData) {
        return new ResponseEntity<>(new HttpResponse<>(message, httpStatus.value(), httpStatus.getReasonPhrase().toUpperCase(),
                responseData), httpStatus);
    }

    public ResponseEntity<HttpResponse<Object>> responsePagination(HttpStatus httpStatus, String message, Object responseData, int page, int size, int totalData, int totalPage) {
        return new ResponseEntity<>(new HttpResponse<>(message, httpStatus.value(), httpStatus.getReasonPhrase().toUpperCase(), responseData, page, size, totalData, totalPage), httpStatus);
    }

    public ResponseEntity<HttpResponse<T>> responseHeader(HttpStatus httpStatus, String message, HttpHeaders headers) {
        return new ResponseEntity<>(new HttpResponse<>(message, httpStatus.value(), httpStatus.getReasonPhrase().toUpperCase()), headers, httpStatus);
    }

    public ResponseEntity<HttpResponse<T>> responseWithDataHeader(HttpStatus httpStatus, String message, T responseData, HttpHeaders headers) {
        return new ResponseEntity<>(new HttpResponse<>(message, httpStatus.value(), httpStatus.getReasonPhrase().toUpperCase(),
                responseData), headers, httpStatus);
    }

    public ResponseEntity<HttpResponse<Set<T>>> responseWithSetData(HttpStatus httpStatus, String message, Set<T> responseData) {
        return new ResponseEntity<>(new HttpResponse<>(message, httpStatus.value(), httpStatus.getReasonPhrase().toUpperCase(),
                responseData), httpStatus);
    }
}
