package com.glady.deposit.exception;

import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;

@ControllerAdvice
public class ErrorHandlingControllerAdvice {

    // Exception handling is weird in Spring... A failed validation can either throw a MethodArgumentNotValidException or a ConstraintViolationException
    // However ConstraintViolationException is not handled by DefaultHandlerExceptionResolver.
    // So we need an extra handling in that particular case !
    // More info : https://lightrun.com/answers/spring-projects-spring-framework-generalize-validation-error-handling
    @ExceptionHandler(ConstraintViolationException.class)
    protected ModelAndView handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request,
            HttpServletResponse response,
            @Nullable Object handler
    ) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return new ModelAndView();
    }
}
