package com.example.home_api.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class UploadExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ModelAndView tooLarge(HttpServletRequest request) {
        String path=request.getRequestURI();
        String target=path.contains("/settings/font")
                ? "/admin/settings/display?fontError=tooLarge"
                : "/admin/data/import?error=tooLarge";
        return new ModelAndView("redirect:"+target);
    }
}
