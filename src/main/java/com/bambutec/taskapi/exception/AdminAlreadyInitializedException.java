package com.bambutec.taskapi.exception;

public class AdminAlreadyInitializedException extends RuntimeException {

    public AdminAlreadyInitializedException(String message) {
        super(message);
    }
}
