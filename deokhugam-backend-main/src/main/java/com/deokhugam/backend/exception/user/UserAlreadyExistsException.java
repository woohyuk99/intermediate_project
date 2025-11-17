package com.deokhugam.backend.exception.user;


import com.deokhugam.backend.exception.ErrorCode;

// 도메인 에러
public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException() {
        super(ErrorCode.DUPLICATE_USER);
    }
    
    public static UserAlreadyExistsException withEmail(String email) {
        UserAlreadyExistsException exception = new UserAlreadyExistsException();
        exception.addDetail("email", email);
        return exception;
    }
    
    public static UserAlreadyExistsException withUsername(String username) {
        UserAlreadyExistsException exception = new UserAlreadyExistsException();
        exception.addDetail("username", username);
        return exception;
    }
} 