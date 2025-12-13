package wooteco.subway.exception;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(SubwayException.class)
    public ResponseEntity<ExceptionResponse> exceptionResponse(SubwayException e) {
        LOGGER.error(e.getMessage());

        return ResponseEntity.status(e.status())
            .body(new ExceptionResponse(e.message()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ExceptionResponse>> exceptionResponse(MethodArgumentNotValidException e) {
        LOGGER.error(e.getMessage());

        return ResponseEntity.badRequest()
            .body(extractErrorMessages(e));
    }

    private List<ExceptionResponse> extractErrorMessages(MethodArgumentNotValidException exception) {
        return exception.getBindingResult()
            .getAllErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .map(ExceptionResponse::new)
            .collect(Collectors.toList());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> exceptionResponse(ConstraintViolationException e) {
        LOGGER.error(e.getMessage());

        return ResponseEntity.badRequest()
            .body(new ExceptionResponse(e.getMessage()));
    }

    private List<ExceptionResponse> extractErrorMessages(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .map(ExceptionResponse::new)
            .collect(Collectors.toList());
    }
}
