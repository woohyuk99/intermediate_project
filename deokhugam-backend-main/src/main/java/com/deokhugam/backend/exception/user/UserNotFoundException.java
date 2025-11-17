package com.deokhugam.backend.exception.user;

import com.deokhugam.backend.exception.ErrorCode;

public class UserNotFoundException extends UserException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }

    public static UserNotFoundException withUsername(String username) {
        UserNotFoundException exception = new UserNotFoundException();
        exception.addDetail("username", username);
        return exception;
    }

    public static UserNotFoundException withId(String userId) {
        UserNotFoundException exception = new UserNotFoundException();
        exception.addDetail("userId", userId);
        return exception;
    }

    public static UserNotFoundException withMessage(String message) {
        UserNotFoundException exception = new UserNotFoundException();
        exception.addDetail("message", message);
        return exception;
    }
}
