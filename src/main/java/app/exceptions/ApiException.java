package app.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiException extends RuntimeException {
    private final int code;

    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public ApiException(int code, String msg, Throwable cause){
        super(msg, cause);
        this.code = code;
    }

    public int getStatusCode(){
        return code;
    }

}