package com.deokhugam.backend.exception.user;

import com.deokhugam.backend.exception.BaseException;
import com.deokhugam.backend.exception.ErrorCode;

public class UserException extends BaseException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
    public UserException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
} 