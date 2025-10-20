package com.inventory.inventory.exception;

import java.io.Serializable;


public class UuidInvalidException extends RuntimeException implements Serializable {
    /*private String requestUrl;
    private String code;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private LocalDateTime timestamp;
    private String exception;
    private HttpStatus status;*/
    public UuidInvalidException(String uuid) {
        super("The UUID" + uuid + "does not have the correct format.");
    }
}
