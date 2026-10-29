package app.exceptions;

import io.javalin.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final int code;

    public ApiException(HttpStatus status, String msg) {
        this(status.getCode(), msg);
    }

    public ApiException(HttpStatus status, String msg, Throwable cause) {
        this(status.getCode(), msg, cause);
    }

    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public ApiException(int code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
    }

    public int getStatusCode() {
        return code;
    }

}