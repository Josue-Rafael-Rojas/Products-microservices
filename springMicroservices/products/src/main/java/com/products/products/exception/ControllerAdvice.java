package com.products.products.exception;

import com.products.products.exception.common.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> uuidException(Exception excep, HttpServletRequest request){
        ErrorDto error = ErrorDto
                .builder()
                .requestUrl(request.getRequestURI())
                .code("INV-4000")
                .timestamp(LocalDateTime.now())
                .description(excep.getMessage())
                .exception(excep.getClass().getSimpleName())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> argumentException(MethodArgumentNotValidException excep, HttpServletRequest request){

        Map<String, String> errores = new HashMap<>();
        excep.getBindingResult().getAllErrors().forEach(error -> {
            String nombreCampo = ((FieldError)error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(nombreCampo, mensaje);
        });

        ErrorDto error = ErrorDto
                .builder()
                .requestUrl(request.getRequestURI())
                .code("INV-4001")
                .timestamp(LocalDateTime.now())
                .description("Invalid or incomplete customer data " + errores)
                .exception(excep.getClass().getSimpleName())
                .build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorDto> handleProductNotFoundException(ProductNotFoundException excep, HttpServletRequest request) {
        var apiError = ErrorDto
                .builder()
                .requestUrl(request.getRequestURI())
                .code("INV-4002")
                .timestamp(LocalDateTime.now())
                .description(excep.getMessage())
                .exception(excep.getClass().getSimpleName())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }


}
