package br.com.zup.edu.nossalojavirtual.exception;

import br.com.zup.edu.nossalojavirtual.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleNotExpectingErrors(RuntimeException e, WebRequest request) {

        logger.error("Exception : " + e.getLocalizedMessage(), e);

        Map<String, Object> body = Map.of(
                "status", 500,
                "path", request.getDescription(false).replace("uri=", ""),
                "timestamp", LocalDateTime.now(),
                "message", e.getLocalizedMessage()
        );
        return ResponseEntity
                .internalServerError().body(body);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValid(MethodArgumentNotValidException e, WebRequest request) {

        logger.error("Exception : " + e.getLocalizedMessage(), e);

        Map<String, Object> body = Map.of(
                "status", 400,
                "path", request.getDescription(false).replace("uri=", ""),
                "timestamp", LocalDateTime.now(),
                "message", e.getLocalizedMessage(),
                "fields", e.getBindingResult().getFieldErrors().stream().map(ExceptionUtil::getFieldAndDefaultErrorMessage).toList()
        );
        return ResponseEntity
                .badRequest().body(body);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UserNotValidException.class)
    public ResponseEntity<?> userNotValidException(UserNotValidException e, WebRequest request) {

        logger.error("Exception : " + e.getLocalizedMessage(), e);

        Map<String, Object> body = Map.of(
                "status", 403,
                "path", request.getDescription(false).replace("uri=", ""),
                "timestamp", LocalDateTime.now(),
                "message", e.getLocalizedMessage()
        );
        return ResponseEntity.status(403).body(body);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> httpMessageNotReadable(HttpMessageNotReadableException e, WebRequest request) {

        logger.error("Exception : " + e.getLocalizedMessage(), e);

        Map<String, Object> body = Map.of(
                "status", 400,
                "path", request.getDescription(false).replace("uri=", ""),
                "timestamp", LocalDateTime.now(),
                "message", e.getLocalizedMessage()
        );
        return ResponseEntity
                .badRequest().body(body);
    }

}
