package com.smallcold.hosts.exception;

import java.io.IOException;

/*
 * Created by smallcold on 2017/9/6.
 */
public class PermissionIOException extends IOException {

    public PermissionIOException() {
        super();
    }

    public PermissionIOException(String msg) {
        super(msg);
    }

    public PermissionIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public PermissionIOException(Throwable cause) {
        super(cause);
    }
}
