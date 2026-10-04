package com.curea.document.web;

import com.curea.document.service.ApiException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handle(ApiException ex) {
        return respond(ex.getStatus(), ex.getCode());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tooLarge(MaxUploadSizeExceededException ex) {
        return respond(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE");
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> multipart(MultipartException ex) {
        return respond(HttpStatus.BAD_REQUEST, "FILE_REQUIRED");
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String code) {
        String description = messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
        return ResponseEntity.status(status).body(new ErrorResponse(code, description));
    }
}
