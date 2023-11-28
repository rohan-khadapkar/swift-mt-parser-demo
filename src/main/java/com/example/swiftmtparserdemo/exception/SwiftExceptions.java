package com.example.swiftmtparserdemo.exception;

public class SwiftExceptions extends Exception {
    private static final long serialVersionUID = 1L;
    private Exception actEx = null;

    /**
     * This is a custom exception for any error occuring with the
     * GPI Business SDK.
     *
     * @param validationReason - The reason for failure.
     */
    public SwiftExceptions(String validationReason) {
        super(validationReason);
    }

    public SwiftExceptions(String reason, Exception ex) {
        super(reason);
        actEx = ex;
    }

    public Exception getActEx() {
        return actEx;
    }

    public void setActEx(Exception actEx) {
        this.actEx = actEx;
    }
}
