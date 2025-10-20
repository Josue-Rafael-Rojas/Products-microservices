package com.products.products.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;



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
