package com.smartservice.common.exception;

public class BusinessRuleException extends RuntimeException {
    private final String code;

    public BusinessRuleException(String message, String code) {
        super(message);
        this.code = code;
    }

    public BusinessRuleException(String message) {
        super(message);
        this.code = "BUSINESS_RULE_VIOLATION";
    }

    public String getCode() {
        return code;
    }
}
