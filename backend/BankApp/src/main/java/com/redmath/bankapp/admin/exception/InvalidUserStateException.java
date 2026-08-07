package com.redmath.bankapp.admin.exception;


public class InvalidUserStateException extends RuntimeException {

  public InvalidUserStateException(String message) {
    super(message);
  }
}