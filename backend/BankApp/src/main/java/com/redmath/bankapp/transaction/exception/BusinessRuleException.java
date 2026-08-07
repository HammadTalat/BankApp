package com.redmath.bankapp.transaction.exception;

public class BusinessRuleException extends RuntimeException {

  public BusinessRuleException(String message) {
    super(message);
  }

  public BusinessRuleException(String message, Throwable cause) {
    super(message, cause);
  }
}