package com.deokhugam.backend.exception.user;


import com.deokhugam.backend.exception.ErrorCode;

public class InvalidCredentialsException extends UserException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_USER_CREDENTIALS);
    }

    public static InvalidCredentialsException wrongPassword() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        return exception;
    }
} 