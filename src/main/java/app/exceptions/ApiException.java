package app.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiException extends RuntimeException {
    private int code;
    private static final Logger logger = LoggerFactory.getLogger(ApiException.class);

    // 1. Constructor for validation/client errors (No cause needed)
    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
        logger.error("ApiException (code={}): {}", code, msg);
    }

    // 2. Constructor for wrapped system/database errors (With cause)
    public ApiException(int code, String msg, Throwable cause){
        super(msg, cause);
        this.code = code;
        logger.error("ApiException (code={}): {}", code, msg, cause); // Optional: log the cause too
    }

    public int getCode(){
        return code;
    }
}