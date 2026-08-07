package com.redmath.bankapp.transaction.exception;

public class BalanceNotFoundException extends RuntimeException {

  public BalanceNotFoundException(String message) {
    super(message);
  }

  public BalanceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
