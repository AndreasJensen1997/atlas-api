package app.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiException extends RuntimeException {
    private final int code;

    // 1. Constructor for validation/client errors (No cause needed)
    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    // 2. Constructor for wrapped system/database errors (With cause)
    public ApiException(int code, String msg, Throwable cause){
        super(msg, cause);
        this.code = code;
    }

    public int getStatusCode(){
        return code;
    }

}