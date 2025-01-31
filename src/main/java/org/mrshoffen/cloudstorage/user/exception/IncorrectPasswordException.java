package org.mrshoffen.cloudstorage.user.exception;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException(String s) {
        super(s);
    }
}
