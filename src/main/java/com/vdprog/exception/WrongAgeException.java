package com.vdprog.exception;

public class WrongAgeException extends RuntimeException{
    public WrongAgeException(String message) {
        super(message);
    }
}
