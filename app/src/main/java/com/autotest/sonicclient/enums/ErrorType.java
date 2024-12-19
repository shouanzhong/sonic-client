package com.autotest.sonicclient.enums;

public enum ErrorType {
    IGNORE(1),
    WARNING(2),
    SHUTDOWN(3);

    private final int code;

    ErrorType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ErrorType fromValue(int value) {
        for (ErrorType errorType : ErrorType.values()) {
            if (errorType.getCode() == value) {
                return errorType;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

//    public static ErrorType fromValue(int value) {
//        for (ErrorType errorType : ErrorType.values()) {
//            if (errorType.ordinal() == value) {
//                return errorType;
//            }
//        }
//        return null; // 或者抛出异常，取决于你的错误处理策略
//    }
}
