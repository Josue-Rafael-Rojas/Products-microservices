package com.inventory.inventory.exception;


import com.inventory.inventory.exception.common.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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


    @ExceptionHandler(InventoryByProductUuidNotFoundException.class)
    public ResponseEntity<ErrorDto> handleProductNotFoundException(InventoryByProductUuidNotFoundException excep, HttpServletRequest request) {
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

    @ExceptionHandler(ProductDeserializationException.class)
    public ResponseEntity<ErrorDto> handleProductDeserializationException(ProductDeserializationException excep, HttpServletRequest request) {
        var apiError = ErrorDto
                .builder()
                .requestUrl(request.getRequestURI())
                .code("INV-5001")
                .timestamp(LocalDateTime.now())
                .description(excep.getMessage())
                .exception(excep.getClass().getSimpleName())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<ErrorDto> handleProductServiceException(ProductServiceException excep, HttpServletRequest request) {
        var apiError = ErrorDto
                .builder()
                .requestUrl(request.getRequestURI())
                .code("INV-5002")
                .timestamp(LocalDateTime.now())
                .description(excep.getMessage())
                .exception(excep.getClass().getSimpleName())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorDto> handleSecurityException(SecurityException excep, HttpServletRequest request) {
        var apiError = ErrorDto
                .builder()
                .requestUrl(request.getRequestURI())
                .code("SR-1002")
                .timestamp(LocalDateTime.now())
                .description("Error de seguridad: " + excep.getMessage())
                .exception(excep.getClass().getSimpleName())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }


}
